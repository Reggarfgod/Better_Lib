package com.reggarf.mods.better_lib.popup.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.reggarf.mods.better_lib.popup.api.WelcomePopupPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ModIconHelper {

    private static final Map<String, Identifier> CACHE = new HashMap<>();
    private static final Identifier MISSING = Identifier.fromNamespaceAndPath("better_lib", "missing_icon");

    private ModIconHelper() {}

    /**
     * Resolves the icon for a plugin:
     * 1. Checks plugin.getIcon() if explicitly provided.
     * 2. Checks cached loaded icon.
     * 3. Attempts to automatically discover and load the mod's icon from jar resources or loader metadata.
     * 4. Returns null if no icon is available (allowing graceful fallback to letter badge).
     */
    public static Identifier getIcon(WelcomePopupPlugin plugin) {
        if (plugin == null) return null;

        Identifier explicit = plugin.getIcon();
        if (explicit != null) {
            return explicit;
        }

        String modId = plugin.getModId();
        if (modId == null || modId.isEmpty()) return null;

        String key = modId.toLowerCase();
        if (CACHE.containsKey(key)) {
            Identifier cached = CACHE.get(key);
            return cached == MISSING ? null : cached;
        }

        Identifier loaded = discoverModIcon(plugin, key);
        if (loaded != null) {
            CACHE.put(key, loaded);
            return loaded;
        } else {
            CACHE.put(key, MISSING);
            return null;
        }
    }

    private static Identifier discoverModIcon(WelcomePopupPlugin plugin, String modId) {
        // 1. Check standard assets Identifier via Minecraft ResourceManager if client is ready
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getResourceManager() != null) {
            String[] resourcePaths = new String[]{
                    "textures/gui/icon.png",
                    "textures/gui/logo.png",
                    "icon.png"
            };
            for (String rp : resourcePaths) {
                Identifier testLoc = Identifier.fromNamespaceAndPath(modId, rp);
                if (mc.getResourceManager().getResource(testLoc).isPresent()) {
                    return testLoc;
                }
            }
        }

        // 2. Check candidate stream paths on the mod's ClassLoader
        ClassLoader cl = plugin.getClass().getClassLoader();
        String[] candidatePaths = new String[]{
                modId + ".png",
                "icon.png",
                "logo.png",
                "assets/" + modId + "/textures/gui/icon.png",
                "assets/" + modId + "/icon.png",
                "assets/" + modId + "/logo.png"
        };

        for (String path : candidatePaths) {
            try (InputStream is = cl.getResourceAsStream(path)) {
                if (is != null) {
                    Identifier registered = registerDynamicIcon(modId, is);
                    if (registered != null) return registered;
                }
            } catch (Throwable ignored) {}
        }

        // 3. Fabric loader metadata
        try {
            Class<?> fabricLoaderClass = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object fabricInstance = fabricLoaderClass.getMethod("getInstance").invoke(null);
            Optional<?> modContainerOpt = (Optional<?>) fabricLoaderClass.getMethod("getModContainer", String.class).invoke(fabricInstance, modId);
            if (modContainerOpt.isPresent()) {
                Object modContainer = modContainerOpt.get();
                Object metadata = modContainer.getClass().getMethod("getMetadata").invoke(modContainer);
                Optional<String> iconPathOpt = (Optional<String>) metadata.getClass().getMethod("getIconPath", int.class).invoke(metadata, 64);
                if (iconPathOpt.isPresent()) {
                    Optional<Path> pathOpt = (Optional<Path>) modContainer.getClass().getMethod("findPath", String.class).invoke(modContainer, iconPathOpt.get());
                    if (pathOpt.isPresent() && Files.isRegularFile(pathOpt.get())) {
                        try (InputStream is = Files.newInputStream(pathOpt.get())) {
                            Identifier registered = registerDynamicIcon(modId, is);
                            if (registered != null) return registered;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}

        // 4. NeoForge & Forge mod container metadata
        try {
            Class<?> modListClass = null;
            try {
                modListClass = Class.forName("net.neoforged.fml.ModList");
            } catch (ClassNotFoundException e) {
                try {
                    modListClass = Class.forName("net.minecraftforge.fml.ModList");
                } catch (ClassNotFoundException ignored) {}
            }

            if (modListClass != null) {
                Object modList = modListClass.getMethod("get").invoke(null);
                Optional<?> modContainerOpt = (Optional<?>) modListClass.getMethod("getModContainerById", String.class).invoke(modList, modId);
                if (modContainerOpt.isPresent()) {
                    Object modContainer = modContainerOpt.get();
                    Object modInfo = modContainer.getClass().getMethod("getModInfo").invoke(modContainer);
                    Optional<String> logoFileOpt = (Optional<String>) modInfo.getClass().getMethod("getLogoFile").invoke(modInfo);
                    if (logoFileOpt.isPresent() && !logoFileOpt.get().isEmpty()) {
                        String logoFile = logoFileOpt.get();
                        try (InputStream is = cl.getResourceAsStream(logoFile)) {
                            if (is != null) {
                                Identifier registered = registerDynamicIcon(modId, is);
                                if (registered != null) return registered;
                            }
                        }
                        try {
                            Optional<Path> pathOpt = (Optional<Path>) modContainer.getClass().getMethod("findResource", String[].class).invoke(modContainer, (Object) new String[]{logoFile});
                            if (pathOpt.isPresent() && Files.isRegularFile(pathOpt.get())) {
                                try (InputStream is = Files.newInputStream(pathOpt.get())) {
                                    Identifier registered = registerDynamicIcon(modId, is);
                                    if (registered != null) return registered;
                                }
                            }
                        } catch (Throwable ignored) {}
                    }
                }
            }
        } catch (Throwable ignored) {}

        return null;
    }

    private static Identifier registerDynamicIcon(String modId, InputStream is) {
        try {
            NativeImage image = NativeImage.read(is);
            DynamicTexture texture = new DynamicTexture(() -> "better_lib_icon_" + modId, image);
            Identifier id = Identifier.fromNamespaceAndPath("better_lib", "dynamic_mod_icon/" + modId);
            Minecraft.getInstance().getTextureManager().register(id, texture);
            return id;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
