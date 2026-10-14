package com.reggarf.mods.better_lib.configs.gui.element;

import org.joml.Matrix3x2fStack;
import net.minecraft.client.gui.GuiGraphics;

public abstract class StencilElement extends RenderElement {

	@Override
	public void render(GuiGraphics graphics) {
		Matrix3x2fStack ms = graphics.pose();
		ms.pushMatrix();
		transform(ms);
		renderStencil(graphics);
		ms.popMatrix();
	}

	protected abstract void renderStencil(GuiGraphics graphics);

	protected abstract void renderElement(GuiGraphics graphics);

	protected void transform(Matrix3x2fStack ms) {
		ms.translate((float) x, (float) y);
	}
}
