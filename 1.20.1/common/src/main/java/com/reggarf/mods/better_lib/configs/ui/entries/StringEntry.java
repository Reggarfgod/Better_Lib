package com.reggarf.mods.better_lib.configs.ui.entries;

import com.reggarf.mods.better_lib.configs.ConfigBase;
import com.reggarf.mods.better_lib.configs.gui.Theme;
import com.reggarf.mods.better_lib.configs.ui.ConfigTextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;

public class StringEntry extends ValueEntry<String> {

	protected ConfigTextField textField;

	public StringEntry(String label, ConfigBase.ConfigString value, String path) {
		super(label, value, path);
		textField = new ConfigTextField(Minecraft.getInstance().font, 0, 0, 200, 20);
		textField.setValue(getValue());
		textField.setTextColor(Theme.i(Theme.Key.TEXT));
		textField.setResponder(this::setValue);
		textField.moveCursorToStart();
		listeners.add(textField);
		onReset();
	}

	@Override
	public void setEditable(boolean b) {
		super.setEditable(b);
		textField.setEditable(b);
	}

	@Override
	public void onValueChange(String newValue) {
		super.onValueChange(newValue);
		if (!textField.getValue().equals(newValue)) {
			textField.setValue(newValue);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
		super.render(graphics, index, y, x, width, height, mouseX, mouseY, isMouseOver, partialTicks);

		int fieldWidth = Math.min(width - getLabelWidth(width) - resetWidth - 20, 100);
		textField.setX(x + width - fieldWidth - resetWidth - 6);
		textField.setY(y + 8);
		textField.setWidth(fieldWidth);
		textField.setHeight(20);
		textField.render(graphics, mouseX, mouseY, partialTicks);
	}
}
