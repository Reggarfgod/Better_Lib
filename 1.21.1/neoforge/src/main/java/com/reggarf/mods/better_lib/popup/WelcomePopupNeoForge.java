package com.reggarf.mods.better_lib.popup;

import com.reggarf.mods.better_lib.Better_lib;
import com.reggarf.mods.better_lib.popup.command.WelcomePopupCommands;
import com.reggarf.mods.better_lib.popup.event.WelcomePopupMessageHandler;
import com.reggarf.mods.better_lib.popup.manager.WelcomePopupManager;
import com.reggarf.mods.better_lib.popup.network.WelcomePopupPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class WelcomePopupNeoForge {

    @EventBusSubscriber(modid = Better_lib.MODID)
    public static class CommonEvents {
        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            WelcomePopupCommands.register(event.getDispatcher());
        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                if (WelcomePopupMessageHandler.isFirstJoin(serverPlayer)) {
                    WelcomePopupMessageHandler.markPlayerAsJoined(serverPlayer);

                    if (FMLEnvironment.dist.isClient()) {
                        // Integrated singleplayer server: trigger locally
                        WelcomePopupManager.triggerPopup(20);
                    } else {
                        // Dedicated server: send packet to client if client has channel registered
                        if (serverPlayer.connection != null && serverPlayer.connection.hasChannel(WelcomePopupPayload.TYPE)) {
                            PacketDistributor.sendToPlayer(serverPlayer, new WelcomePopupPayload(20));
                        }
                    }
                }
            }
        }
    }

    @EventBusSubscriber(modid = Better_lib.MODID, value = Dist.CLIENT)
    public static class ClientEvents {

        @SubscribeEvent
        public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
            WelcomePopupCommands.register(event.getDispatcher());
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            WelcomePopupManager.onClientTick();
        }
    }
}
