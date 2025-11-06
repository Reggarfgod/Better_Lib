package com.reggarf.mods.better_lib.config.gui;


import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * Handles registering config screens so the Mods menu "Config" button opens your BetterConfigScreen.
 */
public class betterConfigScreenHandler {

    /**
     * Register a config screen factory for a given mod.
     *
     * @param modId   your mod id
     * @param factory function that creates your config screen
     */
    public static void register(String modId, ConfigScreenFactory factory) {
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> factory.create(parent))
        );
    }

    @FunctionalInterface
    public interface ConfigScreenFactory {
        Screen create(Screen parent);
    }
}
