package com.reggarf.mods.better_lib;

import com.reggarf.mods.better_lib.config.core.BetterConfigManager;
import com.reggarf.mods.better_lib.demo.DemoConfig;
import com.reggarf.mods.better_lib.demo.DemoCrystalsPlugin;
import com.reggarf.mods.better_lib.demo.DemoPopupPlugin;
import com.reggarf.mods.better_lib.villagers.SimpleVillagerLib;
import com.reggarf.mods.better_lib.villagers.demo.ModOreTrader;
import com.reggarf.mods.better_lib.villagers.json.JsonVillagerLoader;

public class CommonClass {
    public static DemoConfig CONFIG;
    public static final SimpleVillagerLib VILLAGERS = new SimpleVillagerLib(Better_lib.MODID);

    public static void init() {
           // Register demo welcome popups
//           DemoPopupPlugin.register();
//           DemoCrystalsPlugin.register();
        CONFIG = BetterConfigManager.register(DemoConfig.class);
        // Code-defined professions (ore_trader, arcane_trader, etc.)
       // ModOreTrader.register(); // Demo Villager (register)
        VILLAGERS.loadJsonProfessions(CommonClass.class, "/data/better_lib/villagers");
        VILLAGERS.register();
    }
}
