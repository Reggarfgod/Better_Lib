package com.reggarf.mods.better_lib.configs.ui;

import com.reggarf.mods.better_lib.configs.ConfigBase;
import com.reggarf.mods.better_lib.configs.gui.BetterIcons;
import com.reggarf.mods.better_lib.configs.gui.ConfirmationScreen;
import com.reggarf.mods.better_lib.configs.gui.ScreenOpener;
import com.reggarf.mods.better_lib.configs.gui.Theme;
import com.reggarf.mods.better_lib.configs.gui.UIRenderHelper;
import com.reggarf.mods.better_lib.configs.gui.element.DelegatedStencilElement;
import com.reggarf.mods.better_lib.configs.gui.widget.BoxWidget;
import com.reggarf.mods.better_lib.configs.ui.entries.BooleanEntry;
import com.reggarf.mods.better_lib.configs.ui.entries.DropdownStringEntry;
import com.reggarf.mods.better_lib.configs.ui.entries.EnumEntry;
import com.reggarf.mods.better_lib.configs.ui.entries.NumberEntry;
import com.reggarf.mods.better_lib.configs.ui.entries.StringEntry;
import com.reggarf.mods.better_lib.configs.ui.entries.SubMenuEntry;
import com.reggarf.mods.better_lib.configs.ui.entries.ValueEntry;
import com.reggarf.mods.better_lib.configs.utility.Color;
import com.reggarf.mods.better_lib.configs.utility.Couple;
import com.reggarf.mods.better_lib.configs.utility.Pair;
import com.reggarf.mods.better_lib.configs.utility.TooltipHelper;
import com.reggarf.mods.better_lib.configs.utility.TooltipHelper.Palette;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.lwjgl.glfw.GLFW;

public class SubMenuConfigScreen extends ConfigScreen {

	public final ConfigType type;
	protected final ConfigBase config;
	protected final String subMenuTitle;
	protected final Set<String> highlights = new HashSet<>();

	protected ConfigScreenList list;
	protected ConfigTextField search;
	protected BoxWidget resetAll;
	protected BoxWidget saveChanges;
	protected BoxWidget discardChanges;
	protected BoxWidget goBack;
	protected BoxWidget serverLocked;

	public SubMenuConfigScreen(Screen parent, String title, ConfigType type, ConfigBase config) {
		super(parent);
		this.type = type;
		this.config = config;
		this.subMenuTitle = title;
	}

	public SubMenuConfigScreen(Screen parent, ConfigType type, ConfigBase config) {
		this(parent, config.getName(), type, config);
	}

	public ConfigType getType() {
		return type;
	}

	public SubMenuConfigScreen withHighlights(Set<String> highlights) {
		this.highlights.addAll(highlights);
		return this;
	}

	@Override
	protected void init() {
		super.init();

		int listWidth = Math.min(width - 80, 300);
		int listL = this.width / 2 - listWidth / 2;
		int listR = listL + listWidth;
		int yCenter = this.height / 2;

		resetAll = new BoxWidget(listL - 30, yCenter - 25, 20, 20)
				.withPadding(2, 2)
				.withCallback(this::resetConfig);
		resetAll.showingElement(BetterIcons.I_CONFIG_RESET.asStencil().withElementRenderer(BoxWidget.gradientFactory.apply(resetAll)));
		resetAll.getToolTip().add(Component.literal("Reset All"));
		resetAll.getToolTip().addAll(TooltipHelper.cutStringTextComponent("Click to reset all settings to their default values.", Palette.ALL_GRAY));

		saveChanges = new BoxWidget(listL - 30, yCenter + 5, 20, 20)
				.withPadding(2, 2)
				.withCallback(this::saveConfig);
		saveChanges.showingElement(BetterIcons.I_CONFIG_SAVE.asStencil().withElementRenderer(BoxWidget.gradientFactory.apply(saveChanges)));
		saveChanges.getToolTip().add(Component.literal("Save Changes"));
		saveChanges.getToolTip().addAll(TooltipHelper.cutStringTextComponent("Click to save any changes made to this config.", Palette.ALL_GRAY));

		discardChanges = new BoxWidget(listL - 30, yCenter + 35, 20, 20)
				.withPadding(2, 2)
				.withCallback(this::discardChanges);
		discardChanges.showingElement(BetterIcons.I_CONFIG_DISCARD.asStencil().withElementRenderer(BoxWidget.gradientFactory.apply(discardChanges)));
		discardChanges.getToolTip().add(Component.literal("Discard Changes"));
		discardChanges.getToolTip().addAll(TooltipHelper.cutStringTextComponent("Click to discard all unsaved changes.", Palette.ALL_GRAY));

		goBack = new BoxWidget(listL - 30, yCenter + 65, 20, 20)
				.withPadding(2, 2)
				.withCallback(this::attemptBackstep);
		goBack.showingElement(BetterIcons.I_CONFIG_BACK.asStencil().withElementRenderer(BoxWidget.gradientFactory.apply(goBack)));
		goBack.getToolTip().add(Component.literal("Go Back"));

		addRenderableWidget(resetAll);
		addRenderableWidget(saveChanges);
		addRenderableWidget(discardChanges);
		addRenderableWidget(goBack);

		list = new ConfigScreenList(minecraft, listWidth, height - 80, 35, height - 45, 40);
		list.setLeftPos(this.width / 2 - list.getWidth() / 2);
		addRenderableWidget(list);

		search = new ConfigTextField(font, width / 2 - listWidth / 2, height - 35, listWidth, 20);
		search.setResponder(this::updateFilter);
		search.setHint("Search...");
		search.moveCursorToStart();
		addRenderableWidget(search);

		loadEntries();

		// Check server permissions if server config
		if (type == ConfigType.SERVER && minecraft.level != null && !minecraft.hasSingleplayerServer()) {
			boolean canEdit = minecraft.player != null && minecraft.player.hasPermissions(2);
			Couple<Color> red = Theme.p(Theme.Key.BUTTON_FAIL);
			Couple<Color> green = Theme.p(Theme.Key.BUTTON_SUCCESS);
			DelegatedStencilElement stencil = new DelegatedStencilElement();

			serverLocked = new BoxWidget(listR + 10, yCenter + 5, 20, 20)
					.withPadding(2, 2)
					.showingElement(stencil);

			if (!canEdit) {
				list.children().forEach(e -> e.setEditable(false));
				resetAll.active = false;
				saveChanges.active = false;
				stencil.withStencilRenderer((ms, w, h, alpha) -> BetterIcons.I_CONFIG_LOCKED.render(ms, 0, 0));
				stencil.withElementRenderer((ms, w, h, alpha) -> UIRenderHelper.angledGradient(ms, 90, 8, 0, 16, 16, red));
				serverLocked.withBorderColors(red);
				serverLocked.getToolTip().add(Component.literal("Locked").withStyle(ChatFormatting.BOLD));
				serverLocked.getToolTip().addAll(TooltipHelper.cutStringTextComponent("You do not have enough permissions to edit the server config.", Palette.ALL_GRAY));
			} else {
				stencil.withStencilRenderer((ms, w, h, alpha) -> BetterIcons.I_CONFIG_UNLOCKED.render(ms, 0, 0));
				stencil.withElementRenderer((ms, w, h, alpha) -> UIRenderHelper.angledGradient(ms, 90, 8, 0, 16, 16, green));
				serverLocked.withBorderColors(green);
				serverLocked.getToolTip().add(Component.literal("Unlocked").withStyle(ChatFormatting.BOLD));
				serverLocked.getToolTip().addAll(TooltipHelper.cutStringTextComponent("You have enough permissions to edit the server config.", Palette.ALL_GRAY));
			}
			addRenderableWidget(serverLocked);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadEntries() {
		list.children().clear();

		// Add child configs as sub-menu entries (categories)
		if (config.getChildren() != null) {
			for (ConfigBase child : config.getChildren()) {
				String humanKey = toHumanReadable(child.getName());
				SubMenuEntry entry = new SubMenuEntry(this, humanKey, child);
				list.children().add(entry);
			}
		}

		// Add values
		if (config.getAllValues() != null) {
			for (ConfigBase.CValue<?, ?> val : config.getAllValues()) {
				if (val instanceof ConfigBase.ConfigGroup)
					continue;

				String humanKey = toHumanReadable(val.getName());
				ConfigScreenList.Entry entry = null;

				if (val instanceof ConfigBase.ConfigBool boolVal) {
					entry = new BooleanEntry(humanKey, boolVal, val.getName());
				} else if (val instanceof ConfigBase.ConfigEnum<?> enumVal) {
					entry = new EnumEntry(humanKey, (ConfigBase.ConfigEnum) enumVal, val.getName());
				} else if (val instanceof ConfigBase.ConfigInt || val instanceof ConfigBase.ConfigFloat) {
					entry = NumberEntry.create(val, humanKey, val.getName());
				} else if (val instanceof ConfigBase.ConfigString stringVal) {
					Pair<String, Map<String, String>> meta = ConfigHelper.readMetadataFromComment(
							new ArrayList<>(Arrays.asList(stringVal.getComments() != null ? stringVal.getComments() : new String[0]))
					);
					if (meta.getSecond().containsKey("Dropdown")) {
						String[] items = meta.getSecond().get("Dropdown").split(",");
						entry = new DropdownStringEntry(humanKey, stringVal, val.getName(), Arrays.asList(items));
					} else {
						entry = new StringEntry(humanKey, stringVal, val.getName());
					}
				}

				if (entry == null) {
					entry = new ConfigScreenList.LabeledEntry("Unsupported: " + humanKey);
				}

				if (highlights.contains(val.getName())) {
					entry.annotations.put("highlight", ":)");
				}

				list.children().add(entry);
			}
		}

		Collections.sort(list.children(), (e, e2) -> {
			int group = (e2 instanceof SubMenuEntry ? 1 : 0) - (e instanceof SubMenuEntry ? 1 : 0);
			if (group == 0 && e instanceof ConfigScreenList.LabeledEntry le && e2 instanceof ConfigScreenList.LabeledEntry le2) {
				return le.label.getComponent().getString().compareTo(le2.label.getComponent().getString());
			}
			return group;
		});

		list.search(highlights.stream().findFirst().orElse(""));
	}

	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.renderWindow(graphics, mouseX, mouseY, partialTicks);
		int x = width / 2;
		String breadcrumb;
		if (parent instanceof SubMenuConfigScreen) {
			breadcrumb = ConfigScreen.modID.toUpperCase(Locale.ROOT) + " > " + type.name().toLowerCase(Locale.ROOT) + " > " + subMenuTitle;
		} else {
			breadcrumb = ConfigScreen.modID.toUpperCase(Locale.ROOT) + " > " + type.name().toLowerCase(Locale.ROOT);
		}
		graphics.drawCenteredString(font, breadcrumb, x, 15, Theme.i(Theme.Key.TEXT));
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		double scroll = list != null ? list.getScrollAmount() : 0;
		init(client, width, height);
		if (list != null) {
			list.setScrollAmount(scroll);
		}
	}

	@Override
	public GuiEventListener getFocused() {
		if (ConfigScreenList.currentText != null)
			return ConfigScreenList.currentText;
		return super.getFocused();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers))
			return true;

		if (Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_F) {
			search.setFocused(true);
			return true;
		}

		if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
			attemptBackstep();
			return true;
		}

		return false;
	}

	private void updateFilter(String search) {
		if (list.search(search)) {
			this.search.setTextColor(Theme.i(Theme.Key.TEXT));
		} else {
			this.search.setTextColor(Theme.i(Theme.Key.BUTTON_FAIL));
		}
	}

	private void saveConfig() {
		for (Map.Entry<String, ConfigHelper.ConfigChange> change : ConfigHelper.changes.entrySet()) {
			if (config.getAllValues() != null) {
				for (ConfigBase.CValue<?, ?> val : config.getAllValues()) {
					if (val.getName().equals(change.getKey())) {
						((ConfigBase.CValue<Object, ?>) val).set(change.getValue().value);
					}
				}
			}
			if (config.getChildren() != null) {
				for (ConfigBase child : config.getChildren()) {
					if (child.getAllValues() != null) {
						for (ConfigBase.CValue<?, ?> val : child.getAllValues()) {
							if (val.getName().equals(change.getKey())) {
								((ConfigBase.CValue<Object, ?>) val).set(change.getValue().value);
							}
						}
					}
				}
			}
		}
		config.save();
		config.onReload();
		ConfigHelper.changes.clear();
		loadEntries();
	}

	private void discardChanges() {
		ConfigHelper.changes.clear();
		loadEntries();
	}

	private void resetConfig() {
		if (config.getAllValues() != null) {
			for (ConfigBase.CValue<?, ?> val : config.getAllValues()) {
				ConfigHelper.setValue(val.getName(), (ConfigBase.CValue<Object, ?>) val, val.getDefault(), null);
			}
		}
		if (config.getChildren() != null) {
			for (ConfigBase child : config.getChildren()) {
				if (child.getAllValues() != null) {
					for (ConfigBase.CValue<?, ?> val : child.getAllValues()) {
						ConfigHelper.setValue(val.getName(), (ConfigBase.CValue<Object, ?>) val, val.getDefault(), null);
					}
				}
			}
		}
		for (ConfigScreenList.Entry entry : list.children()) {
			if (entry instanceof ValueEntry<?> ve) {
				ve.onReset();
			}
		}
	}

	private void attemptBackstep() {
		if (ConfigHelper.changes.isEmpty() || !(parent instanceof BaseConfigScreen)) {
			ScreenOpener.open(parent);
			return;
		}

		showLeavingPrompt(success -> {
			if (success == ConfirmationScreen.Response.Cancel)
				return;
			if (success == ConfirmationScreen.Response.Confirm)
				saveConfig();
			ConfigHelper.changes.clear();
			ScreenOpener.open(parent);
		});
	}

	@Override
	public void onClose() {
		if (ConfigHelper.changes.isEmpty()) {
			super.onClose();
			return;
		}

		showLeavingPrompt(success -> {
			if (success == ConfirmationScreen.Response.Cancel)
				return;
			if (success == ConfirmationScreen.Response.Confirm)
				saveConfig();
			ConfigHelper.changes.clear();
			super.onClose();
		});
	}

	public void showLeavingPrompt(Consumer<ConfirmationScreen.Response> action) {
		ConfirmationScreen screen = new ConfirmationScreen()
				.centered()
				.withThreeActions(action)
				.addText(FormattedText.of("Leaving with " + ConfigHelper.changes.size() + " unsaved change"
						+ (ConfigHelper.changes.size() != 1 ? "s" : "") + " for this config"));

		addAnnotationsToConfirm(screen).open(this);
	}

	private ConfirmationScreen addAnnotationsToConfirm(ConfirmationScreen screen) {
		AtomicBoolean relog = new AtomicBoolean(false);
		AtomicBoolean restart = new AtomicBoolean(false);
		ConfigHelper.changes.values().forEach(change -> {
			if (change.annotations != null) {
				if (change.annotations.containsKey(ConfigAnnotations.RequiresRelog.TRUE.getName()))
					relog.set(true);
				if (change.annotations.containsKey(ConfigAnnotations.RequiresRestart.CLIENT.getName()))
					restart.set(true);
			}
		});

		if (relog.get()) {
			screen.addText(FormattedText.of(" "));
			screen.addText(FormattedText.of("At least one changed value will require you to relog to take full effect"));
		}

		if (restart.get()) {
			screen.addText(FormattedText.of(" "));
			screen.addText(FormattedText.of("At least one changed value will require you to restart your game to take full effect"));
		}

		return screen;
	}
}
