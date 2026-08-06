package com.reggarf.mods.better_lib;


import com.reggarf.mods.better_lib.config.core.BetterConfigManager;
import com.reggarf.mods.better_lib.demo.DemoConfig;
import com.reggarf.mods.better_lib.villager.SimpleVillagerLib;
import com.reggarf.mods.better_lib.villager.demo.ModOreTrader;


public class CommonClass {
    public static DemoConfig CONFIG;
    public static final SimpleVillagerLib VILLAGERS = new SimpleVillagerLib(Constants.MODID);
    public static void init() {
        //OnlineMessageLib.registerPlugin(new DemoOnlineMessages());
        //DemoPlugin.register();
        CONFIG = BetterConfigManager.register(DemoConfig.class);

        // Code-defined professions (ore_trader, arcane_trader, etc.)
       ModOreTrader.register(); // Demo Villager (register)

    }
}