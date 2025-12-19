package com.reggarf.mods.better_lib.message;


import com.reggarf.mods.better_lib.message.event.JoinMessageLogic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge join message handler
 */
@Mod.EventBusSubscriber
public class JoinMessageForge {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {

        if (event.getEntity() instanceof ServerPlayer player) {
            JoinMessageLogic.handle(player);
        }
    }
}
