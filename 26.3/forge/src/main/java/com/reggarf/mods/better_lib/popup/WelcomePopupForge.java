package com.reggarf.mods.better_lib.popup;

import com.reggarf.mods.better_lib.Better_lib;
import com.reggarf.mods.better_lib.Better_lib_forge;
import com.reggarf.mods.better_lib.popup.command.WelcomePopupCommands;
import com.reggarf.mods.better_lib.popup.event.WelcomePopupMessageHandler;
import com.reggarf.mods.better_lib.popup.manager.WelcomePopupManager;
import com.reggarf.mods.better_lib.popup.network.WelcomePopupPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.PacketDistributor;

public final class WelcomePopupForge {

    private static boolean initialized = false;

    private WelcomePopupForge() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        Better_lib.LOG.info("[WelcomePopupForge] Initializing Forge welcome popup listeners");

        RegisterCommandsEvent.BUS.addListener(event -> {
            Better_lib.LOG.info("[WelcomePopupForge] Registering server commands");
            WelcomePopupCommands.register(event.getDispatcher());
        });

        PlayerEvent.PlayerLoggedInEvent.BUS.addListener(event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                boolean firstJoin = WelcomePopupMessageHandler.isFirstJoin(player);
                Better_lib.LOG.info("[WelcomePopupForge] Player logged in: {}, isFirstJoin={}", player.getScoreboardName(), firstJoin);
                if (firstJoin) {
                    WelcomePopupMessageHandler.markPlayerAsJoined(player);
                    if (FMLEnvironment.dist.isClient()) {
                        WelcomePopupManager.triggerPopup(20);
                    }
                    Better_lib_forge.CHANNEL.send(new WelcomePopupPayload(20), PacketDistributor.PLAYER.with(player));
                }
            }
        });

        if (FMLEnvironment.dist.isClient()) {
            TickEvent.ClientTickEvent.Post.BUS.addListener(event -> {
                WelcomePopupManager.onClientTick();
            });

            RegisterClientCommandsEvent.BUS.addListener(event -> {
                Better_lib.LOG.info("[WelcomePopupForge] Registering client commands");
                WelcomePopupCommands.register(event.getDispatcher());
            });
        }
    }
}
