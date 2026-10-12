package com.reggarf.mods.better_lib.configs.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.reggarf.mods.better_lib.configs.ConfigBase;
import com.reggarf.mods.better_lib.configs.adapter.DynamicConfigCategory;
import com.reggarf.mods.better_lib.configs.gui.ScreenOpener;
import com.reggarf.mods.better_lib.configs.utility.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

/**
 * Modern, Minecrafty two-column dashboard configuration screen.
 */
public class BaseConfigScreen extends Screen {

	protected final Screen parent;
	protected String modName;

	protected ConfigBase clientConfig;
	protected ConfigBase commonConfig;
	protected ConfigBase serverConfig;
	protected final Map<String, ConfigBase> customCategories = new LinkedHashMap<>();

	// UI Layout State
	protected final List<CategoryData> categories = new ArrayList<>();
	protected int selectedCategoryIndex = 0;
	protected double scrollOffset = 0;
	protected double maxScroll = 0;
	protected boolean isDraggingScrollbar = false;
	protected SettingEntry activeSlider = null;

	// Search & Widgets
	protected EditBox searchBox;
	protected String searchQuery = "";

	public BaseConfigScreen(Screen parent, String modID) {
		super(Component.literal((modID != null ? modID : "better_lib") + " Configuration"));
		this.parent = parent;
		this.modName = modID != null ? modID : "better_lib";
	}

	public BaseConfigScreen(Screen parent) {
		this(parent, "better_lib");
	}

	public BaseConfigScreen withConfigs(ConfigBase client, ConfigBase common, ConfigBase server) {
		this.clientConfig = client;
		this.commonConfig = common;
		this.serverConfig = server;
		return this;
	}

	public BaseConfigScreen withCategories(Map<String, ? extends ConfigBase> categories) {
		this.customCategories.clear();
		if (categories != null) {
			this.customCategories.putAll(categories);
		}
		return this;
	}

	public BaseConfigScreen withCategory(String title, ConfigBase config) {
		this.customCategories.put(title, config);
		return this;
	}

	public BaseConfigScreen withTitles(String client, String common, String server) {
		return this;
	}

	public static ConfigScreen.ConfigType classifyCategory(String name) {
		if (name == null) return ConfigScreen.ConfigType.COMMON;
		String lower = name.toLowerCase(Locale.ROOT);
		if (lower.contains("client") || lower.contains("visual") || lower.contains("color")
				|| lower.contains("audio") || lower.contains("sound") || lower.contains("hud")
				|| lower.contains("gui") || lower.contains("ui") || lower.contains("chat")
				|| lower.contains("graphic") || lower.contains("render") || lower.contains("animat")
				|| lower.contains("theme") || lower.contains("display")) {
			return ConfigScreen.ConfigType.CLIENT;
		}
		if (lower.contains("server") || lower.contains("gameplay") || lower.contains("world")
				|| lower.contains("diffic") || lower.contains("spawn") || lower.contains("rule")
				|| lower.contains("dimension") || lower.contains("mechanic")) {
			return ConfigScreen.ConfigType.SERVER;
		}
		return ConfigScreen.ConfigType.COMMON;
	}

	@Override
	protected void init() {
		super.init();
		loadCategories();

		int windowX = getWindowX();
		int windowY = getWindowY();
		int windowWidth = getWindowWidth();

		// Top search box
		int searchWidth = 140;
		int searchHeight = 20;
		int searchX = windowX + windowWidth - searchWidth - 140;
		int searchY = windowY + 12;

		searchBox = new EditBox(font, searchX, searchY, searchWidth, searchHeight, Component.literal("Search"));
		searchBox.setHint(Component.literal("Search settings..."));
		searchBox.setValue(searchQuery);
		searchBox.setResponder(text -> {
			this.searchQuery = text;
			this.scrollOffset = 0;
		});
		searchBox.setTextColor(0xFFE2E8F0);
		addRenderableWidget(searchBox);
	}

	private void loadCategories() {
		categories.clear();

		// Collect from custom categories map
		if (!customCategories.isEmpty()) {
			for (Map.Entry<String, ConfigBase> entry : customCategories.entrySet()) {
				addCategoryFromConfig(entry.getKey(), entry.getValue());
			}
		}

		// Collect from client, common, server configs
		if (clientConfig != null) {
			extractFromSection("Client", clientConfig);
		}
		if (commonConfig != null) {
			extractFromSection("Common", commonConfig);
		}
		if (serverConfig != null) {
			extractFromSection("Server", serverConfig);
		}

		if (selectedCategoryIndex >= categories.size()) {
			selectedCategoryIndex = Math.max(0, categories.size() - 1);
		}
	}

	private void extractFromSection(String fallbackName, ConfigBase config) {
		if (config.getChildren() != null && !config.getChildren().isEmpty()) {
			for (ConfigBase child : config.getChildren()) {
				addCategoryFromConfig(child.getName(), child);
			}
		}
		if (config.getAllValues() != null && !config.getAllValues().isEmpty()) {
			addCategoryFromConfig(fallbackName, config);
		}
	}

	private void addCategoryFromConfig(String name, ConfigBase config) {
		String cleanName = toHumanReadable(name);
		ItemStack icon = resolveCategoryIcon(cleanName);
		String description = resolveCategoryDescription(cleanName);

		List<SettingEntry> entries = new ArrayList<>();
		if (config.getAllValues() != null) {
			for (ConfigBase.CValue<?, ?> val : config.getAllValues()) {
				if (val instanceof ConfigBase.ConfigGroup)
					continue;

				SettingEntry entry = createSettingEntry(val, config);
				if (entry != null) {
					entries.add(entry);
				}
			}
		}

		categories.add(new CategoryData(cleanName, description, icon, config, entries));
	}

	@SuppressWarnings("unchecked")
	private SettingEntry createSettingEntry(ConfigBase.CValue<?, ?> val, ConfigBase ownerConfig) {
		String label = toHumanReadable(val.getName());
		String desc = val.getCommentString();
		ItemStack icon = resolveSettingIcon(val.getName());

		Pair<String, Map<String, String>> meta = ConfigHelper.readMetadataFromComment(
				new ArrayList<>(Arrays.asList(val.getComments() != null ? val.getComments() : new String[0]))
		);

		if (val instanceof ConfigBase.ConfigBool boolVal) {
			return new BoolSetting(label, desc, icon, boolVal, ownerConfig);
		} else if (val instanceof ConfigBase.ConfigInt intVal) {
			if (meta.getSecond().containsKey("ColorField")) {
				return new ColorSetting(label, desc, icon, intVal, ownerConfig);
			}
			return new IntSliderSetting(label, desc, icon, intVal, ownerConfig);
		} else if (val instanceof ConfigBase.ConfigFloat floatVal) {
			return new FloatSliderSetting(label, desc, icon, floatVal, ownerConfig);
		} else if (val instanceof ConfigBase.ConfigEnum<?> enumVal) {
			return new EnumSetting(label, desc, icon, (ConfigBase.ConfigEnum) enumVal, ownerConfig);
		} else if (val instanceof ConfigBase.ConfigString stringVal) {
			if (meta.getSecond().containsKey("Dropdown")) {
				List<String> options = Arrays.asList(meta.getSecond().get("Dropdown").split(","));
				return new DropdownSetting(label, desc, icon, stringVal, options, ownerConfig);
			}
			return new TextSetting(label, desc, icon, stringVal, ownerConfig);
		}
		return null;
	}

	protected int getWindowX() {
		return Math.max(16, (width - 760) / 2);
	}

	protected int getWindowY() {
		return Math.max(16, (height - 440) / 2);
	}

	protected int getWindowWidth() {
		return Math.min(width - 32, 760);
	}

	protected int getWindowHeight() {
		return Math.min(height - 32, 440);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(graphics, mouseX, mouseY, partialTicks);

		// Dark atmospheric vignette over background
		graphics.fill(0, 0, width, height, 0x99_0A0E17);

		int windowX = getWindowX();
		int windowY = getWindowY();
		int windowWidth = getWindowWidth();
		int windowHeight = getWindowHeight();

		// Outer Container Frame
		fillRoundedRect(graphics, windowX, windowY, windowWidth, windowHeight, 8, 0xEE_0F172A, 0xFF_1E293B);

		// Render Top Bar
		renderTopBar(graphics, windowX, windowY, windowWidth, mouseX, mouseY);

		// Main Content Area bounds
		int contentY = windowY + 48;
		int contentHeight = windowHeight - 56;
		int sidebarWidth = Math.min(180, windowWidth / 4);

		// Render Left Sidebar (Categories)
		renderSidebar(graphics, windowX + 8, contentY, sidebarWidth, contentHeight, mouseX, mouseY);

		// Render Right Panel (Settings)
		int panelX = windowX + sidebarWidth + 16;
		int panelWidth = windowWidth - sidebarWidth - 24;
		renderRightPanel(graphics, panelX, contentY, panelWidth, contentHeight, mouseX, mouseY);

		super.render(graphics, mouseX, mouseY, partialTicks);
	}

	private void renderTopBar(GuiGraphics graphics, int x, int y, int width, int mouseX, int mouseY) {
		// Header Icon (Grass Block / Mod Item)
		graphics.renderFakeItem(new ItemStack(Items.GRASS_BLOCK), x + 12, y + 14);

		// Mod Title: White bold mod name + Vibrant Cyan CONFIGURATION
		String upperMod = modName.replace("_", " ").toUpperCase(Locale.ROOT);
		graphics.drawString(font, upperMod, x + 36, y + 12, 0xFFFFFFFF, true);
		int modWidth = font.width(upperMod);
		graphics.drawString(font, " CONFIGURATION", x + 36 + modWidth, y + 12, 0xFF38BDF8, true);

		// Subtitle
		graphics.drawString(font, "Customize your world, your way", x + 36, y + 25, 0xFF94A3B8, false);

		// Top Actions: Reset to Default Button & Close Button
		int closeX = x + width - 36;
		int closeY = y + 12;
		boolean closeHovered = mouseX >= closeX && mouseX <= closeX + 24 && mouseY >= closeY && mouseY <= closeY + 20;
		fillRoundedRect(graphics, closeX, closeY, 24, 20, 4, closeHovered ? 0xFF334155 : 0xFF1E293B, closeHovered ? 0xFF94A3B8 : 0xFF334155);
		graphics.drawCenteredString(font, "✕", closeX + 12, closeY + 6, closeHovered ? 0xFFFFFFFF : 0xFFCBD5E1);

		int resetWidth = 100;
		int resetX = closeX - resetWidth - 8;
		int resetY = y + 12;
		boolean resetHovered = mouseX >= resetX && mouseX <= resetX + resetWidth && mouseY >= resetY && mouseY <= resetY + 20;
		fillRoundedRect(graphics, resetX, resetY, resetWidth, 20, 4, resetHovered ? 0xFF334155 : 0xFF1E293B, resetHovered ? 0xFF38BDF8 : 0xFF334155);
		graphics.drawCenteredString(font, "⟳ Reset to Default", resetX + resetWidth / 2, resetY + 6, resetHovered ? 0xFF38BDF8 : 0xFFCBD5E1);
	}

	private void renderSidebar(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY) {
		// Sidebar background card
		fillRoundedRect(graphics, x, y, width, height, 6, 0x88_111827, 0x40_1E293B);

		int itemY = y + 6;
		int itemHeight = 36;

		for (int i = 0; i < categories.size(); i++) {
			CategoryData cat = categories.get(i);
			boolean isSelected = i == selectedCategoryIndex;
			boolean isHovered = mouseX >= x + 4 && mouseX <= x + width - 4 && mouseY >= itemY && mouseY <= itemY + itemHeight;

			// Category card background
			if (isSelected) {
				fillRoundedRect(graphics, x + 4, itemY, width - 8, itemHeight, 6, 0xDD_0C4A6E, 0xFF_38BDF8);
			} else if (isHovered) {
				fillRoundedRect(graphics, x + 4, itemY, width - 8, itemHeight, 6, 0x99_1E293B, 0x88_475569);
			} else {
				fillRoundedRect(graphics, x + 4, itemY, width - 8, itemHeight, 6, 0x55_1E293B, 0x22_334155);
			}

			// Category icon
			graphics.renderFakeItem(cat.icon, x + 8, itemY + 10);

			// Category title
			graphics.drawString(font, cat.name, x + 30, itemY + 8, isSelected ? 0xFFFFFFFF : 0xFFE2E8F0, false);

			// Category description subtitle
			String sub = font.substrByWidth(Component.literal(cat.description), width - 48).getString();
			graphics.drawString(font, sub, x + 30, itemY + 20, isSelected ? 0xFFBAE6FD : 0xFF64748B, false);

			// Right chevron arrow if selected
			if (isSelected) {
				graphics.drawString(font, ">", x + width - 18, itemY + 14, 0xFF38BDF8, false);
			}

			itemY += itemHeight + 4;
		}
	}

	private void renderRightPanel(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY) {
		// Content panel container
		fillRoundedRect(graphics, x, y, width, height, 6, 0x99_0F172A, 0x60_1E293B);

		if (selectedCategoryIndex < 0 || selectedCategoryIndex >= categories.size())
			return;

		CategoryData cat = categories.get(selectedCategoryIndex);

		// Category Header Card with landscape aesthetic gradient
		int headerHeight = 44;
		fillRoundedRect(graphics, x + 4, y + 4, width - 8, headerHeight, 6, 0xAA_1E293B, 0x40_334155);
		fillGradientH(graphics, x + width - 120, y + 5, x + width - 5, y + 5 + headerHeight - 2, 0x00_0284C7, 0x44_0284C7);

		graphics.drawString(font, cat.name.toUpperCase(Locale.ROOT) + " SETTINGS", x + 14, y + 12, 0xFFFFFFFF, true);
		graphics.drawString(font, cat.description, x + 14, y + 26, 0xFF94A3B8, false);

		// Settings List area
		int listX = x + 4;
		int listY = y + headerHeight + 8;
		int listWidth = width - 8;
		int listHeight = height - headerHeight - 16;

		List<SettingEntry> activeEntries = getFilteredEntries(cat);

		int totalContentHeight = activeEntries.size() * 42;
		maxScroll = Math.max(0, totalContentHeight - listHeight);
		scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);

		// Scissor settings list
		graphics.enableScissor(listX, listY, listX + listWidth, listY + listHeight);

		int entryY = listY - (int) scrollOffset;
		for (SettingEntry entry : activeEntries) {
			if (entryY + 40 >= listY && entryY <= listY + listHeight) {
				entry.render(graphics, listX, entryY, listWidth - 10, 38, mouseX, mouseY);
			}
			entryY += 42;
		}

		graphics.disableScissor();

		// Scrollbar
		if (maxScroll > 0) {
			int scrollbarX = listX + listWidth - 6;
			int scrollbarHeight = Math.max(20, (int) ((float) listHeight * listHeight / totalContentHeight));
			int scrollbarY = listY + (int) ((listHeight - scrollbarHeight) * (scrollOffset / maxScroll));

			graphics.fill(scrollbarX, listY, scrollbarX + 4, listY + listHeight, 0x33_000000);
			graphics.fill(scrollbarX, scrollbarY, scrollbarX + 4, scrollbarY + scrollbarHeight, 0xFF_38BDF8);
		}
	}

	private List<SettingEntry> getFilteredEntries(CategoryData cat) {
		if (searchQuery == null || searchQuery.trim().isEmpty()) {
			return cat.entries;
		}
		String q = searchQuery.toLowerCase(Locale.ROOT);
		List<SettingEntry> filtered = new ArrayList<>();
		for (SettingEntry e : cat.entries) {
			if (e.label.toLowerCase(Locale.ROOT).contains(q) || e.description.toLowerCase(Locale.ROOT).contains(q)) {
				filtered.add(e);
			}
		}
		return filtered;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button))
			return true;

		int windowX = getWindowX();
		int windowY = getWindowY();
		int windowWidth = getWindowWidth();
		int windowHeight = getWindowHeight();

		// Close button
		int closeX = windowX + windowWidth - 36;
		int closeY = windowY + 12;
		if (mouseX >= closeX && mouseX <= closeX + 24 && mouseY >= closeY && mouseY <= closeY + 20) {
			this.onClose();
			return true;
		}

		// Reset to default button
		int resetWidth = 100;
		int resetX = closeX - resetWidth - 8;
		int resetY = windowY + 12;
		if (mouseX >= resetX && mouseX <= resetX + resetWidth && mouseY >= resetY && mouseY <= resetY + 20) {
			resetCurrentCategory();
			return true;
		}

		// Sidebar Category selection
		int contentY = windowY + 48;
		int contentHeight = windowHeight - 56;
		int sidebarWidth = Math.min(180, windowWidth / 4);
		int sideX = windowX + 8;

		if (mouseX >= sideX && mouseX <= sideX + sidebarWidth && mouseY >= contentY && mouseY <= contentY + contentHeight) {
			int clickIndex = (int) ((mouseY - contentY - 6) / 40);
			if (clickIndex >= 0 && clickIndex < categories.size()) {
				selectedCategoryIndex = clickIndex;
				scrollOffset = 0;
				return true;
			}
		}

		// Right Panel Settings click
		int panelX = windowX + sidebarWidth + 16;
		int panelWidth = windowWidth - sidebarWidth - 24;
		int listX = panelX + 4;
		int listY = contentY + 52;
		int listWidth = panelWidth - 8;
		int listHeight = contentHeight - 60;

		if (mouseX >= listX && mouseX <= listX + listWidth && mouseY >= listY && mouseY <= listY + listHeight) {
			if (selectedCategoryIndex >= 0 && selectedCategoryIndex < categories.size()) {
				CategoryData cat = categories.get(selectedCategoryIndex);
				List<SettingEntry> activeEntries = getFilteredEntries(cat);
				int entryY = listY - (int) scrollOffset;

				for (SettingEntry entry : activeEntries) {
					if (mouseY >= entryY && mouseY <= entryY + 38) {
						if (entry.mouseClicked(mouseX, mouseY, button, listX, entryY, listWidth - 10, 38)) {
							activeSlider = entry;
							return true;
						}
					}
					entryY += 42;
				}
			}
		}

		return false;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		activeSlider = null;
		isDraggingScrollbar = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (activeSlider != null) {
			activeSlider.mouseDragged(mouseX, mouseY, button);
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (maxScroll > 0) {
			scrollOffset = Mth.clamp(scrollOffset - scrollY * 20, 0, maxScroll);
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers))
			return true;
		if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
			this.onClose();
			return true;
		}
		return false;
	}

	@Override
	public void onClose() {
		saveAll();
		ScreenOpener.open(parent);
	}

	private void resetCurrentCategory() {
		if (selectedCategoryIndex >= 0 && selectedCategoryIndex < categories.size()) {
			CategoryData cat = categories.get(selectedCategoryIndex);
			for (SettingEntry entry : cat.entries) {
				entry.resetToDefault();
			}
			cat.config.save();
			cat.config.onReload();
		}
	}

	private void saveAll() {
		for (CategoryData cat : categories) {
			cat.config.save();
			cat.config.onReload();
		}
	}

	// ==========================================
	// DRAWING UTILITIES
	// ==========================================
	public static void fillRoundedRect(GuiGraphics graphics, int x, int y, int width, int height, int radius, int bgColor, int borderColor) {
		// Background
		graphics.fill(x + radius, y, x + width - radius, y + height, bgColor);
		graphics.fill(x, y + radius, x + radius, y + height - radius, bgColor);
		graphics.fill(x + width - radius, y + radius, x + width, y + height - radius, bgColor);

		// Border outline
		graphics.fill(x + radius, y, x + width - radius, y + 1, borderColor);
		graphics.fill(x + radius, y + height - 1, x + width - radius, y + height, borderColor);
		graphics.fill(x, y + radius, x + 1, y + height - radius, borderColor);
		graphics.fill(x + width - 1, y + radius, x + width, y + height - radius, borderColor);
	}

	public static void fillGradientH(GuiGraphics graphics, int x0, int y0, int x1, int y1, int colorFrom, int colorTo) {
		graphics.fillGradient(x0, y0, x1, y1, colorFrom, colorTo);
	}

	// ==========================================
	// ICON & LABEL RESOLUTION
	// ==========================================
	public static ItemStack resolveCategoryIcon(String name) {
		String lower = name.toLowerCase(Locale.ROOT);
		if (lower.contains("general")) return new ItemStack(Items.COMPASS);
		if (lower.contains("machine")) return new ItemStack(Items.PISTON);
		if (lower.contains("energy") || lower.contains("power")) return new ItemStack(Items.REDSTONE_TORCH);
		if (lower.contains("world") || lower.contains("generation")) return new ItemStack(Items.GRASS_BLOCK);
		if (lower.contains("item")) return new ItemStack(Items.CHEST);
		if (lower.contains("entit") || lower.contains("mob")) return new ItemStack(Items.PLAYER_HEAD);
		if (lower.contains("client") || lower.contains("color") || lower.contains("visual")) return new ItemStack(Items.PAINTING);
		if (lower.contains("audio") || lower.contains("sound")) return new ItemStack(Items.JUKEBOX);
		if (lower.contains("hud") || lower.contains("gui")) return new ItemStack(Items.FILLED_MAP);
		if (lower.contains("chat")) return new ItemStack(Items.WRITABLE_BOOK);
		if (lower.contains("advanced") || lower.contains("misc")) return new ItemStack(Items.ANVIL);
		if (lower.contains("gameplay") || lower.contains("rule")) return new ItemStack(Items.GOLDEN_SWORD);
		return new ItemStack(Items.BOOK);
	}

	public static String resolveCategoryDescription(String name) {
		String lower = name.toLowerCase(Locale.ROOT);
		if (lower.contains("general")) return "Main mod settings";
		if (lower.contains("machine")) return "Machine behavior";
		if (lower.contains("energy")) return "Power & generation";
		if (lower.contains("world")) return "Structures & world";
		if (lower.contains("item")) return "Item tweaks & balance";
		if (lower.contains("entit")) return "Mobs, villagers & AI";
		if (lower.contains("client")) return "Visuals & client-side";
		if (lower.contains("color")) return "Colors & menu themes";
		if (lower.contains("audio")) return "Sound & volume controls";
		if (lower.contains("hud")) return "On-screen HUD & display";
		if (lower.contains("chat")) return "Chat formatting & filters";
		if (lower.contains("gameplay")) return "Difficulty & mechanics";
		return "Configuration settings";
	}

	public static ItemStack resolveSettingIcon(String name) {
		String lower = name.toLowerCase(Locale.ROOT);
		if (lower.contains("feature") || lower.contains("enable")) return new ItemStack(Items.CYAN_CONCRETE);
		if (lower.contains("efficiency") || lower.contains("speed")) return new ItemStack(Items.REPEATER);
		if (lower.contains("power") || lower.contains("energy")) return new ItemStack(Items.BLAZE_ROD);
		if (lower.contains("structure") || lower.contains("spawn")) return new ItemStack(Items.DIAMOND_PICKAXE);
		if (lower.contains("biome")) return new ItemStack(Items.FILLED_MAP);
		if (lower.contains("tick") || lower.contains("time") || lower.contains("interval")) return new ItemStack(Items.CLOCK);
		if (lower.contains("particle") || lower.contains("shadow") || lower.contains("render")) return new ItemStack(Items.ENDER_EYE);
		if (lower.contains("player")) return new ItemStack(Items.PLAYER_HEAD);
		if (lower.contains("sound") || lower.contains("volume")) return new ItemStack(Items.NOTE_BLOCK);
		if (lower.contains("color")) return new ItemStack(Items.CYAN_DYE);
		return new ItemStack(Items.PAPER);
	}

	public static String toHumanReadable(String key) {
		if (key == null) return "";
		String s = key.replaceAll("_", " ");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (i > 0 && Character.isUpperCase(c) && !Character.isUpperCase(s.charAt(i - 1))) {
				sb.append(' ');
			}
			sb.append(c);
		}
		String result = sb.toString().trim();
		if (!result.isEmpty()) {
			result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
		}
		return result;
	}

	// ==========================================
	// DATA STRUCTURES & SETTING CONTROLS
	// ==========================================
	public static class CategoryData {
		public final String name;
		public final String description;
		public final ItemStack icon;
		public final ConfigBase config;
		public final List<SettingEntry> entries;

		public CategoryData(String name, String description, ItemStack icon, ConfigBase config, List<SettingEntry> entries) {
			this.name = name;
			this.description = description;
			this.icon = icon;
			this.config = config;
			this.entries = entries;
		}
	}

	public static abstract class SettingEntry {
		public final String label;
		public final String description;
		public final ItemStack icon;
		public final ConfigBase owner;

		public SettingEntry(String label, String description, ItemStack icon, ConfigBase owner) {
			this.label = label;
			this.description = description != null ? description : "";
			this.icon = icon;
			this.owner = owner;
		}

		public abstract void render(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY);
		public abstract boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height);
		public void mouseDragged(double mouseX, double mouseY, int button) {}
		public abstract void resetToDefault();

		protected void renderBaseCard(GuiGraphics graphics, int x, int y, int width, int height, boolean isHovered) {
			fillRoundedRect(graphics, x, y, width, height, 6, isHovered ? 0x99_1E293B : 0x66_1E293B, isHovered ? 0x55_475569 : 0x22_334155);
			graphics.renderFakeItem(icon, x + 8, y + 10);

			Font font = Minecraft.getInstance().font;
			graphics.drawString(font, label, x + 30, y + 8, 0xFFF1F5F9, false);

			String desc = description;
			if (desc.startsWith("[")) {
				desc = "";
			}
			if (desc.isEmpty()) {
				desc = "Setting configuration";
			}
			String trimmedDesc = font.substrByWidth(Component.literal(desc), width - 180).getString();
			graphics.drawString(font, trimmedDesc, x + 30, y + 20, 0xFF94A3B8, false);
		}
	}

	// 1. BOOLEAN TOGGLE SWITCH
	public static class BoolSetting extends SettingEntry {
		private final ConfigBase.ConfigBool value;

		public BoolSetting(String label, String description, ItemStack icon, ConfigBase.ConfigBool value, ConfigBase owner) {
			super(label, description, icon, owner);
			this.value = value;
		}

		@Override
		public void render(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY) {
			boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
			renderBaseCard(graphics, x, y, width, height, isHovered);

			// Pill Toggle Switch
			int switchW = 34;
			int switchH = 18;
			int switchX = x + width - switchW - 12;
			int switchY = y + 10;
			boolean val = value.get();

			int trackColor = val ? 0xFF_00D2FF : 0xFF_334155;
			int borderColor = val ? 0xFF_38BDF8 : 0xFF_475569;
			fillRoundedRect(graphics, switchX, switchY, switchW, switchH, 9, trackColor, borderColor);

			// White Knob
			int knobX = val ? switchX + switchW - 16 : switchX + 2;
			int knobY = switchY + 2;
			fillRoundedRect(graphics, knobX, knobY, 14, 14, 7, 0xFF_FFFFFF, 0xFF_CBD5E1);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height) {
			int switchW = 34;
			int switchX = x + width - switchW - 12;
			int switchY = y + 10;
			if (mouseX >= switchX - 10 && mouseX <= switchX + switchW + 10 && mouseY >= switchY - 4 && mouseY <= switchY + 22) {
				value.set(!value.get());
				if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
				return true;
			}
			return false;
		}

		@Override
		public void resetToDefault() {
			value.set(value.getDefault());
			if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
		}
	}

	// 2. INTEGER SLIDER
	public static class IntSliderSetting extends SettingEntry {
		private final ConfigBase.ConfigInt value;
		private int lastTrackX, lastTrackWidth;

		public IntSliderSetting(String label, String description, ItemStack icon, ConfigBase.ConfigInt value, ConfigBase owner) {
			super(label, description, icon, owner);
			this.value = value;
		}

		@Override
		public void render(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY) {
			boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
			renderBaseCard(graphics, x, y, width, height, isHovered);

			int boxW = 44;
			int boxH = 20;
			int boxX = x + width - boxW - 12;
			int boxY = y + 9;

			// Value Badge Box
			fillRoundedRect(graphics, boxX, boxY, boxW, boxH, 4, 0xFF_0F172A, 0xFF_334155);
			Font font = Minecraft.getInstance().font;
			graphics.drawCenteredString(font, String.valueOf(value.get()), boxX + boxW / 2, boxY + 6, 0xFFFFFFFF);

			// Slider Track
			int trackW = 90;
			int trackH = 4;
			int trackX = boxX - trackW - 12;
			int trackY = y + 17;
			lastTrackX = trackX;
			lastTrackWidth = trackW;

			int min = value.getMin() != Integer.MIN_VALUE ? value.getMin() : 0;
			int max = value.getMax() != Integer.MAX_VALUE ? value.getMax() : 100;
			float progress = Mth.clamp((float) (value.get() - min) / (float) (max - min), 0f, 1f);

			// Background track
			graphics.fill(trackX, trackY, trackX + trackW, trackY + trackH, 0xFF_334155);
			// Filled progress track
			graphics.fill(trackX, trackY, trackX + (int) (trackW * progress), trackY + trackH, 0xFF_00D2FF);

			// Thumb circle
			int thumbX = trackX + (int) (trackW * progress);
			int thumbY = trackY + 2;
			fillRoundedRect(graphics, thumbX - 5, thumbY - 5, 10, 10, 5, 0xFF_FFFFFF, 0xFF_38BDF8);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height) {
			int trackX = lastTrackX;
			int trackW = lastTrackWidth;
			if (mouseX >= trackX - 5 && mouseX <= trackX + trackW + 5 && mouseY >= y + 8 && mouseY <= y + 28) {
				updateValueFromMouse(mouseX);
				return true;
			}
			return false;
		}

		@Override
		public void mouseDragged(double mouseX, double mouseY, int button) {
			updateValueFromMouse(mouseX);
		}

		private void updateValueFromMouse(double mouseX) {
			int min = value.getMin() != Integer.MIN_VALUE ? value.getMin() : 0;
			int max = value.getMax() != Integer.MAX_VALUE ? value.getMax() : 100;
			float progress = Mth.clamp((float) (mouseX - lastTrackX) / (float) lastTrackWidth, 0f, 1f);
			int newVal = Math.round(min + progress * (max - min));
			value.set(newVal);
			if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
		}

		@Override
		public void resetToDefault() {
			value.set(value.getDefault());
			if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
		}
	}

	// 3. FLOAT SLIDER
	public static class FloatSliderSetting extends SettingEntry {
		private final ConfigBase.ConfigFloat value;
		private int lastTrackX, lastTrackWidth;

		public FloatSliderSetting(String label, String description, ItemStack icon, ConfigBase.ConfigFloat value, ConfigBase owner) {
			super(label, description, icon, owner);
			this.value = value;
		}

		@Override
		public void render(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY) {
			boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
			renderBaseCard(graphics, x, y, width, height, isHovered);

			int boxW = 44;
			int boxH = 20;
			int boxX = x + width - boxW - 12;
			int boxY = y + 9;

			fillRoundedRect(graphics, boxX, boxY, boxW, boxH, 4, 0xFF_0F172A, 0xFF_334155);
			Font font = Minecraft.getInstance().font;
			String formatted = String.format(Locale.ROOT, "%.1f", value.get());
			graphics.drawCenteredString(font, formatted, boxX + boxW / 2, boxY + 6, 0xFFFFFFFF);

			int trackW = 90;
			int trackH = 4;
			int trackX = boxX - trackW - 12;
			int trackY = y + 17;
			lastTrackX = trackX;
			lastTrackWidth = trackW;

			float min = value.getMin() != -Float.MAX_VALUE ? value.getMin() : 0f;
			float max = value.getMax() != Float.MAX_VALUE ? value.getMax() : 1f;
			float progress = Mth.clamp((value.get() - min) / (max - min), 0f, 1f);

			graphics.fill(trackX, trackY, trackX + trackW, trackY + trackH, 0xFF_334155);
			graphics.fill(trackX, trackY, trackX + (int) (trackW * progress), trackY + trackH, 0xFF_00D2FF);

			int thumbX = trackX + (int) (trackW * progress);
			int thumbY = trackY + 2;
			fillRoundedRect(graphics, thumbX - 5, thumbY - 5, 10, 10, 5, 0xFF_FFFFFF, 0xFF_38BDF8);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height) {
			int trackX = lastTrackX;
			int trackW = lastTrackWidth;
			if (mouseX >= trackX - 5 && mouseX <= trackX + trackW + 5 && mouseY >= y + 8 && mouseY <= y + 28) {
				updateValueFromMouse(mouseX);
				return true;
			}
			return false;
		}

		@Override
		public void mouseDragged(double mouseX, double mouseY, int button) {
			updateValueFromMouse(mouseX);
		}

		private void updateValueFromMouse(double mouseX) {
			float min = value.getMin() != -Float.MAX_VALUE ? value.getMin() : 0f;
			float max = value.getMax() != Float.MAX_VALUE ? value.getMax() : 1f;
			float progress = Mth.clamp((float) (mouseX - lastTrackX) / (float) lastTrackWidth, 0f, 1f);
			float newVal = min + progress * (max - min);
			value.set(newVal);
			if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
		}

		@Override
		public void resetToDefault() {
			value.set(value.getDefault());
			if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
		}
	}

	// 4. DROPDOWN (CYCLE SELECTOR)
	public static class DropdownSetting extends SettingEntry {
		private final ConfigBase.ConfigString value;
		private final List<String> options;

		public DropdownSetting(String label, String description, ItemStack icon, ConfigBase.ConfigString value, List<String> options, ConfigBase owner) {
			super(label, description, icon, owner);
			this.value = value;
			this.options = options;
		}

		@Override
		public void render(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY) {
			boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
			renderBaseCard(graphics, x, y, width, height, isHovered);

			int dropW = 120;
			int dropH = 20;
			int dropX = x + width - dropW - 12;
			int dropY = y + 9;

			boolean dropHovered = mouseX >= dropX && mouseX <= dropX + dropW && mouseY >= dropY && mouseY <= dropY + dropH;
			fillRoundedRect(graphics, dropX, dropY, dropW, dropH, 4, 0xFF_0F172A, dropHovered ? 0xFF_38BDF8 : 0xFF_334155);

			Font font = Minecraft.getInstance().font;
			String text = font.substrByWidth(Component.literal(value.get()), dropW - 24).getString();
			graphics.drawString(font, text, dropX + 8, dropY + 6, 0xFFFFFFFF, false);
			graphics.drawString(font, "▼", dropX + dropW - 14, dropY + 6, 0xFF94A3B8, false);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height) {
			int dropW = 120;
			int dropX = x + width - dropW - 12;
			int dropY = y + 9;
			if (mouseX >= dropX && mouseX <= dropX + dropW && mouseY >= dropY && mouseY <= dropY + 20) {
				if (options != null && !options.isEmpty()) {
					int idx = options.indexOf(value.get());
					int next = (idx + 1) % options.size();
					value.set(options.get(next));
					if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
				}
				return true;
			}
			return false;
		}

		@Override
		public void resetToDefault() {
			value.set(value.getDefault());
			if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
		}
	}

	// 5. ENUM SETTING
	public static class EnumSetting<E extends Enum<E>> extends SettingEntry {
		private final ConfigBase.ConfigEnum<E> value;

		public EnumSetting(String label, String description, ItemStack icon, ConfigBase.ConfigEnum<E> value, ConfigBase owner) {
			super(label, description, icon, owner);
			this.value = value;
		}

		@Override
		public void render(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY) {
			boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
			renderBaseCard(graphics, x, y, width, height, isHovered);

			int dropW = 120;
			int dropH = 20;
			int dropX = x + width - dropW - 12;
			int dropY = y + 9;

			boolean dropHovered = mouseX >= dropX && mouseX <= dropX + dropW && mouseY >= dropY && mouseY <= dropY + dropH;
			fillRoundedRect(graphics, dropX, dropY, dropW, dropH, 4, 0xFF_0F172A, dropHovered ? 0xFF_38BDF8 : 0xFF_334155);

			Font font = Minecraft.getInstance().font;
			String text = toHumanReadable(value.get().name());
			text = font.substrByWidth(Component.literal(text), dropW - 24).getString();
			graphics.drawString(font, text, dropX + 8, dropY + 6, 0xFFFFFFFF, false);
			graphics.drawString(font, "▼", dropX + dropW - 14, dropY + 6, 0xFF94A3B8, false);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height) {
			int dropW = 120;
			int dropX = x + width - dropW - 12;
			int dropY = y + 9;
			if (mouseX >= dropX && mouseX <= dropX + dropW && mouseY >= dropY && mouseY <= dropY + 20) {
				E[] constants = value.get().getDeclaringClass().getEnumConstants();
				int next = (value.get().ordinal() + 1) % constants.length;
				value.set(constants[next]);
				if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
				return true;
			}
			return false;
		}

		@Override
		public void resetToDefault() {
			value.set(value.getDefault());
			if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
		}
	}

	// 6. COLOR SETTING
	public static class ColorSetting extends SettingEntry {
		private final ConfigBase.ConfigInt value;

		public ColorSetting(String label, String description, ItemStack icon, ConfigBase.ConfigInt value, ConfigBase owner) {
			super(label, description, icon, owner);
			this.value = value;
		}

		@Override
		public void render(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY) {
			boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
			renderBaseCard(graphics, x, y, width, height, isHovered);

			int boxW = 80;
			int boxH = 20;
			int boxX = x + width - boxW - 12;
			int boxY = y + 9;

			fillRoundedRect(graphics, boxX, boxY, boxW, boxH, 4, 0xFF_0F172A, 0xFF_334155);

			// Color Swatch
			int colorVal = value.get();
			int swatchX = boxX + 4;
			int swatchY = boxY + 3;
			graphics.fill(swatchX, swatchY, swatchX + 14, swatchY + 14, colorVal | 0xFF000000);

			Font font = Minecraft.getInstance().font;
			String hex = String.format("#%06X", colorVal & 0xFFFFFF);
			graphics.drawString(font, hex, boxX + 22, boxY + 6, 0xFFE2E8F0, false);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height) {
			int boxW = 80;
			int boxX = x + width - boxW - 12;
			int boxY = y + 9;
			if (mouseX >= boxX && mouseX <= boxX + boxW && mouseY >= boxY && mouseY <= boxY + 20) {
				// Cycle predefined theme colors
				int[] palette = {0xFF38BDF8, 0xFF10B981, 0xFFF59E0B, 0xFFEF4444, 0xFF8B5CF6, 0xFFEC4899, 0xFF00D2FF};
				int current = value.get() | 0xFF000000;
				int next = palette[0];
				for (int i = 0; i < palette.length; i++) {
					if (palette[i] == current) {
						next = palette[(i + 1) % palette.length];
						break;
					}
				}
				value.set(next);
				if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
				return true;
			}
			return false;
		}

		@Override
		public void resetToDefault() {
			value.set(value.getDefault());
			if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
		}
	}

	// 7. TEXT SETTING
	public static class TextSetting extends SettingEntry {
		private final ConfigBase.ConfigString value;

		public TextSetting(String label, String description, ItemStack icon, ConfigBase.ConfigString value, ConfigBase owner) {
			super(label, description, icon, owner);
			this.value = value;
		}

		@Override
		public void render(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY) {
			boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
			renderBaseCard(graphics, x, y, width, height, isHovered);

			int boxW = 100;
			int boxH = 20;
			int boxX = x + width - boxW - 12;
			int boxY = y + 9;

			fillRoundedRect(graphics, boxX, boxY, boxW, boxH, 4, 0xFF_0F172A, 0xFF_334155);
			Font font = Minecraft.getInstance().font;
			String text = font.substrByWidth(Component.literal(value.get()), boxW - 12).getString();
			graphics.drawString(font, text, boxX + 6, boxY + 6, 0xFFE2E8F0, false);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height) {
			return false;
		}

		@Override
		public void resetToDefault() {
			value.set(value.getDefault());
			if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
		}
	}
}
