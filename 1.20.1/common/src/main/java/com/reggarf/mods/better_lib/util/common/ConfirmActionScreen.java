package com.reggarf.mods.better_lib.util.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfirmActionScreen extends Screen {

    private final Screen parent;
    private final String targetName;
    private final String action; // "addfriend" or "tpa"

    public ConfirmActionScreen(Screen parent, String targetName, String action) {
        super(Component.literal("Confirm"));
        this.parent = parent;
        this.targetName = targetName;
        this.action = action;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        super.render(gfx, mouseX, mouseY, delta);

        gfx.fillGradient(0, 0, width, height, 0xAA000000, 0xAA000000);

        int boxW = 250;
        int boxH = 120;
        int x = (width - boxW) / 2;
        int y = (height - boxH) / 2;

        gfx.fill(x - 2, y - 2, x + boxW + 2, y + boxH + 2, 0xFFFFFFFF);
        gfx.fill(x, y, x + boxW, y + boxH, 0xDD000000);

        gfx.drawCenteredString(font, "Are you sure?", width / 2, y + 15, 0xFFFFFF);
        gfx.drawCenteredString(font, "Do you want to " + action + " " + targetName + " ?", width / 2, y + 40, 0xCCCCCC);

        gfx.fill(x + 30, y + 70, x + 110, y + 100, 0xAA44FF44);
        gfx.drawCenteredString(font, "YES", x + 70, y + 82, 0xFFFFFF);

        gfx.fill(x + 140, y + 70, x + 220, y + 100, 0xAAFF4444);
        gfx.drawCenteredString(font, "NO", x + 180, y + 82, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        int boxW = 250;
        int boxH = 120;
        int x = (width - boxW) / 2;
        int y = (height - boxH) / 2;

        if (mouseX >= x + 30 && mouseX <= x + 110 &&
                mouseY >= y + 70 && mouseY <= y + 100) {

            Minecraft.getInstance().player.connection.sendCommand(action + " " + targetName);
            this.minecraft.setScreen(parent);
            return true;
        }

        if (mouseX >= x + 140 && mouseX <= x + 220 &&
                mouseY >= y + 70 && mouseY <= y + 100) {

            this.minecraft.setScreen(parent);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
