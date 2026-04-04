package com.reggarf.mods.better_lib.util.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class BaseRow {

    protected final String name;
    protected int lx, ly, w, h;

    public BaseRow(String name) {
        this.name = name;
    }

    /* 🔹 REQUIRED FOR SEARCH / FILTER */
    public String getName() {
        return name;
    }

    /** Draw row contents */
    public abstract void drawContents(
            GuiGraphicsExtractor gfx, int x, int y, int w, int h, int mx, int my
    );

    /** Mouse click */
    public abstract boolean click(double mx, double my);

    /* ================= INPUT FORWARDING ================= */

    public boolean mouseReleased(double mx, double my, int btn) {
        return false;
    }

    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        return false;
    }

    /* ==================================================== */

    public void render(GuiGraphicsExtractor gfx, int x, int y, int w, int h, int mx, int my) {

        lx = x;
        ly = y;
        this.w = w;
        this.h = h;

        boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + h;

        int bg1 = hover ? 0xFF444444 : 0x00000000;
        int bg2 = hover ? 0xFF303030 : 0xFF1A1A1A;

        // Background gradient
        gfx.fillGradient(x + 4, y + 4, x + w - 4, y + h - 7, bg1, bg2);

        // Top highlight line
        if (hover) {
            gfx.fill(x + 4, y + 4, x + w - 4, y + 5, 0xFFFFAA00);
        }

        drawContents(gfx, x, y, w, h, mx, my);
    }

    protected void drawText(GuiGraphicsExtractor gfx, String txt, int x, int y, int color) {
        gfx.text(Minecraft.getInstance().font, txt, x, y, color);
    }
}