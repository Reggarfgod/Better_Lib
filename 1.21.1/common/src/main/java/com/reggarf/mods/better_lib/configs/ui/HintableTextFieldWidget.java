package com.reggarf.mods.better_lib.configs.ui;

import com.reggarf.mods.better_lib.configs.gui.Theme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;
import org.lwjgl.glfw.GLFW;

public class HintableTextFieldWidget extends EditBox {

	protected Font font;
	protected String hint;

	public HintableTextFieldWidget(Font font, int x, int y, int width, int height) {
		super(font, x, y, width, height, CommonComponents.EMPTY);
		this.font = font;
	}

	public void setHint(String hint) {
		this.hint = hint;
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.renderWidget(graphics, mouseX, mouseY, partialTicks);

		if (hint == null || hint.isEmpty())
			return;

		if (!getValue().isEmpty())
			return;

		graphics.drawString(font, hint, getX() + 5, this.getY() + (this.height - 8) / 2, Theme.c(Theme.Key.TEXT).scaleAlpha(.75f).getRGB(), false);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if (!isMouseOver(x, y))
			return false;

		if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
			setValue("");
			return true;
		} else {
			return super.mouseClicked(x, y, button);
		}
	}
}
