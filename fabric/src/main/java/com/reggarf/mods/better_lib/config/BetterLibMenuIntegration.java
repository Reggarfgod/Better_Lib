package com.reggarf.mods.better_lib.config;

import com.reggarf.mods.better_lib.Better_lib;
import com.reggarf.mods.better_lib.gui.screen.BetterConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BetterLibMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> BetterConfigScreen.create(parent, Better_lib.MODID);
    }
}
