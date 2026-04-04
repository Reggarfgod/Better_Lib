package com.reggarf.mods.better_lib.message.util;

import com.reggarf.mods.better_lib.message.api.OnlineMessagePlugin;
import com.reggarf.mods.better_lib.message.event.OnlineMessageSet;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class OnlineMessageHandler {

    private static final Map<String, OnlineMessageSet> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> ENABLED = new ConcurrentHashMap<>();

    private OnlineMessageHandler() {}

    public static void initializeFor(String modId, OnlineMessagePlugin plugin) {

        boolean enabled = plugin.isOnlineMessageEnabled();
        ENABLED.put(modId, enabled);

        if (!enabled) return;

        OnlineMessageSet set = fetchAndBuildMessages(plugin);
        CACHE.put(modId, set);
    }

    public static void refreshMessages(String modId, OnlineMessagePlugin plugin) {
        if (!ENABLED.getOrDefault(modId, true)) return;
        CACHE.put(modId, fetchAndBuildMessages(plugin));
    }

    public static void onPlayerJoin(ServerPlayer player) {

        for (var entry : CACHE.entrySet()) {
            String modId = entry.getKey();
            if (!ENABLED.getOrDefault(modId, true)) continue;

            OnlineMessageSet set = entry.getValue();
            if (set == null) continue;

            String hash = set.hash();
            String tag = "better_lib:online_" + modId + "_" + hash;

            // Already seen this version
            if (player.entityTags().contains(tag)) continue;

            set.sendTo(player);
            player.addTag(tag);
        }
    }

    private static OnlineMessageSet fetchAndBuildMessages(OnlineMessagePlugin plugin) {

        String messageData = OnlineMessageFetcher.fetchOnlineMessage(plugin.getMessageUrl());
        String clickableData = OnlineMessageFetcher.fetchOnlineMessage(plugin.getClickableUrl());

        OnlineMessageSet set = new OnlineMessageSet();

        for (String line : messageData.split("\n")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            set.addText(line, "#FFFFFF");
        }

        for (String line : clickableData.split("\n")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] p = line.split("\\|");
            set.addLink(
                    p.length > 0 ? p[0].trim() : "Link",
                    p.length > 1 ? p[1].trim() : "",
                    p.length > 2 ? p[2].trim() : "#00FF00",
                    p.length > 3 ? p[3].trim() : ""
            );
        }

        return set;
    }
}
