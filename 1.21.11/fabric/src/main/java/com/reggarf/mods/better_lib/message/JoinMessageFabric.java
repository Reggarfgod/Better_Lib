package com.reggarf.mods.better_lib.message;

import com.reggarf.mods.better_lib.message.event.JoinMessageLogic;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

public class JoinMessageFabric {

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = (ServerPlayer) (Object) handler.player;
            if (player != null) {
                JoinMessageLogic.handle(player);
            }
        });
    }
}
