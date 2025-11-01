package com.reggarf.mods.better_lib.message.online;



import com.reggarf.mods.better_lib.message.api.OnlineMessagePlugin;
import com.reggarf.mods.better_lib.message.util.OnlineMessageHandler;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main entry for registering and initializing dual-URL online message plugins.
 */
public class OnlineMessageLib {

    private static final Map<String, OnlineMessagePlugin> PLUGINS = new ConcurrentHashMap<>();

    /** Mods call this in their setup to register URLs. */
    public static void registerPlugin(OnlineMessagePlugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        PLUGINS.put(plugin.getModId(), plugin);
        OnlineMessageHandler.initializeFor(plugin.getModId(), plugin);
    }

    public static Map<String, OnlineMessagePlugin> getRegistered() {
        return PLUGINS;
    }
}
