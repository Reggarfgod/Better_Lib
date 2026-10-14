package com.reggarf.mods.better_lib.util.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class IconButton {

    // Basic grey button
    public static void draw(GuiGraphics gfx, String text, int x, int y) {
        gfx.fillGradient(x, y, x + 18, y + 18, 0xFF444444, 0xFF222222);
        gfx.drawCenteredString(Minecraft.getInstance().font, text, x + 9, y + 5, 0xFFFFFF);
    }

    // Colored version (Accept / Deny buttons)
    public static void drawColored(GuiGraphics gfx, String text, int x, int y, int color) {

        // Darken bottom half
        int bottom = color - 0x22000000;

        gfx.fillGradient(x, y, x + 18, y + 18, color, bottom);

        gfx.drawCenteredString(
                Minecraft.getInstance().font,
                text,
                x + 9,
                y + 5,
                0xFFFFFF
        );
    }
}
