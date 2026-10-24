package com.reggarf.mods.better_lib;

import com.reggarf.mods.better_lib.configs.demo.BetterLibConfigs;
import com.reggarf.mods.better_lib.villager.SimpleVillagerLib;
import com.reggarf.mods.better_lib.villager.demo.ModOreTrader;
import com.reggarf.mods.better_lib.villager.json.JsonVillagerLoader;

public class CommonClass {
  //  public static final SimpleVillagerLib VILLAGERS = new SimpleVillagerLib(Better_lib.MODID);
    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        BetterLibConfigs.init();

        // JSON-defined professions (data/better_lib/villagers/*.json)
        registerJsonVillagers();
    }

    private static void registerJsonVillagers() {
//        JsonVillagerLoader.loadAll(VILLAGERS, CommonClass.class, "/data/better_lib/villagers");
//        VILLAGERS.register();
//        ModOreTrader.register();
    }
}
