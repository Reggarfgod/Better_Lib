package com.reggarf.mods.better_lib.configs.gui;

import com.reggarf.mods.better_lib.configs.gui.widget.BetterWidgetBase;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;

import java.util.Collection;
import java.util.List;

public abstract class BetterScreenBase extends Screen {

	protected BetterScreenBase() { super(Component.empty()); }

	protected BetterScreenBase(Component title) {
		super(title);
	}

	@Override
	protected void extractBlurredBackground(GuiGraphicsExtractor graphics) {
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		Matrix3x2fStack ms = graphics.pose();
		ms.pushMatrix();

		prepareFrame();

		renderWindowBackground(graphics, mouseX, mouseY, partialTicks);
		renderWindow(graphics, mouseX, mouseY, partialTicks);
		super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
		renderWindowForeground(graphics, mouseX, mouseY, partialTicks);

		endFrame();
		ms.popMatrix();
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		boolean keyPressed = super.keyPressed(event);
		if (keyPressed || getFocused() instanceof EditBox)
			return keyPressed;

		if (this.minecraft != null && this.minecraft.options.keyInventory.matches(event)) {
			this.onClose();
			return true;
		}

		return false;
	}

	protected void prepareFrame() {}

	protected void renderWindowBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		extractBackground(graphics, mouseX, mouseY, partialTicks);
	}

	protected abstract void renderWindow(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks);

	protected void renderWindowForeground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		for (GuiEventListener widget : children()) {
			if (widget instanceof BetterWidgetBase betterWidget && betterWidget.isMouseOver(mouseX, mouseY)
				&& betterWidget.visible) {
				List<Component> tooltip = betterWidget.getToolTip();
				if (tooltip.isEmpty())
					continue;
				int ttx = betterWidget.lockedTooltipX == -1 ? mouseX : betterWidget.lockedTooltipX + betterWidget.getX();
				int tty = betterWidget.lockedTooltipY == -1 ? mouseY : betterWidget.lockedTooltipY + betterWidget.getY();
				graphics.setComponentTooltipForNextFrame(font, tooltip, ttx, tty);
			}
		}
	}

	protected void endFrame() {}

	@Override
	public GuiEventListener getFocused() {
		GuiEventListener focused = super.getFocused();
		if (focused instanceof AbstractWidget && !((AbstractWidget) focused).isFocused())
			setFocused(null);
		return super.getFocused();
	}

	@Override
	public void setFocused(GuiEventListener focused) {
		if (getFocused() instanceof AbstractWidget)
			((AbstractWidget) getFocused()).setFocused(false);
		super.setFocused(focused);
		if (focused instanceof AbstractWidget)
			((AbstractWidget) focused).setFocused(true);
	}

	protected <W extends GuiEventListener & Renderable & NarratableEntry> void addRenderableWidgets(Collection<W> widgets) {
		for (W w : widgets) {
			addRenderableWidget(w);
		}
	}

	protected <W extends GuiEventListener & Renderable & NarratableEntry> void addRenderableWidgets(W... widgets) {
		for (W w : widgets) {
			addRenderableWidget(w);
		}
	}

	public ScreenRectangle getArea() {
		return new ScreenRectangle(0, 0, width, height);
	}
}
