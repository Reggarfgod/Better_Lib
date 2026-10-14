package com.reggarf.mods.better_lib.configs.gui.element;

import com.reggarf.mods.better_lib.configs.gui.Theme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TextStencilElement extends DelegatedStencilElement {

	protected Font font;
	protected MutableComponent component;
	protected boolean centerVertically = false;
	protected boolean centerHorizontally = false;
	protected int color;

	public TextStencilElement(Font font) {
		super();
		this.font = font;
		height = 10;
		this.color = Theme.i(Theme.Key.TEXT_ACCENT_STRONG);
	}

	public TextStencilElement(Font font, String text) {
		this(font);
		component = Component.literal(text);
	}

	public TextStencilElement(Font font, MutableComponent component) {
		this(font);
		this.component = component;
	}

	public TextStencilElement withColor(int color) {
		this.color = color;
		return this;
	}

	public TextStencilElement withText(String text) {
		component = Component.literal(text);
		return this;
	}

	public TextStencilElement withText(MutableComponent component) {
		this.component = component;
		return this;
	}

	public TextStencilElement centered(boolean vertical, boolean horizontal) {
		this.centerVertically = vertical;
		this.centerHorizontally = horizontal;
		return this;
	}

	@Override
	protected void renderStencil(GuiGraphics graphics) {
		if (component == null) return;
		float x = 0, y = 0;
		if (centerHorizontally)
			x = width / 2f - font.width(component) / 2f;

		if (centerVertically)
			y = height / 2f - (font.lineHeight - 1) / 2f;

		int alphaInt = Math.max(4, Math.min(255, (int) (alpha * 255)));
		int finalColor = (color & 0x00FFFFFF) | (alphaInt << 24);
		graphics.drawString(font, component, Math.round(x), Math.round(y), finalColor, false);
	}

	@Override
	protected void renderElement(GuiGraphics graphics) {
		// Render handled cleanly via renderStencil
	}

	public MutableComponent getComponent() {
		return component;
	}

	public String getText() {
		return component != null ? component.getString() : "";
	}
}
