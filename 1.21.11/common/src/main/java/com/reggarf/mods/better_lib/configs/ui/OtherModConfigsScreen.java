package com.reggarf.mods.better_lib.configs.ui;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import com.reggarf.mods.better_lib.platform.ModPlatformHelper;
import com.reggarf.mods.better_lib.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OtherModConfigsScreen extends Screen {

	protected final Screen parent;
	protected final Identifier backgroundTexture = BaseConfigScreen.DEFAULT_BG_TEXTURE;

	protected List<ModPlatformHelper.ModEntry> allMods = new ArrayList<>();
	protected List<ModPlatformHelper.ModEntry> filteredMods = new ArrayList<>();

	protected EditBox searchBox;
	protected String searchQuery = "";
	protected boolean showOnlyConfigurable = false;

	protected double scrollOffset = 0;
	protected double maxScroll = 0;

	public OtherModConfigsScreen(Screen parent) {
		super(Component.literal("Other Mod Configurations"));
		this.parent = parent;
	}

	@Override
	protected void renderBlurredBackground(GuiGraphics graphics) {
		// Suppress Minecraft full-screen blur shader pass to ensure all elements stay crisp
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.renderTransparentBackground(graphics);
	}

	@Override
	protected void init() {
		super.init();
		loadModList();

		int winX = getWindowX();
		int winY = getWindowY();
		int winW = getWindowWidth();

		int closeW = 20;
		int closeX = winX + winW - closeW - 8;

		int searchW = Math.min(140, Math.max(70, winW - 320));
		int searchX = closeX - searchW - 8;

		searchBox = new EditBox(font, searchX + 18, winY + 9, searchW - 22, 16, Component.literal("Search"));
		searchBox.setBordered(false);
		searchBox.setMaxLength(40);
		searchBox.setHint(Component.literal("Search mods..."));
		searchBox.setValue(searchQuery);
		searchBox.setResponder(text -> {
			this.searchQuery = text;
			this.scrollOffset = 0;
			updateFilter();
		});
		searchBox.setTextColor(0xFFFFFFFF);
		addRenderableWidget(searchBox);
	}

	private void loadModList() {
		try {
			allMods = Services.MODS.getLoadedMods();
		} catch (Exception e) {
			allMods = new ArrayList<>();
		}
		updateFilter();
	}

	private void updateFilter() {
		filteredMods = new ArrayList<>();
		String q = searchQuery.trim().toLowerCase(Locale.ROOT);

		for (ModPlatformHelper.ModEntry mod : allMods) {
			if (showOnlyConfigurable && !mod.hasConfig() && !com.reggarf.mods.better_lib.configs.BetterLibConfigApi.hasConfigs(mod.id()) && !"better_lib".equalsIgnoreCase(mod.id())) {
				continue;
			}
			if (q.isEmpty() || mod.name().toLowerCase(Locale.ROOT).contains(q) || mod.id().toLowerCase(Locale.ROOT).contains(q)) {
				filteredMods.add(mod);
			}
		}
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

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(graphics, mouseX, mouseY, partialTicks);

		// Fullscreen background backdrop (full unzoomed image)
		graphics.blit(backgroundTexture, 0, 0, width, height, 0.0f, 1.0f, 0.0f, 1.0f);

		// Darkening overlay
		graphics.fill(0, 0, width, height, 0x44_090D16);

		int winX = getWindowX();
		int winY = getWindowY();
		int winW = getWindowWidth();
		int winH = getWindowHeight();

		// Top Bar
		renderTopBar(graphics, winX, winY, winW, mouseX, mouseY);

		// Main Content Area bounds
		int contentY = winY + 38;
		int contentHeight = winH - 42;

		// Main Card Container
		BaseConfigScreen.fillRoundedRect(graphics, winX, contentY, winW, contentHeight, 6, 0xEE_0F172A, 0xFF_334155);

		// Mod List rendering
		renderModList(graphics, winX + 6, contentY + 6, winW - 12, contentHeight - 12, mouseX, mouseY);

		super.render(graphics, mouseX, mouseY, partialTicks);
	}

	private void renderTopBar(GuiGraphics graphics, int x, int y, int width, int mouseX, int mouseY) {
		// Back button
		int backW = 48;
		int backX = x + 8;
		int backY = y + 8;
		boolean backHovered = mouseX >= backX && mouseX <= backX + backW && mouseY >= backY && mouseY <= backY + 20;
		BaseConfigScreen.fillRoundedRect(graphics, backX, backY, backW, 20, 4, backHovered ? 0xFF334155 : 0xFF1E293B, backHovered ? 0xFF38BDF8 : 0xFF475569);
		graphics.drawCenteredString(font, "< Back", backX + backW / 2, backY + 6, backHovered ? 0xFF38BDF8 : 0xFFE2E8F0);

		// Close button
		int closeW = 20;
		int closeX = x + width - closeW - 8;
		int closeY = y + 8;
		boolean closeHovered = mouseX >= closeX && mouseX <= closeX + closeW && mouseY >= closeY && mouseY <= closeY + closeW;
		BaseConfigScreen.fillRoundedRect(graphics, closeX, closeY, closeW, closeW, 4, closeHovered ? 0xFF334155 : 0xFF1E293B, closeHovered ? 0xFF94A3B8 : 0xFF475569);
		graphics.drawCenteredString(font, "X", closeX + closeW / 2, closeY + 6, closeHovered ? 0xFFFFFFFF : 0xFFE2E8F0);

		// Filter Toggle Pill: [All Mods] / [Configurable]
		int filterW = width >= 540 ? 110 : 36;
		int filterX = closeX - filterW - 6;
		boolean filterHovered = mouseX >= filterX && mouseX <= filterX + filterW && mouseY >= closeY && mouseY <= closeY + closeW;
		BaseConfigScreen.fillRoundedRect(graphics, filterX, closeY, filterW, closeW, 4, showOnlyConfigurable ? 0xEE_0284C7 : (filterHovered ? 0xFF334155 : 0xFF1E293B), showOnlyConfigurable ? 0xFF_38BDF8 : 0xFF475569);
		if (width >= 540) {
			String filterLabel = showOnlyConfigurable ? "Config Only" : "All Mods";
			graphics.drawCenteredString(font, filterLabel, filterX + filterW / 2, closeY + 6, showOnlyConfigurable ? 0xFFFFFFFF : (filterHovered ? 0xFF38BDF8 : 0xFFCBD5E1));
		} else {
			graphics.drawCenteredString(font, showOnlyConfigurable ? "Cfg" : "All", filterX + filterW / 2, closeY + 6, 0xFFFFFFFF);
		}

		// Search Box
		int searchW = Math.min(140, Math.max(70, width - 360));
		int searchX = filterX - searchW - 6;
		if (searchBox != null && searchW >= 50 && searchX > x + 180) {
			boolean searchFocused = searchBox.isFocused();
			BaseConfigScreen.fillRoundedRect(graphics, searchX, y + 8, searchW, closeW, 4, 0xFF0F172A, searchFocused ? 0xFF38BDF8 : 0xFF475569);
			graphics.drawString(font, ">", searchX + 6, y + 14, 0xFF94A3B8, false);
			searchBox.setX(searchX + 18);
			searchBox.setY(y + 9);
			searchBox.setWidth(searchW - 22);
			searchBox.visible = true;
		} else if (searchBox != null) {
			searchBox.visible = false;
		}

		// Header Titles
		int titleStartX = backX + backW + 10;
		int availableTitleW = Math.max(50, searchX - titleStartX - 10);
		graphics.enableScissor(titleStartX, y + 2, titleStartX + availableTitleW, y + 36);

		graphics.drawString(font, "MOD CONFIGURATIONS", titleStartX, y + 7, 0xFFFFFFFF, true);
		int headW = font.width("MOD CONFIGURATIONS");
		graphics.drawString(font, " > OTHER MODS", titleStartX + headW, y + 7, 0xFF38BDF8, true);

		String sub = "View installed mods and access their configuration settings";
		String subTrimmed = font.substrByWidth(Component.literal(sub), availableTitleW).getString();
		graphics.drawString(font, subTrimmed, titleStartX, y + 19, 0xFFCBD5E1, true);

		graphics.disableScissor();
	}

	private void renderModList(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY) {
		if (filteredMods.isEmpty()) {
			int centerX = x + width / 2;
			int centerY = y + height / 2;
			BaseConfigScreen.safeFakeItem(graphics, Items.BARRIER, centerX - 8, centerY - 24);
			graphics.drawCenteredString(font, "No Matching Mods Found", centerX, centerY - 4, 0xFFFFFFFF);
			graphics.drawCenteredString(font, "Try clearing the search filter.", centerX, centerY + 10, 0xFF94A3B8);
			return;
		}

		int entryHeight = 36;
		int entryGap = 4;
		int totalHeight = filteredMods.size() * (entryHeight + entryGap);
		maxScroll = Math.max(0, totalHeight - height);
		scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);

		graphics.enableScissor(x, y, x + width, y + height);

		int itemY = y - (int) scrollOffset;
		for (int i = 0; i < filteredMods.size(); i++) {
			ModPlatformHelper.ModEntry mod = filteredMods.get(i);
			if (itemY + entryHeight >= y && itemY <= y + height) {
				renderModCard(graphics, x, itemY, width - (maxScroll > 0 ? 8 : 0), entryHeight, mod, mouseX, mouseY);
			}
			itemY += entryHeight + entryGap;
		}

		graphics.disableScissor();

		// Scrollbar
		if (maxScroll > 0) {
			int scrollbarX = x + width - 5;
			int scrollbarH = Math.max(16, (int) ((float) height / totalHeight * height));
			int scrollbarY = y + (int) ((float) scrollOffset / maxScroll * (height - scrollbarH));
			BaseConfigScreen.fillRoundedRect(graphics, scrollbarX, y, 4, height, 2, 0x44_1E293B, 0x00_000000);
			BaseConfigScreen.fillRoundedRect(graphics, scrollbarX, scrollbarY, 4, scrollbarH, 2, 0xFF_38BDF8, 0xFF_7DD3FC);
		}
	}

	private void renderModCard(GuiGraphics graphics, int x, int y, int width, int height, ModPlatformHelper.ModEntry mod, int mouseX, int mouseY) {
		boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
		BaseConfigScreen.fillRoundedRect(graphics, x, y, width, height, 5, isHovered ? 0xD0_1E293B : 0x90_0F172A, isHovered ? 0xFF_38BDF8 : 0x70_334155);

		// Mod Icon (Item fallback)
		ItemStack icon = resolveModIcon(mod.id());
		BaseConfigScreen.safeFakeItem(graphics, icon, x + 8, y + 10);

		// Action Button on right
		boolean canConfigure = mod.hasConfig() || com.reggarf.mods.better_lib.configs.BetterLibConfigApi.hasConfigs(mod.id()) || "better_lib".equalsIgnoreCase(mod.id());
		int btnW = 86;
		int btnH = 20;
		int btnX = x + width - btnW - 8;
		int btnY = y + 8;
		boolean btnHover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;

		if (canConfigure) {
			BaseConfigScreen.fillRoundedRect(graphics, btnX, btnY, btnW, btnH, 4, btnHover ? 0xFF_0284C7 : 0xFF_1E293B, btnHover ? 0xFF_38BDF8 : 0xFF_475569);
			graphics.drawCenteredString(font, "Configure >", btnX + btnW / 2, btnY + 6, btnHover ? 0xFFFFFFFF : 0xFF38BDF8);
		} else {
			BaseConfigScreen.fillRoundedRect(graphics, btnX, btnY, btnW, btnH, 4, 0x44_1E293B, 0x44_334155);
			graphics.drawCenteredString(font, "No Menu", btnX + btnW / 2, btnY + 6, 0xFF64748B);
		}

		// Titles
		int maxTextW = btnX - (x + 32) - 8;
		String nameText = font.substrByWidth(Component.literal(mod.name()), maxTextW).getString();
		graphics.drawString(font, nameText, x + 32, y + 7, 0xFFFFFFFF, true);

		String idVer = mod.id() + (mod.version().isEmpty() ? "" : " v" + mod.version());
		String idVerTrimmed = font.substrByWidth(Component.literal(idVer), maxTextW).getString();
		graphics.drawString(font, idVerTrimmed, x + 32, y + 19, 0xFF94A3B8, true);
	}

	private ItemStack resolveModIcon(String modId) {
		if ("minecraft".equalsIgnoreCase(modId)) return BaseConfigScreen.safeItemStack(Items.GRASS_BLOCK);
		if ("better_lib".equalsIgnoreCase(modId)) return BaseConfigScreen.safeItemStack(Items.COMMAND_BLOCK);
		if (modId.contains("create")) return BaseConfigScreen.safeItemStack(Items.COMPASS);
		if (modId.contains("jei") || modId.contains("rei") || modId.contains("emi")) return BaseConfigScreen.safeItemStack(Items.BOOK);
		if (modId.contains("cloth") || modId.contains("menu")) return BaseConfigScreen.safeItemStack(Items.PAINTING);
		return BaseConfigScreen.safeItemStack(Items.IRON_INGOT);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();
		int winX = getWindowX();
		int winY = getWindowY();
		int winW = getWindowWidth();
		int winH = getWindowHeight();

		// Back button
		int backW = 48;
		int backX = winX + 8;
		int backY = winY + 8;
		if (mouseX >= backX && mouseX <= backX + backW && mouseY >= backY && mouseY <= backY + 20) {
			onClose();
			return true;
		}

		// Close button
		int closeW = 20;
		int closeX = winX + winW - closeW - 8;
		int closeY = winY + 8;
		if (mouseX >= closeX && mouseX <= closeX + closeW && mouseY >= closeY && mouseY <= closeY + closeW) {
			onClose();
			return true;
		}

		// Filter Toggle
		int filterW = winW >= 540 ? 110 : 36;
		int filterX = closeX - filterW - 6;
		if (mouseX >= filterX && mouseX <= filterX + filterW && mouseY >= closeY && mouseY <= closeY + closeW) {
			showOnlyConfigurable = !showOnlyConfigurable;
			scrollOffset = 0;
			updateFilter();
			return true;
		}

		// Content mod card clicks
		int contentY = winY + 38;
		int contentHeight = winH - 42;
		int listX = winX + 6;
		int listY = contentY + 6;
		int listWidth = winW - 12;
		int listHeight = contentHeight - 12;

		if (mouseX >= listX && mouseX <= listX + listWidth && mouseY >= listY && mouseY <= listY + listHeight) {
			int entryHeight = 36;
			int entryGap = 4;
			int itemY = listY - (int) scrollOffset;

			for (ModPlatformHelper.ModEntry mod : filteredMods) {
				if (mouseY >= itemY && mouseY <= itemY + entryHeight) {
					int btnW = 86;
					int btnH = 20;
					int cardW = listWidth - (maxScroll > 0 ? 8 : 0);
					int btnX = listX + cardW - btnW - 8;
					int btnY = itemY + 8;

					if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
						openConfigFor(mod);
						return true;
					}
				}
				itemY += entryHeight + entryGap;
			}
		}

		return super.mouseClicked(event, doubleClick);
	}

	private void openConfigFor(ModPlatformHelper.ModEntry mod) {
		if ("better_lib".equalsIgnoreCase(mod.id())) {
			Minecraft.getInstance().setScreen(new BaseConfigScreen(this, "better_lib"));
			return;
		}
		try {
			// Exact same behavior as Create mod: if mod has Forge/NeoForge configs, open in Better_Lib's config screen
			if (Services.MODS.hasModConfig(mod.id())) {
				Screen screen = Services.MODS.createModConfigScreen(mod.id(), this);
				if (screen != null) {
					Minecraft.getInstance().setScreen(screen);
					return;
				}
			}
			// Fallback to custom config screen factory if available
			Screen screen = Services.MODS.openConfigScreen(mod.id(), this);
			if (screen != null) {
				Minecraft.getInstance().setScreen(screen);
			} else {
				System.err.println("[BetterLib] No config screen found or created for mod: " + mod.id());
			}
		} catch (Throwable t) {
			System.err.println("[BetterLib] Error opening config screen for mod: " + mod.id());
			t.printStackTrace();
		}
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
	public boolean keyPressed(KeyEvent event) {
		int keyCode = event.key();
		if (searchBox != null && searchBox.isFocused()) {
			if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
				searchBox.setFocused(false);
				return true;
			}
			if (searchBox.keyPressed(event)) {
				return true;
			}
		}
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			onClose();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void onClose() {
		Minecraft.getInstance().setScreen(parent);
	}
}
