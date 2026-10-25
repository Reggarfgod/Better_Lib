package com.reggarf.mods.better_lib.configs.demo;

import com.reggarf.mods.better_lib.configs.BetterLibConfigApi;
import com.reggarf.mods.better_lib.configs.ui.BaseConfigScreen;
import net.minecraft.client.gui.screens.Screen;

public class BetterLibConfigScreen {

	/**
	 * Default Better_Lib config screen.
	 */
	public static Screen ModScreen(Screen parent) {
		return ModScreen(parent, "better_lib");
	}

	/**
	 * Screen factory for any mod by its modId.
	 * Allows other mods to use Better_Lib config screens:
	 * {@code BetterLibConfigScreen.ModScreen(parent, "mymod")}
	 */
	public static Screen ModScreen(Screen parent, String modId) {
		return createScreen(parent, modId);
	}

	/**
	 * Convenience alias for {@link #ModScreen(Screen, String)}.
	 */
	public static Screen createScreen(Screen parent, String modId) {
		BaseConfigScreen screen = new BaseConfigScreen(parent, modId);
		BetterLibConfigApi.ModConfigEntry entry = BetterLibConfigApi.get(modId);
		if (entry != null) {
			if (entry.background != null) {
				screen.withBackground(entry.background);
			}
			screen.withConfigs(entry.client, entry.common, entry.server);
		}
		return screen;
	}
}
