package com.reggarf.mods.better_lib.configs.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.reggarf.mods.better_lib.Better_lib;
import com.reggarf.mods.better_lib.configs.ConfigBase;
import com.reggarf.mods.better_lib.configs.adapter.DynamicConfigCategory;
import com.reggarf.mods.better_lib.configs.gui.ScreenOpener;
import com.reggarf.mods.better_lib.configs.utility.Pair;
import com.reggarf.mods.better_lib.platform.Services;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

/**
 * Modern, Minecrafty configuration screen.
 * Phase 1: 1st selection page for Client, Common, and Server configurations.
 * Phase 2: Respective configuration dashboards with high-contrast categories, search, and typable inputs.
 * Visuals: Fullscreen scenic landscape backdrop & matching category header banner.
 */
public class BaseConfigScreen extends Screen {

	public static final ResourceLocation DEFAULT_BG_TEXTURE = ResourceLocation.fromNamespaceAndPath("better_lib", "textures/gui/config_background.png");
	public static final int BANNER_HEADER_HEIGHT = 48;
	protected ResourceLocation backgroundTexture = DEFAULT_BG_TEXTURE;

	public enum SectionType {
		CLIENT("Client", "Client-side options, visuals, colors, HUD, and audio.", Items.PAINTING),
		COMMON("Common", "General gameplay rules, performance, and shared features.", Items.COMPASS),
		SERVER("Server", "Multiplayer server rules, permissions, and world options.", Items.COMMAND_BLOCK);

		public final String title;
		public final String description;
		public final ItemStack icon;

		SectionType(String title, String description, net.minecraft.world.item.Item item) {
			this.title = title;
			this.description = description;
			this.icon = new ItemStack(item);
		}
	}

	public enum CurrentView {
		SELECTION,
		DASHBOARD
	}

	protected final Screen parent;
	protected String modName;

	protected ConfigBase clientConfig;
	protected ConfigBase commonConfig;
	protected ConfigBase serverConfig;
	protected final Map<String, ConfigBase> customCategories = new LinkedHashMap<>();

	// View State
	protected CurrentView currentView = CurrentView.SELECTION;
	protected SectionType currentSection = SectionType.CLIENT;

	// UI Layout State for Dashboard
	protected final List<CategoryData> categories = new ArrayList<>();
	protected int selectedCategoryIndex = 0;
	protected double scrollOffset = 0;
	protected double maxScroll = 0;
	protected double sidebarScrollOffset = 0;
	protected double maxSidebarScroll = 0;
	protected SettingEntry activeSlider = null;
	protected SettingEntry activeTypableEntry = null;

	// Search & Widgets
	protected EditBox searchBox;
	protected String searchQuery = "";

	public BaseConfigScreen(Screen parent, String modID) {
		super(Component.literal((modID != null ? modID : "better_lib") + " Configuration"));
		this.parent = parent;
		this.modName = modID != null ? modID : "better_lib";
		if (!"better_lib".equalsIgnoreCase(this.modName)) {
			Services.MODS.populateConfigs(this.modName, this);
		}
	}

	public BaseConfigScreen(Screen parent) {
		this(parent, "better_lib");
	}

	public BaseConfigScreen withBackground(ResourceLocation texture) {
		if (texture != null) {
			this.backgroundTexture = texture;
		}
		return this;
	}

	public BaseConfigScreen withConfigs(ConfigBase client, ConfigBase common, ConfigBase server) {
		this.clientConfig = client;
		this.commonConfig = common;
		this.serverConfig = server;
		this.currentView = CurrentView.SELECTION;
		return this;
	}

	public BaseConfigScreen withCategories(Map<String, ? extends ConfigBase> categories) {
		this.customCategories.clear();
		if (categories != null) {
			this.customCategories.putAll(categories);
		}
		if (clientConfig == null && commonConfig == null && serverConfig == null && !this.customCategories.isEmpty()) {
			this.currentView = CurrentView.DASHBOARD;
		}
		return this;
	}

	public BaseConfigScreen withCategory(String title, ConfigBase config) {
		this.customCategories.put(title, config);
		if (clientConfig == null && commonConfig == null && serverConfig == null) {
			this.currentView = CurrentView.DASHBOARD;
		}
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

	public static boolean isServerLocked() {
		Minecraft mc = Minecraft.getInstance();
		return mc.level != null && !mc.hasSingleplayerServer() && (mc.player == null || !mc.player.hasPermissions(2));
	}

	// ==========================================
	// SUPPRESS MINECRAFT 1.21 MENU BLUR SHADER
	// ==========================================
	@Override
	protected void renderBlurredBackground(float partialTick) {
		// Suppress Minecraft full-screen blur shader pass to ensure all elements stay crisp
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.renderTransparentBackground(graphics);
	}

	@Override
	protected void init() {
		super.init();
		loadCategories();

		int windowX = getWindowX();
		int windowY = getWindowY();
		int windowWidth = getWindowWidth();

		// Calculate responsive positions for top actions
		int closeW = 20;
		int closeX = windowX + windowWidth - closeW - 8;
		int resetW = windowWidth >= 500 ? 96 : 44;
		int resetX = closeX - resetW - 6;
		int otherW = windowWidth >= 560 ? 86 : 22;
		int otherX = resetX - otherW - 6;

		int maxSearchW = Math.min(130, Math.max(50, otherX - (windowX + 220)));
		int searchW = Math.max(50, maxSearchW);
		int searchX = otherX - searchW - 6;

		searchBox = new EditBox(font, searchX + 18, windowY + 9, searchW - 22, 16, Component.literal("Search"));
		searchBox.setBordered(false);
		searchBox.setMaxLength(40);
		searchBox.setHint(Component.literal("Search..."));
		searchBox.setValue(searchQuery);
		searchBox.setResponder(text -> {
			this.searchQuery = text;
			this.scrollOffset = 0;
		});
		searchBox.setTextColor(0xFFFFFFFF);
		searchBox.visible = (currentView == CurrentView.DASHBOARD) && (searchW >= 50) && (searchX > windowX + 160);
		addRenderableWidget(searchBox);
	}

	private void loadCategories() {
		if (currentView == CurrentView.SELECTION) {
			categories.clear();
			return;
		}
		loadCategoriesForSection(currentSection);
	}

	public void selectSection(SectionType section) {
		this.currentSection = section;
		this.currentView = CurrentView.DASHBOARD;
		this.searchQuery = "";
		if (this.searchBox != null) {
			this.searchBox.setValue("");
			this.searchBox.setFocused(false);
		}
		loadCategoriesForSection(section);
	}

	public void returnToSelection() {
		this.currentView = CurrentView.SELECTION;
		if (searchBox != null) {
			searchBox.setFocused(false);
			searchBox.visible = false;
		}
		if (activeTypableEntry != null) {
			activeTypableEntry.setFocused(false);
			activeTypableEntry = null;
		}
	}

	private void loadCategoriesForSection(SectionType section) {
		categories.clear();
		selectedCategoryIndex = 0;
		scrollOffset = 0;
		sidebarScrollOffset = 0;

		ConfigBase target = switch (section) {
			case CLIENT -> clientConfig;
			case COMMON -> commonConfig;
			case SERVER -> serverConfig;
		};

		if (target != null) {
			extractFromSection(section.title, target);
		}

		// Also check any custom categories matching this section
		for (Map.Entry<String, ConfigBase> entry : customCategories.entrySet()) {
			ConfigScreen.ConfigType classified = classifyCategory(entry.getKey());
			boolean match = (section == SectionType.CLIENT && classified == ConfigScreen.ConfigType.CLIENT)
					|| (section == SectionType.COMMON && classified == ConfigScreen.ConfigType.COMMON)
					|| (section == SectionType.SERVER && classified == ConfigScreen.ConfigType.SERVER);
			if (match) {
				addCategoryFromConfig(entry.getKey(), entry.getValue());
			}
		}

		if (selectedCategoryIndex >= categories.size()) {
			selectedCategoryIndex = Math.max(0, categories.size() - 1);
		}
	}

	public int getCategoryCount(SectionType section) {
		int count = 0;
		ConfigBase target = switch (section) {
			case CLIENT -> clientConfig;
			case COMMON -> commonConfig;
			case SERVER -> serverConfig;
		};
		if (target != null) {
			count += countCategoriesRecursively(target);
		}
		for (Map.Entry<String, ConfigBase> entry : customCategories.entrySet()) {
			ConfigScreen.ConfigType classified = classifyCategory(entry.getKey());
			boolean match = (section == SectionType.CLIENT && classified == ConfigScreen.ConfigType.CLIENT)
					|| (section == SectionType.COMMON && classified == ConfigScreen.ConfigType.COMMON)
					|| (section == SectionType.SERVER && classified == ConfigScreen.ConfigType.SERVER);
			if (match) count++;
		}
		return count;
	}

	private int countCategoriesRecursively(ConfigBase config) {
		if (config == null) return 0;
		int count = 0;
		if (config.getAllValues() != null) {
			for (ConfigBase.CValue<?, ?> v : config.getAllValues()) {
				if (!(v instanceof ConfigBase.ConfigGroup)) {
					count = 1;
					break;
				}
			}
		}
		if (config.getChildren() != null) {
			for (ConfigBase child : config.getChildren()) {
				count += countCategoriesRecursively(child);
			}
		}
		return count;
	}

	public int getSettingCount(SectionType section) {
		int count = 0;
		ConfigBase target = switch (section) {
			case CLIENT -> clientConfig;
			case COMMON -> commonConfig;
			case SERVER -> serverConfig;
		};
		if (target != null) {
			count += countSettingsRecursively(target);
		}
		for (Map.Entry<String, ConfigBase> entry : customCategories.entrySet()) {
			ConfigScreen.ConfigType classified = classifyCategory(entry.getKey());
			boolean match = (section == SectionType.CLIENT && classified == ConfigScreen.ConfigType.CLIENT)
					|| (section == SectionType.COMMON && classified == ConfigScreen.ConfigType.COMMON)
					|| (section == SectionType.SERVER && classified == ConfigScreen.ConfigType.SERVER);
			if (match && entry.getValue().getAllValues() != null) {
				count += entry.getValue().getAllValues().size();
			}
		}
		return count;
	}

	private int countSettingsRecursively(ConfigBase config) {
		if (config == null) return 0;
		int count = 0;
		if (config.getAllValues() != null) {
			for (ConfigBase.CValue<?, ?> v : config.getAllValues()) {
				if (!(v instanceof ConfigBase.ConfigGroup)) {
					count++;
				}
			}
		}
		if (config.getChildren() != null) {
			for (ConfigBase child : config.getChildren()) {
				count += countSettingsRecursively(child);
			}
		}
		return count;
	}

	private void extractFromSection(String fallbackName, ConfigBase config) {
		if (config == null) return;
		extractCategoriesRecursively(fallbackName, config);
	}

	private void extractCategoriesRecursively(String currentPath, ConfigBase config) {
		if (config == null) return;

		// If this node has direct values, register as a category
		if (config.getAllValues() != null && !config.getAllValues().isEmpty()) {
			boolean hasActualValues = false;
			for (ConfigBase.CValue<?, ?> v : config.getAllValues()) {
				if (!(v instanceof ConfigBase.ConfigGroup)) {
					hasActualValues = true;
					break;
				}
			}
			if (hasActualValues) {
				addCategoryFromConfig(currentPath, config);
			}
		}

		// Recurse into children
		if (config.getChildren() != null && !config.getChildren().isEmpty()) {
			for (ConfigBase child : config.getChildren()) {
				String childName = child.getName();
				String childPath = (currentPath == null || currentPath.isEmpty())
						? childName
						: (currentPath.equalsIgnoreCase(currentSection.title) ? childName : currentPath + " / " + childName);
				extractCategoriesRecursively(childPath, child);
			}
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
		String desc = cleanDescription(val.getCommentString());
		ItemStack icon = resolveSettingIcon(val.getName());

		Pair<String, Map<String, String>> meta = ConfigHelper.readMetadataFromComment(
				new ArrayList<>(Arrays.asList(val.getComments() != null ? val.getComments() : new String[0]))
		);

		SettingEntry entry = null;
		if (val instanceof ConfigBase.ConfigBool boolVal) {
			entry = new BoolSetting(label, desc, icon, boolVal, ownerConfig);
		} else if (val instanceof ConfigBase.ConfigInt intVal) {
			if (meta.getSecond().containsKey("ColorField")) {
				entry = new ColorSetting(label, desc, icon, intVal, ownerConfig);
			} else {
				entry = new IntSliderSetting(label, desc, icon, intVal, ownerConfig);
			}
		} else if (val instanceof ConfigBase.ConfigFloat floatVal) {
			entry = new FloatSliderSetting(label, desc, icon, floatVal, ownerConfig);
		} else if (val instanceof ConfigBase.ConfigEnum<?> enumVal) {
			entry = new EnumSetting(label, desc, icon, (ConfigBase.ConfigEnum) enumVal, ownerConfig);
		} else if (val instanceof ConfigBase.ConfigString stringVal) {
			if (meta.getSecond().containsKey("Dropdown")) {
				List<String> options = Arrays.asList(meta.getSecond().get("Dropdown").split(","));
				entry = new DropdownSetting(label, desc, icon, stringVal, options, ownerConfig);
			} else {
				entry = new TextSetting(label, desc, icon, stringVal, ownerConfig);
			}
		}

		if (entry != null && currentSection == SectionType.SERVER && isServerLocked()) {
			entry.editable = false;
		}
		return entry;
	}

	public static String cleanDescription(String raw) {
		if (raw == null) return "";
		String[] lines = raw.split("\n");
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			String trimmed = line.trim();
			if (trimmed.startsWith("[@cui:") || trimmed.startsWith("[in ") || trimmed.isEmpty()) {
				continue;
			}
			if (sb.length() > 0) sb.append(" ");
			sb.append(trimmed);
		}
		return sb.toString();
	}

	protected int getWindowX() {
		return Math.max(16, (width - 720) / 2);
	}

	protected int getWindowY() {
		return 12;
	}

	protected int getWindowWidth() {
		return width - 2 * getWindowX();
	}

	protected int getWindowHeight() {
		return height - getWindowY() - 14;
	}

	protected int getSidebarWidth() {
		int w = getWindowWidth();
		if (w >= 540) return 160;
		return Math.max(120, (int) (w * 0.28f));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(graphics, mouseX, mouseY, partialTicks);

		// 1. Full-Screen Sunset Landscape Backdrop
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		graphics.blit(backgroundTexture, 0, 0, width, height, 0.0f, 0.0f, 1672, 941, 1672, 941);

		// Soft darkening overlay for high contrast and depth
		graphics.fill(0, 0, width, height, 0x44_090D16);

		int windowX = getWindowX();
		int windowY = getWindowY();
		int windowWidth = getWindowWidth();
		int windowHeight = getWindowHeight();

		if (currentView == CurrentView.SELECTION) {
			renderSelectionScreen(graphics, windowX, windowY, windowWidth, windowHeight, mouseX, mouseY);
		} else {
			// Render Top Bar
			renderTopBar(graphics, windowX, windowY, windowWidth, mouseX, mouseY);

			// Main Content Area bounds
			int contentY = windowY + 36;
			int contentHeight = windowHeight - 40;
			int sidebarWidth = getSidebarWidth();

			// Render Left Sidebar (Categories)
			renderSidebar(graphics, windowX, contentY, sidebarWidth, contentHeight, mouseX, mouseY);

			// Render Right Panel (Settings)
			int panelX = windowX + sidebarWidth + 10;
			int panelWidth = windowWidth - sidebarWidth - 10;
			renderRightPanel(graphics, panelX, contentY, panelWidth, contentHeight, mouseX, mouseY);
		}

		super.render(graphics, mouseX, mouseY, partialTicks);
	}

	// ==========================================
	// 1ST PAGE: SELECTION SCREEN
	// ==========================================
	private void renderSelectionScreen(GuiGraphics graphics, int windowX, int windowY, int windowWidth, int windowHeight, int mouseX, int mouseY) {
		// Header Icon (Grass Block)
		graphics.renderFakeItem(new ItemStack(Items.GRASS_BLOCK), windowX + 8, windowY + 8);

		// Close button (X)
		int closeW = 20;
		int closeX = windowX + windowWidth - closeW - 8;
		int closeY = windowY + 8;
		boolean closeHovered = mouseX >= closeX && mouseX <= closeX + closeW && mouseY >= closeY && mouseY <= closeY + closeW;
		fillRoundedRect(graphics, closeX, closeY, closeW, closeW, 4, closeHovered ? 0xFF334155 : 0xFF1E293B, closeHovered ? 0xFF94A3B8 : 0xFF475569);
		graphics.drawCenteredString(font, "X", closeX + closeW / 2, closeY + 6, closeHovered ? 0xFFFFFFFF : 0xFFE2E8F0);

		// Other Mod Configs button (Like Create)
		int otherW = windowWidth >= 520 ? 92 : 20;
		int otherX = closeX - otherW - 6;
		boolean otherHovered = mouseX >= otherX && mouseX <= otherX + otherW && mouseY >= closeY && mouseY <= closeY + closeW;
		fillRoundedRect(graphics, otherX, closeY, otherW, closeW, 4, otherHovered ? 0xFF334155 : 0xFF1E293B, otherHovered ? 0xFF38BDF8 : 0xFF475569);
		if (windowWidth >= 520) {
			graphics.drawCenteredString(font, "Other Mods", otherX + otherW / 2, closeY + 6, otherHovered ? 0xFF38BDF8 : 0xFFE2E8F0);
		} else {
			graphics.drawCenteredString(font, ">", otherX + otherW / 2, closeY + 6, otherHovered ? 0xFF38BDF8 : 0xFFE2E8F0);
		}

		// Header Titles bounded safely by closeX
		int titleStartX = windowX + 28;
		int maxTitleW = Math.max(50, otherX - titleStartX - 8);
		graphics.enableScissor(titleStartX, windowY + 2, otherX - 6, windowY + 34);

		String upperMod = modName.replace("_", " ").toUpperCase(Locale.ROOT);
		String fullHeader = upperMod + " CONFIGURATION";
		if (font.width(fullHeader) <= maxTitleW) {
			graphics.drawString(font, upperMod, titleStartX, windowY + 7, 0xFFFFFFFF, true);
			int modW = font.width(upperMod);
			graphics.drawString(font, " CONFIGURATION", titleStartX + modW, windowY + 7, 0xFF38BDF8, true);
		} else {
			String trimmedHeader = font.substrByWidth(Component.literal(fullHeader), maxTitleW).getString();
			graphics.drawString(font, trimmedHeader, titleStartX, windowY + 7, 0xFF38BDF8, true);
		}

		String sub = "Select a configuration scope to configure";
		String subTrimmed = font.substrByWidth(Component.literal(sub), maxTitleW).getString();
		graphics.drawString(font, subTrimmed, titleStartX, windowY + 19, 0xFFCBD5E1, true);

		graphics.disableScissor();

		// Horizontal divider under top bar
		graphics.fill(windowX + 8, windowY + 34, windowX + windowWidth - 8, windowY + 35, 0xFF334155);

		// Center Area for 3 Selection Cards
		int contentY = windowY + 42;
		int contentHeight = windowHeight - 50;

		SectionType[] sections = { SectionType.CLIENT, SectionType.COMMON, SectionType.SERVER };
		boolean horizontal = windowWidth >= 480;

		if (horizontal) {
			int padding = 12;
			int totalGap = padding * 2;
			int cardW = (windowWidth - 24 - totalGap) / 3;
			int cardH = Math.min(220, contentHeight - 16);
			int startX = windowX + 12;
			int startY = contentY + (contentHeight - cardH) / 2;

			for (int i = 0; i < 3; i++) {
				SectionType sec = sections[i];
				int cx = startX + i * (cardW + padding);
				renderSectionCard(graphics, cx, startY, cardW, cardH, sec, mouseX, mouseY);
			}
		} else {
			int cardH = (contentHeight - 16) / 3;
			int cardW = windowWidth - 24;
			int cx = windowX + 12;
			for (int i = 0; i < 3; i++) {
				SectionType sec = sections[i];
				int cy = contentY + i * (cardH + 4);
				renderSectionCardVertical(graphics, cx, cy, cardW, cardH, sec, mouseX, mouseY);
			}
		}
	}

	private void renderSectionCard(GuiGraphics graphics, int x, int y, int width, int height, SectionType sec, int mouseX, int mouseY) {
		boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;

		// Card Background & Glowing Border on Hover
		fillRoundedRect(graphics, x, y, width, height, 6, isHovered ? 0xF0_1E293B : 0xEE_111827, isHovered ? 0xFF_38BDF8 : 0xFF_334155);

		// Icon Well (32x32)
		fillRoundedRect(graphics, x + 10, y + 10, 32, 32, 5, 0xCC_0F172A, isHovered ? 0xFF_38BDF8 : 0xFF_475569);
		graphics.renderFakeItem(sec.icon, x + 18, y + 18);

		// Pill Tag
		int tagW = font.width(sec.name()) + 8;
		fillRoundedRect(graphics, x + 48, y + 10, tagW, 12, 4, 0xEE_0369A1, 0xFF_38BDF8);
		graphics.drawString(font, sec.name(), x + 52, y + 12, 0xFFE0F2FE, true);

		// Section Title
		graphics.drawString(font, sec.title + " Settings", x + 48, y + 25, 0xFFFFFFFF, true);

		// Divider
		graphics.fill(x + 10, y + 48, x + width - 10, y + 49, 0xFF_334155);

		// Description (wrapped)
		List<FormattedCharSequence> descLines = font.split(Component.literal(sec.description), width - 20);
		int textY = y + 56;
		for (int i = 0; i < Math.min(3, descLines.size()); i++) {
			graphics.drawString(font, descLines.get(i), x + 10, textY, 0xFFCBD5E1, true);
			textY += 11;
		}

		// Categories & Settings count badge
		int cats = getCategoryCount(sec);
		int opts = getSettingCount(sec);
		String badgeText = cats > 0 ? (cats + (cats == 1 ? " Category" : " Categories") + " - " + opts + " Options") : "No Categories Configured";
		int badgeColor = cats > 0 ? 0xFF4ADE80 : 0xFF94A3B8;

		if (sec == SectionType.SERVER && Minecraft.getInstance().level == null && cats == 0) {
			graphics.drawString(font, "* Available In-World", x + 10, y + height - 38, 0xFF94A3B8, true);
		} else if (sec == SectionType.SERVER && isServerLocked()) {
			graphics.drawString(font, "[OP] Operator Only", x + 10, y + height - 38, 0xFFF87171, true);
		} else {
			graphics.drawString(font, "* " + badgeText, x + 10, y + height - 38, badgeColor, true);
		}

		// Bottom Action Button
		int btnH = 20;
		int btnY = y + height - btnH - 8;
		int btnW = width - 20;
		fillRoundedRect(graphics, x + 10, btnY, btnW, btnH, 4, isHovered ? 0xFF_0284C7 : 0xFF_1E293B, isHovered ? 0xFF_38BDF8 : 0xFF_475569);
		graphics.drawCenteredString(font, "Configure " + sec.title + " >", x + 10 + btnW / 2, btnY + 6, isHovered ? 0xFFFFFFFF : 0xFFE2E8F0);
	}

	private void renderSectionCardVertical(GuiGraphics graphics, int x, int y, int width, int height, SectionType sec, int mouseX, int mouseY) {
		boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
		fillRoundedRect(graphics, x, y, width, height, 5, isHovered ? 0xF0_1E293B : 0xEE_111827, isHovered ? 0xFF_38BDF8 : 0xFF_334155);

		graphics.renderFakeItem(sec.icon, x + 8, y + 8);
		graphics.drawString(font, sec.title + " Settings", x + 30, y + 6, 0xFFFFFFFF, true);
		String descTrimmed = font.substrByWidth(Component.literal(sec.description), width - 110).getString();
		graphics.drawString(font, descTrimmed, x + 30, y + 18, 0xFFCBD5E1, true);

		int btnW = 70;
		int btnX = x + width - btnW - 8;
		int btnY = y + 6;
		fillRoundedRect(graphics, btnX, btnY, btnW, 20, 4, isHovered ? 0xFF0284C7 : 0xFF1E293B, isHovered ? 0xFF38BDF8 : 0xFF475569);
		graphics.drawCenteredString(font, "Open >", btnX + btnW / 2, btnY + 6, isHovered ? 0xFFFFFFFF : 0xFFE2E8F0);
	}

	// ==========================================
	// 2ND PAGE: DASHBOARD (CATEGORY & SETTINGS)
	// ==========================================
	private void renderTopBar(GuiGraphics graphics, int x, int y, int width, int mouseX, int mouseY) {
		// Back button: returns to 1st Selection Page
		int backW = 48;
		int backX = x + 8;
		int backY = y + 8;
		boolean backHovered = mouseX >= backX && mouseX <= backX + backW && mouseY >= backY && mouseY <= backY + 20;
		fillRoundedRect(graphics, backX, backY, backW, 20, 4, backHovered ? 0xFF334155 : 0xFF1E293B, backHovered ? 0xFF38BDF8 : 0xFF475569);
		graphics.drawCenteredString(font, "< Back", backX + backW / 2, backY + 6, backHovered ? 0xFF38BDF8 : 0xFFE2E8F0);

		// Mod Icon
		graphics.renderFakeItem(new ItemStack(Items.GRASS_BLOCK), x + 62, y + 8);

		// Calculate available space for top buttons
		int closeW = 20;
		int closeX = x + width - closeW - 8;
		int closeY = y + 8;
		boolean closeHovered = mouseX >= closeX && mouseX <= closeX + closeW && mouseY >= closeY && mouseY <= closeY + closeW;
		fillRoundedRect(graphics, closeX, closeY, closeW, closeW, 4, closeHovered ? 0xFF334155 : 0xFF1E293B, closeHovered ? 0xFF94A3B8 : 0xFF475569);
		graphics.drawCenteredString(font, "X", closeX + closeW / 2, closeY + 6, closeHovered ? 0xFFFFFFFF : 0xFFE2E8F0);

		int resetW = width >= 500 ? 96 : 44;
		int resetX = closeX - resetW - 6;
		int resetY = y + 8;
		boolean resetHovered = mouseX >= resetX && mouseX <= resetX + resetW && mouseY >= resetY && mouseY <= resetY + closeW;
		fillRoundedRect(graphics, resetX, resetY, resetW, closeW, 4, resetHovered ? 0xFF334155 : 0xFF1E293B, resetHovered ? 0xFF38BDF8 : 0xFF475569);
		String resetLabel = resetW >= 80 ? "Reset Category" : (resetW >= 36 ? "Reset" : "R");
		graphics.drawCenteredString(font, resetLabel, resetX + resetW / 2, resetY + 6, resetHovered ? 0xFF38BDF8 : 0xFFE2E8F0);

		// Other Mod Configs button in top bar
		int otherW = width >= 540 ? 86 : 20;
		int otherX = resetX - otherW - 6;
		boolean otherHovered = mouseX >= otherX && mouseX <= otherX + otherW && mouseY >= y + 8 && mouseY <= y + 8 + closeW;
		fillRoundedRect(graphics, otherX, y + 8, otherW, closeW, 4, otherHovered ? 0xFF334155 : 0xFF1E293B, otherHovered ? 0xFF38BDF8 : 0xFF475569);
		if (width >= 540) {
			graphics.drawCenteredString(font, "Other Mods", otherX + otherW / 2, y + 14, otherHovered ? 0xFF38BDF8 : 0xFFE2E8F0);
		} else {
			graphics.drawCenteredString(font, ">", otherX + otherW / 2, y + 14, otherHovered ? 0xFF38BDF8 : 0xFFE2E8F0);
		}

		// Search Box Container
		int maxSearchW = Math.min(130, Math.max(50, otherX - (x + 220)));
		int searchW = Math.max(50, maxSearchW);
		int searchX = otherX - searchW - 6;
		if (searchW >= 50 && (searchX > x + 160)) {
			boolean searchFocused = searchBox != null && searchBox.isFocused();
			fillRoundedRect(graphics, searchX, y + 8, searchW, closeW, 4, 0xFF0F172A, searchFocused ? 0xFF38BDF8 : 0xFF475569);
			graphics.drawString(font, ">", searchX + 4, y + 14, 0xFF94A3B8, false);
			if (searchBox != null) {
				searchBox.setX(searchX + 18);
				searchBox.setY(y + 9);
				searchBox.setWidth(searchW - 22);
				searchBox.visible = true;
			}
		} else if (searchBox != null) {
			searchBox.visible = false;
		}

		// Quick Switcher Tabs (Client / Common / Server) if width permits
		int tabW = 46;
		int tabH = 18;
		int tabsTotalW = 3 * (tabW + 3);
		int tabStartX = searchX - tabsTotalW - 10;
		boolean showTabs = width >= 560 && tabStartX > x + 240;
		if (showTabs) {
			SectionType[] tabs = { SectionType.CLIENT, SectionType.COMMON, SectionType.SERVER };
			for (int i = 0; i < 3; i++) {
				SectionType t = tabs[i];
				int tx = tabStartX + i * (tabW + 3);
				boolean isCurrent = t == currentSection;
				boolean tabHovered = mouseX >= tx && mouseX <= tx + tabW && mouseY >= y + 9 && mouseY <= y + 9 + tabH;
				fillRoundedRect(graphics, tx, y + 9, tabW, tabH, 3, isCurrent ? 0xEE_0369A1 : (tabHovered ? 0xCC_334155 : 0x88_1E293B), isCurrent ? 0xFF_38BDF8 : (tabHovered ? 0xFF_94A3B8 : 0xFF_475569));
				graphics.drawCenteredString(font, t.title, tx + tabW / 2, y + 14, isCurrent ? 0xFFFFFFFF : 0xFFCBD5E1);
			}
		}

		// Title & Subtitle bounded strictly before tabs or search box to NEVER overlap
		int titleStartX = x + 82;
		int rightLimit = showTabs ? tabStartX - 8 : (searchBox != null && searchBox.visible ? searchX - 8 : resetX - 8);
		int availableTitleW = Math.max(0, rightLimit - titleStartX);

		if (availableTitleW > 40) {
			graphics.enableScissor(titleStartX, y + 2, rightLimit, y + 36);

			String upperMod = modName.replace("_", " ").toUpperCase(Locale.ROOT);
			String sectionTitle = currentSection.title.toUpperCase(Locale.ROOT) + " SETTINGS";
			String fullTitle = upperMod + " > " + sectionTitle;

			if (font.width(fullTitle) <= availableTitleW) {
				graphics.drawString(font, upperMod, titleStartX, y + 7, 0xFFFFFFFF, true);
				int modW = font.width(upperMod);
				graphics.drawString(font, " > " + sectionTitle, titleStartX + modW, y + 7, 0xFF38BDF8, true);
			} else {
				String trimmedFull = font.substrByWidth(Component.literal(fullTitle), availableTitleW).getString();
				graphics.drawString(font, trimmedFull, titleStartX, y + 7, 0xFF38BDF8, true);
			}

			// Subtitle cleanly truncated to availableTitleW so it NEVER overlaps search box or tabs
			String subDesc = (currentSection == SectionType.SERVER && isServerLocked())
					? "[OP] Server settings require operator permission (level 2)"
					: font.substrByWidth(Component.literal(currentSection.description), availableTitleW).getString();
			graphics.drawString(font, subDesc, titleStartX, y + 19, (currentSection == SectionType.SERVER && isServerLocked()) ? 0xFFF87171 : 0xFFCBD5E1, true);

			graphics.disableScissor();
		}
	}

	private void renderSidebar(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY) {
		fillRoundedRect(graphics, x, y, width, height, 6, 0xEE_0F172A, 0xFF_334155);

		if (categories.isEmpty()) {
			graphics.drawCenteredString(font, "No Categories", x + width / 2, y + 20, 0xFF94A3B8);
			return;
		}

		int itemHeight = 34;
		int itemGap = 3;
		int totalHeight = categories.size() * (itemHeight + itemGap);
		maxSidebarScroll = Math.max(0, totalHeight - height + 8);
		sidebarScrollOffset = Mth.clamp(sidebarScrollOffset, 0, maxSidebarScroll);

		graphics.enableScissor(x + 2, y + 2, x + width - 2, y + height - 2);

		int itemY = y + 4 - (int) sidebarScrollOffset;
		for (int i = 0; i < categories.size(); i++) {
			CategoryData cat = categories.get(i);
			boolean isSelected = i == selectedCategoryIndex;
			boolean isHovered = mouseX >= x + 3 && mouseX <= x + width - 3 && mouseY >= itemY && mouseY <= itemY + itemHeight;

			if (itemY + itemHeight >= y && itemY <= y + height) {
				if (isSelected) {
					fillRoundedRect(graphics, x + 3, itemY, width - 6, itemHeight, 5, 0xEE_0284C7, 0xFF_38BDF8);
				} else if (isHovered) {
					fillRoundedRect(graphics, x + 3, itemY, width - 6, itemHeight, 5, 0xCC_334155, 0xFF_64748B);
				} else {
					fillRoundedRect(graphics, x + 3, itemY, width - 6, itemHeight, 5, 0x88_1E293B, 0xFF_334155);
				}

				graphics.renderFakeItem(cat.icon, x + 6, itemY + 9);

				String title = font.substrByWidth(Component.literal(cat.name), width - 40).getString();
				graphics.drawString(font, title, x + 26, itemY + 6, 0xFFFFFFFF, true);

				String sub = font.substrByWidth(Component.literal(cat.description), width - 40).getString();
				graphics.drawString(font, sub, x + 26, itemY + 18, isSelected ? 0xFFE0F2FE : 0xFFCBD5E1, true);

				graphics.drawString(font, ">", x + width - 14, itemY + 12, isSelected ? 0xFF38BDF8 : 0xFF64748B, true);
			}

			itemY += itemHeight + itemGap;
		}

		graphics.disableScissor();
	}

	private void renderRightPanel(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY) {
		fillRoundedRect(graphics, x, y, width, height, 6, 0xEE_0F172A, 0xFF_334155);

		if (categories.isEmpty()) {
			int centerX = x + width / 2;
			int centerY = y + height / 2;
			graphics.renderFakeItem(new ItemStack(Items.BARRIER), centerX - 8, centerY - 32);
			graphics.drawCenteredString(font, "No " + currentSection.title + " Settings Found", centerX, centerY - 10, 0xFFFFFFFF);
			graphics.drawCenteredString(font, "This mod does not have any " + currentSection.title.toLowerCase(Locale.ROOT) + "-side configuration options.", centerX, centerY + 4, 0xFFCBD5E1);

			int btnW = 140;
			int btnH = 20;
			int btnX = centerX - btnW / 2;
			int btnY = centerY + 24;
			boolean btnHover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
			fillRoundedRect(graphics, btnX, btnY, btnW, btnH, 4, btnHover ? 0xFF0284C7 : 0xFF1E293B, btnHover ? 0xFF38BDF8 : 0xFF475569);
			graphics.drawCenteredString(font, "< Return to Selection", centerX, btnY + 6, btnHover ? 0xFFFFFFFF : 0xFFE2E8F0);
			return;
		}

		if (selectedCategoryIndex < 0 || selectedCategoryIndex >= categories.size())
			return;

		CategoryData cat = categories.get(selectedCategoryIndex);

		int bannerX = x + 4;
		int bannerY = y + 4;
		int bannerW = width - 8;

		// 1. Draw Card Background
		fillRoundedRect(graphics, bannerX, bannerY, bannerW, BANNER_HEADER_HEIGHT, 6, 0xEE_0F172A, 0xFF_334155);

		// 2. Render Landscape Texture across the FULL banner width with scissor
		graphics.enableScissor(bannerX + 1, bannerY + 1, bannerX + bannerW - 1, bannerY + BANNER_HEADER_HEIGHT - 1);

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		graphics.blit(backgroundTexture, bannerX, bannerY, bannerW, BANNER_HEADER_HEIGHT, 0.0f, 180.0f, 1672, 540, 1672, 941);

		int fadeW = Math.min(bannerW, Math.max(160, (int) (bannerW * 0.65f)));
		fillGradientH(graphics, bannerX, bannerY, bannerX + fadeW, bannerY + BANNER_HEADER_HEIGHT, 0xF2_0B1120, 0x00_0B1120);

		graphics.fill(bannerX, bannerY, bannerX + bannerW, bannerY + BANNER_HEADER_HEIGHT, 0x18_090D16);

		String catTitle = cat.name.toUpperCase(Locale.ROOT) + " SETTINGS";
		graphics.drawString(font, catTitle, bannerX + 12, bannerY + 11, 0xFFFFFFFF, true);

		String catDesc = font.substrByWidth(Component.literal(cat.description), Math.max(80, bannerW - 30)).getString();
		graphics.drawString(font, catDesc, bannerX + 12, bannerY + 25, 0xFFCBD5E1, true);

		graphics.disableScissor();

		fillRoundedRect(graphics, bannerX, bannerY, bannerW, BANNER_HEADER_HEIGHT, 6, 0x00_000000, 0xFF_334155);

		// Settings List area
		int listX = x + 4;
		int listY = y + BANNER_HEADER_HEIGHT + 6;
		int listWidth = width - 8;
		int listHeight = height - BANNER_HEADER_HEIGHT - 12;

		List<SettingEntry> activeEntries = getFilteredEntries(cat);

		int entryHeight = 34;
		int totalContentHeight = activeEntries.size() * (entryHeight + 3);
		maxScroll = Math.max(0, totalContentHeight - listHeight);
		scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);

		graphics.enableScissor(listX, listY, listX + listWidth, listY + listHeight);

		int entryY = listY - (int) scrollOffset;
		for (SettingEntry entry : activeEntries) {
			if (entryY + entryHeight >= listY && entryY <= listY + listHeight) {
				entry.render(graphics, listX, entryY, listWidth - 6, entryHeight, mouseX, mouseY);
			}
			entryY += entryHeight + 3;
		}

		graphics.disableScissor();

		// Scrollbar
		if (maxScroll > 0) {
			int scrollbarX = listX + listWidth - 4;
			int scrollbarHeight = Math.max(16, (int) ((float) listHeight * listHeight / totalContentHeight));
			int scrollbarY = listY + (int) ((listHeight - scrollbarHeight) * (scrollOffset / maxScroll));

			graphics.fill(scrollbarX, listY, scrollbarX + 3, listY + listHeight, 0x44_000000);
			graphics.fill(scrollbarX, scrollbarY, scrollbarX + 3, scrollbarY + scrollbarHeight, 0xFF_38BDF8);
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
		int windowX = getWindowX();
		int windowY = getWindowY();
		int windowWidth = getWindowWidth();
		int windowHeight = getWindowHeight();

		// 1ST PAGE: SELECTION SCREEN CLICKS
		if (currentView == CurrentView.SELECTION) {
			// Close button
			int closeW = 20;
			int closeX = windowX + windowWidth - closeW - 8;
			int closeY = windowY + 8;
			if (mouseX >= closeX && mouseX <= closeX + closeW && mouseY >= closeY && mouseY <= closeY + closeW) {
				this.onClose();
				return true;
			}

			// Other Mods click
			int otherW = windowWidth >= 520 ? 92 : 20;
			int otherX = closeX - otherW - 6;
			if (mouseX >= otherX && mouseX <= otherX + otherW && mouseY >= closeY && mouseY <= closeY + closeW) {
				Minecraft.getInstance().setScreen(new OtherModConfigsScreen(this));
				return true;
			}

			// Cards
			SectionType[] sections = { SectionType.CLIENT, SectionType.COMMON, SectionType.SERVER };
			int contentY = windowY + 42;
			int contentHeight = windowHeight - 50;
			boolean horizontal = windowWidth >= 480;

			if (horizontal) {
				int padding = 12;
				int totalGap = padding * 2;
				int cardW = (windowWidth - 24 - totalGap) / 3;
				int cardH = Math.min(220, contentHeight - 16);
				int startX = windowX + 12;
				int startY = contentY + (contentHeight - cardH) / 2;

				for (int i = 0; i < 3; i++) {
					int cx = startX + i * (cardW + padding);
					if (mouseX >= cx && mouseX <= cx + cardW && mouseY >= startY && mouseY <= startY + cardH) {
						selectSection(sections[i]);
						return true;
					}
				}
			} else {
				int cardH = (contentHeight - 16) / 3;
				int cardW = windowWidth - 24;
				int cx = windowX + 12;
				for (int i = 0; i < 3; i++) {
					int cy = contentY + i * (cardH + 4);
					if (mouseX >= cx && mouseX <= cx + cardW && mouseY >= cy && mouseY <= cy + cardH) {
						selectSection(sections[i]);
						return true;
					}
				}
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}

		// 2ND PAGE: DASHBOARD SCREEN CLICKS
		// Back button
		int backW = 48;
		int backX = windowX + 8;
		int backY = windowY + 8;
		if (mouseX >= backX && mouseX <= backX + backW && mouseY >= backY && mouseY <= backY + 20) {
			returnToSelection();
			return true;
		}

		// Close button
		int closeW = 20;
		int closeX = windowX + windowWidth - closeW - 8;
		int closeY = windowY + 8;
		if (mouseX >= closeX && mouseX <= closeX + closeW && mouseY >= closeY && mouseY <= closeY + closeW) {
			this.onClose();
			return true;
		}

		// Reset to default button
		int resetW = windowWidth >= 500 ? 96 : 44;
		int resetX = closeX - resetW - 6;
		int resetY = windowY + 8;
		if (mouseX >= resetX && mouseX <= resetX + resetW && mouseY >= resetY && mouseY <= resetY + closeW) {
			resetCurrentCategory();
			return true;
		}

		// Other Mods click in top bar
		int otherW = windowWidth >= 560 ? 86 : 22;
		int otherX = resetX - otherW - 6;
		if (mouseX >= otherX && mouseX <= otherX + otherW && mouseY >= resetY && mouseY <= resetY + closeW) {
			Minecraft.getInstance().setScreen(new OtherModConfigsScreen(this));
			return true;
		}

		// Quick Switcher tabs (Client, Common, Server)
		int maxSearchW = Math.min(130, Math.max(50, otherX - (windowX + 220)));
		int searchW = Math.max(50, maxSearchW);
		int searchX = otherX - searchW - 6;

		int tabW = 46;
		int tabH = 18;
		int tabsTotalW = 3 * (tabW + 3);
		int tabStartX = searchX - tabsTotalW - 10;
		boolean showTabs = windowWidth >= 560 && tabStartX > windowX + 240;
		if (showTabs) {
			SectionType[] tabs = { SectionType.CLIENT, SectionType.COMMON, SectionType.SERVER };
			for (int i = 0; i < 3; i++) {
				int tx = tabStartX + i * (tabW + 3);
				if (mouseX >= tx && mouseX <= tx + tabW && mouseY >= windowY + 9 && mouseY <= windowY + 9 + tabH) {
					if (tabs[i] != currentSection) {
						selectSection(tabs[i]);
					}
					return true;
				}
			}
		}

		// Search Box focus click
		if (searchBox != null && searchW >= 50 && mouseX >= searchX && mouseX <= searchX + searchW && mouseY >= windowY + 6 && mouseY <= windowY + 28) {
			searchBox.setFocused(true);
			setFocused(searchBox);
			if (activeTypableEntry != null) {
				activeTypableEntry.setFocused(false);
				activeTypableEntry = null;
			}
			return true;
		} else if (searchBox != null && searchBox.isFocused()) {
			searchBox.setFocused(false);
		}

		// Sidebar Category selection
		int contentY = windowY + 36;
		int contentHeight = windowHeight - 40;
		int sidebarWidth = getSidebarWidth();
		int sideX = windowX;

		if (mouseX >= sideX && mouseX <= sideX + sidebarWidth && mouseY >= contentY && mouseY <= contentY + contentHeight) {
			int itemHeight = 34;
			int itemGap = 3;
			int clickIndex = (int) ((mouseY - contentY - 4 + sidebarScrollOffset) / (itemHeight + itemGap));
			if (clickIndex >= 0 && clickIndex < categories.size()) {
				int slotY = contentY + 4 + clickIndex * (itemHeight + itemGap) - (int) sidebarScrollOffset;
				if (mouseY >= slotY && mouseY <= slotY + itemHeight) {
					selectedCategoryIndex = clickIndex;
					scrollOffset = 0;
					if (activeTypableEntry != null) {
						activeTypableEntry.setFocused(false);
						activeTypableEntry = null;
					}
					return true;
				}
			}
		}

		// Right Panel Settings click
		int panelX = windowX + sidebarWidth + 10;
		int panelWidth = windowWidth - sidebarWidth - 10;
		int listX = panelX + 4;
		int listY = contentY + BANNER_HEADER_HEIGHT + 6;
		int listWidth = panelWidth - 8;
		int listHeight = contentHeight - BANNER_HEADER_HEIGHT - 12;

		if (categories.isEmpty()) {
			int centerX = panelX + panelWidth / 2;
			int centerY = contentY + contentHeight / 2;
			int btnW = 140;
			int btnH = 20;
			int btnX = centerX - btnW / 2;
			int btnY = centerY + 24;
			if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
				returnToSelection();
				return true;
			}
		} else if (mouseX >= listX && mouseX <= listX + listWidth && mouseY >= listY && mouseY <= listY + listHeight) {
			if (selectedCategoryIndex >= 0 && selectedCategoryIndex < categories.size()) {
				CategoryData cat = categories.get(selectedCategoryIndex);
				List<SettingEntry> activeEntries = getFilteredEntries(cat);
				int entryHeight = 34;
				int entryY = listY - (int) scrollOffset;

				for (SettingEntry entry : activeEntries) {
					if (mouseY >= entryY && mouseY <= entryY + entryHeight) {
						if (entry.mouseClicked(mouseX, mouseY, button, listX, entryY, listWidth - 6, entryHeight)) {
							activeSlider = entry;
							if (entry.isFocused()) {
								if (activeTypableEntry != null && activeTypableEntry != entry) {
									activeTypableEntry.setFocused(false);
								}
								activeTypableEntry = entry;
							} else if (activeTypableEntry != null) {
								activeTypableEntry.setFocused(false);
								activeTypableEntry = null;
							}
							return true;
						}
					}
					entryY += entryHeight + 3;
				}
			}
		}

		if (activeTypableEntry != null) {
			activeTypableEntry.setFocused(false);
			activeTypableEntry = null;
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (activeSlider != null) {
			activeSlider.mouseReleased(mouseX, mouseY, button);
			activeSlider = null;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (activeSlider != null) {
			activeSlider.mouseDragged(mouseX, mouseY, button);
			return true;
		}
		if (activeTypableEntry != null && activeTypableEntry.isFocused()) {
			activeTypableEntry.mouseDragged(mouseX, mouseY, button);
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		int windowX = getWindowX();
		int sidebarWidth = getSidebarWidth();

		if (mouseX >= windowX && mouseX <= windowX + sidebarWidth) {
			if (maxSidebarScroll > 0) {
				sidebarScrollOffset = Mth.clamp(sidebarScrollOffset - scrollY * 18, 0, maxSidebarScroll);
				return true;
			}
		} else {
			if (maxScroll > 0) {
				scrollOffset = Mth.clamp(scrollOffset - scrollY * 18, 0, maxScroll);
				return true;
			}
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		// When search box is focused, handle input and never close screen on backspace
		if (searchBox != null && searchBox.isFocused()) {
			if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
				searchBox.setFocused(false);
				return true;
			}
			if (searchBox.keyPressed(keyCode, scanCode, modifiers)) {
				return true;
			}
		}

		// When active setting text/color box is focused, handle input
		if (activeTypableEntry != null && activeTypableEntry.isFocused()) {
			if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
				activeTypableEntry.setFocused(false);
				activeTypableEntry = null;
				return true;
			}
			if (activeTypableEntry.keyPressed(keyCode, scanCode, modifiers)) {
				return true;
			}
			return true; // Consume all other keys to prevent inventory (E) or game keybinds while typing!
		}

		if (super.keyPressed(keyCode, scanCode, modifiers))
			return true;

		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			if (currentView == CurrentView.DASHBOARD) {
				returnToSelection();
				return true;
			} else {
				this.onClose();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		if (searchBox != null && searchBox.isFocused()) {
			if (searchBox.charTyped(codePoint, modifiers)) {
				return true;
			}
		}
		if (activeTypableEntry != null && activeTypableEntry.isFocused()) {
			if (activeTypableEntry.charTyped(codePoint, modifiers)) {
				return true;
			}
		}
		return super.charTyped(codePoint, modifiers);
	}

	@Override
	public void onClose() {
		saveAll();
		ScreenOpener.open(parent);
	}

	private void resetCurrentCategory() {
		if (currentSection == SectionType.SERVER && isServerLocked()) return;
		if (selectedCategoryIndex >= 0 && selectedCategoryIndex < categories.size()) {
			CategoryData cat = categories.get(selectedCategoryIndex);
			for (SettingEntry entry : cat.entries) {
				entry.resetToDefault();
			}
			try {
				if (cat.config != null) {
					cat.config.save();
					cat.config.onReload();
				}
			} catch (Throwable t) {
				Better_lib.LOG.warn("Failed to reset config category {}: {}", cat.name, t.getMessage());
			}
		}
	}

	private void saveAll() {
		try {
			if (clientConfig != null) {
				clientConfig.save();
				clientConfig.onReload();
			}
		} catch (Throwable t) {
			Better_lib.LOG.warn("Failed to save client config: {}", t.getMessage());
		}
		try {
			if (commonConfig != null) {
				commonConfig.save();
				commonConfig.onReload();
			}
		} catch (Throwable t) {
			Better_lib.LOG.warn("Failed to save common config: {}", t.getMessage());
		}
		try {
			if (serverConfig != null && !isServerLocked()) {
				serverConfig.save();
				serverConfig.onReload();
			}
		} catch (Throwable t) {
			Better_lib.LOG.warn("Failed to save server config: {}", t.getMessage());
		}
		for (ConfigBase cat : customCategories.values()) {
			try {
				if (cat != null) {
					cat.save();
					cat.onReload();
				}
			} catch (Throwable t) {
				Better_lib.LOG.warn("Failed to save custom category: {}", t.getMessage());
			}
		}
		for (CategoryData cat : categories) {
			try {
				if (cat != null && cat.config != null) {
					cat.config.save();
					cat.config.onReload();
				}
			} catch (Throwable t) {
				Better_lib.LOG.warn("Failed to save category {}: {}", cat != null ? cat.name : "unknown", t.getMessage());
			}
		}
	}

	// ==========================================
	// DRAWING UTILITIES WITH SMOOTH EDGES
	// ==========================================
	private static int blendAlpha(int argb, float alphaFactor) {
		int a = (argb >> 24) & 0xFF;
		int r = (argb >> 16) & 0xFF;
		int g = (argb >> 8) & 0xFF;
		int b = argb & 0xFF;
		int newA = Math.round(a * Mth.clamp(alphaFactor, 0.0f, 1.0f));
		return (newA << 24) | (r << 16) | (g << 8) | b;
	}

	public static void fillRoundedRect(GuiGraphics graphics, int x, int y, int width, int height, int radius, int bgColor, int borderColor) {
		if (width <= 0 || height <= 0) return;
		radius = Math.min(radius, Math.min(width / 2, height / 2));

		if ((borderColor & 0xFF000000) == 0) {
			borderColor = bgColor;
		}

		if (radius <= 0) {
			graphics.fill(x, y, x + width, y + height, bgColor);
			if (borderColor != bgColor) {
				graphics.fill(x, y, x + width, y + 1, borderColor);
				graphics.fill(x, y + height - 1, x + width, y + height, borderColor);
				graphics.fill(x, y + 1, x + 1, y + height - 1, borderColor);
				graphics.fill(x + width - 1, y + 1, x + width, y + height - 1, borderColor);
			}
			return;
		}

		// 1. Central body and horizontal/vertical spans
		graphics.fill(x + radius, y, x + width - radius, y + height, bgColor);
		graphics.fill(x, y + radius, x + radius, y + height - radius, bgColor);
		graphics.fill(x + width - radius, y + radius, x + width, y + height - radius, bgColor);

		// 2. Straight border lines
		if (borderColor != bgColor) {
			graphics.fill(x + radius, y, x + width - radius, y + 1, borderColor);
			graphics.fill(x + radius, y + height - 1, x + width - radius, y + height, borderColor);
			graphics.fill(x, y + radius, x + 1, y + height - radius, borderColor);
			graphics.fill(x + width - 1, y + radius, x + width, y + height - radius, borderColor);
		}

		// 3. Crisp, clean rounded corners
		float rSq = (radius - 0.25f) * (radius - 0.25f);
		float innerRSq = (radius - 1.25f) * (radius - 1.25f);

		for (int cy = 0; cy < radius; cy++) {
			for (int cx = 0; cx < radius; cx++) {
				float dx = (radius - cx) - 0.5f;
				float dy = (radius - cy) - 0.5f;
				float dSq = dx * dx + dy * dy;

				if (dSq <= rSq) {
					int pixelColor = (borderColor != bgColor && dSq >= innerRSq) ? borderColor : bgColor;

					// 4 Symmetrical Corners
					// Top-Left
					graphics.fill(x + cx, y + cy, x + cx + 1, y + cy + 1, pixelColor);
					// Top-Right
					graphics.fill(x + width - 1 - cx, y + cy, x + width - cx, y + cy + 1, pixelColor);
					// Bottom-Left
					graphics.fill(x + cx, y + height - 1 - cy, x + cx + 1, y + height - cy, pixelColor);
					// Bottom-Right
					graphics.fill(x + width - 1 - cx, y + height - 1 - cy, x + width - cx, y + height - cy, pixelColor);
				}
			}
		}
	}

	public static void fillGradientH(GuiGraphics graphics, int x0, int y0, int x1, int y1, int colorFrom, int colorTo) {
		if (x1 <= x0 || y1 <= y0) return;
		int steps = x1 - x0;
		int a0 = (colorFrom >> 24) & 0xFF, r0 = (colorFrom >> 16) & 0xFF, g0 = (colorFrom >> 8) & 0xFF, b0 = colorFrom & 0xFF;
		int a1 = (colorTo >> 24) & 0xFF, r1 = (colorTo >> 16) & 0xFF, g1 = (colorTo >> 8) & 0xFF, b1 = colorTo & 0xFF;
		for (int i = 0; i < steps; i++) {
			float t = steps > 1 ? (float) i / (float) (steps - 1) : 0.0f;
			int a = Math.round(a0 + (a1 - a0) * t);
			int r = Math.round(r0 + (r1 - r0) * t);
			int g = Math.round(g0 + (g1 - g0) * t);
			int b = Math.round(b0 + (b1 - b0) * t);
			int col = (a << 24) | (r << 16) | (g << 8) | b;
			graphics.fill(x0 + i, y0, x0 + i + 1, y1, col);
		}
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
		public boolean editable = true;

		public SettingEntry(String label, String description, ItemStack icon, ConfigBase owner) {
			this.label = label;
			this.description = description != null ? description : "";
			this.icon = icon;
			this.owner = owner;
		}

		public abstract void render(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY);
		public abstract boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height);
		public void mouseDragged(double mouseX, double mouseY, int button) {}
		public void mouseReleased(double mouseX, double mouseY, int button) {}
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
		public boolean charTyped(char codePoint, int modifiers) { return false; }
		public boolean isTypable() { return false; }
		public boolean isFocused() { return false; }
		public void setFocused(boolean focused) {}
		public abstract void resetToDefault();

		protected void renderBaseCard(GuiGraphics graphics, int x, int y, int width, int height, boolean isHovered, int rightControlsWidth) {
			fillRoundedRect(graphics, x, y, width, height, 5, isHovered ? 0xDD_1E293B : 0xAA_1E293B, isHovered ? 0xFF_64748B : 0xFF_334155);
			graphics.renderFakeItem(icon, x + 6, y + 8);

			Font font = Minecraft.getInstance().font;
			int availableTextWidth = Math.max(50, width - rightControlsWidth - 36);

			String displayLabel = (!editable ? "[OP] " : "") + label;
			String trimmedLabel = font.substrByWidth(Component.literal(displayLabel), availableTextWidth).getString();
			graphics.drawString(font, trimmedLabel, x + 26, y + 5, editable ? 0xFFFFFFFF : 0xFF94A3B8, true);

			String desc = description.isEmpty() ? "Setting configuration" : description;
			String trimmedDesc = font.substrByWidth(Component.literal(desc), availableTextWidth).getString();
			graphics.drawString(font, trimmedDesc, x + 26, y + 17, 0xFFCBD5E1, true);
		}
	}

	// 1. BOOLEAN TOGGLE SWITCH (SMOOTH SLIDING CAPSULE SWITCH)
	public static class BoolSetting extends SettingEntry {
		private final ConfigBase.ConfigBool value;
		private float knobProgress;
		private long lastAnimTime = 0;

		public BoolSetting(String label, String description, ItemStack icon, ConfigBase.ConfigBool value, ConfigBase owner) {
			super(label, description, icon, owner);
			this.value = value;
			this.knobProgress = value.get() ? 1.0f : 0.0f;
		}

		@Override
		public void render(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY) {
			boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;

			int trackW = 40;
			int trackH = 18;
			int labelW = 28;
			int totalControlsW = trackW + labelW + 8;

			renderBaseCard(graphics, x, y, width, height, isHovered, totalControlsW);

			boolean val = value.get();

			// Smooth animation for sliding knob
			long now = System.currentTimeMillis();
			if (lastAnimTime == 0) lastAnimTime = now;
			float dt = Math.min(0.1f, (now - lastAnimTime) / 1000.0f);
			lastAnimTime = now;

			float target = val ? 1.0f : 0.0f;
			if (Math.abs(knobProgress - target) > 0.01f) {
				knobProgress += (target - knobProgress) * Math.min(1.0f, dt * 20.0f);
			} else {
				knobProgress = target;
			}

			int switchX = x + width - totalControlsW;
			int switchY = y + 8;

			Font font = Minecraft.getInstance().font;
			String statusText = val ? "ON" : "OFF";
			int statusColor = !editable ? 0xFF_64748B : (val ? 0xFF_38BDF8 : 0xFF_94A3B8);
			graphics.drawString(font, statusText, switchX, switchY + 5, statusColor, true);

			int trackX = switchX + labelW;
			int trackY = switchY;
			boolean mouseOverSwitch = mouseX >= trackX && mouseX <= trackX + trackW && mouseY >= trackY && mouseY <= trackY + trackH;

			// Smooth transition between OFF (slate-800) and ON (cyan-600)
			int rTrack = (int) (0x1E + (0x02 - 0x1E) * knobProgress);
			int gTrack = (int) (0x29 + (0x84 - 0x29) * knobProgress);
			int bTrack = (int) (0x3B + (0xC7 - 0x3B) * knobProgress);
			int trackBg = 0xFF_000000 | (rTrack << 16) | (gTrack << 8) | bTrack;

			int rBorder = (int) (0x47 + (0x38 - 0x47) * knobProgress);
			int gBorder = (int) (0x55 + (0xBD - 0x55) * knobProgress);
			int bBorder = (int) (0x69 + (0xF8 - 0x69) * knobProgress);
			int trackBorder = !editable ? 0xFF_334155 : (0xFF_000000 | (rBorder << 16) | (gBorder << 8) | bBorder);
			if (mouseOverSwitch && editable) {
				trackBorder = val ? 0xFF_7DD3FC : 0xFF_64748B;
			}

			fillRoundedRect(graphics, trackX, trackY, trackW, trackH, 9, trackBg, trackBorder);

			// Crisp circular knob
			int knobD = 14;
			int minKnobX = trackX + 2;
			int maxKnobX = trackX + trackW - knobD - 2;
			int knobX = minKnobX + Math.round(knobProgress * (maxKnobX - minKnobX));
			int knobY = trackY + 2;

			int knobFill = !editable ? 0xFF_94A3B8 : (val ? 0xFF_FFFFFF : 0xFF_F1F5F9);
			int knobBorder = !editable ? 0xFF_64748B : (val ? 0xFF_E0F2FE : 0xFF_CBD5E1);
			fillRoundedRect(graphics, knobX, knobY, knobD, knobD, 7, knobFill, knobBorder);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height) {
			if (!editable) return false;
			int trackW = 40;
			int labelW = 28;
			int totalControlsW = trackW + labelW + 8;
			int switchX = x + width - totalControlsW;
			int switchY = y + 8;

			if (mouseX >= switchX - 4 && mouseX <= x + width && mouseY >= switchY - 4 && mouseY <= switchY + 22) {
				value.set(!value.get());
				if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
				return true;
			}
			return false;
		}

		@Override
		public void resetToDefault() {
			if (!editable) return;
			value.set(value.getDefault());
			knobProgress = value.get() ? 1.0f : 0.0f;
			if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
		}
	}

	// 2. INTEGER SLIDER (SEPARATE TRACK & VALUE BADGE, SLIDE OR CLICK TO TYPE)
	public static class IntSliderSetting extends SettingEntry {
		private final ConfigBase.ConfigInt value;
		private boolean isDragging = false;
		private boolean focused = false;
		private String editText = null;
		private int trackX, trackW, trackY, trackH;
		private int badgeX, badgeW, badgeY, badgeH;

		public IntSliderSetting(String label, String description, ItemStack icon, ConfigBase.ConfigInt value, ConfigBase owner) {
			super(label, description, icon, owner);
			this.value = value;
		}

		@Override
		public boolean isTypable() { return editable; }

		@Override
		public boolean isFocused() { return focused; }

		@Override
		public void setFocused(boolean focused) {
			if (this.focused && !focused) {
				applyEditText();
			}
			this.focused = focused;
			if (focused) {
				editText = String.valueOf(value.get());
			} else {
				editText = null;
			}
		}

		private void applyEditText() {
			if (editText != null) {
				try {
					int parsed = Integer.parseInt(editText.trim());
					int min = value.getMin() <= Integer.MIN_VALUE / 2 ? 0 : value.getMin();
					int max = value.getMax() >= Integer.MAX_VALUE / 2 ? Math.max(100, Math.max(min + 100, value.getDefault() > 0 ? value.getDefault() * 4 : 100)) : value.getMax();
					parsed = Mth.clamp(parsed, min, max);
					value.set(parsed);
					if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
				} catch (Exception ignored) {}
				editText = null;
			}
		}

		@Override
		public void render(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY) {
			boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;

			this.badgeW = 42;
			this.badgeH = 18;
			this.trackW = 76;
			this.trackH = 12;

			int totalW = trackW + 8 + badgeW;
			renderBaseCard(graphics, x, y, width, height, isHovered, totalW + 8);

			this.trackX = x + width - totalW - 8;
			this.trackY = y + 11;

			this.badgeX = trackX + trackW + 8;
			this.badgeY = y + 8;

			int min = value.getMin() <= Integer.MIN_VALUE / 2 ? 0 : value.getMin();
			int max = value.getMax() >= Integer.MAX_VALUE / 2 ? Math.max(100, Math.max(min + 100, value.getDefault() > 0 ? value.getDefault() * 4 : 100)) : value.getMax();
			float progress = max > min ? (float) (value.get() - min) / (max - min) : 0;
			progress = Mth.clamp(progress, 0.0f, 1.0f);

			boolean mouseOverTrack = mouseX >= trackX - 2 && mouseX <= trackX + trackW + 2 && mouseY >= trackY - 4 && mouseY <= trackY + trackH + 4;
			boolean mouseOverBadge = mouseX >= badgeX && mouseX <= badgeX + badgeW && mouseY >= badgeY && mouseY <= badgeY + badgeH;

			// 1. Sleek Track Groove
			fillRoundedRect(graphics, trackX, trackY, trackW, trackH, 4, 0xFF_0F172A, mouseOverTrack ? 0xFF_475569 : 0xFF_334155);

			// 2. Filled Progress Area
			int thumbW = 10;
			int thumbH = 16;
			int thumbX = trackX + (int) (progress * (trackW - thumbW));
			int thumbY = y + 9;

			int fillW = (thumbX - trackX) + thumbW / 2;
			if (fillW > 3) {
				int fillColor = editable ? 0xFF_0284C7 : 0xFF_334155;
				fillRoundedRect(graphics, trackX + 1, trackY + 1, fillW, trackH - 2, 3, fillColor, fillColor);
			}

			// 3. Thumb Handle (Vertical pill extending cleanly over the groove)
			boolean highlightThumb = (isDragging || mouseOverTrack) && editable;
			int thumbFill = highlightThumb ? 0xFF_38BDF8 : 0xFF_F8FAFC;
			int thumbBorder = highlightThumb ? 0xFF_FFFFFF : 0xFF_38BDF8;
			fillRoundedRect(graphics, thumbX, thumbY, thumbW, thumbH, 4, thumbFill, thumbBorder);

			// 4. Value Badge (Separate chip on the right - NO overlapping text!)
			int badgeBg = focused ? 0xFF_0F172A : (mouseOverBadge ? 0xFF_334155 : 0xFF_1E293B);
			int badgeBorder = focused ? 0xFF_38BDF8 : (mouseOverBadge ? 0xFF_475569 : 0xFF_334155);
			fillRoundedRect(graphics, badgeX, badgeY, badgeW, badgeH, 4, badgeBg, badgeBorder);

			Font font = Minecraft.getInstance().font;
			String valStr = focused && editText != null ? editText + (System.currentTimeMillis() % 1000 > 500 ? "_" : "") : String.valueOf(value.get());
			graphics.drawCenteredString(font, valStr, badgeX + badgeW / 2, badgeY + 5, focused ? 0xFF_38BDF8 : (editable ? 0xFF_FFFFFF : 0xFF_94A3B8));
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height) {
			if (!editable) return false;
			// Clicked on the badge -> focus to type
			if (mouseX >= badgeX && mouseX <= badgeX + badgeW && mouseY >= badgeY && mouseY <= badgeY + badgeH) {
				setFocused(true);
				return true;
			}
			// Clicked on the slider track -> drag
			if (mouseX >= trackX - 2 && mouseX <= trackX + trackW + 2 && mouseY >= trackY - 4 && mouseY <= trackY + trackH + 4) {
				if (focused) {
					setFocused(false);
				}
				isDragging = true;
				updateFromMouse(mouseX);
				return true;
			}
			if (focused) {
				setFocused(false);
			}
			return false;
		}

		@Override
		public void mouseDragged(double mouseX, double mouseY, int button) {
			if (!editable) return;
			if (isDragging) {
				updateFromMouse(mouseX);
			}
		}

		@Override
		public void mouseReleased(double mouseX, double mouseY, int button) {
			this.isDragging = false;
		}

		private void updateFromMouse(double mouseX) {
			int min = value.getMin() <= Integer.MIN_VALUE / 2 ? 0 : value.getMin();
			int max = value.getMax() >= Integer.MAX_VALUE / 2 ? Math.max(100, Math.max(min + 100, value.getDefault() > 0 ? value.getDefault() * 4 : 100)) : value.getMax();
			int thumbW = 10;
			float progress = (float) (mouseX - trackX - thumbW / 2.0f) / (float) (trackW - thumbW);
			progress = Mth.clamp(progress, 0.0f, 1.0f);
			int newVal = Math.round(min + progress * (max - min));
			value.set(newVal);
			if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
		}

		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			if (!focused || !editable) return false;
			if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
				setFocused(false);
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
				editText = null;
				focused = false;
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
				if (editText != null && !editText.isEmpty()) {
					editText = editText.substring(0, editText.length() - 1);
				}
				return true;
			}
			return false;
		}

		@Override
		public boolean charTyped(char codePoint, int modifiers) {
			if (!focused || !editable) return false;
			if (editText == null) editText = "";
			if ((codePoint >= '0' && codePoint <= '9') || (codePoint == '-' && editText.isEmpty())) {
				if (editText.length() < 10) {
					editText += (char) codePoint;
					return true;
				}
			}
			return false;
		}

		@Override
		public void resetToDefault() {
			if (!editable) return;
			value.set(value.getDefault());
			editText = null;
			if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
		}
	}

	// 3. FLOAT SLIDER (SEPARATE TRACK & VALUE BADGE, SLIDE OR CLICK TO TYPE)
	public static class FloatSliderSetting extends SettingEntry {
		private final ConfigBase.ConfigFloat value;
		private boolean isDragging = false;
		private boolean focused = false;
		private String editText = null;
		private int trackX, trackW, trackY, trackH;
		private int badgeX, badgeW, badgeY, badgeH;

		public FloatSliderSetting(String label, String description, ItemStack icon, ConfigBase.ConfigFloat value, ConfigBase owner) {
			super(label, description, icon, owner);
			this.value = value;
		}

		@Override
		public boolean isTypable() { return editable; }

		@Override
		public boolean isFocused() { return focused; }

		@Override
		public void setFocused(boolean focused) {
			if (this.focused && !focused) {
				applyEditText();
			}
			this.focused = focused;
			if (focused) {
				editText = String.format(Locale.ROOT, "%.2f", value.get());
			} else {
				editText = null;
			}
		}

		private void applyEditText() {
			if (editText != null) {
				try {
					float parsed = Float.parseFloat(editText.trim());
					float min = value.getMin() <= -Float.MAX_VALUE / 2 ? 0.0f : value.getMin();
					float max = value.getMax() >= Float.MAX_VALUE / 2 ? Math.max(1.0f, Math.max(min + 1.0f, value.getDefault() > 0 ? value.getDefault() * 4.0f : 1.0f)) : value.getMax();
					parsed = Mth.clamp(parsed, min, max);
					parsed = Math.round(parsed * 100.0f) / 100.0f;
					value.set(parsed);
					if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
				} catch (Exception ignored) {}
				editText = null;
			}
		}

		@Override
		public void render(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY) {
			boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;

			this.badgeW = 44;
			this.badgeH = 18;
			this.trackW = 76;
			this.trackH = 12;

			int totalW = trackW + 8 + badgeW;
			renderBaseCard(graphics, x, y, width, height, isHovered, totalW + 8);

			this.trackX = x + width - totalW - 8;
			this.trackY = y + 11;

			this.badgeX = trackX + trackW + 8;
			this.badgeY = y + 8;

			float min = value.getMin() <= -Float.MAX_VALUE / 2 ? 0.0f : value.getMin();
			float max = value.getMax() >= Float.MAX_VALUE / 2 ? Math.max(1.0f, Math.max(min + 1.0f, value.getDefault() > 0 ? value.getDefault() * 4.0f : 1.0f)) : value.getMax();
			float progress = max > min ? (value.get() - min) / (max - min) : 0;
			progress = Mth.clamp(progress, 0.0f, 1.0f);

			boolean mouseOverTrack = mouseX >= trackX - 2 && mouseX <= trackX + trackW + 2 && mouseY >= trackY - 4 && mouseY <= trackY + trackH + 4;
			boolean mouseOverBadge = mouseX >= badgeX && mouseX <= badgeX + badgeW && mouseY >= badgeY && mouseY <= badgeY + badgeH;

			// 1. Sleek Track Groove
			fillRoundedRect(graphics, trackX, trackY, trackW, trackH, 4, 0xFF_0F172A, mouseOverTrack ? 0xFF_475569 : 0xFF_334155);

			// 2. Filled Progress Area
			int thumbW = 10;
			int thumbH = 16;
			int thumbX = trackX + (int) (progress * (trackW - thumbW));
			int thumbY = y + 9;

			int fillW = (thumbX - trackX) + thumbW / 2;
			if (fillW > 3) {
				int fillColor = editable ? 0xFF_0284C7 : 0xFF_334155;
				fillRoundedRect(graphics, trackX + 1, trackY + 1, fillW, trackH - 2, 3, fillColor, fillColor);
			}

			// 3. Thumb Handle
			boolean highlightThumb = (isDragging || mouseOverTrack) && editable;
			int thumbFill = highlightThumb ? 0xFF_38BDF8 : 0xFF_F8FAFC;
			int thumbBorder = highlightThumb ? 0xFF_FFFFFF : 0xFF_38BDF8;
			fillRoundedRect(graphics, thumbX, thumbY, thumbW, thumbH, 4, thumbFill, thumbBorder);

			// 4. Value Badge
			int badgeBg = focused ? 0xFF_0F172A : (mouseOverBadge ? 0xFF_334155 : 0xFF_1E293B);
			int badgeBorder = focused ? 0xFF_38BDF8 : (mouseOverBadge ? 0xFF_475569 : 0xFF_334155);
			fillRoundedRect(graphics, badgeX, badgeY, badgeW, badgeH, 4, badgeBg, badgeBorder);

			Font font = Minecraft.getInstance().font;
			String valStr = focused && editText != null ? editText + (System.currentTimeMillis() % 1000 > 500 ? "_" : "") : String.format(Locale.ROOT, "%.2f", value.get());
			graphics.drawCenteredString(font, valStr, badgeX + badgeW / 2, badgeY + 5, focused ? 0xFF_38BDF8 : (editable ? 0xFF_FFFFFF : 0xFF_94A3B8));
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height) {
			if (!editable) return false;
			if (mouseX >= badgeX && mouseX <= badgeX + badgeW && mouseY >= badgeY && mouseY <= badgeY + badgeH) {
				setFocused(true);
				return true;
			}
			if (mouseX >= trackX - 2 && mouseX <= trackX + trackW + 2 && mouseY >= trackY - 4 && mouseY <= trackY + trackH + 4) {
				if (focused) {
					setFocused(false);
				}
				isDragging = true;
				updateFromMouse(mouseX);
				return true;
			}
			if (focused) {
				setFocused(false);
			}
			return false;
		}

		@Override
		public void mouseDragged(double mouseX, double mouseY, int button) {
			if (!editable) return;
			if (isDragging) {
				updateFromMouse(mouseX);
			}
		}

		@Override
		public void mouseReleased(double mouseX, double mouseY, int button) {
			this.isDragging = false;
		}

		private void updateFromMouse(double mouseX) {
			float min = value.getMin() <= -Float.MAX_VALUE / 2 ? 0.0f : value.getMin();
			float max = value.getMax() >= Float.MAX_VALUE / 2 ? Math.max(1.0f, Math.max(min + 1.0f, value.getDefault() > 0 ? value.getDefault() * 4.0f : 1.0f)) : value.getMax();
			int thumbW = 10;
			float progress = (float) (mouseX - trackX - thumbW / 2.0f) / (float) (trackW - thumbW);
			progress = Mth.clamp(progress, 0.0f, 1.0f);
			float newVal = min + progress * (max - min);
			newVal = Math.round(newVal * 100.0f) / 100.0f;
			value.set(newVal);
			if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
		}

		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			if (!focused || !editable) return false;
			if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
				setFocused(false);
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
				editText = null;
				focused = false;
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
				if (editText != null && !editText.isEmpty()) {
					editText = editText.substring(0, editText.length() - 1);
				}
				return true;
			}
			return false;
		}

		@Override
		public boolean charTyped(char codePoint, int modifiers) {
			if (!focused || !editable) return false;
			if (editText == null) editText = "";
			if ((codePoint >= '0' && codePoint <= '9') || codePoint == '.' || (codePoint == '-' && editText.isEmpty())) {
				if (codePoint == '.' && editText.contains(".")) return false;
				if (editText.length() < 10) {
					editText += (char) codePoint;
					return true;
				}
			}
			return false;
		}

		@Override
		public void resetToDefault() {
			if (!editable) return;
			value.set(value.getDefault());
			editText = null;
			if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
		}
	}

	// 4. ENUM SELECTOR
	public static class EnumSetting<E extends Enum<E>> extends SettingEntry {
		private final ConfigBase.ConfigEnum<E> value;

		public EnumSetting(String label, String description, ItemStack icon, ConfigBase.ConfigEnum<E> value, ConfigBase owner) {
			super(label, description, icon, owner);
			this.value = value;
		}

		@Override
		public void render(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY) {
			boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
			int btnW = 80;
			int btnH = 16;
			renderBaseCard(graphics, x, y, width, height, isHovered, btnW + 12);

			int btnX = x + width - btnW - 8;
			int btnY = y + 9;
			boolean bHover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;

			fillRoundedRect(graphics, btnX, btnY, btnW, btnH, 4, bHover ? 0xFF_334155 : 0xFF_1E293B, bHover ? 0xFF_38BDF8 : 0xFF_475569);

			Font font = Minecraft.getInstance().font;
			String valName = value.get().name();
			String trimmed = font.substrByWidth(Component.literal(valName), btnW - 8).getString();
			graphics.drawCenteredString(font, trimmed, btnX + btnW / 2, btnY + 4, editable ? 0xFFFFFFFF : 0xFF94A3B8);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height) {
			if (!editable) return false;
			int btnW = 80;
			int btnX = x + width - btnW - 8;
			int btnY = y + 9;
			if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + 16) {
				E current = value.get();
				E[] constants = current.getDeclaringClass().getEnumConstants();
				int next = (current.ordinal() + 1) % constants.length;
				value.set(constants[next]);
				if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
				return true;
			}
			return false;
		}

		@Override
		public void resetToDefault() {
			if (!editable) return;
			value.set(value.getDefault());
			if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
		}
	}

	// 5. DROPDOWN STRING SELECTOR
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
			int btnW = 80;
			int btnH = 16;
			renderBaseCard(graphics, x, y, width, height, isHovered, btnW + 12);

			int btnX = x + width - btnW - 8;
			int btnY = y + 9;
			boolean bHover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;

			fillRoundedRect(graphics, btnX, btnY, btnW, btnH, 4, bHover ? 0xFF_334155 : 0xFF_1E293B, bHover ? 0xFF_38BDF8 : 0xFF_475569);

			Font font = Minecraft.getInstance().font;
			String valStr = value.get();
			String trimmed = font.substrByWidth(Component.literal(valStr), btnW - 8).getString();
			graphics.drawCenteredString(font, trimmed, btnX + btnW / 2, btnY + 4, editable ? 0xFFFFFFFF : 0xFF94A3B8);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height) {
			if (!editable) return false;
			int btnW = 80;
			int btnX = x + width - btnW - 8;
			int btnY = y + 9;
			if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + 16) {
				if (!options.isEmpty()) {
					int idx = options.indexOf(value.get());
					int next = (idx + 1) % options.size();
					value.set(options.get(next));
					if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
					return true;
				}
			}
			return false;
		}

		@Override
		public void resetToDefault() {
			if (!editable) return;
			value.set(value.getDefault());
			if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
		}
	}

	// 6. COLOR PICKER & HEX INPUT FIELD
	public static class ColorSetting extends SettingEntry {
		private final ConfigBase.ConfigInt value;
		private int boxX, boxY, boxW, boxH;

		public ColorSetting(String label, String description, ItemStack icon, ConfigBase.ConfigInt value, ConfigBase owner) {
			super(label, description, icon, owner);
			this.value = value;
		}

		@Override
		public void render(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY) {
			boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
			this.boxW = 86;
			this.boxH = 18;
			renderBaseCard(graphics, x, y, width, height, isHovered, boxW + 12);

			this.boxX = x + width - boxW - 8;
			this.boxY = y + 8;

			boolean mouseOverBox = mouseX >= boxX && mouseX <= boxX + boxW && mouseY >= boxY && mouseY <= boxY + boxH;
			int color = 0xFF000000 | (0x00FFFFFF & value.get());

			// Button card
			fillRoundedRect(graphics, boxX, boxY, boxW, boxH, 4, mouseOverBox ? 0xFF_334155 : 0xFF_1E293B, mouseOverBox ? 0xFF_38BDF8 : 0xFF_475569);

			// Color Swatch square
			fillRoundedRect(graphics, boxX + 3, boxY + 2, 14, 14, 3, color, 0xFF_FFFFFF);

			// Text Hex code
			Font font = Minecraft.getInstance().font;
			String text = String.format(Locale.ROOT, "#%06X", (0xFFFFFF & value.get()));
			graphics.drawString(font, text, boxX + 22, boxY + 5, mouseOverBox ? 0xFF_38BDF8 : (editable ? 0xFF_FFFFFF : 0xFF_94A3B8), true);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height) {
			if (!editable) return false;
			if (mouseX >= boxX && mouseX <= boxX + boxW && mouseY >= boxY && mouseY <= boxY + boxH) {
				Minecraft.getInstance().setScreen(new ColorPickerScreen(Minecraft.getInstance().screen, value, owner, label));
				return true;
			}
			return false;
		}

		@Override
		public void resetToDefault() {
			if (!editable) return;
			value.set(value.getDefault());
			if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
		}
	}

	// 7. TEXT STRING INPUT FIELD
	public static class TextSetting extends SettingEntry {
		private final ConfigBase.ConfigString value;
		private final EditBox editBox;
		private int boxX, boxY, boxW, boxH;

		public TextSetting(String label, String description, ItemStack icon, ConfigBase.ConfigString value, ConfigBase owner) {
			super(label, description, icon, owner);
			this.value = value;
			Font font = Minecraft.getInstance().font;
			this.editBox = new EditBox(font, 0, 0, 130, 14, Component.literal(label));
			this.editBox.setBordered(false);
			this.editBox.setMaxLength(128);
			this.editBox.setValue(value.get() != null ? value.get() : "");
			this.editBox.setTextColor(0xFFFFFFFF);
			this.editBox.setResponder(val -> {
				if (editable) {
					value.set(val);
					if (owner instanceof DynamicConfigCategory dyn) {
						dyn.applyToTarget();
					}
				}
			});
		}

		@Override
		public boolean isTypable() { return editable; }

		@Override
		public boolean isFocused() { return editBox.isFocused(); }

		@Override
		public void setFocused(boolean focused) {
			editBox.setFocused(focused);
		}

		@Override
		public void render(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY) {
			boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
			this.boxW = 130;
			this.boxH = 18;
			renderBaseCard(graphics, x, y, width, height, isHovered, boxW + 12);

			this.boxX = x + width - boxW - 8;
			this.boxY = y + 8;

			boolean mouseOverBox = mouseX >= boxX && mouseX <= boxX + boxW && mouseY >= boxY && mouseY <= boxY + boxH;
			fillRoundedRect(graphics, boxX, boxY, boxW, boxH, 4, 0xFF_0F172A, editBox.isFocused() ? 0xFF_38BDF8 : (mouseOverBox ? 0xFF_64748B : 0xFF_334155));

			editBox.setX(boxX + 5);
			editBox.setY(boxY + 5);
			editBox.setWidth(boxW - 10);
			editBox.setEditable(editable);
			editBox.render(graphics, mouseX, mouseY, 0f);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height) {
			if (!editable) return false;
			if (mouseX >= boxX && mouseX <= boxX + boxW && mouseY >= boxY && mouseY <= boxY + boxH) {
				editBox.setFocused(true);
				return editBox.mouseClicked(mouseX, mouseY, button);
			}
			if (editBox.isFocused()) {
				editBox.setFocused(false);
			}
			return false;
		}

		@Override
		public void mouseDragged(double mouseX, double mouseY, int button) {
			if (editBox.isFocused() && editable) {
				editBox.mouseDragged(mouseX, mouseY, button, 0, 0);
			}
		}

		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			if (!editBox.isFocused() || !editable) return false;
			if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
				editBox.setFocused(false);
				return true;
			}
			return editBox.keyPressed(keyCode, scanCode, modifiers);
		}

		@Override
		public boolean charTyped(char codePoint, int modifiers) {
			if (!editBox.isFocused() || !editable) return false;
			return editBox.charTyped(codePoint, modifiers);
		}

		@Override
		public void resetToDefault() {
			if (!editable) return;
			value.set(value.getDefault());
			if (editBox != null) {
				editBox.setValue(value.get() != null ? value.get() : "");
			}
			if (owner instanceof DynamicConfigCategory dyn) dyn.applyToTarget();
		}
	}
}
