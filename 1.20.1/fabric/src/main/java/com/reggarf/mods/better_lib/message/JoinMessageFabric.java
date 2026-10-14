package com.reggarf.mods.better_lib.message;


import com.reggarf.mods.better_lib.message.event.JoinMessageLogic;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class JoinMessageFabric {

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            JoinMessageLogic.handle(handler.player);
        });
    }
}
