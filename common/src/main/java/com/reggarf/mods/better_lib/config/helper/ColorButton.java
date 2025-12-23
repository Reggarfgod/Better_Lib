//package com.reggarf.mods.better_lib.config.helper;
//
//import net.minecraft.client.gui.GuiGraphics;
//import net.minecraft.client.gui.components.Button;
//import net.minecraft.network.chat.Component;
//
//public class ColorButton extends Button {
//
//    private int color;
//
//    public ColorButton(
//            int x, int y, int w, int h,
//            Component text,
//            int initialColor,
//            OnPress onPress
//    ) {
//        super(x, y, w, h, text, onPress, DEFAULT_NARRATION);
//        this.color = initialColor;
//    }
//
//    public void setColor(int color) {
//        this.color = color;
//    }
//
//    public int getColor() {
//        return color;
//    }
//
//    @Override
//    protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
//        // draw normal button background
//        super.renderWidget(gfx, mouseX, mouseY, partialTick);
//
//        // draw colored square indicator (right side)
//        int boxSize = 10;
//        int bx = getX() + getWidth() - boxSize - 6;
//        int by = getY() + (getHeight() - boxSize) / 2;
//
//        gfx.fill(bx, by, bx + boxSize, by + boxSize, color);
//    }
//}
