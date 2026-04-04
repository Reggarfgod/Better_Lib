package com.reggarf.mods.better_lib;


import com.reggarf.mods.better_lib.config.core.BetterConfigScreenFactory;
import com.reggarf.mods.better_lib.demo.DemoConfig;
import com.reggarf.mods.better_lib.gui.screen.BetterConfigScreenHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;


@Mod(Constants.MODID)
public class Better_lib_neoforge {



    public Better_lib_neoforge(IEventBus eventBus) {
        Constants.LOG.info("Hello NeoForge world!");
        CommonClass.init();

    }
    @EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
    public final class BetterLibClient {

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