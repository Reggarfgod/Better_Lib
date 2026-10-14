package com.reggarf.mods.better_lib.popup.event;

import net.minecraft.server.level.ServerPlayer;

public class WelcomePopupMessageHandler {

    public static final String JOIN_TAG = "better_lib_has_joined";

    /**
     * Checks whether this player is joining this world for the first time.
     * Persistent tags on the player entity are saved per world / per server in the player's .dat file.
     */
    public static boolean isFirstJoin(ServerPlayer player) {
        return !player.entityTags().contains(JOIN_TAG);
    }

    /**
     * Marks this player as having joined this world, persisting the join tag into the world save.
     */
    public static void markPlayerAsJoined(ServerPlayer player) {
        player.addTag(JOIN_TAG);
    }
}
