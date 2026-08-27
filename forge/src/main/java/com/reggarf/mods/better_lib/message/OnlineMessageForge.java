package com.reggarf.mods.better_lib.message;

import com.reggarf.mods.better_lib.Better_lib;
import com.reggarf.mods.better_lib.message.util.OnlineMessageHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Better_lib.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class OnlineMessageForge {

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            OnlineMessageHandler.onPlayerJoin(player);
        }
    }
}
