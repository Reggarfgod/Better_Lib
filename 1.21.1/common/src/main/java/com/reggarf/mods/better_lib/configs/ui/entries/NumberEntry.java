package com.reggarf.mods.better_lib.configs.ui.entries;

import com.reggarf.mods.better_lib.configs.ConfigBase;
import com.reggarf.mods.better_lib.configs.gui.Theme;
import com.reggarf.mods.better_lib.configs.gui.UIRenderHelper;
import com.reggarf.mods.better_lib.configs.gui.element.TextStencilElement;
import com.reggarf.mods.better_lib.configs.ui.ConfigTextField;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public abstract class NumberEntry<T extends Number> extends ValueEntry<T> {

	protected int minOffset = 0, maxOffset = 0;
	protected TextStencilElement minText = null, maxText = null;
	protected EditBox textField;

	public static NumberEntry<? extends Number> create(ConfigBase.CValue<?, ?> value, String label, String path) {
		if (value instanceof ConfigBase.ConfigInt intVal) {
			return new IntegerEntry(label, intVal, path);
		} else if (value instanceof ConfigBase.ConfigFloat floatVal) {
			return new FloatEntry(label, floatVal, path);
		}
		return null;
	}

	public NumberEntry(String label, ConfigBase.CValue<T, ?> value, String path, T min, T max) {
		super(label, value, path);
		textField = new ConfigTextField(Minecraft.getInstance().font, 0, 0, 200, 20);
		if (this instanceof IntegerEntry && annotations.containsKey("ColorField")) {
			int intValue = (Integer) getValue();
			textField.setValue("#" + String.format("%08X", intValue));
		} else if (this instanceof IntegerEntry && annotations.containsKey("IntDisplay")) {
			String intDisplay = annotations.get("IntDisplay");
			int intValue = (Integer) getValue();
			String textValue;
			switch (intDisplay) {
				case "#" -> textValue = "#" + Integer.toHexString(intValue).toUpperCase(Locale.ROOT);
				case "0x" -> textValue = "0x" + Integer.toHexString(intValue).toUpperCase(Locale.ROOT);
				case "0b" -> textValue = "0b" + Integer.toBinaryString(intValue);
				default -> textValue = String.valueOf(intValue);
			}
			textField.setValue(textValue);
		} else {
			textField.setValue(String.valueOf(getValue()));
		}
		textField.setTextColor(Theme.i(Theme.Key.TEXT));

		Font font = Minecraft.getInstance().font;
		if (min != null && min.doubleValue() > getTypeMin().doubleValue()) {
			MutableComponent t = Component.literal(formatBound(min) + " < ");
			minText = new TextStencilElement(font, t).centered(true, false);
			minText.withElementRenderer((ms, w, h, alpha) -> UIRenderHelper.angledGradient(ms, 0, 0, h / 2, h, w, Theme.p(Theme.Key.TEXT_DARKER)));
			minOffset = font.width(t);
		}
		if (max != null && max.doubleValue() < getTypeMax().doubleValue()) {
			MutableComponent t = Component.literal(" < " + formatBound(max));
			maxText = new TextStencilElement(font, t).centered(true, false);
			maxText.withElementRenderer((ms, w, h, alpha) -> UIRenderHelper.angledGradient(ms, 0, 0, h / 2, h, w, Theme.p(Theme.Key.TEXT_DARKER)));
			maxOffset = font.width(t);
		}

		textField.setResponder(s -> {
			try {
				T number = getParser().apply(s);
				if (!isValid(number))
					throw new IllegalArgumentException();

				textField.setTextColor(Theme.i(Theme.Key.TEXT));
				setValue(number);
			} catch (IllegalArgumentException ignored) {
				textField.setTextColor(Theme.i(Theme.Key.BUTTON_FAIL));
			}
		});

		textField.moveCursorToStart(false);
		listeners.add(textField);
		onReset();
	}

	protected abstract boolean isValid(T number);

	protected String formatBound(T bound) {
		String sci = String.format("%.2E", bound.doubleValue());
		String str = String.valueOf(bound);
		return sci.length() < str.length() ? sci : str;
	}

	protected abstract T getTypeMin();

	protected abstract T getTypeMax();

	protected abstract Function<String, T> getParser();

	@Override
	public void setEditable(boolean b) {
		super.setEditable(b);
		textField.setEditable(b);
	}

	@Override
	public void onValueChange(T newValue) {
		super.onValueChange(newValue);

		try {
			T current = getParser().apply(textField.getValue());
			if (!current.equals(newValue)) {
				if (this instanceof IntegerEntry && annotations.containsKey("ColorField")) {
					textField.setValue("#" + String.format("%08X", (Integer) newValue));
				} else {
					textField.setValue(String.valueOf(newValue));
				}
			}
		} catch (IllegalArgumentException ignored) {}
	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	public void render(GuiGraphics graphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
		super.render(graphics, index, y, x, width, height, mouseX, mouseY, isMouseOver, partialTicks);

		int inputWidth = annotations.containsKey("ColorField") ? 64 : Math.min(width - getLabelWidth(width) - resetWidth - minOffset - maxOffset, 40);
		textField.setX(x + width - inputWidth - resetWidth - 6);
		textField.setY(y + 8);
		textField.setWidth(inputWidth);
		textField.setHeight(20);
		textField.render(graphics, mouseX, mouseY, partialTicks);

		if (annotations.containsKey("ColorField") && getValue() instanceof Integer colorInt) {
			int swatchX = textField.getX() - 18;
			int swatchY = textField.getY() + 3;
			graphics.fill(swatchX - 1, swatchY - 1, swatchX + 15, swatchY + 15, 0xFFFFFFFF);
			graphics.fill(swatchX, swatchY, swatchX + 14, swatchY + 14, colorInt | 0xFF000000);
		}

		if (minText != null)
			minText.at(textField.getX() - minOffset, textField.getY(), 0)
					.withBounds(minOffset, textField.getHeight())
					.render(graphics);

		if (maxText != null)
			maxText.at(textField.getX() + textField.getWidth(), textField.getY(), 0)
					.withBounds(maxOffset, textField.getHeight())
					.render(graphics);
	}

	public static class IntegerEntry extends NumberEntry<Integer> {
		private final ConfigBase.ConfigInt intVal;

		public IntegerEntry(String label, ConfigBase.ConfigInt value, String path) {
			super(label, value, path, value.getMin(), value.getMax());
			this.intVal = value;
		}

		@Override
		protected boolean isValid(Integer number) {
			return number >= intVal.getMin() && number <= intVal.getMax();
		}

		@Override
		protected Integer getTypeMin() {
			return Integer.MIN_VALUE;
		}

		@Override
		protected Integer getTypeMax() {
			return Integer.MAX_VALUE;
		}

		@Override
		protected Function<String, Integer> getParser() {
			return (string) -> {
				if (string == null || string.isEmpty()) throw new IllegalArgumentException();
				String s = string.trim();
				if (s.startsWith("#")) {
					return (int) Long.parseUnsignedLong(s.substring(1), 16);
				} else if (s.startsWith("0x") || s.startsWith("0X")) {
					return (int) Long.parseUnsignedLong(s.substring(2), 16);
				} else if (s.startsWith("0b") || s.startsWith("0B")) {
					return (int) Long.parseUnsignedLong(s.substring(2), 2);
				} else {
					return Integer.parseInt(s);
				}
			};
		}
	}

	public static class FloatEntry extends NumberEntry<Float> {
		private final ConfigBase.ConfigFloat floatVal;

		public FloatEntry(String label, ConfigBase.ConfigFloat value, String path) {
			super(label, value, path, value.getMin(), value.getMax());
			this.floatVal = value;
		}

		@Override
		protected boolean isValid(Float number) {
			return number >= floatVal.getMin() && number <= floatVal.getMax();
		}

		@Override
		protected Float getTypeMin() {
			return -Float.MAX_VALUE;
		}

		@Override
		protected Float getTypeMax() {
			return Float.MAX_VALUE;
		}

		@Override
		protected Function<String, Float> getParser() {
			return Float::parseFloat;
		}
	}
}
