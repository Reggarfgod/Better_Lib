package com.reggarf.mods.better_lib.config;

import com.reggarf.mods.better_lib.gui.screen.BetterConfigScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 * <h1>BetterConfig API & Developer Guide</h1>
 * Central access point for mod developers to register, pull, and open configuration screens using BetterLib.
 *
 * <hr>
 * <h2>1. Creating Your Config Class</h2>
 * <pre>{@code
 * public class MyModConfig {
 *     // Build config instances with 1 method:
 *     public static final Client CLIENT = ConfigBase.build(Client::new);
 *     public static final Common COMMON = ConfigBase.build(Common::new);
 *
 *     public static class Client extends ConfigBase {
 *         public final ConfigBool enableGlow = b(true, "enableGlow", "Enable UI glow effect");
 *         public final ConfigFloat scale = f(1.0f, 0.5f, 2.0f, "scale", "UI scale multiplier");
 *
 *         @Override
 *         public String getName() { return "client"; }
 *     }
 *
 *     public static class Common extends ConfigBase {
 *         public final ConfigInt maxCount = i(100, 0, 1000, "maxCount", "Maximum item count");
 *
 *         @Override
 *         public String getName() { return "common"; }
 *     }
 * }
 * }</pre>
 *
 * <hr>
 * <h2>2. Registering in NeoForge (1 Single Line!)</h2>
 * <pre>{@code
 * @Mod("mymod")
 * public class MyModNeoForge {
 *     public MyModNeoForge(IEventBus modBus, ModContainer modContainer) {
 *         // Registers TOML files, event listeners, UI tabs, and Mods menu "Config" button:
 *         BetterConfigScreenHandler.registerMod(modContainer, modBus, "mymod",
 *                 MyModConfig.CLIENT,
 *                 MyModConfig.COMMON
 *         );
 *     }
 * }
 * }</pre>
 *
 * <hr>
 * <h2>3. Pulling & Opening the Config Screen (In-Game / Commands / Keybinds)</h2>
 * <pre>{@code
 * // Open directly in-game:
 * BetterConfigApi.openScreen(parentScreen, "mymod");
 *
 * // Or pull the Screen instance:
 * Screen screen = BetterConfigApi.createScreen(parentScreen, "mymod");
 *
 * // Or build custom multi-tab screens fluently:
 * BetterConfigApi.builder(parentScreen)
 *         .title("My Mod Settings")
 *         .addTab("Client Settings", MyModConfig.CLIENT)
 *         .addTab("Gameplay", MyModConfig.COMMON)
 *         .open();
 * }</pre>
 *
 * <hr>
 * <h2>4. Fabric / ModMenu Integration</h2>
 * <pre>{@code
 * public class MyModMenuIntegration implements ModMenuApi {
 *     @Override
 *     public ConfigScreenFactory<?> getModConfigScreenFactory() {
 *         return parent -> BetterConfigApi.createScreen(parent, "mymod");
 *     }
 * }
 * }</pre>
 */
public final class BetterConfigApi {

    private BetterConfigApi() {}

    /**
     * Register configs under a specific mod ID into the BetterLib registry.
     *
     * @param modId   the mod ID
     * @param configs array of ConfigBase instances
     */
    public static void register(String modId, ConfigBase... configs) {
        BetterConfigScreen.register(modId, configs);
    }

    /**
     * Register a single named tab config for a mod.
     *
     * @param modId   the mod ID
     * @param tabName label to show in the tab switcher
     * @param config  the ConfigBase instance
     */
    public static void register(String modId, String tabName, ConfigBase config) {
        BetterConfigScreen.register(modId, tabName, config);
    }

    /**
     * Register multiple named tabs for a mod.
     *
     * @param modId   the mod ID
     * @param configs map of tab name -> ConfigBase
     */
    public static void register(String modId, Map<String, ConfigBase> configs) {
        BetterConfigScreen.register(modId, configs);
    }

    /**
     * Check if a mod ID has configs registered.
     *
     * @param modId the mod ID
     * @return true if configs are registered
     */
    public static boolean hasConfigs(String modId) {
        return BetterConfigScreen.hasConfigs(modId);
    }

    /**
     * Get all registered configs for a mod ID.
     *
     * @param modId the mod ID
     * @return map of tab name -> ConfigBase
     */
    public static Map<String, ConfigBase> getConfigs(String modId) {
        return BetterConfigScreen.getConfigs(modId);
    }

    /**
     * Pull and create a {@link BetterConfigScreen} instance for a registered mod ID.
     *
     * @param parent the parent screen
     * @param modId  the mod ID
     * @return the created config screen
     */
    public static BetterConfigScreen createScreen(Screen parent, String modId) {
        return BetterConfigScreen.create(parent, modId);
    }

    /**
     * Pull and create a {@link BetterConfigScreen} instance for specific config objects.
     *
     * @param parent  the parent screen
     * @param configs config instances to include
     * @return the created config screen
     */
    public static BetterConfigScreen createScreen(Screen parent, ConfigBase... configs) {
        return BetterConfigScreen.create(parent, configs);
    }

    /**
     * Open the config screen directly for a registered mod ID.
     *
     * @param parent the parent screen
     * @param modId  the mod ID
     */
    public static void openScreen(Screen parent, String modId) {
        BetterConfigScreen.open(parent, modId);
    }

    /**
     * Open the config screen directly for specific config objects.
     *
     * @param parent  the parent screen
     * @param configs config instances to include
     */
    public static void openScreen(Screen parent, ConfigBase... configs) {
        BetterConfigScreen.open(parent, configs);
    }

    /**
     * Create a fluent builder to configure and open a custom {@link BetterConfigScreen}.
     *
     * @param parent the parent screen
     * @return a new Builder instance
     */
    public static BetterConfigScreen.Builder builder(Screen parent) {
        return BetterConfigScreen.builder(parent);
    }
}
