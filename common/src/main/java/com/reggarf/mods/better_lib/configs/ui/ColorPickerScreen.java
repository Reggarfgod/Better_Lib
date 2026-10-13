package com.reggarf.mods.better_lib.configs.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.reggarf.mods.better_lib.configs.ConfigBase;
import com.reggarf.mods.better_lib.configs.adapter.DynamicConfigCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

/**
 * Modern, high-quality Color Selector dialog.
 * Styled to perfectly match BetterLib config screen theme.
 * Features:
 * - Scenic background backdrop matching config theme
 * - 100% opaque card preventing any background element bleed
 * - Live color preview box
 * - Interactive Red, Green, Blue sliders with numeric readouts
 * - Hex code input (#RRGGBB)
 * - 16-color quick preset palette
 * - Reset and Done buttons
 */
public class ColorPickerScreen extends Screen {

    private final Screen parent;
    private final ConfigBase.ConfigInt targetValue;
    private final ConfigBase owner;
    private final String settingTitle;

    private int red;
    private int green;
    private int blue;

    private int activeSlider = -1; // 0=Red, 1=Green, 2=Blue
    private boolean hexFocused = false;
    private String hexText = null;

    private static final int[] PRESETS = {
        0xFFFFFF, 0xCBD5E1, 0x64748B, 0x0F172A,
        0xEF4444, 0xF97316, 0xF59E0B, 0x22C55E,
        0x10B981, 0x06B6D4, 0x0EA5E9, 0x38BDF8,
        0x6366F1, 0x8B5CF6, 0xD946EF, 0xEC4899
    };

    public ColorPickerScreen(Screen parent, ConfigBase.ConfigInt targetValue, ConfigBase owner, String settingTitle) {
        super(Component.literal("Select Color - " + settingTitle));
        this.parent = parent;
        this.targetValue = targetValue;
        this.owner = owner;
        this.settingTitle = settingTitle;

        int current = targetValue.get();
        this.red = (current >> 16) & 0xFF;
        this.green = (current >> 8) & 0xFF;
        this.blue = current & 0xFF;
    }

    private int getColorInt() {
        return (red << 16) | (green << 8) | blue;
    }

    private void setColor(int color) {
        this.red = (color >> 16) & 0xFF;
        this.green = (color >> 8) & 0xFF;
        this.blue = color & 0xFF;
        this.hexText = String.format(Locale.ROOT, "#%06X", getColorInt());
    }

    private void apply() {
        targetValue.set(getColorInt());
        if (owner instanceof DynamicConfigCategory dyn) {
            dyn.applyToTarget();
        }
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
        // Suppress Minecraft 1.21 full-screen blur shader pass to ensure all elements stay crisp
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderTransparentBackground(graphics);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 1. Full-Screen Sunset Landscape Backdrop (matching config theme)
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(BaseConfigScreen.DEFAULT_BG_TEXTURE, 0, 0, width, height, 0.0f, 0.0f, 1672, 941, 1672, 941);

        // Soft darkening overlay for high contrast and depth (matching config theme)
        graphics.fill(0, 0, width, height, 0x66_090D16);

        int panelW = 300;
        int panelH = 224;
        int panelX = (width - panelW) / 2;
        int panelY = (height - panelH) / 2;

        // Dialog card: 100% opaque solid slate background matching config cards
        BaseConfigScreen.fillRoundedRect(graphics, panelX, panelY, panelW, panelH, 6, 0xFF_0F172A, 0xFF_334155);

        // Header Icon Well (Cyan Dye / Color)
        BaseConfigScreen.fillRoundedRect(graphics, panelX + 10, panelY + 9, 20, 20, 4, 0xEE_1E293B, 0xFF_38BDF8);
        graphics.renderFakeItem(new ItemStack(Items.CYAN_DYE), panelX + 12, panelY + 11);

        // Header Title & Subtitle
        graphics.drawString(font, "COLOR SELECTOR", panelX + 34, panelY + 9, 0xFFFFFFFF, true);
        String sub = font.substrByWidth(Component.literal(settingTitle), panelW - 70).getString();
        graphics.drawString(font, sub, panelX + 34, panelY + 21, 0xFF38BDF8, true);

        // Close X button
        int closeX = panelX + panelW - 24;
        int closeY = panelY + 9;
        boolean closeHover = mouseX >= closeX && mouseX <= closeX + 16 && mouseY >= closeY && mouseY <= closeY + 16;
        BaseConfigScreen.fillRoundedRect(graphics, closeX, closeY, 16, 16, 4, closeHover ? 0xFF334155 : 0xFF1E293B, closeHover ? 0xFFFFFFFF : 0xFF475569);
        graphics.drawCenteredString(font, "X", closeX + 8, closeY + 4, closeHover ? 0xFFFFFFFF : 0xFFCBD5E1);

        // Divider line matching config theme
        graphics.fill(panelX + 8, panelY + 35, panelX + panelW - 8, panelY + 36, 0xFF334155);

        // Left Section: Color Preview Box (54x54) & Hex input
        int previewX = panelX + 16;
        int previewY = panelY + 46;
        int previewW = 54;
        int previewH = 54;
        int currentColor = 0xFF000000 | getColorInt();
        BaseConfigScreen.fillRoundedRect(graphics, previewX, previewY, previewW, previewH, 4, currentColor, 0xFFFFFFFF);

        // Hex Code input box below preview
        int hexX = previewX;
        int hexY = previewY + previewH + 8;
        int hexW = previewW;
        int hexH = 18;
        BaseConfigScreen.fillRoundedRect(graphics, hexX, hexY, hexW, hexH, 4, 0xFF0F172A, hexFocused ? 0xFF38BDF8 : 0xFF334155);
        String currentHex = (hexFocused && hexText != null) ? hexText + (System.currentTimeMillis() % 1000 > 500 ? "_" : "") : String.format(Locale.ROOT, "#%06X", getColorInt());
        graphics.drawCenteredString(font, currentHex, hexX + hexW / 2, hexY + 5, hexFocused ? 0xFF38BDF8 : 0xFFFFFFFF);

        // Right Section: 3 Sliders (Red, Green, Blue)
        int slidersX = panelX + 84;
        int slidersY = panelY + 46;
        int sliderW = 140;
        int sliderH = 12;

        renderChannelSlider(graphics, slidersX, slidersY, sliderW, sliderH, "R", red, 0xFFEF4444, mouseX, mouseY);
        renderChannelSlider(graphics, slidersX, slidersY + 24, sliderW, sliderH, "G", green, 0xFF22C55E, mouseX, mouseY);
        renderChannelSlider(graphics, slidersX, slidersY + 48, sliderW, sliderH, "B", blue, 0xFF38BDF8, mouseX, mouseY);

        // Preset Color Swatches (2 rows of 8)
        int presetStartX = panelX + 16;
        int presetStartY = panelY + 138;
        int swatchSize = 13;
        int swatchGap = 4;

        for (int i = 0; i < PRESETS.length; i++) {
            int row = i / 8;
            int col = i % 8;
            int sx = presetStartX + col * (swatchSize + swatchGap);
            int sy = presetStartY + row * (swatchSize + swatchGap);
            int pColor = 0xFF000000 | PRESETS[i];
            boolean sHover = mouseX >= sx && mouseX <= sx + swatchSize && mouseY >= sy && mouseY <= sy + swatchSize;
            BaseConfigScreen.fillRoundedRect(graphics, sx, sy, swatchSize, swatchSize, 3, pColor, sHover ? 0xFF38BDF8 : 0xFF334155);
        }

        // Action Buttons at bottom
        int doneW = 72;
        int doneH = 20;
        int doneX = panelX + panelW - doneW - 14;
        int doneY = panelY + panelH - doneH - 12;
        boolean doneHover = mouseX >= doneX && mouseX <= doneX + doneW && mouseY >= doneY && mouseY <= doneY + doneH;
        BaseConfigScreen.fillRoundedRect(graphics, doneX, doneY, doneW, doneH, 4, doneHover ? 0xFF0284C7 : 0xFF1E293B, doneHover ? 0xFF38BDF8 : 0xFF475569);
        graphics.drawCenteredString(font, "Done", doneX + doneW / 2, doneY + 6, doneHover ? 0xFFFFFFFF : 0xFFE2E8F0);

        int resetW = 60;
        int resetX = doneX - resetW - 8;
        boolean resetHover = mouseX >= resetX && mouseX <= resetX + resetW && mouseY >= doneY && mouseY <= doneY + doneH;
        BaseConfigScreen.fillRoundedRect(graphics, resetX, doneY, resetW, doneH, 4, resetHover ? 0xFF334155 : 0xFF1E293B, resetHover ? 0xFF38BDF8 : 0xFF475569);
        graphics.drawCenteredString(font, "Reset", resetX + resetW / 2, doneY + 6, resetHover ? 0xFF38BDF8 : 0xFFCBD5E1);
    }

    private void renderChannelSlider(GuiGraphics graphics, int x, int y, int width, int height, String label, int value, int color, int mouseX, int mouseY) {
        // Label (R, G, B)
        graphics.drawString(font, label, x, y + 2, color, true);

        int trackX = x + 14;
        int trackY = y;
        int trackW = width - 44;
        int trackH = height;

        float progress = (float) value / 255.0f;

        // Groove
        BaseConfigScreen.fillRoundedRect(graphics, trackX, trackY, trackW, trackH, 4, 0xFF0F172A, 0xFF334155);

        // Fill
        int thumbW = 8;
        int thumbX = trackX + (int) (progress * (trackW - thumbW));
        int fillW = thumbX - trackX + thumbW / 2;
        if (fillW > 2) {
            BaseConfigScreen.fillRoundedRect(graphics, trackX + 1, trackY + 1, fillW, trackH - 2, 3, color, color);
        }

        // Thumb
        BaseConfigScreen.fillRoundedRect(graphics, thumbX, trackY - 2, thumbW, trackH + 4, 3, 0xFFFFFFFF, color);

        // Numeric Badge
        int badgeX = trackX + trackW + 6;
        int badgeW = 28;
        BaseConfigScreen.fillRoundedRect(graphics, badgeX, trackY - 1, badgeW, trackH + 2, 3, 0xFF1E293B, 0xFF334155);
        graphics.drawCenteredString(font, String.valueOf(value), badgeX + badgeW / 2, trackY + 1, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int panelW = 300;
        int panelH = 224;
        int panelX = (width - panelW) / 2;
        int panelY = (height - panelH) / 2;

        // Close X
        int closeX = panelX + panelW - 24;
        int closeY = panelY + 9;
        if (mouseX >= closeX && mouseX <= closeX + 16 && mouseY >= closeY && mouseY <= closeY + 16) {
            apply();
            Minecraft.getInstance().setScreen(parent);
            return true;
        }

        // Done button
        int doneW = 72;
        int doneH = 20;
        int doneX = panelX + panelW - doneW - 14;
        int doneY = panelY + panelH - doneH - 12;
        if (mouseX >= doneX && mouseX <= doneX + doneW && mouseY >= doneY && mouseY <= doneY + doneH) {
            apply();
            Minecraft.getInstance().setScreen(parent);
            return true;
        }

        // Reset button
        int resetW = 60;
        int resetX = doneX - resetW - 8;
        if (mouseX >= resetX && mouseX <= resetX + resetW && mouseY >= doneY && mouseY <= doneY + doneH) {
            setColor(targetValue.getDefault());
            return true;
        }

        // Hex Box
        int previewX = panelX + 16;
        int previewY = panelY + 46;
        int previewW = 54;
        int hexX = previewX;
        int hexY = previewY + previewW + 8;
        int hexH = 18;
        if (mouseX >= hexX && mouseX <= hexX + previewW && mouseY >= hexY && mouseY <= hexY + hexH) {
            hexFocused = true;
            if (hexText == null) {
                hexText = String.format(Locale.ROOT, "#%06X", getColorInt());
            }
            return true;
        } else {
            if (hexFocused) {
                applyHexText();
                hexFocused = false;
            }
        }

        // Check 3 Sliders
        int slidersX = panelX + 84;
        int slidersY = panelY + 46;
        int sliderW = 140;
        int trackX = slidersX + 14;
        int trackW = sliderW - 44;

        for (int i = 0; i < 3; i++) {
            int sy = slidersY + i * 24;
            if (mouseX >= trackX - 2 && mouseX <= trackX + trackW + 2 && mouseY >= sy - 4 && mouseY <= sy + 16) {
                activeSlider = i;
                updateSliderFromMouse(i, mouseX, trackX, trackW);
                return true;
            }
        }

        // Check Presets
        int presetStartX = panelX + 16;
        int presetStartY = panelY + 138;
        int swatchSize = 13;
        int swatchGap = 4;
        for (int i = 0; i < PRESETS.length; i++) {
            int row = i / 8;
            int col = i % 8;
            int sx = presetStartX + col * (swatchSize + swatchGap);
            int sy = presetStartY + row * (swatchSize + swatchGap);
            if (mouseX >= sx && mouseX <= sx + swatchSize && mouseY >= sy && mouseY <= sy + swatchSize) {
                setColor(PRESETS[i]);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void updateSliderFromMouse(int slider, double mouseX, int trackX, int trackW) {
        int thumbW = 8;
        float progress = (float) (mouseX - trackX - thumbW / 2.0f) / (float) (trackW - thumbW);
        progress = Mth.clamp(progress, 0.0f, 1.0f);
        int val = Math.round(progress * 255.0f);
        if (slider == 0) red = val;
        else if (slider == 1) green = val;
        else if (slider == 2) blue = val;
        this.hexText = String.format(Locale.ROOT, "#%06X", getColorInt());
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (activeSlider >= 0) {
            int panelW = 300;
            int panelX = (width - panelW) / 2;
            int slidersX = panelX + 84;
            int sliderW = 140;
            int trackX = slidersX + 14;
            int trackW = sliderW - 44;
            updateSliderFromMouse(activeSlider, mouseX, trackX, trackW);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.activeSlider = -1;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void applyHexText() {
        if (hexText != null) {
            try {
                String clean = hexText.replace("#", "").trim();
                long parsed = Long.parseLong(clean, 16);
                setColor((int) (parsed & 0xFFFFFF));
            } catch (Exception ignored) {}
            hexText = null;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (hexFocused) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                applyHexText();
                hexFocused = false;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                hexText = null;
                hexFocused = false;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (hexText != null && hexText.length() > 1) {
                    hexText = hexText.substring(0, hexText.length() - 1);
                }
                return true;
            }
            if (Screen.isPaste(keyCode)) {
                String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
                if (clip != null) {
                    clip = clip.trim().replace("#", "");
                    if (clip.matches("^[0-9a-fA-F]{1,6}$")) {
                        hexText = "#" + clip.toUpperCase(Locale.ROOT);
                        applyHexText();
                        return true;
                    }
                }
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            apply();
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (hexFocused) {
            if (hexText == null) hexText = "#";
            if (hexText.length() < 7) {
                char c = Character.toUpperCase(codePoint);
                if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')) {
                    hexText += c;
                    return true;
                }
            }
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void onClose() {
        apply();
        Minecraft.getInstance().setScreen(parent);
    }
}
