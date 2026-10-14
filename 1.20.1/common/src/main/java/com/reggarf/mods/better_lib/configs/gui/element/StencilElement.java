package com.reggarf.mods.better_lib.configs.gui.element;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;

public abstract class StencilElement extends RenderElement {

	@Override
	public void render(GuiGraphics graphics) {
		PoseStack ms = graphics.pose();
		ms.pushPose();
		transform(ms);
		renderStencil(graphics);
		ms.popPose();
	}

	protected abstract void renderStencil(GuiGraphics graphics);

	protected abstract void renderElement(GuiGraphics graphics);

	protected void transform(PoseStack ms) {
		ms.translate(x, y, z);
	}
}
