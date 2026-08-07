package com.reggarf.mods.better_lib;

import com.reggarf.mods.better_lib.message.JoinMessageFabric;
import com.reggarf.mods.better_lib.message.OnlineMessageFabric;
import com.reggarf.mods.better_lib.villager.VillagerRegistrar;
import net.fabricmc.api.ModInitializer;

public class Better_lib_fabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        Constants.LOG.info("Hello Fabric world!");
        CommonClass.init();
        VillagerRegistrar.register();
        JoinMessageFabric.register();
        OnlineMessageFabric.register();
    }

}
