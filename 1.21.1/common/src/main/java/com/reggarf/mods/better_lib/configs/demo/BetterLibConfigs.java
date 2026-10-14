package com.reggarf.mods.better_lib.configs.demo;

import com.reggarf.mods.better_lib.Better_lib;
import com.reggarf.mods.better_lib.configs.BetterLibConfigApi;
import com.reggarf.mods.better_lib.configs.ConfigBase;
import com.reggarf.mods.better_lib.configs.format.ConfigFormat;

/**
 * Example configuration for Better_Lib demonstrating Cloth Config-style registration.
 * Includes all setting types (Toggle, Int Slider, Float Slider, Text Input,
 * Dropdown Selector, Hex Color Picker, Enum Selector) to test every widget.
 */
public class BetterLibConfigs {

	public static final Client CLIENT = new Client();
	public static final Common COMMON = new Common();
	public static final Server SERVER = new Server();

	public static void init() {
		init(ConfigFormat.YAML);
	}

	public static void init(ConfigFormat format) {
		BetterLibConfigApi.register(Better_lib.MODID, format, CLIENT, COMMON, SERVER).loadAll();
	}

	public enum GraphicsQuality {
		LOW, MEDIUM, HIGH, ULTRA
	}

	public enum LogLevel {
		INFO, WARN, DEBUG, TRACE
	}

	public enum PvPMode {
		DISABLED, OPT_IN, ALWAYS_ON
	}

	public static class Client extends ConfigBase {
		// Boolean Toggle Switch
		public final ConfigBool enableAnimations = b(true, "enableAnimations", "Whether the config screen should use animations.");
		// Integer Slider
		public final ConfigInt maxParticles = i(1000, 100, 10000, "maxParticles", "Maximum particle count.");
		// Float Slider
		public final ConfigFloat soundVolume = f(0.8f, 0.0f, 1.0f, "soundVolume", "Volume multiplier for custom sounds.");
		// Text String Input
		public final ConfigString greetingMessage = s("Hello BetterLib!", "greetingMessage", "Custom greeting displayed in player HUD.");
		// Dropdown String Selector (via @cui:Dropdown metadata)
		public final ConfigString themeName = s("Dark", "themeName", "UI Color Theme.", "[@cui:Dropdown:Dark,Light,Midnight,Cyberpunk]");
		// Color Picker (via @cui:ColorField metadata)
		public final ConfigInt accentColor = i(0x38BDF8, "accentColor", "Accent color used for GUI highlights.", "[@cui:ColorField]");
		// Enum Selector
		public final ConfigEnum<GraphicsQuality> graphicsQuality = e(GraphicsQuality.HIGH, "graphicsQuality", "Overall graphics quality preset.");

		@Override
		public String getName() {
			return "client";
		}
	}

	public static class Common extends ConfigBase {
		// Boolean Toggle Switch
		public final ConfigBool debugMode = b(false, "debugMode", "Show developer debug logs in console.", "[@cui:RequiresRestart:CLIENT]");
		// Integer Slider
		public final ConfigInt updateInterval = i(15, 1, 120, "updateInterval", "How often to check for updates.", "[in minutes]");
		// Float Slider
		public final ConfigFloat simulationSpeed = f(1.0f, 0.1f, 5.0f, "simulationSpeed", "Base simulation speed multiplier.");
		// Text String Input
		public final ConfigString serverName = s("My Minecraft Server", "serverName", "Name of the server displayed in notifications.");
		// Enum Selector
		public final ConfigEnum<LogLevel> logLevel = e(LogLevel.INFO, "logLevel", "Console logging verbosity level.");

		@Override
		public String getName() {
			return "common";
		}
	}

	public static class Server extends ConfigBase {
		// Boolean Toggle Switch
		public final ConfigBool allowCheats = b(false, "allowCheats", "Whether cheats and privileged commands are permitted.");
		// Integer Slider
		public final ConfigInt maxPlayers = i(20, 1, 100, "maxPlayers", "Max allowed concurrent players.");
		// Text String Input
		public final ConfigString motd = s("A BetterLib powered world!", "motd", "Message of the day displayed to connecting players.");
		// Enum Selector
		public final ConfigEnum<PvPMode> pvpMode = e(PvPMode.OPT_IN, "pvpMode", "Server player vs player combat policy.");

		@Override
		public String getName() {
			return "server";
		}
	}
}
