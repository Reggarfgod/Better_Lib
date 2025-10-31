package com.reggarf.mods.better_lib.message.util;

import java.util.*;

public class JoinMessagePlugins {
    private static final Map<String, JoinMessagePlugin> PLUGINS = new LinkedHashMap<>();

    public static void register(JoinMessagePlugin plugin) {
        PLUGINS.put(plugin.getModId(), plugin);
    }

    public static Collection<JoinMessagePlugin> all() {
        return PLUGINS.values();
    }

    public static JoinMessagePlugin get(String modId) {
        return PLUGINS.get(modId);
    }
}
