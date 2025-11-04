package com.reggarf.mods.better_lib;

import com.reggarf.mods.better_lib.config.annotation.Config;
import com.reggarf.mods.better_lib.config.annotation.ConfigEntry.*;
import com.reggarf.mods.better_lib.config.api.ConfigData;

import java.util.List;

@Config(name = "better_lib", background = "minecraft:textures/block/diamond_block.png")
public class DemoConfig implements ConfigData {

    // =========================
    // GENERAL SETTINGS
    // =========================
    @Category("General Settings")
    @Description("Enable or disable experimental features.")
    public boolean enableFeatureX = true;

    @Category("General Settings")
    @Description("Set your in-game player name for identification.")
    public String playerName = "Reggarf";

    @Category("General Settings")
    @Description("Show developer debug logs in the console.")
    public boolean debugMode = false;

    @Category("General Settings")
    @Description("Auto-save configuration changes on exit.")
    public boolean autoSave = true;

    @Category("General Settings")
    @Description("How often (in minutes) to check for updates.")
    @BoundedDiscrete(min = 1, max = 120)
    public int updateCheckInterval = 15;

    @Category("General Settings")
    @Description("Choose your preferred language.")
    @Dropdown(values = {"English", "Spanish", "German", "Hindi", "Japanese"})
    public String language = "English";

    @Category("General Settings")
    @Description("Whether the config screen should use animations.")
    public boolean enableAnimations = true;

    @Category("General Settings")
    @Description("Whether to sync this config across multiplayer servers.")
    public boolean syncAcrossServers = false;


    // =========================
    // PERFORMANCE
    // =========================
    @Category("Performance")
    @Description("Adjust the render distance to balance performance and visuals.")
    @BoundedDiscrete(min = 2, max = 32)
    public int renderDistance = 12;

    @Category("Performance")
    @Description("Enable async loading for world chunks.")
    public boolean asyncChunkLoading = true;

    @Category("Performance")
    @Description("Enable GPU-based rendering optimizations.")
    public boolean gpuOptimizations = true;

    @Category("Performance")
    @Description("Limit FPS to reduce GPU usage.")
    @BoundedDiscrete(min = 30, max = 240)
    public int fpsLimit = 120;

    @Category("Performance")
    @Description("Enable automatic memory cleanup.")
    public boolean memoryCleanup = true;

    @Category("Performance")
    @Description("Set the max entity render count.")
    @BoundedDiscrete(min = 10, max = 500)
    public int maxEntityRender = 200;

    @Category("Performance")
    @Description("Enable smooth lighting.")
    public boolean smoothLighting = true;

    @Category("Performance")
    @Description("Enable dynamic shadows (may affect FPS).")
    public boolean dynamicShadows = false;


    // =========================
    // VISUALS / COLORS
    // =========================
    @Category("Colors")
    @ColorField(alpha = true)
    @Description("UI color theme with optional transparency support.")
    public int uiColor = 0x80FF0000;

    @Category("Colors")
    @ColorField
    @Description("Accent color used for buttons.")
    public int accentColor = 0xFF00FF00;

    @Category("Colors")
    @ColorField
    @Description("Background overlay color for GUIs.")
    public int backgroundOverlay = 0x80000000;

    @Category("Colors")
    @ColorField
    @Description("Tooltip background color.")
    public int tooltipColor = 0xCC222222;

    @Category("Colors")
    @Description("Enable gradient effects in menus.")
    public boolean enableGradient = true;

    @Category("Colors")
    @Description("Select theme style.")
    @Dropdown(values = {"Default", "Dark", "Light", "Classic", "Cyber"})
    public String theme = "Default";


    // =========================
    // GAMEPLAY
    // =========================
    @Category("Gameplay")
    @Dropdown(values = {"EASY", "NORMAL", "HARD"})
    @Description("Select the overall game difficulty level.")
    public String difficulty = "NORMAL";

    @Category("Gameplay")
    @Description("Enable hardcore mode (no respawns).")
    public boolean hardcoreMode = false;

    @Category("Gameplay")
    @Description("Enable keep inventory on death.")
    public boolean keepInventory = true;

    @Category("Gameplay")
    @Description("Maximum number of tamed pets.")
    @BoundedDiscrete(min = 1, max = 50)
    public int maxPets = 5;

//    @Category("Gameplay")
//    @Description("Player base movement speed multiplier.")
//    @BoundedFloat(min = 0.1f, max = 3.0f)
//    public float movementSpeedMultiplier = 1.0f;

    @Category("Gameplay")
    @Description("Enable weather effects (rain, thunder, snow).")
    public boolean weatherEffects = true;

    @Category("Gameplay")
    @Description("Respawn delay in seconds after death.")
    @BoundedDiscrete(min = 1, max = 60)
    public int respawnDelay = 5;

    @Category("Gameplay")
    @Description("Enable random events in survival mode.")
    public boolean randomEvents = false;


    // =========================
    // AUDIO
    // =========================
    @Category("Audio")
    @BoundedDiscrete(min = 0, max = 100)
    @Description("Master sound volume.")
    public int masterVolume = 80;

    @Category("Audio")
    @BoundedDiscrete(min = 0, max = 100)
    @Description("Music volume.")
    public int musicVolume = 50;

    @Category("Audio")
    @BoundedDiscrete(min = 0, max = 100)
    @Description("Block interaction sounds.")
    public int blockSoundVolume = 70;

    @Category("Audio")
    @Description("Enable ambient sounds.")
    public boolean ambientSounds = true;

    @Category("Audio")
    @Description("Enable sound when receiving chat messages.")
    public boolean chatPingSound = false;

    @Category("Audio")
    @Description("Voice chat quality (requires restart).")
    @Dropdown(values = {"Low", "Medium", "High", "Ultra"})
    public String voiceChatQuality = "High";


    // =========================
    // HUD / INTERFACE
    // =========================
    @Category("HUD")
    @Description("Enable custom HUD overlay.")
    public boolean customHUD = true;

    @Category("HUD")
    @Description("Show coordinates on screen.")
    public boolean showCoordinates = true;

    @Category("HUD")
    @Description("Display FPS counter on screen.")
    public boolean showFPS = true;

    @Category("HUD")
    @Description("Show armor durability as a percentage.")
    public boolean showArmorDurability = true;

    @Category("HUD")
    @Description("Enable compass direction indicator.")
    public boolean compassEnabled = false;

//    @Category("HUD")
//    @Description("Adjust HUD scale.")
//    @BoundedFloat(min = 0.5f, max = 2.0f)
//    public float hudScale = 1.0f;


    // =========================
    // CHAT / SOCIAL
    // =========================
    @Category("Chat")
    @Description("Enable chat formatting.")
    public boolean formattedChat = true;

    @Category("Chat")
    @Description("Highlight mentions with color.")
    @ColorField
    public int mentionHighlightColor = 0xFFFFAA00;

    @Category("Chat")
    @Description("Mute system messages.")
    public boolean muteSystemMessages = false;

    @Category("Chat")
    @Description("Auto-hide chat after inactivity (seconds).")
    @BoundedDiscrete(min = 5, max = 120)
    public int chatHideDelay = 30;

    @Category("Chat")
    @Description("Show timestamp in chat messages.")
    public boolean showChatTimestamp = true;


    // =========================
    // MISC
    // =========================
    @Category("Misc")
    @Description("Enable experimental particle effects.")
    public boolean fancyParticles = true;

    @Category("Misc")
    @Description("List of custom server IPs.")
    public List<String> favoriteServers = List.of("play.example.net", "mc.reggarf.com");

    @Category("Misc")
    @Description("Enable mod compatibility layer.")
    public boolean modCompatibility = true;

    @Category("Misc")
    @Description("Show random fun tips on the loading screen.")
    public boolean loadingTips = true;


    // =========================
    // CONFIG CALLBACKS
    // =========================
    @Override
    public void onLoad() {
        System.out.println("[ExampleConfig] Loaded config!");
    }

    @Override
    public void onSave() {
        System.out.println("[ExampleConfig] Saved config!");
    }
}
