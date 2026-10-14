package com.reggarf.mods.better_lib.configs.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.reggarf.mods.better_lib.configs.gui.widget.BetterWidgetBase;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public abstract class BetterScreenBase extends Screen {

	protected int windowWidth, windowHeight;
	protected int windowXOffset, windowYOffset;
	protected int guiLeft, guiTop;

	protected BetterScreenBase(Component title) {
		super(title);
	}

	protected BetterScreenBase() {
		this(CommonComponents.EMPTY);
	}

	@Override
	public void renderBackground(GuiGraphics graphics) {
		super.renderBackground(graphics);
	}

	protected void setWindowSize(int width, int height) {
		windowWidth = width;
		windowHeight = height;
	}

	protected void setWindowOffset(int xOffset, int yOffset) {
		windowXOffset = xOffset;
		windowYOffset = yOffset;
	}

	@Override
	protected void init() {
		guiLeft = (width - windowWidth) / 2;
		guiTop = (height - windowHeight) / 2;
		guiLeft += windowXOffset;
		guiTop += windowYOffset;
	}

	@Override
	public void tick() {
		for (GuiEventListener listener : children()) {
			if (listener instanceof BetterWidgetBase tickable) {
				tickable.tick();
			}
		}
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		if (getFocused() != null && !getFocused().isMouseOver(pMouseX, pMouseY))
			setFocused(null);
		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	protected <W extends GuiEventListener & Renderable & NarratableEntry> void addRenderableWidgets(Collection<W> widgets) {
		for (W widget : widgets) {
			addRenderableWidget(widget);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		PoseStack ms = graphics.pose();
		ms.pushPose();

		prepareFrame();

		renderWindowBackground(graphics, mouseX, mouseY, partialTicks);
		renderWindow(graphics, mouseX, mouseY, partialTicks);
		super.render(graphics, mouseX, mouseY, partialTicks);
		renderWindowForeground(graphics, mouseX, mouseY, partialTicks);

		endFrame();
		ms.popPose();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		boolean keyPressed = super.keyPressed(keyCode, scanCode, modifiers);
		if (keyPressed || getFocused() instanceof EditBox)
			return keyPressed;

		if (this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
			this.onClose();
			return true;
		}

		return false;
	}

	protected void prepareFrame() {}

	protected void renderWindowBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(graphics);
	}

	protected abstract void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks);

	protected void renderWindowForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		for (GuiEventListener widget : children()) {
			if (widget instanceof BetterWidgetBase betterWidget && betterWidget.isMouseOver(mouseX, mouseY)
				&& betterWidget.visible) {
				List<Component> tooltip = betterWidget.getToolTip();
				if (tooltip.isEmpty())
					continue;
				int ttx = betterWidget.lockedTooltipX == -1 ? mouseX : betterWidget.lockedTooltipX + betterWidget.getX();
				int tty = betterWidget.lockedTooltipY == -1 ? mouseY : betterWidget.lockedTooltipY + betterWidget.getY();
				graphics.renderComponentTooltip(font, tooltip, ttx, tty);
			}
		}
	}

	protected void endFrame() {}

	@Override
	public GuiEventListener getFocused() {
		GuiEventListener focused = super.getFocused();
		if (focused instanceof AbstractWidget && !((AbstractWidget) focused).isFocused())
			focused = null;
		setFocused(focused);
		return focused;
	}
}
