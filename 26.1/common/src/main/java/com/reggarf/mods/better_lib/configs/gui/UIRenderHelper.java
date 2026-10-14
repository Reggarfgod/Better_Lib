package com.reggarf.mods.better_lib.configs.gui;

import com.reggarf.mods.better_lib.configs.utility.Color;
import com.reggarf.mods.better_lib.configs.utility.Couple;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class UIRenderHelper {

	public static void angledGradient(GuiGraphicsExtractor graphics, float angle, float x, float y, float width, float height, Couple<Color> c) {
		angledGradient(graphics, angle, x, y, width, height, c.getFirst(), c.getSecond());
	}

	public static void angledGradient(GuiGraphicsExtractor graphics, float angle, float x, float y, float width, float height, Color c1, Color c2) {
		int argb1 = c1.getRGB();
		int argb2 = c2.getRGB();
		graphics.fillGradient((int) x, (int) y, (int) (x + width), (int) (y + height), argb1, argb2);
	}

	public static void streak(GuiGraphicsExtractor graphics, float angle, int x, int y, int height, int width, Color color) {
		streak(graphics, angle, x, y, height, width, color, Color.TRANSPARENT_BLACK);
	}

	public static void streak(GuiGraphicsExtractor graphics, float angle, int x, int y, int height, int width, int color) {
		streak(graphics, angle, x, y, height, width, new Color(color, true), Color.TRANSPARENT_BLACK);
	}

	public static void streak(GuiGraphicsExtractor graphics, float angle, int x, int y, int height, int width, Color c1, Color c2) {
		angledGradient(graphics, angle, x, y - height / 2f, width, height, c1, c2);
	}

	public static void breadcrumbArrow(GuiGraphicsExtractor graphics, int x, int y, int z, int indent, int width, int height, Color c1, Color c2) {
		angledGradient(graphics, 0, x, y, width, height, c1, c2);
	}
}
