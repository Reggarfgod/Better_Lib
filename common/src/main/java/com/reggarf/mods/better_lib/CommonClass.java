package com.reggarf.mods.better_lib;


import com.reggarf.mods.better_lib.config.core.BetterConfigManager;
import com.reggarf.mods.better_lib.demo.DemoConfig;
import com.reggarf.mods.better_lib.villagers.SimpleVillagerLib;
import com.reggarf.mods.better_lib.villagers.json.JsonVillagerLoader;

public class CommonClass {
    public static DemoConfig CONFIG;
    public static final SimpleVillagerLib VILLAGERS = new SimpleVillagerLib(Better_lib.MODID);

    public static void init() {
        //OnlineMessageLib.registerPlugin(new DemoOnlineMessages());
        //DemoPlugin.register();
        CONFIG = BetterConfigManager.register(DemoConfig.class);
        // registers every profession under /villagers/*.json, including the final flush
        JsonVillagerLoader.loadAll(VILLAGERS, Better_lib.class, "/data/better_lib/villagers");
        VILLAGERS.register();
    }
}