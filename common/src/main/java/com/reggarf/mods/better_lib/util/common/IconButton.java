package com.reggarf.mods.better_lib.util.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class IconButton {

    // Basic grey button
    public static void draw(GuiGraphicsExtractor gfx, String text, int x, int y) {
        gfx.fillGradient(x, y, x + 18, y + 18, 0xFF444444, 0xFF222222);

        gfx.centeredText(
                Minecraft.getInstance().font,
                text,
                x + 9,
                y + 5,
                0xFFFFFF
        );
    }

    // Colored version (Accept / Deny buttons)
    public static void drawColored(GuiGraphicsExtractor gfx, String text, int x, int y, int color) {

        // Darken bottom half
        int bottom = color - 0x22000000;

        gfx.fillGradient(x, y, x + 18, y + 18, color, bottom);

        gfx.centeredText(
                Minecraft.getInstance().font,
                text,
                x + 9,
                y + 5,
                0xFFFFFF
        );
    }
}