package com.reggarf.mods.better_lib.popup.gui;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import com.reggarf.mods.better_lib.popup.api.PopupTheme;
import com.reggarf.mods.better_lib.popup.api.WelcomePopupButton;
import com.reggarf.mods.better_lib.popup.api.WelcomePopupData;
import com.reggarf.mods.better_lib.popup.api.WelcomePopupHighlight;
import com.reggarf.mods.better_lib.popup.api.WelcomePopupPlugin;
import com.reggarf.mods.better_lib.popup.api.WelcomePopupPlugins;
import com.reggarf.mods.better_lib.popup.config.WelcomePopupStorage;
import com.reggarf.mods.better_lib.popup.gui.widget.ModEntryButton;
import com.reggarf.mods.better_lib.popup.gui.widget.ModernButton;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class WelcomePopupScreen extends Screen {

    private final Screen parentScreen;
    private final List<WelcomePopupPlugin> plugins;
    private int selectedPluginIndex = 0;
    private int modListScrollOffset = 0;
    private int maxModListScroll = 0;
    private boolean dontShowAgain = false;

    // Interactive widgets
    private final List<ModEntryButton> modButtons = new ArrayList<>();
    private final List<ModernButton> actionButtons = new ArrayList<>();
    private ModernButton copyCodeButton;
    private ModernButton continueButton;
    private ModernButton themeSwitchButton;
    private int copiedFeedbackTicks = 0;

    public WelcomePopupScreen() {
        this(null, null);
    }

    public WelcomePopupScreen(@Nullable Screen parentScreen) {
        this(parentScreen, null);
    }

    public WelcomePopupScreen(@Nullable Screen parentScreen, @Nullable String initialModId) {
        super(Component.translatableWithFallback("better_lib.popup.title", "Welcome"));
        this.parentScreen = parentScreen;
        this.plugins = new ArrayList<>(WelcomePopupPlugins.getEnabledPlugins());

        if (initialModId != null) {
            for (int i = 0; i < plugins.size(); i++) {
                if (plugins.get(i).getModId().equalsIgnoreCase(initialModId)) {
                    this.selectedPluginIndex = i;
                    break;
                }
            }
        } else {
            // Default to the first undismissed mod
            for (int i = 0; i < plugins.size(); i++) {
                if (!WelcomePopupStorage.isModDismissed(plugins.get(i).getModId())) {
                    this.selectedPluginIndex = i;
                    break;
                }
            }
        }

        WelcomePopupPlugin current = getSelectedPlugin();
        this.dontShowAgain = current != null && WelcomePopupStorage.isModDismissed(current.getModId());
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    private WelcomePopupPlugin getSelectedPlugin() {
        if (plugins.isEmpty()) return null;
        int idx = Mth.clamp(selectedPluginIndex, 0, plugins.size() - 1);
        return plugins.get(idx);
    }

    private WelcomePopupData getSelectedData() {
        WelcomePopupPlugin plugin = getSelectedPlugin();
        if (plugin == null) return null;
        WelcomePopupData data = plugin.getPopupData();
        if (data != null) return data;
        return WelcomePopupData.fromJoinMessageSets(plugin.getModName(), plugin.getAccentColor(), plugin.getMessageSets());
    }

    public PopupTheme getActiveTheme() {
        String userChoice = WelcomePopupStorage.getUserTheme();
        if (userChoice != null) {
            return PopupTheme.fromString(userChoice);
        }
        WelcomePopupData data = getSelectedData();
        if (data != null && data.getTheme() != null) {
            return data.getTheme();
        }
        WelcomePopupPlugin plugin = getSelectedPlugin();
        if (plugin != null) {
            return plugin.getTheme();
        }
        return PopupTheme.MODERN;
    }

    private void cycleTheme() {
        PopupTheme current = getActiveTheme();
        PopupTheme next = current.next();
        WelcomePopupStorage.setUserTheme(next.name());
        setupPopupWidgets();
    }

    @Override
    protected void init() {
        super.init();
        setupPopupWidgets();
    }

    protected void setupPopupWidgets() {
        this.clearWidgets();
        this.modButtons.clear();
        this.actionButtons.clear();

        if (plugins.isEmpty()) {
            return;
        }

        boolean isMultiMod = plugins.size() > 1;
        int cardW = isMultiMod ? Math.min(430, this.width - 20) : Math.min(360, this.width - 20);
        int cardH = Math.min(250, this.height - 20);
        int cardX = (this.width - cardW) / 2;
        int cardY = (this.height - cardH) / 2;

        int sidebarW = isMultiMod ? 94 : 0;
        int contentX = cardX + (isMultiMod ? sidebarW + 10 : 16);
        int contentW = cardW - (isMultiMod ? sidebarW + 20 : 32);

        WelcomePopupPlugin currentPlugin = getSelectedPlugin();
        WelcomePopupData data = getSelectedData();
        PopupTheme currentTheme = getActiveTheme();

        int accent = (currentPlugin != null && currentPlugin.getAccentColor() != 0xFF5CB85C)
                ? currentPlugin.getAccentColor()
                : currentTheme.getDefaultAccent();

        // 1. Theme Switch Button in top right of the card
        int themeBtnW = 82;
        int themeBtnH = 15;
        int themeBtnX = cardX + cardW - themeBtnW - 10;
        int themeBtnY = cardY + 7;

        themeSwitchButton = new ModernButton(
                themeBtnX, themeBtnY, themeBtnW, themeBtnH,
                Component.literal("🎨 " + currentTheme.getDisplayName()),
                b -> cycleTheme(),
                WelcomePopupButton.Style.SECONDARY, accent
        );
        themeSwitchButton.setTheme(currentTheme);
        addRenderableWidget(themeSwitchButton);

        // 2. Sidebar Mod Buttons (if multiple mods registered)
        if (isMultiMod) {
            int entryH = 18;
            int visibleEntries = (cardH - 36) / (entryH + 3);
            maxModListScroll = Math.max(0, plugins.size() - visibleEntries);
            modListScrollOffset = Mth.clamp(modListScrollOffset, 0, maxModListScroll);

            int startY = cardY + 24;
            for (int i = 0; i < plugins.size(); i++) {
                int displayIndex = i - modListScrollOffset;
                if (displayIndex >= 0 && displayIndex < visibleEntries) {
                    final int pluginIndex = i;
                    WelcomePopupPlugin plugin = plugins.get(i);
                    int btnY = startY + displayIndex * (entryH + 3);

                    ModEntryButton btn = new ModEntryButton(
                            cardX + 5, btnY, sidebarW - 10, entryH,
                            plugin, pluginIndex == selectedPluginIndex,
                            b -> {
                                this.selectedPluginIndex = pluginIndex;
                                this.copiedFeedbackTicks = 0;
                                WelcomePopupPlugin sel = getSelectedPlugin();
                                if (sel != null) {
                                    this.dontShowAgain = WelcomePopupStorage.isModDismissed(sel.getModId());
                                }
                                setupPopupWidgets();
                            }
                    );
                    btn.setTheme(currentTheme);
                    modButtons.add(btn);
                    addRenderableWidget(btn);
                }
            }
        }

        // 3. Highlight Box Copy Button (if highlight box present)
        boolean hasHighlight = data != null && data.getHighlight() != null;
        if (hasHighlight && data.getHighlight().isCopyable()) {
            WelcomePopupHighlight hl = data.getHighlight();
            int vBoxX = contentX;
            int vBoxY = cardY + 95;
            int vBoxW = contentW;
            int vBoxH = 40;

            int copyBtnW = 56;
            int copyBtnH = 20;
            int copyBtnX = vBoxX + vBoxW - copyBtnW - 8;
            int copyBtnY = vBoxY + (vBoxH - copyBtnH) / 2;

            copyCodeButton = new ModernButton(copyBtnX, copyBtnY, copyBtnW, copyBtnH,
                    Component.translatableWithFallback("better_lib.popup.copy", "Copy"),
                    b -> {
                        if (this.minecraft != null) {
                            this.minecraft.keyboardHandler.setClipboard(hl.getValue());
                            this.copiedFeedbackTicks = 50; // 2.5 seconds
                            updateCopyButtonText();
                        }
                    },
                    WelcomePopupButton.Style.ACTION, accent
            );
            copyCodeButton.setTheme(currentTheme);
            addRenderableWidget(copyCodeButton);
        }

        // 4. Mod Action Buttons
        int footerY = cardY + cardH - 24;
        int maxBtnBottomY = footerY - 8;

        if (data != null && !data.getButtons().isEmpty()) {
            List<WelcomePopupButton> buttons = data.getButtons();
            int btnStartY = hasHighlight ? cardY + 144 : cardY + 96;

            if (buttons.size() == 1) {
                ModernButton mb = createActionButton(contentX, btnStartY, contentW, 20, buttons.get(0), accent, currentTheme);
                actionButtons.add(mb);
                addRenderableWidget(mb);
            } else if (buttons.size() == 2) {
                int btnW = (contentW - 6) / 2;
                actionButtons.add(createActionButton(contentX, btnStartY, btnW, 20, buttons.get(0), accent, currentTheme));
                actionButtons.add(createActionButton(contentX + btnW + 6, btnStartY, btnW, 20, buttons.get(1), accent, currentTheme));
                for (ModernButton mb : actionButtons) addRenderableWidget(mb);
            } else if (buttons.size() == 3) {
                int btnW = (contentW - 8) / 3;
                for (int i = 0; i < 3; i++) {
                    int bx = contentX + i * (btnW + 4);
                    actionButtons.add(createActionButton(bx, btnStartY, btnW, 20, buttons.get(i), accent, currentTheme));
                }
                for (ModernButton mb : actionButtons) addRenderableWidget(mb);
            } else {
                // 4 or more buttons: 2 per row
                int btnW = (contentW - 6) / 2;
                int btnH = 20;
                int gapY = 4;
                for (int i = 0; i < buttons.size(); i++) {
                    int row = i / 2;
                    int col = i % 2;
                    int bx = contentX + col * (btnW + 6);
                    int by = btnStartY + row * (btnH + gapY);
                    if (by + btnH > maxBtnBottomY) break;
                    actionButtons.add(createActionButton(bx, by, btnW, btnH, buttons.get(i), accent, currentTheme));
                }
                for (ModernButton mb : actionButtons) addRenderableWidget(mb);
            }
        }

        // 5. Footer: Continue button at bottom right
        int footerBtnW = 80;
        int footerBtnH = 20;
        int footerBtnX = cardX + cardW - footerBtnW - 14;
        int footerBtnY = footerY;

        continueButton = new ModernButton(footerBtnX, footerBtnY, footerBtnW, footerBtnH,
                Component.translatableWithFallback("better_lib.popup.continue", "Continue"),
                b -> markSeenAndClose(),
                WelcomePopupButton.Style.SECONDARY, accent
        );
        continueButton.setTheme(currentTheme);
        addRenderableWidget(continueButton);
    }

    private ModernButton createActionButton(int x, int y, int w, int h, WelcomePopupButton button, int accent, PopupTheme theme) {
        ModernButton mb = new ModernButton(x, y, w, h, button.getLabel(), b -> {
            if (button.getCustomAction() != null) {
                button.getCustomAction().run();
            } else if (button.getUrl() != null && !button.getUrl().trim().isEmpty()) {
                try {
                    Util.getPlatform().openUri(URI.create(button.getUrl()));
                } catch (Exception ignored) {}
            }
        }, button.getStyle(), accent);
        mb.setTheme(theme);
        return mb;
    }

    private void updateCopyButtonText() {
        if (copyCodeButton != null) {
            if (copiedFeedbackTicks > 0) {
                copyCodeButton.setMessage(Component.translatableWithFallback("better_lib.popup.copied", "\u2713 Copied!")
                        .copy().withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
            } else {
                copyCodeButton.setMessage(Component.translatableWithFallback("better_lib.popup.copy", "Copy"));
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (copiedFeedbackTicks > 0) {
            copiedFeedbackTicks--;
            if (copiedFeedbackTicks == 0) {
                updateCopyButtonText();
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x(); double mouseY = event.y(); int button = event.button();
        boolean isMultiMod = plugins.size() > 1;
        int cardW = isMultiMod ? Math.min(430, this.width - 20) : Math.min(360, this.width - 20);
        int cardH = Math.min(250, this.height - 20);
        int cardX = (this.width - cardW) / 2;
        int cardY = (this.height - cardH) / 2;

        int sidebarW = isMultiMod ? 94 : 0;
        int contentX = cardX + (isMultiMod ? sidebarW + 10 : 16);

        // Checkbox click toggle in footer
        int footerY = cardY + cardH - 24;
        int chkX = contentX;
        int chkY = footerY + 3;
        Component chkLabel = Component.translatableWithFallback("better_lib.popup.dont_show", "Don't show this again");
        int chkW = this.font.width(chkLabel) + 20;
        int chkH = 16;

        if (mouseX >= chkX && mouseX <= chkX + chkW && mouseY >= chkY && mouseY <= chkY + chkH) {
            dontShowAgain = !dontShowAgain;
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean isMultiMod = plugins.size() > 1;
        int cardW = isMultiMod ? Math.min(430, this.width - 20) : Math.min(360, this.width - 20);
        int cardX = (this.width - cardW) / 2;
        int sidebarW = isMultiMod ? 94 : 0;

        if (isMultiMod && mouseX >= cardX && mouseX <= cardX + sidebarW) {
            if (scrollY != 0 && maxModListScroll > 0) {
                modListScrollOffset = Mth.clamp(modListScrollOffset - (int) Math.signum(scrollY), 0, maxModListScroll);
                setupPopupWidgets();
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (keyCode == 256) { // ESC key
            markSeenAndClose();
            return true;
        }
        return super.keyPressed(event);
    }

    private void markSeen() {
        if (dontShowAgain) {
            WelcomePopupStorage.setGlobalEnabled(false);
        }
    }

    private void markSeenAndClose() {
        markSeen();
        onClose();
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fillGradient(0, 0, this.width, this.height, 0x880A0C0E, 0xAA121517);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.extractBackground(graphics, mouseX, mouseY, partialTick);

        if (plugins.isEmpty()) {
            graphics.centeredText(this.font, Component.literal("No Welcome Popups Registered"), this.width / 2, this.height / 2, 0xFFFFFFFF);
            return;
        }

        boolean isMultiMod = plugins.size() > 1;
        int cardW = isMultiMod ? Math.min(430, this.width - 20) : Math.min(360, this.width - 20);
        int cardH = Math.min(250, this.height - 20);
        int cardX = (this.width - cardW) / 2;
        int cardY = (this.height - cardH) / 2;

        int sidebarW = isMultiMod ? 94 : 0;
        int contentX = cardX + (isMultiMod ? sidebarW + 10 : 16);
        int contentW = cardW - (isMultiMod ? sidebarW + 20 : 32);

        WelcomePopupPlugin currentPlugin = getSelectedPlugin();
        WelcomePopupData data = getSelectedData();
        PopupTheme currentTheme = getActiveTheme();

        int accent = (currentPlugin != null && currentPlugin.getAccentColor() != 0xFF5CB85C)
                ? currentPlugin.getAccentColor()
                : currentTheme.getDefaultAccent();

        // 1. Draw Card Panel with Theme colors
        drawCard(graphics, cardX, cardY, cardW, cardH, false, false, accent, currentTheme.getCardBackground(), currentTheme.getCardBorder());

        // Top Accent Strip (or 6-color Pride Rainbow for PRISM theme)
        if (currentTheme.isRainbowBar()) {
            int[] rainbowColors = new int[]{
                    0xFFE40303, // Red
                    0xFFFF8C00, // Orange
                    0xFFFFED00, // Yellow
                    0xFF008026, // Green
                    0xFF004DFF, // Blue
                    0xFF750787  // Purple
            };
            int segW = (cardW - 2) / rainbowColors.length;
            for (int i = 0; i < rainbowColors.length; i++) {
                int sx = cardX + 1 + i * segW;
                int ex = (i == rainbowColors.length - 1) ? cardX + cardW - 1 : sx + segW;
                graphics.fill(sx, cardY + 1, ex, cardY + 3, rainbowColors[i]);
            }
        } else {
            graphics.fill(cardX + 1, cardY + 1, cardX + cardW - 1, cardY + 3, accent);
        }

        // 2. Render Left Sidebar (if multiple mods registered) - sleek & theme-styled
        if (isMultiMod) {
            graphics.fill(cardX + 2, cardY + 3, cardX + sidebarW, cardY + cardH - 2, currentTheme.getSidebarBackground());
            graphics.fill(cardX + sidebarW, cardY + 3, cardX + sidebarW + 1, cardY + cardH - 2, currentTheme.getSidebarBorder());

            Component sidebarHeader = Component.literal("MODS (" + plugins.size() + ")");
            graphics.text(this.font, sidebarHeader, cardX + 8, cardY + 10, currentTheme.getTextSecondary());
            graphics.fill(cardX + 8, cardY + 20, cardX + sidebarW - 6, cardY + 21, currentTheme.getSidebarBorder());
        }

        // 3. Render Right Panel Header (Icon + Title + Subtitle)
        int logoSize = 22;
        int logoX = contentX;
        int logoY = cardY + 10;

        Identifier currentIcon = ModIconHelper.getIcon(currentPlugin);
        if (currentPlugin != null && currentIcon != null) {
            graphics.fill(logoX - 1, logoY - 1, logoX + logoSize + 1, logoY + logoSize + 1, currentTheme.getHighlightBackground());
            graphics.outline(logoX - 1, logoY - 1, logoSize + 2, logoSize + 2, accent);
            graphics.blit(RenderPipelines.GUI_TEXTURED, currentIcon, logoX, logoY, 0.0f, 0.0f, logoSize, logoSize, logoSize, logoSize);
        } else {
            graphics.fill(logoX - 1, logoY - 1, logoX + logoSize + 1, logoY + logoSize + 1, currentTheme.getCardBackground());
            graphics.outline(logoX - 1, logoY - 1, logoSize + 2, logoSize + 2, accent);
            String initial = currentPlugin != null && !currentPlugin.getModName().isEmpty() ? currentPlugin.getModName().substring(0, 1).toUpperCase() : "B";
            int initialW = this.font.width(initial);
            graphics.text(this.font, initial, logoX + (logoSize - initialW) / 2, logoY + (logoSize - 7) / 2, accent);
        }

        int headerTextX = logoX + logoSize + 8;
        String title = data != null ? data.getTitle() : (currentPlugin != null ? currentPlugin.getModName() : "Better Mod");
        graphics.text(this.font, Component.literal(title).withStyle(ChatFormatting.BOLD), headerTextX, logoY, currentTheme.getTextPrimary());

        String subtitle = data != null && data.getSubtitle() != null ? data.getSubtitle() : "OFFICIAL MOD INFO & SUPPORT";
        graphics.text(this.font, Component.literal(subtitle), headerTextX, logoY + 12, currentTheme.getTextSecondary());

        // Header separator
        graphics.fill(contentX, cardY + 36, contentX + contentW, cardY + 37, currentTheme.getCardBorder());

        // 4. Render Description lines
        int descY = cardY + 44;
        if (data != null && !data.getDescriptionLines().isEmpty()) {
            for (Component line : data.getDescriptionLines()) {
                if (descY > cardY + 86) break;
                if (line.getString().isEmpty()) {
                    descY += 3;
                    continue;
                }
                List<FormattedCharSequence> wrapped = this.font.split(line, contentW);
                for (FormattedCharSequence wLine : wrapped) {
                    if (descY > cardY + 86) break;
                    graphics.text(this.font, wLine, contentX, descY, currentTheme.getTextSecondary());
                    descY += 10;
                }
            }
        }

        // 5. Render Highlight Box (Voucher / Discord / Promo / Server IP)
        if (data != null && data.getHighlight() != null) {
            WelcomePopupHighlight hl = data.getHighlight();
            int vBoxX = contentX;
            int vBoxY = cardY + 95;
            int vBoxW = contentW;
            int vBoxH = 40;

            graphics.fill(vBoxX, vBoxY, vBoxX + vBoxW, vBoxY + vBoxH, currentTheme.getHighlightBackground());
            graphics.outline(vBoxX, vBoxY, vBoxW, vBoxH, currentTheme.getHighlightBorder());

            // Highlight label
            graphics.text(this.font,
                    Component.literal(hl.getLabel()).withStyle(ChatFormatting.BOLD),
                    vBoxX + 8, vBoxY + 6, accent);

            // Highlight value in bold golden yellow
            Component valComp = Component.literal(hl.getValue()).withStyle(ChatFormatting.BOLD);
            int valW = this.font.width(valComp);
            graphics.text(this.font, valComp, vBoxX + 8, vBoxY + 20, 0xFFFFD700);

            // Optional badge pill
            if (hl.getBadge() != null && !hl.getBadge().isEmpty()) {
                Component badgeComp = Component.literal(hl.getBadge()).withStyle(ChatFormatting.BOLD);
                int badgeTextW = this.font.width(badgeComp);
                int badgePad = 4;
                int badgeW = badgeTextW + badgePad * 2;
                int badgeH = 12;
                int badgeX = vBoxX + 8 + valW + 8;
                int badgeY = vBoxY + 18;

                graphics.fill(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH, 0xFF3E9B3E);
                graphics.outline(badgeX, badgeY, badgeW, badgeH, 0xFF63C963);
                graphics.text(this.font, badgeComp, badgeX + badgePad, badgeY + 2, 0xFFFFFFFF);
            }
        }

        // 6. Footer: "Don't show this again" checkbox
        int footerY = cardY + cardH - 24;
        int chkX = contentX;
        int chkY = footerY + 5;
        int boxSize = 10;

        graphics.fill(chkX, chkY, chkX + boxSize, chkY + boxSize, currentTheme.getButtonInactiveBackground());
        graphics.outline(chkX, chkY, boxSize, boxSize, dontShowAgain ? accent : currentTheme.getCardBorder());
        if (dontShowAgain) {
            graphics.fill(chkX + 2, chkY + 2, chkX + boxSize - 2, chkY + boxSize - 2, accent);
        }

        Component chkLabel = Component.translatableWithFallback("better_lib.popup.dont_show", "Don't show this again");
        graphics.text(this.font, chkLabel, chkX + boxSize + 6, chkY + 1, currentTheme.getTextSecondary());

        // 7. Render interactive widgets
        for (GuiEventListener child : this.children()) {
            if (child instanceof Renderable renderable) {
                renderable.extractRenderState(graphics, mouseX, mouseY, partialTick);
            }
        }
    }

    public static void drawCard(GuiGraphicsExtractor graphics, int x, int y, int width, int height, boolean selected, boolean hovered, int accent) {
        drawCard(graphics, x, y, width, height, selected, hovered, accent, 0xFF16191B, 0xFF2D3236);
    }

    public static void drawCard(GuiGraphicsExtractor graphics, int x, int y, int width, int height, boolean selected, boolean hovered, int accent, int bg, int border) {
        graphics.fill(x, y, x + width, y + height, bg);
        graphics.fill(x, y, x + width, y + 1, border);
        graphics.fill(x, y + height - 1, x + width, y + height, border);
        graphics.fill(x, y + 1, x + 1, y + height, border);
        graphics.fill(x + width - 1, y, x + width, y + height, border);
    }
}

