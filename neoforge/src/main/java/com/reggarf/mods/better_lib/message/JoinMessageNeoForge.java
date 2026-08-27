package com.reggarf.mods.better_lib.message;

import com.reggarf.mods.better_lib.Better_lib;
import com.reggarf.mods.better_lib.message.event.JoinMessageLogic;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Better_lib.MODID, bus = EventBusSubscriber.Bus.GAME)
public class JoinMessageNeoForge {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            JoinMessageLogic.handle(player);
        }
    }
}
