package com.reggarf.mods.better_lib.message.event;

import com.reggarf.mods.better_lib.message.api.JoinMessagePlugin;
import com.reggarf.mods.better_lib.message.api.JoinMessagePlugins;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class JoinMessageLib {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        for (JoinMessagePlugin plugin : JoinMessagePlugins.all()) {
            if (!plugin.enabled()) continue;

            // Use player tags instead of persistent NBT
            String key = "joinmsglib_" + plugin.getModId() + "_hasJoinedBefore";

            if (!player.getTags().contains(key)) {

                // First join → send messages
                plugin.getMessageSets().forEach(set -> set.sendTo(player));

                // Save tag so it never triggers again
                player.addTag(key);
            }
        }
    }
}
