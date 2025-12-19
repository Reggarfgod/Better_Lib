package com.reggarf.mods.better_lib;


import com.reggarf.mods.better_lib.config.core.BetterConfigManager;
import com.reggarf.mods.better_lib.demo.DemoConfig;

public class CommonClass {
    public static DemoConfig CONFIG;
    public static void init() {
        //OnlineMessageLib.registerPlugin(new DemoOnlineMessages());
        //DemoPlugin.register();
        CONFIG = BetterConfigManager.register(DemoConfig.class);
    }
}