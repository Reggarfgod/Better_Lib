package com.reggarf.mods.better_lib.popup;

import com.reggarf.mods.better_lib.popup.manager.WelcomePopupManager;
import com.reggarf.mods.better_lib.popup.network.WelcomePopupPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public class WelcomePopupFabricClient {

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            WelcomePopupManager.onClientTick();
        });

        ClientPlayNetworking.registerGlobalReceiver(WelcomePopupPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                WelcomePopupManager.triggerPopup(payload.delayTicks());
            });
        });
    }
}
