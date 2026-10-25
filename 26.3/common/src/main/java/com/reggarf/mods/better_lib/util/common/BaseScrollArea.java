package com.reggarf.mods.better_lib.util.common;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseScrollArea<R extends BaseRow> extends AbstractWidget {

    protected final List<R> rows = new ArrayList<>();
    protected int scroll = 0;
    protected final int rowHeight;

    public BaseScrollArea(int x, int y, int w, int h, int rowHeight) {
        super(x, y, w, h, Component.empty());
        this.rowHeight = rowHeight;
    }

    public void clear() {
        rows.clear();
        scroll = 0;
    }

    public void addRow(R row) {
        rows.add(row);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput n) {}

    /* ================= SCROLL ================= */

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        int total = rows.size() * rowHeight;
        int max = Math.max(0, total - height);
        scroll = Mth.clamp(scroll - (int)(dy * 20), 0, max);
        return true;
    }

    /* ================= INPUT ================= */

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x(); double my = event.y(); int btn = event.button();
        if (btn != 0) return false;

        int y = getY() - scroll;

        for (R row : rows) {
            if (my >= y && my <= y + rowHeight &&
                mx >= getX() && mx <= getX() + width) {

                if (row.click(mx, my))
                    return true;
            }
            y += rowHeight;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        double mx = event.x(); double my = event.y(); int btn = event.button();
        for (R row : rows)
            if (row.mouseReleased(mx, my, btn))
                return true;
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        double mx = event.x(); double my = event.y(); int btn = event.button();
        for (R row : rows)
            if (row.mouseDragged(mx, my, btn, dx, dy))
                return true;
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key(); int scanCode = event.keycode(); int modifiers = event.modifiers();
        for (R row : rows)
            if (row.keyPressed(keyCode, scanCode, modifiers))
                return true;
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        char codePoint = (char) event.codepoint();
        for (R row : rows)
            if (row.charTyped(codePoint, 0))
                return true;
        return false;
    }

    /* ================= RENDER ================= */

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor gfx, int mx, int my, float pt) {

        int y = getY() - scroll;
        int top = getY();
        int bottom = getY() + height;

        for (R row : rows) {
            if (y + rowHeight > top && y < bottom)
                row.render(gfx, getX(), y, width, rowHeight, mx, my);
            y += rowHeight;
        }

        drawScrollbar(gfx);
    }

    private void drawScrollbar(GuiGraphicsExtractor gfx) {

        int total = rows.size() * rowHeight;
        if (total <= height) return;

        int barX = getX() + width - 8;

        gfx.fill(barX, getY(), barX + 6, getY() + height, 0x44000000);

        float p = scroll / (float)(total - height);
        int thumb = Math.max(20, height * height / total);
        int ty = getY() + (int)((height - thumb) * p);

        gfx.fill(barX, ty, barX + 6, ty + thumb, 0xFFFFAA00);
    }
}
