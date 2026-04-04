package com.reggarf.mods.better_lib.platform;


import com.reggarf.mods.better_lib.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
//    @Override
//    public boolean hasPlayerTag(Object obj, String tag) {
//        if (!(obj instanceof ServerPlayer player)) return false;
//        return player.entityTags().contains(tag);
//    }
//
//    @Override
//    public void addPlayerTag(Object obj, String tag) {
//        if (obj instanceof ServerPlayer player) {
//            player.addTag(tag);
//        }
//    }
}
