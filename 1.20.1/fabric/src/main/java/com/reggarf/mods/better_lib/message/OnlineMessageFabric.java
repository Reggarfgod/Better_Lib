package com.reggarf.mods.better_lib.message;

import com.reggarf.mods.better_lib.message.util.OnlineMessageHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class OnlineMessageFabric {

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            OnlineMessageHandler.onPlayerJoin(handler.player);
        });
    }
}
