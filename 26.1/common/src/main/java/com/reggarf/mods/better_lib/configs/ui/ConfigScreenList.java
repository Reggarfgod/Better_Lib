package com.reggarf.mods.better_lib.configs.ui;

import com.reggarf.mods.better_lib.configs.gui.Theme;
import com.reggarf.mods.better_lib.configs.gui.UIRenderHelper;
import com.reggarf.mods.better_lib.configs.gui.element.TextStencilElement;
import com.reggarf.mods.better_lib.configs.ui.entries.NumberEntry;
import com.reggarf.mods.better_lib.configs.utility.Color;
import com.reggarf.mods.better_lib.configs.utility.LerpedFloat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ConfigScreenList extends ObjectSelectionList<ConfigScreenList.Entry> {

	public static ConfigTextField currentText = null;

	public ConfigScreenList(Minecraft client, int width, int height, int top, int bottom, int elementHeight) {
		super(client, width, bottom - top, top, elementHeight);
		setX(client.screen != null ? (client.screen.width - width) / 2 : 0);
		setY(top);
	}

	public double getScrollAmount() {
		return scrollAmount();
	}

	public void setLeftPos(int left) {
		setX(left);
	}

	@Override
	public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		int x0 = getX();
		int y0 = getY();
		int x1 = x0 + width;
		int y1 = y0 + height;

		Color c = new Color(0x60_000000);
		UIRenderHelper.angledGradient(graphics, 90, x0 + width / 2, y0, width, 5, c, Color.TRANSPARENT_BLACK);
		UIRenderHelper.angledGradient(graphics, -90, x0 + width / 2, y1, width, 5, c, Color.TRANSPARENT_BLACK);
		UIRenderHelper.angledGradient(graphics, 0, x0, y0 + height / 2, height, 5, c, Color.TRANSPARENT_BLACK);
		UIRenderHelper.angledGradient(graphics, 180, x1, y0 + height / 2, height, 5, c, Color.TRANSPARENT_BLACK);

		graphics.enableScissor(x0, y0, x1, y1);
		super.extractWidgetRenderState(graphics, mouseX, mouseY, partialTicks);
		graphics.disableScissor();
	}

	@Override
	protected void extractListBackground(GuiGraphicsExtractor graphics) {}

	@Override
	protected void extractListSeparators(GuiGraphicsExtractor graphics) {}

	@Override
	protected void extractSelection(GuiGraphicsExtractor graphics, Entry entry, int backgroundColor) {}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		children().stream().filter(e -> e instanceof NumberEntry<?>).forEach(e -> e.mouseClicked(event, doubleClick));
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public int getRowWidth() {
		return width - 16;
	}

	@Override
	protected int scrollBarX() {
		return getX() + this.width - 6;
	}

	public void tick() {
		children().forEach(Entry::tick);
	}

	public boolean search(String query) {
		if (query == null || query.isEmpty()) {
			setScrollAmount(0);
			return true;
		}

		String q = query.toLowerCase(Locale.ROOT);
		Optional<Entry> first = children().stream().filter(entry -> {
			if (entry.path == null)
				return false;

			String[] split = entry.path.split("\\.");
			String key = split[split.length - 1].toLowerCase(Locale.ROOT);
			return key.contains(q);
		}).findFirst();

		if (first.isEmpty()) {
			setScrollAmount(0);
			return false;
		}

		Entry e = first.get();
		e.annotations.put("highlight", "(:");
		centerScrollOn(e);
		return true;
	}

	public void bumpCog(float force) {
		ConfigScreen.cogSpin.bump(3, force);
	}

	public static abstract class Entry extends ObjectSelectionList.Entry<Entry> {
		protected List<GuiEventListener> listeners;
		public Map<String, String> annotations;
		public String path;

		protected Entry() {
			listeners = new ArrayList<>();
			annotations = new HashMap<>();
		}

		@Override
		public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean isHovering, float partialTick) {
			render(graphics, 0, getY(), getX(), getWidth(), getHeight(), mouseX, mouseY, isHovering, partialTick);
		}

		public void render(GuiGraphicsExtractor graphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
			return getGuiListeners().stream().anyMatch(l -> l.mouseClicked(event, doubleClick));
		}

		@Override
		public boolean keyPressed(KeyEvent event) {
			return getGuiListeners().stream().anyMatch(l -> l.keyPressed(event));
		}

		@Override
		public boolean charTyped(CharacterEvent event) {
			return getGuiListeners().stream().anyMatch(l -> l.charTyped(event));
		}

		public void tick() {}

		public List<GuiEventListener> getGuiListeners() {
			return listeners;
		}

		public void setEditable(boolean b) {}

		protected boolean isCurrentValueChanged() {
			if (path == null) {
				return false;
			}
			return ConfigHelper.changes.containsKey(path);
		}

		@Override
		public Component getNarration() {
			return CommonComponents.EMPTY;
		}
	}

	public static class LabeledEntry extends Entry {

		protected static final float labelWidthMult = 0.4f;

		public TextStencilElement label;
		protected List<Component> labelTooltip;
		protected String unit = null;
		protected LerpedFloat differenceAnimation = LerpedFloat.linear().startWithValue(0);
		protected LerpedFloat highlightAnimation = LerpedFloat.linear().startWithValue(0);

		public LabeledEntry(String label) {
			this.label = new TextStencilElement(Minecraft.getInstance().font, label);
			this.label.withElementRenderer((graphics, width, height, alpha) -> UIRenderHelper.angledGradient(graphics,
				0, 0, height / 2, height, width, Theme.p(Theme.Key.TEXT_ACCENT_STRONG)));
			labelTooltip = new ArrayList<>();
		}

		public LabeledEntry(String label, String path) {
			this(label);
			this.path = path;
		}

		@Override
		public void tick() {
			differenceAnimation.tickChaser();
			highlightAnimation.tickChaser();
			super.tick();
		}

		@Override
		public void render(GuiGraphicsExtractor graphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
			if (isCurrentValueChanged()) {
				if (differenceAnimation.getChaseTarget() != 1)
					differenceAnimation.chase(1, .5f, LerpedFloat.Chaser.EXP);
			} else {
				if (differenceAnimation.getChaseTarget() != 0)
					differenceAnimation.chase(0, .6f, LerpedFloat.Chaser.EXP);
			}

			float animation = differenceAnimation.getValue(partialTicks);
			if (animation > .1f) {
				int offset = (int) (30 * (1 - animation));

				if (annotations.containsKey(ConfigAnnotations.RequiresRestart.CLIENT.getName())) {
					UIRenderHelper.streak(graphics, 180, x + width + 10 + offset, y + height / 2, height - 6, 110, new Color(0x50_601010));
				} else if (annotations.containsKey(ConfigAnnotations.RequiresRelog.TRUE.getName())) {
					UIRenderHelper.streak(graphics, 180, x + width + 10 + offset, y + height / 2, height - 6, 110, new Color(0x40_eefb17));
				}

				UIRenderHelper.breadcrumbArrow(graphics, x - 10 - offset, y + 6, 0, -20, 24, -18, new Color(0x70_ffffff), Color.TRANSPARENT_BLACK);
			}

			UIRenderHelper.streak(graphics, 0, x - 10, y + height / 2, height - 6, width / 8 * 7, 0xdd_000000);
			UIRenderHelper.streak(graphics, 180, x + (int) (width * 1.35f) + 10, y + height / 2, height - 6, width / 8 * 7, 0xdd_000000);
			MutableComponent component = label.getComponent();
			Font font = Minecraft.getInstance().font;
			if (font.width(component) > getLabelWidth(width) - 10) {
				label.withText(font.substrByWidth(component, getLabelWidth(width) - 15).getString() + "...");
			}
			if (unit != null) {
				int unitWidth = font.width(unit);
				graphics.text(font, unit, x + getLabelWidth(width) - unitWidth - 5, y + height / 2 + 2, Theme.i(Theme.Key.TEXT_DARKER), false);
				label.at(x + 10, y + height / 2 - 10, 0).render(graphics);
			} else {
				label.at(x + 10, y + height / 2 - 4, 0).render(graphics);
			}

			if (annotations.containsKey("highlight")) {
				highlightAnimation.startWithValue(1).chase(0, 0.1f, LerpedFloat.Chaser.LINEAR);
				annotations.remove("highlight");
			}

			animation = highlightAnimation.getValue(partialTicks);
			if (animation > .01f) {
				Color highlight = new Color(0xa0_ffffff).scaleAlpha(animation);
				UIRenderHelper.streak(graphics, 0, x - 10, y + height / 2, height - 6, 5, highlight);
				UIRenderHelper.streak(graphics, 180, x + width, y + height / 2, height - 6, 5, highlight);
				UIRenderHelper.streak(graphics, 90, x + width / 2 - 5, y + 3, width + 10, 5, highlight);
				UIRenderHelper.streak(graphics, -90, x + width / 2 - 5, y + height - 3, width + 10, 5, highlight);
			}

			if (mouseX > x && mouseX < x + getLabelWidth(width) && mouseY > y + 5 && mouseY < y + height - 5) {
				List<Component> tooltip = getLabelTooltip();
				if (!tooltip.isEmpty()) {
					graphics.setComponentTooltipForNextFrame(font, tooltip, mouseX, mouseY);
				}
			}
		}

		public List<Component> getLabelTooltip() {
			return labelTooltip;
		}

		protected int getLabelWidth(int totalWidth) {
			return totalWidth;
		}
	}
}
