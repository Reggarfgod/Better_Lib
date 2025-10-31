package com.reggarf.mods.better_lib.message.event;

import com.reggarf.mods.better_lib.message.util.JoinMessagePlugin;
import com.reggarf.mods.better_lib.message.util.JoinMessagePlugins;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber
public class JoinMessageLib {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        CompoundTag persistent = player.getPersistentData();
        CompoundTag tag = persistent.getCompound(ServerPlayer.PERSISTED_NBT_TAG);

        for (JoinMessagePlugin plugin : JoinMessagePlugins.all()) {
            if (!plugin.enabled()) continue;
            String key = "joinmsglib_" + plugin.getModId() + "_hasJoinedBefore";

            if (!tag.getBoolean(key)) {
                for (JoinMessageSet set : plugin.getMessageSets()) {
                    set.sendTo(player);
                }
                tag.putBoolean(key, true);
                persistent.put(ServerPlayer.PERSISTED_NBT_TAG, tag);
            }
        }
    }
}
