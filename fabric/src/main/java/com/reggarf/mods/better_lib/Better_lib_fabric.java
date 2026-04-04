package com.reggarf.mods.better_lib;

import com.reggarf.mods.better_lib.message.JoinMessageFabric;
import com.reggarf.mods.better_lib.message.OnlineMessageFabric;
import net.fabricmc.api.ModInitializer;

public class Better_lib_fabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        Constants.LOG.info("Hello Fabric world!");
        CommonClass.init();
        JoinMessageFabric.register();
        OnlineMessageFabric.register();
    }

}
