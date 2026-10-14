package com.reggarf.mods.better_lib.configs.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.reggarf.mods.better_lib.configs.gui.element.BoxElement;
import com.reggarf.mods.better_lib.configs.gui.element.TextStencilElement;
import com.reggarf.mods.better_lib.configs.gui.widget.BoxWidget;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

public class ConfirmationScreen extends BetterScreenBase {

	private Screen source;
	private Consumer<Response> action = _success -> {};
	private List<FormattedText> text = new ArrayList<>();
	private boolean centered = false;
	private int x;
	private int y;
	private int textWidth;
	private int textHeight;
	private boolean tristate;

	private BoxWidget confirm;
	private BoxWidget confirmDontSave;
	private BoxWidget cancel;
	private BoxElement textBackground;

	public enum Response {
		Confirm, ConfirmDontSave, Cancel
	}

	public ConfirmationScreen clearText() {
		this.text.clear();
		return this;
	}

	public ConfirmationScreen addText(FormattedText text) {
		this.text.add(text);
		return this;
	}

	public ConfirmationScreen withText(FormattedText text) {
		return clearText().addText(text);
	}

	public ConfirmationScreen centered() {
		this.centered = true;
		return this;
	}

	public ConfirmationScreen withThreeActions(Consumer<Response> action) {
		this.action = action;
		this.tristate = true;
		return this;
	}

	public void open(Screen source) {
		this.source = source;
		Minecraft client = Minecraft.getInstance();
		this.init(client, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
		this.minecraft = client;
		client.setScreen(this);
	}

	@Override
	public void tick() {
		super.tick();
		if (source != null)
			source.tick();
	}

	@Override
	protected void init() {
		super.init();

		ArrayList<FormattedText> copy = new ArrayList<>(text);
		text.clear();
		copy.forEach(t -> text.addAll(font.getSplitter().splitLines(t, 300, Style.EMPTY)));

		textHeight = text.size() * (font.lineHeight + 1) + 4;
		textWidth = 300;

		if (centered) {
			x = width / 2 - textWidth / 2 - 2;
			y = height / 2 - textHeight / 2 - 16;
		} else {
			x = Math.max(0, x - textWidth / 2);
			y = Math.max(0, y - textHeight);
		}

		if (x + textWidth > width) {
			x = width - textWidth;
		}

		if (y + textHeight + 30 > height) {
			y = height - textHeight - 30;
		}

		int buttonX = x + textWidth / 2 - 6 - (int) (70 * (tristate ? 1.5f : 1));

		TextStencilElement confirmText =
				new TextStencilElement(font, tristate ? "Save" : "Confirm").centered(true, true);
		confirm = new BoxWidget(buttonX, y + textHeight + 6, 70, 16).withCallback(() -> accept(Response.Confirm));
		confirm.showingElement(confirmText.withElementRenderer(BoxWidget.gradientFactory.apply(confirm)));
		addRenderableWidget(confirm);

		buttonX += 12 + 70;

		if (tristate) {
			TextStencilElement confirmDontSaveText =
					new TextStencilElement(font, "Don't Save").centered(true, true);
			confirmDontSave =
					new BoxWidget(buttonX, y + textHeight + 6, 70, 16).withCallback(() -> accept(Response.ConfirmDontSave));
			confirmDontSave.showingElement(
					confirmDontSaveText.withElementRenderer(BoxWidget.gradientFactory.apply(confirmDontSave)));
			addRenderableWidget(confirmDontSave);
			buttonX += 12 + 70;
		}

		TextStencilElement cancelText = new TextStencilElement(font, "Cancel").centered(true, true);
		cancel = new BoxWidget(buttonX, y + textHeight + 6, 70, 16)
				.withCallback(() -> accept(Response.Cancel));
		cancel.showingElement(cancelText.withElementRenderer(BoxWidget.gradientFactory.apply(cancel)));
		addRenderableWidget(cancel);

		textBackground = new BoxElement()
				.gradientBorder(Theme.p(Theme.Key.BUTTON_DISABLE))
				.withBounds(width + 10, textHeight + 35)
				.at(-5, y - 5);

		if (text.size() == 1)
			x = (width - font.width(text.get(0).getString())) / 2;
	}

	@Override
	public void onClose() {
		accept(Response.Cancel);
	}

	private void accept(Response success) {
		if (minecraft != null)
			minecraft.setScreen(source);
		action.accept(success);
	}

	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		textBackground.render(graphics);
		int offset = font.lineHeight + 1;
		int lineY = y - offset;

		PoseStack ms = graphics.pose();
		ms.pushPose();
		ms.translate(0, 0, 200);

		for (FormattedText line : text) {
			lineY += offset;
			if (line == null)
				continue;
			graphics.drawString(font, line.getString(), x, lineY, 0xeaeaea, false);
		}

		ms.popPose();
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}
}
