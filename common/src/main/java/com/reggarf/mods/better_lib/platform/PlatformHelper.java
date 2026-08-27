package com.reggarf.mods.better_lib.platform;

import java.io.InputStream;

public interface PlatformHelper {

    /**
     * Get the human-readable platform name (e.g. "NeoForge", "Fabric", "Forge").
     *
     * @return platform name
     */
    String getPlatformName();

    /**
     * Get the input stream for the mod's logo/icon file from the platform mod container.
     *
     * @param modId the mod ID
     * @return InputStream of the logo file, or null if not found
     */
    InputStream getModLogoInputStream(String modId);
}
