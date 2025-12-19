package com.reggarf.mods.better_lib.util.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class GuiUtils {

    public static void drawDimBackground(GuiGraphics gfx, int w, int h) {
        gfx.fillGradient(0, 0, w, h, 0xAA000000, 0xDD000000);
    }

    public static void drawPanel(GuiGraphics gfx, int x1, int y1, int x2, int y2) {
        gfx.fillGradient(x1, y1, x2, y2, 0x99222222, 0xDD000000);
        //drawBorder(gfx, x1, y1, x2, y2);
    }

    public static void drawBorder(GuiGraphics gfx, int x1, int y1, int x2, int y2) {
        gfx.fill(x1 - 2, y1 - 2, x2 + 2, y1 + 2, 0x66FFFFFF);
        gfx.fill(x1 - 2, y2 - 2, x2 + 2, y2 + 2, 0x66FFFFFF);
        gfx.fill(x1 - 2, y1, x1 + 2, y2, 0x66FFFFFF);
        gfx.fill(x2 - 2, y1, x2 + 2, y2, 0x66FFFFFF);
    }

    public static void drawHeader(GuiGraphics gfx, int x1, int x2, int y, String title) {
        gfx.fill(x1, y, x2, y + 25, 0x99181818);

        int center = (x1 + x2) / 2;
        var font = Minecraft.getInstance().font;

        gfx.drawString(font, "< Back", x1 + 10, y + 9, 0xFFFFFF);
        gfx.drawString(font, "X", x2 - 20, y + 9, 0xFF4444);
        gfx.drawCenteredString(font, Component.literal(title), center, y + 9, 0xFFFFFF);
    }

    public static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    public static boolean clickBack(double mx, double my, int x1) {
        return inside(mx, my, x1 + 10, 35, 60, 30);
    }

    public static boolean clickClose(double mx, double my, int x2) {
        return inside(mx, my, x2 - 20, 35, 20, 30);
    }
}
