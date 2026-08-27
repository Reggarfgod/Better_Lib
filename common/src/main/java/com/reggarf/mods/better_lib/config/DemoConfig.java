package com.reggarf.mods.better_lib.config;

/**
 * Example demonstrating how to create Client, Common, and Server configurations with BetterLib.
 * <p>
 * Supported config value types:
 * <ul>
 *   <li>{@code b(default, name, comments...)} - Boolean toggle widget</li>
 *   <li>{@code i(default, min, max, name, comments...)} - Integer slider/input with bounds</li>
 *   <li>{@code f(default, min, max, name, comments...)} - Float/Double slider/input with bounds</li>
 *   <li>{@code s(default, name, comments...)} - Text input widget</li>
 *   <li>{@code e(defaultEnum, name, comments...)} - Enum cycle switcher</li>
 *   <li>{@code group(depth, name, comments...)} - Section / Category banner</li>
 * </ul>
 *
 * <h3>Quick Usage in Your Mod:</h3>
 * <pre>{@code
 * // In your common/client/server code:
 * boolean glow = DemoConfig.CLIENT.customHudGlow.get();
 * int maxParticles = DemoConfig.COMMON.maxParticles.get();
 * }</pre>
 */
public class DemoConfig {

    public static final Client CLIENT = ConfigBase.build(Client::new);
    public static final Common COMMON = ConfigBase.build(Common::new);
    public static final Server SERVER = ConfigBase.build(Server::new);

    /* ========================================================================= */
    /* 1. Client Config (Visuals, HUD, Graphics presets)                         */
    /* ========================================================================= */
    public static class Client extends ConfigBase {

        public enum GraphicsQuality {
            LOW,
            MEDIUM,
            HIGH,
            ULTRA
        }

        public final ConfigGroup visuals = group(0, "visuals", "Client visual & HUD styling");
        public final ConfigBool customHudGlow = b(true, "customHudGlow", "Enable glowing borders on custom HUD elements");
        public final ConfigInt primaryAccentColor = i(0x38BDF8, Integer.MIN_VALUE, Integer.MAX_VALUE, "primaryAccentColor", "Primary UI accent color in HEX format", ConfigAnnotations.IntDisplay.HEX.asComment());
        public final ConfigEnum<GraphicsQuality> graphicsQuality = e(GraphicsQuality.HIGH, "graphicsQuality", "Visual effects quality preset");
        public final ConfigFloat hudScale = f(1.0f, 0.5f, 2.0f, "hudScale", "Scale multiplier for custom HUD overlays");

        @Override
        public String getName() {
            return "demo_client";
        }
    }

    /* ========================================================================= */
    /* 2. Common Config (Shared gameplay mechanics, speed, messages)             */
    /* ========================================================================= */
    public static class Common extends ConfigBase {

        public enum DifficultyMode {
            EASY,
            NORMAL,
            HARD,
            EXPERT
        }

        public final ConfigGroup gameplay = group(0, "gameplay", "Common gameplay settings & mechanics");
        public final ConfigBool enableDemoFeatures = b(true, "enableDemoFeatures", "Enable experimental demo gameplay features");
        public final ConfigInt maxParticles = i(100, 0, 1000, "maxParticles", "Maximum allowed demo particles");
        public final ConfigFloat speedMultiplier = f(1.25f, 0.1f, 10.0f, "speedMultiplier", "Movement speed multiplier for demo effects");
        public final ConfigString welcomeMessage = s("Welcome to BetterLib Demo!", "welcomeMessage", "Message broadcast when world loads");
        public final ConfigEnum<DifficultyMode> difficultyMode = e(DifficultyMode.NORMAL, "difficultyMode", "Gameplay difficulty preset");

        @Override
        public String getName() {
            return "demo_common";
        }
    }

    /* ========================================================================= */
    /* 3. Server Config (Server rules, network sync, damage multipliers)         */
    /* ========================================================================= */
    public static class Server extends ConfigBase {

        public final ConfigGroup serverRules = group(0, "server_rules", "Server security, networking, and sync options");
        public final ConfigBool enableServerLogging = b(true, "enableServerLogging", "Log detailed demo events in server console");
        public final ConfigInt maxPlayerConnections = i(20, 1, 100, "maxPlayerConnections", "Maximum concurrent demo connections");
        public final ConfigInt syncIntervalTicks = i(20, 1, 200, "syncIntervalTicks", "Tick interval for periodic demo data synchronization");
        public final ConfigFloat damageMultiplier = f(1.0f, 0.0f, 5.0f, "damageMultiplier", "Global damage multiplier applied on server side");

        @Override
        public String getName() {
            return "demo_server";
        }
    }
}
