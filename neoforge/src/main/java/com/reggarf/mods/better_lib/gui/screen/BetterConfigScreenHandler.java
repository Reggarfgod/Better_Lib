package com.reggarf.mods.better_lib.gui.screen;

import com.reggarf.mods.better_lib.config.ConfigBase;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.Locale;
import java.util.Map;

/**
 * Super easy config screen and ModConfig handler for NeoForge.
 * <p>
 * Allows modders to register all their configs, listeners, and UI screens in ONE simple line.
 */
public class BetterConfigScreenHandler {

    /**
     * Complete 1-line registration for all configs in a mod!
     * <p>
     * Automatically:
     * <ul>
     *   <li>Registers each config specification with NeoForge (Client, Common, Server)</li>
     *   <li>Hooks up lifecycle event listeners (onLoad / onReload)</li>
     *   <li>Registers all config tabs to BetterConfigScreen</li>
     *   <li>Hooks the NeoForge Mods menu "Config" button to open the screen</li>
     * </ul>
     *
     * @param modContainer NeoForge ModContainer
     * @param modBus       NeoForge mod event bus
     * @param modId        your mod id
     * @param configs      config instances to register
     */
    public static void registerMod(ModContainer modContainer, IEventBus modBus, String modId, ConfigBase... configs) {
        for (ConfigBase config : configs) {
            if (config == null || config.specification == null) continue;

            ModConfig.Type type = determineType(config.getName());
            String fileName = modId + "-" + config.getName() + ".toml";
            modContainer.registerConfig(type, config.specification, fileName);
        }

        // Auto lifecycle listeners
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

        // Register GUI tabs & Mods menu button
        BetterConfigScreen.register(modId, configs);
        registerAuto(modId);
    }

    /**
     * Complete 1-line registration with custom tab name mappings.
     *
     * @param modContainer NeoForge ModContainer
     * @param modBus       NeoForge mod event bus
     * @param modId        your mod id
     * @param configs      tab name -> ConfigBase map
     */
    public static void registerMod(ModContainer modContainer, IEventBus modBus, String modId, Map<String, ConfigBase> configs) {
        for (Map.Entry<String, ConfigBase> entry : configs.entrySet()) {
            ConfigBase config = entry.getValue();
            if (config == null || config.specification == null) continue;

            ModConfig.Type type = determineType(entry.getKey() + "_" + config.getName());
            String fileName = modId + "-" + config.getName() + ".toml";
            modContainer.registerConfig(type, config.specification, fileName);
        }

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

    /**
     * Automatically register a config screen factory for a given mod using BetterConfigScreen.
     *
     * @param modId your mod id
     */
    public static void registerAuto(String modId) {
        register(modId, parent -> BetterConfigScreen.create(parent, modId));
    }

    /**
     * Register a mod's configs and hook up the NeoForge config screen automatically.
     *
     * @param modId   your mod id
     * @param configs config instances to register
     */
    public static void register(String modId, ConfigBase... configs) {
        BetterConfigScreen.register(modId, configs);
        registerAuto(modId);
    }

    /**
     * Register a mod's configs with tab mapping and hook up the NeoForge config screen automatically.
     *
     * @param modId   your mod id
     * @param configs tab map -> ConfigBase
     */
    public static void register(String modId, Map<String, ConfigBase> configs) {
        BetterConfigScreen.register(modId, configs);
        registerAuto(modId);
    }

    /**
     * Register a custom config screen factory for a given mod.
     *
     * @param modId   your mod id
     * @param factory function that creates your config screen
     */
    public static void register(String modId, ConfigScreenFactory factory) {
        ModList.get().getModContainerById(modId).ifPresentOrElse(
                container -> container.registerExtensionPoint(
                        IConfigScreenFactory.class,
                        (containerInstance, parent) -> factory.create(parent)
                ),
                () -> ModLoadingContext.get().registerExtensionPoint(
                        IConfigScreenFactory.class,
                        () -> (containerInstance, parent) -> factory.create(parent)
                )
        );
    }

    /**
     * Register a config screen factory with access to the ModContainer.
     *
     * @param modId   your mod id
     * @param factory function that creates your config screen given the ModContainer and parent Screen
     */
    public static void register(String modId, ExtendedConfigScreenFactory factory) {
        ModList.get().getModContainerById(modId).ifPresentOrElse(
                container -> container.registerExtensionPoint(
                        IConfigScreenFactory.class,
                        factory::create
                ),
                () -> ModLoadingContext.get().registerExtensionPoint(
                        IConfigScreenFactory.class,
                        () -> factory::create
                )
        );
    }

    @FunctionalInterface
    public interface ConfigScreenFactory {
        Screen create(Screen parent);
    }

    @FunctionalInterface
    public interface ExtendedConfigScreenFactory {
        Screen create(ModContainer container, Screen parent);
    }
}
