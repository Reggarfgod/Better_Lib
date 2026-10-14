package com.reggarf.mods.better_lib.configs.ui.entries;

import com.reggarf.mods.better_lib.configs.ConfigBase;
import com.reggarf.mods.better_lib.configs.gui.BetterIcons;
import com.reggarf.mods.better_lib.configs.gui.Theme;
import com.reggarf.mods.better_lib.configs.gui.UIRenderHelper;
import com.reggarf.mods.better_lib.configs.gui.element.BoxElement;
import com.reggarf.mods.better_lib.configs.gui.element.DelegatedStencilElement;
import com.reggarf.mods.better_lib.configs.gui.element.TextStencilElement;
import com.reggarf.mods.better_lib.configs.gui.widget.BoxWidget;
import com.reggarf.mods.better_lib.configs.ui.ConfigScreen;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class DropdownStringEntry extends ValueEntry<String> {

	protected static final int cycleWidth = 34;

	protected final List<String> options;
	protected TextStencilElement valueText;
	protected BoxWidget cycleLeft;
	protected BoxWidget cycleRight;

	public DropdownStringEntry(String label, ConfigBase.ConfigString value, String path, List<String> options) {
		super(label, value, path);
		this.options = options;

		valueText = new TextStencilElement(Minecraft.getInstance().font, getValue()).centered(true, true);
		valueText.withElementRenderer((ms, width, height, alpha) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2,
				height, width, Theme.p(Theme.Key.TEXT)));

		DelegatedStencilElement l = BetterIcons.I_CONFIG_PREV.asStencil();
		cycleLeft = new BoxWidget(0, 0, cycleWidth + 8, 16)
				.withCustomBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
				.showingElement(l)
				.withCallback(() -> cycleValue(-1));
		l.withElementRenderer(BoxWidget.gradientFactory.apply(cycleLeft));

		DelegatedStencilElement r = BetterIcons.I_CONFIG_NEXT.asStencil();
		cycleRight = new BoxWidget(0, 0, cycleWidth + 8, 16)
				.withCustomBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
				.showingElement(r)
				.withCallback(() -> cycleValue(1));
		r.at(cycleWidth - 8, 0);
		r.withElementRenderer(BoxWidget.gradientFactory.apply(cycleRight));

		listeners.add(cycleLeft);
		listeners.add(cycleRight);

		onReset();
	}

	protected void cycleValue(int direction) {
		if (options == null || options.isEmpty())
			return;

		String current = getValue();
		int currentIndex = options.indexOf(current);
		if (currentIndex == -1) currentIndex = 0;

		int newIndex = Math.floorMod(currentIndex + direction, options.size());
		String next = options.get(newIndex);
		setValue(next);
		bumpCog(direction * 15f);
	}

	@Override
	public void setEditable(boolean b) {
		super.setEditable(b);
		cycleLeft.active = b;
		cycleLeft.animateGradientFromState();
		cycleRight.active = b;
		cycleRight.animateGradientFromState();
	}

	@Override
	public void tick() {
		super.tick();
		cycleLeft.tick();
		cycleRight.tick();
	}

	@Override
	public void render(GuiGraphics graphics, int index, int y, int x, int width, int height, int mouseX, int mouseY,
			boolean isMouseOver, float partialTicks) {
		super.render(graphics, index, y, x, width, height, mouseX, mouseY, isMouseOver, partialTicks);

		cycleLeft.setX(x + getLabelWidth(width) + 4);
		cycleLeft.setY(y + 10);
		cycleLeft.render(graphics, mouseX, mouseY, partialTicks);

		valueText.at(cycleLeft.getX() + cycleWidth - 8, y + 10, 200)
				.withBounds(width - getLabelWidth(width) - 2 * cycleWidth - resetWidth - 4, 16)
				.render(graphics);

		cycleRight.setX(x + width - cycleWidth * 2 - resetWidth + 10);
		cycleRight.setY(y + 10);
		cycleRight.render(graphics, mouseX, mouseY, partialTicks);

		new BoxElement()
				.withBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
				.flatBorder(0x01_000000)
				.withBounds(48, 6)
				.at(cycleLeft.getX() + 22, cycleLeft.getY() + 5)
				.render(graphics);
	}

	@Override
	public void onValueChange(String newValue) {
		super.onValueChange(newValue);
		valueText.withText(ConfigScreen.toHumanReadable(newValue));
	}
}
