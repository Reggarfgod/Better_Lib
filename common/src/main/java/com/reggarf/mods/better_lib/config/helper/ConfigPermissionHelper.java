package com.reggarf.mods.better_lib.config.helper;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public final class ConfigPermissionHelper {

    private ConfigPermissionHelper() {}

    public static boolean canClientEditServerConfig(Minecraft mc) {
        if (mc.level == null) return true;           // main menu
        if (mc.hasSingleplayerServer()) return true; // singleplayer

        Player player = mc.player;
        return player != null && player.canUseGameMasterBlocks();
    }
}
