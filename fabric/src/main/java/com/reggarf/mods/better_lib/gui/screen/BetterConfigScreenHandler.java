package com.reggarf.mods.better_lib.gui.screen;

import com.reggarf.mods.better_lib.config.ConfigBase;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;
import net.neoforged.fml.config.ModConfig;

import java.util.Locale;
import java.util.Map;

/**
 * Config registration and handler for Fabric via Forge Config API Port.
 */
public class BetterConfigScreenHandler {

    /**
     * Complete 1-line registration for all configs in a Fabric mod!
     * Automatically registers specifications to generate .toml config files,
     * hooks up lifecycle events, and registers GUI tabs.
     *
     * @param modId   your mod id
     * @param configs config instances to register
     */
    public static void registerMod(String modId, ConfigBase... configs) {
        for (ConfigBase config : configs) {
            if (config == null || config.specification == null) continue;

            ModConfig.Type type = determineType(config.getName());
            String fileName = modId + "-" + config.getName() + ".toml";
            NeoForgeConfigRegistry.INSTANCE.register(modId, type, config.specification, fileName);
        }

        // Auto lifecycle listeners
        NeoForgeModConfigEvents.loading(modId).register(config -> {
            for (ConfigBase c : configs) {
                if (c != null && c.specification == config.getSpec()) {
                    c.onLoad();
                }
            }
        });
        NeoForgeModConfigEvents.reloading(modId).register(config -> {
            for (ConfigBase c : configs) {
                if (c != null && c.specification == config.getSpec()) {
                    c.onReload();
                }
            }
        });

        // Register GUI tabs
        BetterConfigScreen.register(modId, configs);
    }

    /**
     * Complete 1-line registration with custom tab name mappings.
     *
     * @param modId   your mod id
     * @param configs tab name -> ConfigBase map
     */
    public static void registerMod(String modId, Map<String, ConfigBase> configs) {
        for (Map.Entry<String, ConfigBase> entry : configs.entrySet()) {
            ConfigBase config = entry.getValue();
            if (config == null || config.specification == null) continue;

            ModConfig.Type type = determineType(entry.getKey() + "_" + config.getName());
            String fileName = modId + "-" + config.getName() + ".toml";
            NeoForgeConfigRegistry.INSTANCE.register(modId, type, config.specification, fileName);
        }

        NeoForgeModConfigEvents.loading(modId).register(config -> {
            for (ConfigBase c : configs.values()) {
                if (c != null && c.specification == config.getSpec()) {
                    c.onLoad();
                }
            }
        });
        NeoForgeModConfigEvents.reloading(modId).register(config -> {
            for (ConfigBase c : configs.values()) {
                if (c != null && c.specification == config.getSpec()) {
                    c.onReload();
                }
            }
        });

        BetterConfigScreen.register(modId, configs);
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

    public static void register(String modId, ConfigBase... configs) {
        registerMod(modId, configs);
    }

    public static void register(String modId, Map<String, ConfigBase> configs) {
        registerMod(modId, configs);
    }
}
