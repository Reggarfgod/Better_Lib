package com.reggarf.mods.better_lib.message.api;

/**
 * Interface mods implement to register their online message source and clickable URL.
 */
public interface OnlineMessagePlugin {
    /**
     * @return Your mod id (e.g. "create_better_villager").
     */
    String getModId();

    /**
     * @return URL to fetch the main message (plain text).
     */
    String getMessageUrl();

    /**
     * @return URL to fetch the clickable link (plain text).
     */
    String getClickableUrl();

    /**
     * @return true if this mod’s online message fetching should be enabled.
     * Mods can override this to allow player config, debugging, etc.
     */
    default boolean isOnlineMessageEnabled() {
        return true;
    }
}
