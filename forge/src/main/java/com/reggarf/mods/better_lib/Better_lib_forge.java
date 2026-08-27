package com.reggarf.mods.better_lib;

import com.reggarf.mods.better_lib.config.DemoConfig;
import com.reggarf.mods.better_lib.gui.screen.BetterConfigScreenHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Better_lib.MODID)
public class Better_lib_forge {

    public Better_lib_forge(FMLJavaModLoadingContext context) {
        Better_lib.LOG.info("Hello Forge world!");
        CommonClass.init();


        BetterConfigScreenHandler.registerMod(context.getModEventBus(), Better_lib.MODID,
                DemoConfig.CLIENT,
                DemoConfig.COMMON,
                DemoConfig.SERVER
        );
    }
}
