package com.reggarf.mods.better_lib;

import com.reggarf.mods.better_lib.configs.demo.BetterLibConfigs;
import com.reggarf.mods.better_lib.demo.DemoCrystalsPlugin;
import com.reggarf.mods.better_lib.demo.DemoOnlineMessages;
import com.reggarf.mods.better_lib.demo.DemoPlugin;
import com.reggarf.mods.better_lib.demo.DemoPopupPlugin;
import com.reggarf.mods.better_lib.message.online.OnlineMessageLib;
import com.reggarf.mods.better_lib.villager.SimpleVillagerLib;
import com.reggarf.mods.better_lib.villager.demo.ModOreTrader;
import com.reggarf.mods.better_lib.villager.json.JsonVillagerLoader;

public class CommonClass {
    public static final SimpleVillagerLib VILLAGERS = new SimpleVillagerLib(Better_lib.MODID);
    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        BetterLibConfigs.init();

        // Register Demo Join Message Plugin and Online Messages
        DemoPlugin.register();
        OnlineMessageLib.registerPlugin(new DemoOnlineMessages());

        // Register Demo Welcome Popup Plugins
        DemoPopupPlugin.register();
        DemoCrystalsPlugin.register();
        // JSON-defined professions (data/better_lib/villagers/*.json)
        registerJsonVillagers();
    }

    private static void registerJsonVillagers() {
        JsonVillagerLoader.loadAll(VILLAGERS, CommonClass.class, "/data/better_lib/villagers");
        VILLAGERS.register();
        ModOreTrader.register();
    }
}
