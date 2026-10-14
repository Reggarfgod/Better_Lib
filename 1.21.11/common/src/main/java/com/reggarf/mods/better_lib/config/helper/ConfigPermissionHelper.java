package com.reggarf.mods.better_lib.config.helper;

import net.minecraft.client.Minecraft;
import net.minecraft.server.permissions.Permissions;

public final class ConfigPermissionHelper {

    private ConfigPermissionHelper() {}

    public static boolean canClientEditServerConfig(Minecraft mc) {
        if (mc.level == null) return true;                 // main menu / client
        if (mc.hasSingleplayerServer()) return true;       // singleplayer
        return mc.player != null && mc.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER); // OP level 2 on server
    }
}
