package com.reggarf.mods.better_lib.gui.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * Handles registering config screens so the Mods menu "Config" button opens your BetterConfigScreen.
 */
public class BetterConfigScreenHandler {

    /**
     * Register a config screen factory for a given mod.
     *
     * @param modId   your mod id
     * @param factory function that creates your config screen
     */
    public static void register(String modId, ConfigScreenFactory factory) {
        try {
            MinecraftForge.registerConfigScreen((mc, parent) -> factory.create(parent));
        } catch (Throwable t) {
            try {
                ModLoadingContext.get().registerExtensionPoint(
                        ConfigScreenHandler.ConfigScreenFactory.class,
                        () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> factory.create(parent))
                );
            } catch (Throwable ignored) {}
        }
    }

    @FunctionalInterface
    public interface ConfigScreenFactory {
        Screen create(Screen parent);
    }
}
