package com.reggarf.mods.better_lib.popup.gui.widget;

import com.reggarf.mods.better_lib.popup.api.PopupTheme;
import com.reggarf.mods.better_lib.popup.api.WelcomePopupButton.Style;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ModernButton extends Button {

    private final Style style;
    private int accentColor;
    private PopupTheme theme = PopupTheme.MODERN;

    public ModernButton(int x, int y, int width, int height, Component message, OnPress onPress, Style style, int accentColor) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.style = style;
        this.accentColor = accentColor;
    }

    public void setAccentColor(int accentColor) {
        this.accentColor = accentColor;
    }

    public void setTheme(PopupTheme theme) {
        this.theme = theme != null ? theme : PopupTheme.MODERN;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        boolean hovered = this.isHoveredOrFocused();
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        int bg;
        int border;
        int textColor;

        if (!this.active) {
            bg = 0xFF1C1E20;
            border = 0xFF2A2D2F;
            textColor = 0xFF555555;
        } else if (style == Style.PRIMARY) {
            if (hovered) {
                bg = blend(accentColor, 0xFFFFFFFF, 0.15f);
                border = blend(accentColor, 0xFFFFFFFF, 0.40f);
                textColor = 0xFFFFFFFF;
            } else {
                bg = accentColor;
                border = blend(accentColor, 0xFFFFFFFF, 0.20f);
                textColor = 0xFFFFFFFF;
            }
        } else if (style == Style.ACTION) {
            if (hovered) {
                bg = theme.getButtonHoverBackground();
                border = accentColor;
                textColor = accentColor;
            } else {
                bg = theme.getButtonInactiveBackground();
                border = theme.getCardBorder();
                textColor = theme.getTextPrimary();
            }
        } else { // SECONDARY
            if (hovered) {
                bg = theme.getButtonHoverBackground();
                border = accentColor;
                textColor = 0xFFFFFFFF;
            } else {
                bg = theme.getButtonInactiveBackground();
                border = theme.getCardBorder();
                textColor = theme.getTextSecondary();
            }
        }

        // Draw crisp card box
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.fill(x, y, x + w, y + 1, border);
        graphics.fill(x, y + h - 1, x + w, y + h, border);
        graphics.fill(x, y + 1, x + 1, y + h, border);
        graphics.fill(x + w - 1, y, x + w, y + h, border);

        // Highlight stripe on top
        if (this.active && !hovered) {
            int highlight = (style == Style.PRIMARY) ? 0x30FFFFFF : 0x14FFFFFF;
            graphics.fill(x + 1, y + 1, x + w - 1, y + 2, highlight);
        }

        int textY = y + (h - 8) / 2;
        graphics.centeredText(Minecraft.getInstance().font, getMessage(), x + w / 2, textY, textColor);
    }

    private static int blend(int colorA, int colorB, float ratio) {
        int a1 = (colorA >> 24) & 0xFF;
        int r1 = (colorA >> 16) & 0xFF;
        int g1 = (colorA >> 8) & 0xFF;
        int b1 = colorA & 0xFF;

        int a2 = (colorB >> 24) & 0xFF;
        int r2 = (colorB >> 16) & 0xFF;
        int g2 = (colorB >> 8) & 0xFF;
        int b2 = colorB & 0xFF;

        int a = (int) (a1 + (a2 - a1) * ratio);
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
