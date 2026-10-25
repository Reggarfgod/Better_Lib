package com.reggarf.mods.better_lib.configs.ui.entries;

import com.google.common.base.Predicates;
import com.reggarf.mods.better_lib.configs.ConfigBase;
import com.reggarf.mods.better_lib.configs.gui.BetterIcons;
import com.reggarf.mods.better_lib.configs.gui.element.DelegatedStencilElement;
import com.reggarf.mods.better_lib.configs.gui.widget.BoxWidget;
import com.reggarf.mods.better_lib.configs.ui.ConfigAnnotations;
import com.reggarf.mods.better_lib.configs.ui.ConfigHelper;
import com.reggarf.mods.better_lib.configs.ui.ConfigScreen;
import com.reggarf.mods.better_lib.configs.ui.ConfigScreenList;
import com.reggarf.mods.better_lib.configs.utility.Pair;
import com.reggarf.mods.better_lib.configs.utility.TooltipHelper;
import com.reggarf.mods.better_lib.configs.utility.TooltipHelper.Palette;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class ValueEntry<T> extends ConfigScreenList.LabeledEntry {

	protected static final int resetWidth = 28; // including 6px offset on either side

	protected ConfigBase.CValue<T, ?> value;
	protected BoxWidget resetButton;
	protected boolean editable = true;

	public ValueEntry(String label, ConfigBase.CValue<T, ?> value, String path) {
		super(label);
		this.value = value;
		this.path = path != null ? path : value.getName();

		resetButton = new BoxWidget(0, 0, resetWidth - 12, 16)
				.showingElement(BetterIcons.I_CONFIG_RESET.asStencil())
				.withCallback(() -> {
					setValue(this.value.getDefault());
					this.onReset();
				});
		resetButton.modifyElement(e -> ((DelegatedStencilElement) e).withElementRenderer(BoxWidget.gradientFactory.apply(resetButton)));

		listeners.add(resetButton);

		labelTooltip.add(Component.literal(label).withStyle(ChatFormatting.WHITE));
		String[] comments = value.getComments();
		if (comments == null || comments.length == 0)
			return;

		List<String> commentLines = new ArrayList<>(Arrays.asList(comments));

		Pair<String, Map<String, String>> metadata = ConfigHelper.readMetadataFromComment(commentLines);
		if (metadata.getFirst() != null) {
			unit = metadata.getFirst();
		}
		if (metadata.getSecond() != null && !metadata.getSecond().isEmpty()) {
			annotations.putAll(metadata.getSecond());
		}

		labelTooltip.addAll(commentLines.stream()
				.filter(Predicates.not(s -> s.startsWith("Range")))
				.filter(s -> !s.equals("."))
				.map(Component::literal)
				.flatMap(stc -> TooltipHelper.cutTextComponent(stc, Palette.ALL_GRAY).stream())
				.collect(Collectors.toList()));

		if (annotations.containsKey(ConfigAnnotations.RequiresRelog.TRUE.getName()))
			labelTooltip.addAll(TooltipHelper.cutStringTextComponent("Changing this value will require a _relog_ to take full effect", Palette.GRAY_AND_GOLD));

		if (annotations.containsKey(ConfigAnnotations.RequiresRestart.CLIENT.getName()))
			labelTooltip.addAll(TooltipHelper.cutStringTextComponent("Changing this value will require a _restart_ to take full effect", Palette.GRAY_AND_RED));

		labelTooltip.add(Component.literal(ConfigScreen.modID + ":" + this.path).withStyle(ChatFormatting.DARK_GRAY));
	}

	@Override
	public void setEditable(boolean b) {
		editable = b;
		resetButton.active = editable && !isCurrentValueDefault();
		resetButton.animateGradientFromState();
	}

	@Override
	public void tick() {
		super.tick();
		resetButton.tick();
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
		super.render(graphics, index, y, x, width, height, mouseX, mouseY, isMouseOver, partialTicks);

		resetButton.setX(x + width - resetWidth + 6);
		resetButton.setY(y + 10);
		resetButton.extractRenderState(graphics, mouseX, mouseY, partialTicks);
	}

	@Override
	protected int getLabelWidth(int totalWidth) {
		return (int) (totalWidth * labelWidthMult) + 30;
	}

	public void setValue(T val) {
		ConfigHelper.setValue(path, this.value, val, annotations);
		onValueChange(val);
	}

	public T getValue() {
		return ConfigHelper.getValue(path, this.value);
	}

	protected boolean isCurrentValueDefault() {
		return value.getDefault().equals(getValue());
	}

	public void onReset() {
		onValueChange(getValue());
	}

	public void onValueChange() {
		onValueChange(getValue());
	}

	public void onValueChange(T newValue) {
		resetButton.active = editable && !isCurrentValueDefault();
		resetButton.animateGradientFromState();
	}

	protected void bumpCog() {
		bumpCog(10f);
	}

	protected void bumpCog(float force) {
		ConfigScreen.cogSpin.bump(3, force);
	}
}
