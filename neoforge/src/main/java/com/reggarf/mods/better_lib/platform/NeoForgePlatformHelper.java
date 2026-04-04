package com.reggarf.mods.better_lib.platform;


import com.reggarf.mods.better_lib.platform.services.IPlatformHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.getCurrent().isProduction();
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