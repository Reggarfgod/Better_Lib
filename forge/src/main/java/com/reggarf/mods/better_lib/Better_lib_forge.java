package com.reggarf.mods.better_lib;

import com.reggarf.mods.better_lib.config.core.BetterConfigScreenFactory;
import com.reggarf.mods.better_lib.demo.DemoConfig;
import com.reggarf.mods.better_lib.gui.screen.BetterConfigScreenHandler;
import com.reggarf.mods.better_lib.villagers.demo.ModOreTrader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(Better_lib.MODID)
public class Better_lib_forge {

    public Better_lib_forge() {
        Better_lib.LOG.info("Hello Forge world!");
        CommonClass.init();

       // ModOreTrader.register();  // Demo Villager (register)
    }

    @Mod.EventBusSubscriber(modid = Better_lib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                BetterConfigScreenHandler.register("better_lib", parent ->
                        BetterConfigScreenFactory.from(DemoConfig.class, CommonClass.CONFIG, parent)
                );
            });
        }
    }
}