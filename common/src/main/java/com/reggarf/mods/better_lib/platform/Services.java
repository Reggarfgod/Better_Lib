package com.reggarf.mods.better_lib.platform;

import com.reggarf.mods.better_lib.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

/**
 * Loads whichever VillagerRegistryPlatform implementation the current
 * loader's module contributes on the classpath (Fabric, Forge, or
 * NeoForge - only one will ever be present at runtime).
 */
public final class Services {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    private Services() {
    }

    private static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException(
                        "Failed to load service for " + clazz.getName()
                                + " - does this loader's module have a "
                                + "META-INF/services/" + clazz.getName() + " file?"));
    }
}