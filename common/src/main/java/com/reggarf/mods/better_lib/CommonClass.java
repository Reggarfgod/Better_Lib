package com.reggarf.mods.better_lib;

import com.reggarf.mods.better_lib.config.core.BetterConfigManager;
import com.reggarf.mods.better_lib.demo.DemoConfig;
import com.reggarf.mods.better_lib.villagers.SimpleVillagerLib;
import com.reggarf.mods.better_lib.villagers.demo.ModOreTrader;

public class CommonClass {
    public static DemoConfig CONFIG;
    public static final SimpleVillagerLib VILLAGERS = new SimpleVillagerLib(Better_lib.MODID);

    public static void init() {
        //OnlineMessageLib.registerPlugin(new DemoOnlineMessages());
        //DemoPlugin.register();
        CONFIG = BetterConfigManager.register(DemoConfig.class);

        // Code-defined professions (ore_trader, arcane_trader, etc.)
        ModOreTrader.register(); // Demo Villager (register)

        // JSON-defined professions under /villagers/*.json - isolated: a
        // missing/empty folder or a bad file logs an error instead of
        // throwing, so it can never block the code-defined professions
        // above (which is what was happening before).
        VILLAGERS.loadJsonProfessions(CommonClass.class, "/data/better_lib/villagers");

        // Single flush, once, after every profession (code AND json) has
        // been added. This must be last - flushing earlier and then adding
        // more professions afterward means those later ones never get
        // registered with the platform.
        VILLAGERS.register();
    }
}