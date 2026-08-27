package com.reggarf.mods.better_lib;

import com.reggarf.mods.better_lib.config.DemoConfig;
import com.reggarf.mods.better_lib.gui.screen.BetterConfigScreenHandler;
import com.reggarf.mods.better_lib.message.JoinMessageFabric;
import com.reggarf.mods.better_lib.message.OnlineMessageFabric;
import net.fabricmc.api.ModInitializer;

public class Better_lib_fabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Better_lib.LOG.info("Hello Fabric world!");
        CommonClass.init();
        JoinMessageFabric.register();
        OnlineMessageFabric.register();

        // 1-line registration for all configs (Toml specs registered to ForgeConfigRegistry + Screen Tabs):
        BetterConfigScreenHandler.registerMod(Better_lib.MODID,
                DemoConfig.CLIENT,
                DemoConfig.COMMON,
                DemoConfig.SERVER
        );
    }
}
