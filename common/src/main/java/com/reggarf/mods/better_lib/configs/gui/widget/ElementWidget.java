package com.reggarf.mods.better_lib.configs.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import com.reggarf.mods.better_lib.configs.gui.element.RenderElement;
import com.reggarf.mods.better_lib.configs.gui.element.ScreenElement;
import com.reggarf.mods.better_lib.configs.utility.LerpedFloat;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import net.minecraft.client.gui.GuiGraphics;

public class ElementWidget extends BetterWidgetBase {

	protected RenderElement element = RenderElement.EMPTY;

	protected boolean usesFade = false;
	protected int fadeModX;
	protected int fadeModY;
	protected LerpedFloat fade = LerpedFloat.linear().startWithValue(1);

	protected boolean rescaleElement = false;
	protected float rescaleSizeX;
	protected float rescaleSizeY;

	protected float paddingX = 0;
	protected float paddingY = 0;

	public ElementWidget(int x, int y) {
		super(x, y);
	}

	public ElementWidget(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	public <T extends ElementWidget> T showingElement(RenderElement element) {
		this.element = element;
		return (T) this;
	}

	public <T extends ElementWidget> T showing(ScreenElement renderable) {
		return this.showingElement(RenderElement.of(renderable));
	}

	public <T extends ElementWidget> T modifyElement(Consumer<RenderElement> consumer) {
		if (element != null)
			consumer.accept(element);
		return (T) this;
	}

	public <T extends ElementWidget> T mapElement(UnaryOperator<RenderElement> function) {
		if (element != null)
			element = function.apply(element);
		return (T) this;
	}

	public <T extends ElementWidget> T withPadding(float paddingX, float paddingY) {
		this.paddingX = paddingX;
		this.paddingY = paddingY;
		return (T) this;
	}

	public <T extends ElementWidget> T rescaleElement(float rescaleSizeX, float rescaleSizeY) {
		this.rescaleElement = true;
		this.rescaleSizeX = rescaleSizeX;
		this.rescaleSizeY = rescaleSizeY;
		return (T) this;
	}

	@Override
	public void tick() {
		super.tick();
		fade.tickChaser();
	}

	@Override
	protected void beforeRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.beforeRender(graphics, mouseX, mouseY, partialTicks);
		isHovered = isMouseOver(mouseX, mouseY);

		float fadeValue = fade.getValue(partialTicks);
		element.withAlpha(fadeValue);
		if (fadeValue < 1) {
			graphics.pose().translate((1 - fadeValue) * fadeModX, (1 - fadeValue) * fadeModY, 0);
		}
	}

	@Override
	public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		PoseStack ms = graphics.pose();
		ms.pushPose();
		ms.translate(getX() + paddingX, getY() + paddingY, z);
		float innerWidth = width - 2 * paddingX;
		float innerHeight = height - 2 * paddingY;
		float eX = element.getX(), eY = element.getY();
		if (rescaleElement) {
			float xScale = innerWidth / rescaleSizeX;
			float yScale = innerHeight / rescaleSizeY;
			ms.scale(xScale, yScale, 1);
			element.at(eX / xScale, eY / yScale);
			innerWidth /= xScale;
			innerHeight /= yScale;
		}
		element.withBounds((int) innerWidth, (int) innerHeight).render(graphics);
		ms.popPose();
		if (rescaleElement) {
			element.at(eX, eY);
		}
	}

	public RenderElement getRenderElement() {
		return element;
	}
}
