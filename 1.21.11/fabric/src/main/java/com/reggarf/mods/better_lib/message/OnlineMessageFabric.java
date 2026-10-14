package com.reggarf.mods.better_lib.message;

import com.reggarf.mods.better_lib.message.util.OnlineMessageHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

public class OnlineMessageFabric {

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = (ServerPlayer) (Object) handler.player;
            if (player != null) {
                OnlineMessageHandler.onPlayerJoin(player);
            }
        });
    }
}
