package com.reggarf.mods.better_lib.popup;

import com.reggarf.mods.better_lib.Better_lib;
import com.reggarf.mods.better_lib.Better_lib_forge;
import com.reggarf.mods.better_lib.popup.command.WelcomePopupCommands;
import com.reggarf.mods.better_lib.popup.event.WelcomePopupMessageHandler;
import com.reggarf.mods.better_lib.popup.manager.WelcomePopupManager;
import com.reggarf.mods.better_lib.popup.network.WelcomePopupPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

public class WelcomePopupForge {

    @Mod.EventBusSubscriber(modid = Better_lib.MODID)
    public static class CommonEvents {
        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            WelcomePopupCommands.register(event.getDispatcher());
        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (WelcomePopupMessageHandler.isFirstJoin(player)) {
                    WelcomePopupMessageHandler.markPlayerAsJoined(player);
                    Better_lib_forge.CHANNEL.send(new WelcomePopupPayload(20), PacketDistributor.PLAYER.with(player));
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = Better_lib.MODID, value = Dist.CLIENT)
    public static class ClientEvents {

        @SubscribeEvent
        public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
            WelcomePopupCommands.register(event.getDispatcher());
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
            WelcomePopupManager.onClientTick();
        }
    }
}
