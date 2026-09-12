package com.reggarf.mods.better_lib;


import com.reggarf.mods.better_lib.config.core.BetterConfigManager;
import com.reggarf.mods.better_lib.demo.DemoConfig;
import com.reggarf.mods.better_lib.villager.SimpleVillagerLib;
import com.reggarf.mods.better_lib.villager.demo.ModOreTrader;
import com.reggarf.mods.better_lib.villager.json.JsonVillagerLoader;

public class CommonClass {
    public static DemoConfig CONFIG;
    public static final SimpleVillagerLib VILLAGERS = new SimpleVillagerLib(Constants.MODID);
    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        //OnlineMessageLib.registerPlugin(new DemoOnlineMessages());
        //DemoPlugin.register();
        CONFIG = BetterConfigManager.register(DemoConfig.class);
        // JSON-defined professions (data/better_lib/villagers/*.json)
        registerJsonVillagers();
    }

    private static void registerJsonVillagers() {
        JsonVillagerLoader.loadAll(VILLAGERS, CommonClass.class, "/data/better_lib/villagers");
        VILLAGERS.register();
        //Code-defined professions (ore_trader, etc.)
        //ModOreTrader.register();
    }
}
