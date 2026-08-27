package com.reggarf.mods.better_lib.gui.screen;

import com.reggarf.mods.better_lib.config.ConfigBase;
import fuzs.forgeconfigapiport.forge.api.neoforge.v4.NeoForgeConfigRegistry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.Locale;
import java.util.Map;

/**
 * Super easy config screen and ModConfig handler for Minecraft Forge.
 * <p>
 * Allows modders to register all their configs, listeners, and UI screens in ONE simple line.
 */
public class BetterConfigScreenHandler {

    /**
     * Complete 1-line registration for all configs in a Forge mod!
     * Automatically registers specifications to generate .toml config files,
     * hooks up lifecycle events, and registers GUI tabs.
     *
     * @param modBus  Forge mod event bus
     * @param modId   your mod id
     * @param configs config instances to register
     */
    public static void registerMod(IEventBus modBus, String modId, ConfigBase... configs) {
        for (ConfigBase config : configs) {
            if (config == null || config.specification == null) continue;

            ModConfig.Type type = determineType(config.getName());
            String fileName = modId + "-" + config.getName() + ".toml";

            try {
                NeoForgeConfigRegistry.INSTANCE.register(modId, type, config.specification, fileName);
            } catch (Throwable t) {
                if (config.specification instanceof IConfigSpec<?> spec) {
                    ModLoadingContext.get().registerConfig(type, spec, fileName);
                }
            }
        }

        // Auto lifecycle listeners
        if (modBus != null) {
            modBus.addListener((ModConfigEvent.Loading event) -> {
                Object spec = event.getConfig().getSpec();
                for (ConfigBase config : configs) {
                    if (config != null && config.specification == spec) {
                        config.onLoad();
                    }
                }
            });
            modBus.addListener((ModConfigEvent.Reloading event) -> {
                Object spec = event.getConfig().getSpec();
                for (ConfigBase config : configs) {
                    if (config != null && config.specification == spec) {
                        config.onReload();
                    }
                }
            });
        }

        // Register GUI tabs & Mods menu button
        BetterConfigScreen.register(modId, configs);
        registerAuto(modId);
    }

    /**
     * Complete 1-line registration with custom tab name mappings.
     *
     * @param modBus  Forge mod event bus
     * @param modId   your mod id
     * @param configs tab name -> ConfigBase map
     */
    public static void registerMod(IEventBus modBus, String modId, Map<String, ConfigBase> configs) {
        for (Map.Entry<String, ConfigBase> entry : configs.entrySet()) {
            ConfigBase config = entry.getValue();
            if (config == null || config.specification == null) continue;

            ModConfig.Type type = determineType(entry.getKey() + "_" + config.getName());
            String fileName = modId + "-" + config.getName() + ".toml";

            try {
                NeoForgeConfigRegistry.INSTANCE.register(modId, type, config.specification, fileName);
            } catch (Throwable t) {
                if (config.specification instanceof IConfigSpec<?> spec) {
                    ModLoadingContext.get().registerConfig(type, spec, fileName);
                }
            }
        }

        if (modBus != null) {
            modBus.addListener((ModConfigEvent.Loading event) -> {
                Object spec = event.getConfig().getSpec();
                for (ConfigBase config : configs.values()) {
                    if (config != null && config.specification == spec) {
                        config.onLoad();
                    }
                }
            });
            modBus.addListener((ModConfigEvent.Reloading event) -> {
                Object spec = event.getConfig().getSpec();
                for (ConfigBase config : configs.values()) {
                    if (config != null && config.specification == spec) {
                        config.onReload();
                    }
                }
            });
        }

        BetterConfigScreen.register(modId, configs);
        registerAuto(modId);
    }

    private static ModConfig.Type determineType(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.contains("client")) {
            return ModConfig.Type.CLIENT;
        } else if (lower.contains("server")) {
            return ModConfig.Type.SERVER;
        }
        return ModConfig.Type.COMMON;
    }

    public static void registerAuto(String modId) {
        register(modId, parent -> BetterConfigScreen.create(parent, modId));
    }

    public static void register(String modId, ConfigBase... configs) {
        BetterConfigScreen.register(modId, configs);
        registerAuto(modId);
    }

    public static void register(String modId, ScreenFactory factory) {
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> factory.create(parent))
        );
    }

    @FunctionalInterface
    public interface ScreenFactory {
        Screen create(Screen parent);
    }
}
