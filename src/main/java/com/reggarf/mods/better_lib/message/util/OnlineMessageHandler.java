package com.reggarf.mods.better_lib.message.util;

import com.reggarf.mods.better_lib.message.api.OnlineMessagePlugin;
import com.reggarf.mods.better_lib.message.event.OnlineMessageSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cross-version compatible Online Message Handler for Better Lib.
 * Works on both pre-1.21.5 and 1.21.5+ NeoForge builds.
 * Supports multiple messages per mod, refetching, and clickable links.
 */
public class OnlineMessageHandler {

    private static final Map<String, OnlineMessageSet> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> ENABLED = new ConcurrentHashMap<>();

    /**
     * Called during mod initialization to register online messages.
     */
    public static void initializeFor(String modId, OnlineMessagePlugin plugin) {
        boolean enabled = plugin.isOnlineMessageEnabled();
        ENABLED.put(modId, enabled);

        if (!enabled) {
            System.out.println("[BetterLib] Online messages disabled for: " + modId);
            return;
        }

        OnlineMessageSet set = fetchAndBuildMessages(plugin);
        CACHE.put(modId, set);

        System.out.println("[BetterLib] Loaded online message set for " + modId);
    }

    /**
     * Developers can call this to refetch the messages while the game is running.
     */
    public static void refreshMessages(String modId, OnlineMessagePlugin plugin) {
        if (!ENABLED.getOrDefault(modId, true)) return;
        OnlineMessageSet set = fetchAndBuildMessages(plugin);
        CACHE.put(modId, set);
        System.out.println("[BetterLib] Refetched online messages for " + modId);
    }

    /**
     * Reads multi-line and multi-link messages.
     */
    private static OnlineMessageSet fetchAndBuildMessages(OnlineMessagePlugin plugin) {
        String messageData = OnlineMessageFetcher.fetchOnlineMessage(plugin.getMessageUrl());
        String clickableData = OnlineMessageFetcher.fetchOnlineMessage(plugin.getClickableUrl());

        OnlineMessageSet set = new OnlineMessageSet();

        // Normal text lines
        for (String line : messageData.split("\n")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue; // skip comments
            set.addText(line, "#FFFFFF");
        }

        // Clickable links
        for (String line : clickableData.split("\n")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            // Format: Label|URL|ColorHex|Description
            String[] parts = line.split("\\|");
            String label = parts.length > 0 ? parts[0].trim() : "Link";
            String url = parts.length > 1 ? parts[1].trim() : "";
            String color = parts.length > 2 ? parts[2].trim() : "#00FF00";
            String desc = parts.length > 3 ? parts[3].trim() : "";

            set.addLink(label, url, color, desc);
        }

        return set;
    }

    /**
     * Sends the message set to players when they join or when a new message version is detected.
     * Fully compatible with 1.21.1 to 1.21.8+ (handles NBT API changes).
     */
    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        CompoundTag data = player.getPersistentData();

        for (var entry : CACHE.entrySet()) {
            String modId = entry.getKey();
            if (!ENABLED.getOrDefault(modId, true)) continue;

            OnlineMessageSet set = entry.getValue();
            if (set == null) continue;

            String key = "lastSeenMessage_" + modId;
            String lastHash = safeGetString(data, key);
            String currentHash = set.hash();

            if (!currentHash.equals(lastHash)) {
                set.sendTo(player);
                safePutString(data, key, currentHash);
            }
        }
    }

    /**
     * Safely reads a String tag across 1.21.1 → 1.21.8.
     */
    private static String safeGetString(CompoundTag tag, String key) {
        if (tag == null || key == null) return "";
        try {
            if (tag.contains(key, 8)) { // 8 = String
                return tag.getString(key);
            }
        } catch (Throwable t) {
            try {
                // In case mappings changed in later versions
                Object o = tag.get(key);
                return o instanceof String ? (String) o : "";
            } catch (Throwable ignored) {}
        }
        return "";
    }

    /**
     * Safely writes a String tag across 1.21.1 → 1.21.8.
     */
    private static void safePutString(CompoundTag tag, String key, String value) {
        if (tag == null || key == null) return;
        try {
            tag.putString(key, value);
        } catch (Throwable t) {
            try {
                // fallback reflection in case of future rename
                var method = tag.getClass().getMethod("put", String.class, Object.class);
                method.invoke(tag, key, value);
            } catch (Throwable ignored) {}
        }
    }
}
