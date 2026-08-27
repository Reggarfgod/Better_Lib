package com.reggarf.mods.better_lib;

import com.reggarf.mods.better_lib.config.DemoConfig;
import com.reggarf.mods.better_lib.gui.screen.BetterConfigScreenHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Better_lib.MODID)
public class Better_lib_neoforge {

    public Better_lib_neoforge(IEventBus modBus, ModContainer modContainer) {
        Better_lib.LOG.info("Hello NeoForge world!");
        CommonClass.init();

        // 1-line registration for all configs (Specs, Events, GUI Tabs, and Mods Menu hook):
        BetterConfigScreenHandler.registerMod(modContainer, modBus, Better_lib.MODID,
                DemoConfig.CLIENT,
                DemoConfig.COMMON,
                DemoConfig.SERVER
        );
    }
}
