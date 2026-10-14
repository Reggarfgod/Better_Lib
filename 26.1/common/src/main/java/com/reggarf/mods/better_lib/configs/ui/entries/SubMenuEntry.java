package com.reggarf.mods.better_lib.configs.ui.entries;

import com.reggarf.mods.better_lib.configs.ConfigBase;
import com.reggarf.mods.better_lib.configs.gui.BetterIcons;
import com.reggarf.mods.better_lib.configs.gui.ScreenOpener;
import com.reggarf.mods.better_lib.configs.gui.element.DelegatedStencilElement;
import com.reggarf.mods.better_lib.configs.gui.widget.BoxWidget;
import com.reggarf.mods.better_lib.configs.ui.ConfigScreenList;
import com.reggarf.mods.better_lib.configs.ui.SubMenuConfigScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class SubMenuEntry extends ConfigScreenList.LabeledEntry {

	protected BoxWidget button;
	protected SubMenuConfigScreen parent;
	protected ConfigBase childConfig;

	public SubMenuEntry(SubMenuConfigScreen parent, String label, ConfigBase childConfig) {
		super(label);
		this.parent = parent;
		this.childConfig = childConfig;
		this.path = childConfig.getName();

		int count = (childConfig.getAllValues() != null ? childConfig.getAllValues().size() : 0)
				+ (childConfig.getChildren() != null ? childConfig.getChildren().size() : 0);
		this.unit = count + (count == 1 ? " option" : " options");

		button = new BoxWidget(0, 0, 36, 18)
				.showingElement(BetterIcons.I_CONFIG_OPEN.asStencil().at(10, 1))
				.withCallback(this::openCategory);
		button.modifyElement(e -> ((DelegatedStencilElement) e).withElementRenderer(BoxWidget.gradientFactory.apply(button)));
		button.getToolTip().add(Component.literal("Open " + label));

		labelTooltip.add(Component.literal("Category: " + label));
		labelTooltip.add(Component.literal("Contains " + this.unit));

		listeners.add(button);
	}

	public void openCategory() {
		ScreenOpener.open(new SubMenuConfigScreen(parent, label.getComponent().getString(), parent.getType(), childConfig));
	}

	@Override
	public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick))
			return true;
		if (event.button() == 0) {
			openCategory();
			return true;
		}
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		button.tick();
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
		super.render(graphics, index, y, x, width, height, mouseX, mouseY, isMouseOver, partialTicks);

		button.setX(x + width - 44);
		button.setY(y + 9);
		button.setWidth(36);
		button.setHeight(height - 18);
		button.extractRenderState(graphics, mouseX, mouseY, partialTicks);
	}

	@Override
	protected int getLabelWidth(int totalWidth) {
		return (int) (totalWidth * labelWidthMult) + 30;
	}
}
