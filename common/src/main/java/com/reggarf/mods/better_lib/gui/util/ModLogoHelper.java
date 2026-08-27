package com.reggarf.mods.better_lib.gui.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.reggarf.mods.better_lib.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Universal helper that discovers and dynamically loads mod logos/icons
 * directly using the active loader platform (NeoForge, Forge, or Fabric)
 * with robust caching and DynamicTexture registration.
 */
public final class ModLogoHelper {

    private static final Map<String, ResourceLocation> LOGO_CACHE = new ConcurrentHashMap<>();
    private static final Set<String> FAILED_LOOKUPS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private ModLogoHelper() {
    }

    /**
     * Manually register a custom logo texture for a mod ID.
     */
    public static void registerCustomLogo(String modId, ResourceLocation logo) {
        if (modId == null || logo == null) return;
        LOGO_CACHE.put(modId.toLowerCase(Locale.ROOT), logo);
    }

    /**
     * Retrieve or dynamically load the mod logo texture.
     *
     * @param modId the mod ID
     * @return ResourceLocation of the loaded dynamic or static texture, or null if none found
     */
    public static ResourceLocation getOrLoadLogo(String modId) {
        if (modId == null || modId.isEmpty()) return null;
        String cleanId = modId.toLowerCase(Locale.ROOT);

        if (LOGO_CACHE.containsKey(cleanId)) {
            return LOGO_CACHE.get(cleanId);
        }
        if (FAILED_LOOKUPS.contains(cleanId)) {
            return null;
        }

        ResourceLocation resolved = resolveAndUploadLogo(cleanId);
        if (resolved != null) {
            LOGO_CACHE.put(cleanId, resolved);
            return resolved;
        }

        FAILED_LOOKUPS.add(cleanId);
        return null;
    }

    private static ResourceLocation resolveAndUploadLogo(String modId) {
        InputStream stream = null;

        // 1. Ask loader platform service for the mod container's logo stream
        try {
            if (Services.HELPER != null) {
                stream = Services.HELPER.getModLogoInputStream(modId);
            }
        } catch (Throwable ignored) {
        }

        // 2. Classpath resource candidates for this specific mod ID
        if (stream == null) {
            stream = tryLoadFromCommonPaths(modId);
        }

        if (stream == null) {
            return null;
        }

        try (InputStream is = stream) {
            NativeImage image = NativeImage.read(is);
            DynamicTexture texture = new DynamicTexture(image);
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath("better_lib_dynamic", "logo_" + modId);
            Minecraft.getInstance().getTextureManager().register(loc, texture);
            return loc;
        } catch (Throwable t) {
            return null;
        }
    }

    private static InputStream tryLoadFromCommonPaths(String modId) {
        String[] candidates = new String[]{
                modId + ".png",
                "assets/" + modId + "/textures/gui/logo.png",
                "assets/" + modId + "/textures/gui/icon.png",
                "assets/" + modId + "/icon.png",
                "assets/" + modId + "/logo.png",
                "assets/" + modId + "/" + modId + ".png"
        };

        for (String candidate : candidates) {
            InputStream is = openResourceStream(candidate, modId);
            if (is != null) return is;
        }
        return null;
    }

    private static InputStream openResourceStream(String path, String modId) {
        if (path == null || path.isEmpty()) return null;
        if (path.startsWith("/")) path = path.substring(1);

        ClassLoader cl = ModLogoHelper.class.getClassLoader();
        InputStream is = cl.getResourceAsStream(path);
        if (is != null) return is;

        try {
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            if (ccl != null && ccl != cl) {
                is = ccl.getResourceAsStream(path);
                if (is != null) return is;
            }
        } catch (Throwable ignored) {
        }

        return null;
    }
}
