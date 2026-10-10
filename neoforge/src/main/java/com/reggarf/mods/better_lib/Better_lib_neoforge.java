package com.reggarf.mods.better_lib;

import com.reggarf.mods.better_lib.config.core.BetterConfigScreenFactory;
import com.reggarf.mods.better_lib.demo.DemoConfig;
import com.reggarf.mods.better_lib.gui.screen.BetterConfigScreenHandler;
import com.reggarf.mods.better_lib.popup.manager.WelcomePopupManager;
import com.reggarf.mods.better_lib.popup.network.WelcomePopupPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Better_lib.MODID)
public class Better_lib_neoforge {

    public Better_lib_neoforge(IEventBus modBus) {
        Better_lib.LOG.info("Hello NeoForge world!");
        CommonClass.init();

        modBus.addListener(this::registerPayloads);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToClient(
                WelcomePopupPayload.TYPE,
                WelcomePopupPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (FMLEnvironment.dist.isClient()) {
                        context.enqueueWork(() -> {
                            WelcomePopupManager.triggerPopup(payload.delayTicks());
                        });
                    }
                }
        );
    }

    @EventBusSubscriber(modid = Better_lib.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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
