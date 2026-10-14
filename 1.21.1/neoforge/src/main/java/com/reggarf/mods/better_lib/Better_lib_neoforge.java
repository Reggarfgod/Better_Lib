package com.reggarf.mods.better_lib;

import com.reggarf.mods.better_lib.configs.demo.BetterLibConfigScreen;
import com.reggarf.mods.better_lib.gui.screen.BetterConfigScreenHandler;
import com.reggarf.mods.better_lib.popup.manager.WelcomePopupManager;
import com.reggarf.mods.better_lib.popup.network.WelcomePopupPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Better_lib.MODID)
public class Better_lib_neoforge {

    public Better_lib_neoforge(IEventBus modBus, ModContainer modContainer) {
        Better_lib.LOG.info("Hello NeoForge world!");
        CommonClass.init();

        modBus.addListener(this::registerPayloads);

        if (FMLEnvironment.dist.isClient()) {
            modContainer.registerExtensionPoint(
                    IConfigScreenFactory.class,
                    (mc, parent) -> BetterLibConfigScreen.ModScreen(parent)
            );
            modBus.addListener(this::onClientSetup);
        }
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

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BetterConfigScreenHandler.register("better_lib", BetterLibConfigScreen::ModScreen);
        });
    }
}
