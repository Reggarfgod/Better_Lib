package com.reggarf.mods.better_lib;

import com.reggarf.mods.better_lib.configs.demo.BetterLibConfigs;
import com.reggarf.mods.better_lib.demo.DemoCrystalsPlugin;
import com.reggarf.mods.better_lib.demo.DemoOnlineMessages;
import com.reggarf.mods.better_lib.demo.DemoPlugin;
import com.reggarf.mods.better_lib.demo.DemoPopupPlugin;
import com.reggarf.mods.better_lib.message.online.OnlineMessageLib;
import com.reggarf.mods.better_lib.villagers.SimpleVillagerLib;
import com.reggarf.mods.better_lib.villagers.demo.ModOreTrader;

public class CommonClass {
   // public static DemoConfig CONFIG;
//    public static final SimpleVillagerLib VILLAGERS = new SimpleVillagerLib(Better_lib.MODID);

    public static void init() {
        BetterLibConfigs.init();

//         Code-defined professions (ore_trader, arcane_trader, etc.)
//        ModOreTrader.register(); // Demo Villager (register)

//         Register Demo Join Message Plugin and Online Messages
//        DemoPlugin.register();
//        OnlineMessageLib.registerPlugin(new DemoOnlineMessages());

//         Register Demo Welcome Popup Plugins
//        DemoPopupPlugin.register();
//        DemoCrystalsPlugin.register();

//        VILLAGERS.loadJsonProfessions(CommonClass.class, "/data/better_lib/villagers");
//        VILLAGERS.register();
    }
}
