package com.reggarf.mods.better_lib.config.helper;

import net.minecraft.client.Minecraft;

public final class ConfigPermissionHelper {

    private ConfigPermissionHelper() {}

    public static boolean canClientEditServerConfig(Minecraft mc) {
        if (mc.level == null) return true;                 // main menu / client
        if (mc.hasSingleplayerServer()) return true;       // singleplayer
        return mc.player != null && mc.player.hasPermissions(2); // OP on server
    }
}
