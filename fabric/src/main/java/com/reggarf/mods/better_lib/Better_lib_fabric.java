package com.reggarf.mods.better_lib;

import com.reggarf.mods.better_lib.message.JoinMessageFabric;
import com.reggarf.mods.better_lib.message.OnlineMessageFabric;
import com.reggarf.mods.better_lib.popup.WelcomePopupFabric;
import com.reggarf.mods.better_lib.popup.network.WelcomePopupPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class Better_lib_fabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Better_lib.LOG.info("Hello Fabric world!");
        CommonClass.init();
        PayloadTypeRegistry.playS2C().register(WelcomePopupPayload.TYPE, WelcomePopupPayload.STREAM_CODEC);
        JoinMessageFabric.register();
        OnlineMessageFabric.register();
        WelcomePopupFabric.register();
    }
}
