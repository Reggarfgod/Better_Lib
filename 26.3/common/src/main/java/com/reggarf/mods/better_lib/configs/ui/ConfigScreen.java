package com.reggarf.mods.better_lib.configs.ui;

import com.reggarf.mods.better_lib.configs.gui.BetterScreenBase;
import com.reggarf.mods.better_lib.configs.utility.LerpedFloat;
import java.util.Arrays;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.apache.commons.lang3.StringUtils;

public abstract class ConfigScreen extends BetterScreenBase {

	public static String modID = "better_lib";
	public static LerpedFloat cogSpin = LerpedFloat.linear().startWithValue(0);
	protected final Screen parent;

	public enum ConfigType {
		CLIENT,
		COMMON,
		SERVER
	}

	public ConfigScreen(Screen parent) {
		super();
		this.parent = parent;
	}

	@Override
	public void tick() {
		super.tick();
		cogSpin.tickChaser();
	}

	@Override
	protected void renderWindowBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		super.renderWindowBackground(graphics, mouseX, mouseY, partialTicks);
		graphics.fill(0, 0, this.width, this.height, 0xb0_282c34);
	}

	@Override
	protected void renderWindow(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {}

	@Override
	public boolean isPauseScreen() {
		return true;
	}

	public static String toHumanReadable(String key) {
		if (key == null) return "";
		String s = key.replaceAll("_", " ");
		s = Arrays.stream(StringUtils.splitByCharacterTypeCamelCase(s)).map(StringUtils::capitalize).collect(Collectors.joining(" "));
		s = StringUtils.normalizeSpace(s);
		return s;
	}
}
