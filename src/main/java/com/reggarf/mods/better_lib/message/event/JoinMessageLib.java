package com.reggarf.mods.better_lib.message.event;

import com.reggarf.mods.better_lib.message.api.JoinMessagePlugin;
import com.reggarf.mods.better_lib.message.api.JoinMessagePlugins;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.lang.reflect.Method;

@EventBusSubscriber
public class JoinMessageLib {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        CompoundTag persistent = player.getPersistentData();
        CompoundTag tag = getCompoundSafe(persistent, ServerPlayer.PERSISTED_NBT_TAG);

        for (JoinMessagePlugin plugin : JoinMessagePlugins.all()) {
            if (!plugin.enabled())
                continue;

            String key = "joinmsglib_" + plugin.getModId() + "_hasJoinedBefore";

            if (!getBooleanSafe(tag, key)) {
                for (JoinMessageSet set : plugin.getMessageSets()) {
                    set.sendTo(player);
                }
                putBooleanSafe(tag, key, true);
                putCompoundSafe(persistent, ServerPlayer.PERSISTED_NBT_TAG, tag);
            }
        }
    }

    /** --- Helpers for cross-version NBT access --- */

    private static CompoundTag getCompoundSafe(CompoundTag tag, String key) {
        try {
            Method m = CompoundTag.class.getMethod("getCompound", String.class);
            return (CompoundTag) m.invoke(tag, key);
        } catch (Throwable ignored) {
            // Fallback: use contains
            try {
                if ((boolean) CompoundTag.class
                        .getMethod("contains", String.class, int.class)
                        .invoke(tag, key, 10)) {
                    return (CompoundTag) CompoundTag.class
                            .getMethod("get", String.class)
                            .invoke(tag, key);
                }
            } catch (Throwable ignored2) {}
        }
        return new CompoundTag();
    }

    private static boolean getBooleanSafe(CompoundTag tag, String key) {
        try {
            // Old versions
            Method m = CompoundTag.class.getMethod("getBoolean", String.class);
            return (boolean) m.invoke(tag, key);
        } catch (NoSuchMethodException e) {
            // Newer versions (1.21.5+)
            try {
                Method m = CompoundTag.class.getMethod("getBooleanTag", String.class);
                return (boolean) m.invoke(tag, key);
            } catch (Throwable ignored2) {}
        } catch (Throwable ignored) {}
        return false;
    }

    private static void putBooleanSafe(CompoundTag tag, String key, boolean value) {
        try {
            Method m = CompoundTag.class.getMethod("putBoolean", String.class, boolean.class);
            m.invoke(tag, key, value);
        } catch (Throwable ignored) {}
    }

    private static void putCompoundSafe(CompoundTag parent, String key, CompoundTag value) {
        try {
            Method m = CompoundTag.class.getMethod("put", String.class, net.minecraft.nbt.Tag.class);
            m.invoke(parent, key, value);
        } catch (Throwable ignored) {}
    }
}
