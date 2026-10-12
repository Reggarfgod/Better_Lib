package com.reggarf.mods.better_lib.popup;

import com.reggarf.mods.better_lib.popup.command.WelcomePopupCommands;
import com.reggarf.mods.better_lib.popup.event.WelcomePopupMessageHandler;
import com.reggarf.mods.better_lib.popup.network.WelcomePopupPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

public class WelcomePopupFabric {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            WelcomePopupCommands.register(dispatcher);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer serverPlayer = handler.getPlayer();
            if (WelcomePopupMessageHandler.isFirstJoin(serverPlayer)) {
                WelcomePopupMessageHandler.markPlayerAsJoined(serverPlayer);
                ServerPlayNetworking.send(serverPlayer, new WelcomePopupPayload(20));
            }
        });

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            WelcomePopupFabricClient.register();
        }
    }
}
