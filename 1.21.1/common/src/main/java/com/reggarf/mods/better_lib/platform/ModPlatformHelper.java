package com.reggarf.mods.better_lib.platform;

import com.reggarf.mods.better_lib.configs.ui.BaseConfigScreen;
import net.minecraft.client.gui.screens.Screen;
import java.util.List;

public interface ModPlatformHelper {

    record ModEntry(
            String id,
            String name,
            String version,
            String description,
            boolean hasConfig
    ) {}

    List<ModEntry> getLoadedMods();

    Screen openConfigScreen(String modId, Screen parent);

    default boolean hasModConfig(String modId) {
        return false;
    }

    default Screen createModConfigScreen(String modId, Screen parent) {
        return null;
    }

    default void populateConfigs(String modId, BaseConfigScreen screen) {
    }
}
