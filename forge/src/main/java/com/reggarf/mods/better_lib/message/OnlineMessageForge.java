package com.reggarf.mods.better_lib.message;

import com.reggarf.mods.better_lib.message.util.OnlineMessageHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class OnlineMessageForge {

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            OnlineMessageHandler.onPlayerJoin(player);
        }
    }
}
