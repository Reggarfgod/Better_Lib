package com.reggarf.mods.better_lib.configs.demo;

import com.reggarf.mods.better_lib.configs.ConfigBase;

public class BetterLibConfigs {

	public static final Client CLIENT = new Client();
	public static final Common COMMON = new Common();
	public static final Server SERVER = new Server();

	public static class Client extends ConfigBase {
		public final ConfigBool enableAnimations = b(true, "enableAnimations", "Whether the config screen should use animations.");
		public final ConfigInt maxParticles = i(1000, 100, 10000, "maxParticles", "Maximum particle count.");
		public final ConfigFloat soundVolume = f(0.8f, 0.0f, 1.0f, "soundVolume", "Volume multiplier for custom sounds.");

		@Override
		public String getName() {
			return "client";
		}
	}

	public static class Common extends ConfigBase {
		public final ConfigBool debugMode = b(false, "debugMode", "Show developer debug logs in console.", "[@cui:RequiresRestart:CLIENT]");
		public final ConfigInt updateInterval = i(15, 1, 120, "updateInterval", "How often to check for updates.", "[in minutes]");
		public final ConfigFloat simulationSpeed = f(1.0f, 0.1f, 5.0f, "simulationSpeed", "Base simulation speed multiplier.");

		@Override
		public String getName() {
			return "common";
		}
	}

	public static class Server extends ConfigBase {
		public final ConfigBool allowCheats = b(false, "allowCheats", "Whether cheats and privileged commands are permitted.");
		public final ConfigInt maxPlayers = i(20, 1, 100, "maxPlayers", "Max allowed concurrent players.");

		@Override
		public String getName() {
			return "server";
		}
	}
}
