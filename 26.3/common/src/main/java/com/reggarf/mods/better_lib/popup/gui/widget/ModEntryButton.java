package com.reggarf.mods.better_lib.popup.gui.widget;

import net.minecraft.client.renderer.RenderPipelines;
import com.reggarf.mods.better_lib.popup.api.PopupTheme;
import com.reggarf.mods.better_lib.popup.api.WelcomePopupPlugin;
import com.reggarf.mods.better_lib.popup.gui.ModIconHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ModEntryButton extends Button {

    private final WelcomePopupPlugin plugin;
    private boolean selected;
    private PopupTheme theme = PopupTheme.MODERN;

    public ModEntryButton(int x, int y, int width, int height, WelcomePopupPlugin plugin, boolean selected, OnPress onPress) {
        super(x, y, width, height, Component.literal(plugin.getModName()), onPress, DEFAULT_NARRATION);
        this.plugin = plugin;
        this.selected = selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setTheme(PopupTheme theme) {
        this.theme = theme != null ? theme : PopupTheme.MODERN;
    }

    public WelcomePopupPlugin getPlugin() {
        return plugin;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        boolean hovered = this.isHoveredOrFocused();
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();
        int accent = plugin.getAccentColor();

        int bg = selected ? blend(theme.getSidebarBackground(), accent, 0.25f)
                : (hovered ? theme.getButtonHoverBackground() : theme.getSidebarBackground());
        int border = selected ? accent
                : (hovered ? theme.getSidebarBorder() : theme.getSidebarBackground());

        graphics.fill(x, y, x + w, y + h, bg);
        graphics.fill(x, y, x + w, y + 1, border);
        graphics.fill(x, y + h - 1, x + w, y + h, border);
        graphics.fill(x, y + 1, x + 1, y + h, border);
        graphics.fill(x + w - 1, y, x + w, y + h, border);

        // Selected left accent indicator (subtle 2px)
        if (selected) {
            graphics.fill(x + 1, y + 1, x + 3, y + h - 1, accent);
        }

        // Mod badge icon (avatar with first letter or custom icon)
        Identifier icon = ModIconHelper.getIcon(plugin);
        int iconSize = 12;
        int iconX = x + 5;
        int iconY = y + (h - iconSize) / 2;

        if (icon != null) {
            graphics.fill(iconX - 1, iconY - 1, iconX + iconSize + 1, iconY + iconSize + 1, 0xFF1A1C1E);
            graphics.outline(iconX - 1, iconY - 1, iconSize + 2, iconSize + 2, accent);
            graphics.blit(RenderPipelines.GUI_TEXTURED, icon, iconX, iconY, 0.0f, 0.0f, iconSize, iconSize, iconSize, iconSize);
        } else {
            // Stylized letter avatar
            graphics.fill(iconX, iconY, iconX + iconSize, iconY + iconSize, theme.getSidebarBackground());
            graphics.outline(iconX, iconY, iconSize, iconSize, accent);
            String initial = plugin.getModName().isEmpty() ? "?" : plugin.getModName().substring(0, 1).toUpperCase();
            Font font = Minecraft.getInstance().font;
            int letterW = font.width(initial);
            graphics.text(font, initial, iconX + (iconSize - letterW) / 2, iconY + (iconSize - 7) / 2, accent);
        }

        // Mod Name text (truncated if too long)
        Font font = Minecraft.getInstance().font;
        int textX = iconX + iconSize + 4;
        int maxTextW = w - (textX - x) - 4;
        String modName = plugin.getModName();
        String trimmed = font.plainSubstrByWidth(modName, maxTextW);
        if (trimmed.length() < modName.length() && trimmed.length() > 3) {
            trimmed = font.plainSubstrByWidth(modName, maxTextW - 5) + "..";
        }

        int textColor = selected ? theme.getTextPrimary() : (hovered ? 0xFFFFFFFF : theme.getTextSecondary());
        graphics.text(font, trimmed, textX, y + (h - 8) / 2, textColor);
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
