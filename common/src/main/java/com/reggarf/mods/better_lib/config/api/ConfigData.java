package com.reggarf.mods.better_lib.config.api;

/**
 * Marker interface for all configuration data classes.
 * Mods should implement this when using BetterConfigManager.
 */
public interface ConfigData {

    /**
     * Called when the config is first loaded.
     * Optional override for initialization logic.
     */
    default void onLoad() {}

    /**
     * Called just before the config is saved.
     * Optional override for validation or cleanup.
     */
    default void onSave() {}


}
