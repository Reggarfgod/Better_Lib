package com.reggarf.mods.better_lib;

import com.reggarf.mods.better_lib.configs.demo.BetterLibConfigs;
import com.reggarf.mods.better_lib.villagers.SimpleVillagerLib;

public class CommonClass {
   // public static DemoConfig CONFIG;
    public static final SimpleVillagerLib VILLAGERS = new SimpleVillagerLib(Better_lib.MODID);

    public static void init() {
        BetterLibConfigs.init();
        VILLAGERS.loadJsonProfessions(CommonClass.class, "/data/better_lib/villagers");
        VILLAGERS.register();
    }
}
