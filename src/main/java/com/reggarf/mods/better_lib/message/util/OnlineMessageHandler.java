package com.reggarf.mods.better_lib.message.util;

import com.reggarf.mods.better_lib.message.api.OnlineMessagePlugin;
import com.reggarf.mods.better_lib.message.event.OnlineMessageSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.reggarf.mods.better_lib.Better_lib.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class OnlineMessageHandler {

    private static final Map<String, OnlineMessageSet> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> ENABLED = new ConcurrentHashMap<>();

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

    public static void refreshMessages(String modId, OnlineMessagePlugin plugin) {
        if (!ENABLED.getOrDefault(modId, true)) return;
        OnlineMessageSet set = fetchAndBuildMessages(plugin);
        CACHE.put(modId, set);
        System.out.println("[BetterLib] Refetched online messages for " + modId);
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

            String[] parts = line.split("\\|");
            String label = parts.length > 0 ? parts[0].trim() : "Link";
            String url = parts.length > 1 ? parts[1].trim() : "";
            String color = parts.length > 2 ? parts[2].trim() : "#00FF00";
            String desc = parts.length > 3 ? parts[3].trim() : "";

            set.addLink(label, url, color, desc);
        }

        return set;
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        for (var entry : CACHE.entrySet()) {
            String modId = entry.getKey();
            if (!ENABLED.getOrDefault(modId, true)) continue;

            OnlineMessageSet set = entry.getValue();
            if (set == null) continue;

            String key = "onlinemsg_seen_" + modId;
            String currentHash = set.hash();

            // Check if the player has seen this hash
            if (!player.getTags().contains(key + "_" + currentHash)) {

                // Send message
                set.sendTo(player);

                // Remove old versions
                player.getTags().removeIf(tag -> tag.startsWith(key + "_"));

                // Add new "seen" tag
                player.addTag(key + "_" + currentHash);
            }
        }
    }
}
