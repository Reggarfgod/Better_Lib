package com.reggarf.mods.better_lib.gui.screen;

import com.reggarf.mods.better_lib.config.ConfigBase;
import com.reggarf.mods.better_lib.config.ConfigBase.*;
import com.reggarf.mods.better_lib.gui.util.ModLogoHelper;
import com.reggarf.mods.better_lib.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Universal, high-performance, modular config screen provided by BetterLib.
 * Fully responsive across all Minecraft GUI scales (1, 2, 3, 4, Auto) and screen resolutions.
 * Features modern cyberpunk/dark-themed UI styling, tab navigation, search filtering,
 * automatic mod logo discovery via platform mod containers, loader tags,
 * rich interactive tooltips, live sliders, toggles, enum cyclers, per-entry resets, and zero emojis.
 */
public class BetterConfigScreen extends Screen {

    private static final Map<String, Map<String, ConfigBase>> MOD_CONFIG_REGISTRY = new ConcurrentHashMap<>();

    /* ========================================================================= */
    /* Public Static Registration & Factory API (For Other Mods & Players)       */
    /* ========================================================================= */

    public static void registerLogo(String modId, ResourceLocation logo) {
        ModLogoHelper.registerCustomLogo(modId, logo);
    }

    public static void register(String modId, ConfigBase... configs) {
        if (modId == null || configs == null) return;
        Map<String, ConfigBase> map = MOD_CONFIG_REGISTRY.computeIfAbsent(modId.toLowerCase(Locale.ROOT), k -> new LinkedHashMap<>());
        for (ConfigBase config : configs) {
            if (config != null) {
                map.put(capitalize(config.getName()), config);
            }
        }
    }

    public static void register(String modId, ResourceLocation logo, ConfigBase... configs) {
        registerLogo(modId, logo);
        register(modId, configs);
    }

    public static void register(String modId, Map<String, ConfigBase> configs) {
        if (modId == null || configs == null) return;
        Map<String, ConfigBase> map = MOD_CONFIG_REGISTRY.computeIfAbsent(modId.toLowerCase(Locale.ROOT), k -> new LinkedHashMap<>());
        map.putAll(configs);
    }

    public static void register(String modId, ResourceLocation logo, Map<String, ConfigBase> configs) {
        registerLogo(modId, logo);
        register(modId, configs);
    }

    public static void register(String modId, String tabName, ConfigBase config) {
        if (modId == null || config == null) return;
        Map<String, ConfigBase> map = MOD_CONFIG_REGISTRY.computeIfAbsent(modId.toLowerCase(Locale.ROOT), k -> new LinkedHashMap<>());
        map.put(tabName, config);
    }

    public static boolean hasConfigs(String modId) {
        return modId != null && MOD_CONFIG_REGISTRY.containsKey(modId.toLowerCase(Locale.ROOT));
    }

    public static Map<String, ConfigBase> getConfigs(String modId) {
        if (modId == null) return Collections.emptyMap();
        return MOD_CONFIG_REGISTRY.getOrDefault(modId.toLowerCase(Locale.ROOT), Collections.emptyMap());
    }

    public static ResourceLocation getLogo(String modId) {
        return ModLogoHelper.getOrLoadLogo(modId);
    }

    public static BetterConfigScreen create(Screen parent, String modId) {
        Map<String, ConfigBase> configs = getConfigs(modId);
        ResourceLocation logo = getLogo(modId);
        Component screenTitle = Component.literal(capitalize(modId) + " Settings");
        BetterConfigScreen screen = new BetterConfigScreen(parent, screenTitle, configs, logo);
        screen.setModId(modId);
        return screen;
    }

    public static BetterConfigScreen create(Screen parent, ConfigBase... configs) {
        if (configs.length == 1 && configs[0] != null) {
            return new BetterConfigScreen(parent, configs[0]);
        }
        Map<String, ConfigBase> map = new LinkedHashMap<>();
        for (ConfigBase c : configs) {
            if (c != null) {
                map.put(capitalize(c.getName()), c);
            }
        }
        return new BetterConfigScreen(parent, Component.literal("Configuration"), map);
    }

    public static void open(Screen parent, String modId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.setScreen(create(parent, modId));
        }
    }

    public static void open(Screen parent, ConfigBase... configs) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.setScreen(create(parent, configs));
        }
    }

    public static Builder builder(Screen parent) {
        return new Builder(parent);
    }

    public static class Builder {
        private final Screen parent;
        private Component title = Component.literal("Configuration");
        private ResourceLocation logo;
        private String modId;
        private final Map<String, ConfigBase> tabs = new LinkedHashMap<>();

        public Builder(Screen parent) {
            this.parent = parent;
        }

        public Builder title(Component title) {
            this.title = title;
            return this;
        }

        public Builder title(String title) {
            this.title = Component.literal(title);
            return this;
        }

        public Builder logo(ResourceLocation logo) {
            this.logo = logo;
            return this;
        }

        public Builder modId(String modId) {
            this.modId = modId;
            this.title = Component.literal(capitalize(modId) + " Settings");
            this.logo = getLogo(modId);
            Map<String, ConfigBase> registered = getConfigs(modId);
            if (!registered.isEmpty()) {
                this.tabs.putAll(registered);
            }
            return this;
        }

        public Builder addTab(String tabName, ConfigBase config) {
            if (config != null) {
                this.tabs.put(tabName, config);
            }
            return this;
        }

        public Builder addConfigs(ConfigBase... configs) {
            for (ConfigBase config : configs) {
                if (config != null) {
                    this.tabs.put(capitalize(config.getName()), config);
                }
            }
            return this;
        }

        public BetterConfigScreen build() {
            BetterConfigScreen screen = new BetterConfigScreen(parent, title, tabs, logo);
            if (modId != null) screen.setModId(modId);
            return screen;
        }

        public void open() {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                mc.setScreen(build());
            }
        }
    }

    /* ========================================================================= */
    /* UI Screen State & Fields                                                  */
    /* ========================================================================= */

    private final Screen parent;
    private final Map<String, ConfigBase> configMap;
    private ResourceLocation modLogo;
    private String modId = "better_lib";
    private String activeTab;

    private ConfigList list;
    private EditBox searchBox;
    private String filterText = "";
    private int modifiedCount = 0;
    private int headerHeight = 72;
    private int footerHeight = 40;
    private long openTime;
    private List<Component> hoveredTooltip = null;

    // Responsive cached layout coordinates
    private int searchBoxWidth = 140;
    private int searchBoxX = 0;
    private int saveBtnX = 0;
    private int resetBtnEndX = 0;

    public BetterConfigScreen(Screen parent) {
        this(parent, Component.literal("BetterLib Configuration"), createDefaultConfigs(), getLogo("better_lib"));
        this.modId = "better_lib";
    }

    public BetterConfigScreen(Screen parent, ConfigBase config) {
        this(parent, Component.literal(capitalize(config.getName()) + " Config"), Collections.singletonMap(config.getName(), config), null);
    }

    public BetterConfigScreen(Screen parent, Component title, ConfigBase config) {
        this(parent, title, Collections.singletonMap(config.getName(), config), null);
    }

    public BetterConfigScreen(Screen parent, Component title, Map<String, ConfigBase> configs) {
        this(parent, title, configs, null);
    }

    public BetterConfigScreen(Screen parent, Component title, Map<String, ConfigBase> configs, ResourceLocation modLogo) {
        super(title);
        this.parent = parent;
        this.configMap = new LinkedHashMap<>(configs);
        this.modLogo = modLogo;
        this.activeTab = configMap.keySet().stream().findFirst().orElse("");
        this.openTime = System.currentTimeMillis();
    }

    public BetterConfigScreen setLogo(ResourceLocation logo) {
        this.modLogo = logo;
        return this;
    }

    public BetterConfigScreen setModId(String modId) {
        this.modId = modId;
        if (this.modLogo == null && modId != null) {
            this.modLogo = getLogo(modId);
        }
        return this;
    }

    private static Map<String, ConfigBase> createDefaultConfigs() {
        Map<String, ConfigBase> registered = MOD_CONFIG_REGISTRY.get("better_lib");
        if (registered != null && !registered.isEmpty()) {
            return new LinkedHashMap<>(registered);
        }
        if (!MOD_CONFIG_REGISTRY.isEmpty()) {
            return new LinkedHashMap<>(MOD_CONFIG_REGISTRY.values().iterator().next());
        }
        return Collections.emptyMap();
    }

    @Override
    protected void init() {
        super.init();

        boolean hasMultipleTabs = configMap.size() > 1;
        this.headerHeight = hasMultipleTabs ? 74 : 50;
        this.footerHeight = 38;

        if (this.modLogo == null && this.modId != null) {
            this.modLogo = getLogo(this.modId);
        }

        // Scrollable Config Option List
        this.list = new ConfigList(this.minecraft, this.width, this.height - this.headerHeight - footerHeight, this.headerHeight, 36);
        this.addRenderableWidget(this.list);

        // 1. Responsive Search Box (Adapts to screen width on all GUI scales)
        this.searchBoxWidth = Math.min(150, Math.max(80, this.width / 4));
        this.searchBoxX = this.width - this.searchBoxWidth - 12;
        int searchBoxY = 10;
        this.searchBox = new EditBox(this.font, searchBoxX, searchBoxY, searchBoxWidth, 20, Component.literal("Search"));
        this.searchBox.setHint(Component.literal(this.width < 460 ? "SEARCH" : "[ SEARCH ]"));
        this.searchBox.setValue(this.filterText);
        this.searchBox.setResponder(text -> {
            this.filterText = text.trim().toLowerCase(Locale.ROOT);
            rebuildConfigList();
        });
        this.addRenderableWidget(this.searchBox);

        // 2. Responsive Tab Navigation Bar (Row 2: y = 42)
        if (hasMultipleTabs) {
            int availableTabWidth = this.width - 24;
            int tabCount = configMap.size();
            int maxIndividualTabWidth = Math.max(50, (availableTabWidth - ((tabCount - 1) * 4)) / tabCount);

            int tabX = 12;
            for (Map.Entry<String, ConfigBase> entry : configMap.entrySet()) {
                String tab = entry.getKey();
                int count = entry.getValue() != null ? entry.getValue().getAllValues().size() : 0;
                String label = this.width < 450 ? tab.toUpperCase(Locale.ROOT) : tab.toUpperCase(Locale.ROOT) + " (" + count + ")";
                int idealWidth = this.font.width(label) + 16;
                int tabWidth = Math.min(idealWidth, maxIndividualTabWidth);
                boolean isActive = tab.equals(this.activeTab);

                CoolTabButton tabBtn = new CoolTabButton(tabX, 42, tabWidth, 22, Component.literal(truncate(label, tabWidth - 6)), isActive, btn -> {
                    this.activeTab = tab;
                    this.rebuildWidgets();
                });
                this.addRenderableWidget(tabBtn);
                tabX += tabWidth + 4;
            }
        }

        // 3. Responsive Footer Action Buttons (Bottom Bar: y = height - 30)
        int btnHeight = 22;
        int btnY = this.height - 30;

        int resetWidth = this.width < 450 ? 70 : 90;
        int saveWidth = this.width < 450 ? 95 : 120;
        int cancelWidth = this.width < 450 ? 65 : 80;

        Component resetLabel = Component.literal(this.width < 450 ? "RESET" : "RESET TAB");
        Component saveLabel = Component.literal(this.width < 450 ? "SAVE" : "SAVE & APPLY");
        Component cancelLabel = Component.literal("CANCEL");

        // Left: Reset Active Tab to Defaults
        CoolButton resetBtn = new CoolButton(12, btnY, resetWidth, btnHeight, resetLabel, 0xEF4444, btn -> {
            if (this.list != null) {
                this.list.resetActiveTabToDefaults();
                this.updateModifiedCount();
            }
        });
        this.addRenderableWidget(resetBtn);
        this.resetBtnEndX = 12 + resetWidth;

        // Right: Cancel & Save
        int cancelX = this.width - cancelWidth - 12;
        int saveX = cancelX - saveWidth - 6;
        this.saveBtnX = saveX;

        this.addRenderableWidget(new CoolButton(saveX, btnY, saveWidth, btnHeight, saveLabel, 0x10B981, btn -> {
            this.saveAndClose();
        }));

        this.addRenderableWidget(new CoolButton(cancelX, btnY, cancelWidth, btnHeight, cancelLabel, 0x64748B, btn -> {
            this.onClose();
        }));

        rebuildConfigList();
    }

    public void updateModifiedCount() {
        if (this.list != null) {
            this.modifiedCount = this.list.getModifiedCount();
        }
    }

    public void setTooltip(List<Component> tooltip) {
        this.hoveredTooltip = tooltip;
    }

    private void rebuildConfigList() {
        if (this.list == null) return;
        this.list.clear();

        ConfigBase activeConfig = configMap.get(activeTab);
        if (activeConfig == null) return;

        for (CValue<?, ?> cValue : activeConfig.getAllValues()) {
            if (!filterText.isEmpty()) {
                boolean match = cValue.getName().toLowerCase(Locale.ROOT).contains(filterText);
                if (!match && cValue.getComments().length > 0) {
                    for (String comment : cValue.getComments()) {
                        if (comment.toLowerCase(Locale.ROOT).contains(filterText)) {
                            match = true;
                            break;
                        }
                    }
                }
                if (!match) continue;
            }

            if (cValue instanceof ConfigGroup group) {
                this.list.addConfigEntry(new GroupEntry(group.getName()));
            } else if (cValue instanceof ConfigBool boolVal) {
                this.list.addConfigEntry(new BoolEntry(boolVal, this));
            } else if (cValue instanceof ConfigInt intVal) {
                this.list.addConfigEntry(new IntEntry(intVal, this));
            } else if (cValue instanceof ConfigFloat floatVal) {
                this.list.addConfigEntry(new FloatEntry(floatVal, this));
            } else if (cValue instanceof ConfigString strVal) {
                this.list.addConfigEntry(new StringEntry(strVal, this));
            } else if (cValue instanceof ConfigEnum<?> enumVal) {
                this.list.addConfigEntry(new EnumEntry<>(enumVal, this));
            }
        }
        updateModifiedCount();
    }

    private void saveAndClose() {
        if (this.list != null) {
            this.list.saveAll();
        }
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.hoveredTooltip = null;
        long elapsed = System.currentTimeMillis() - openTime;

        // 1. Deep Futuristic Dark Void Gradient
        graphics.fillGradient(0, 0, this.width, this.height, 0xFB040711, 0xFD0B132B);

        // 2. Atmospheric Aurora Ambient Radiance
        graphics.fillGradient(0, 0, this.width, Math.min(this.height, 120), 0x220284C7, 0x00000000);
        graphics.fillGradient(0, Math.max(0, this.height - 100), this.width, this.height, 0x00000000, 0x1E4338CA);

        // 3. Cyber Matrix Grid with Intersecting Micro-Junctions
        int gridSpacing = 36;
        int gridLineColor = 0x0938BDF8;
        for (int gx = 0; gx < this.width; gx += gridSpacing) {
            graphics.fill(gx, 0, gx + 1, this.height, gridLineColor);
        }
        for (int gy = 0; gy < this.height; gy += gridSpacing) {
            graphics.fill(0, gy, this.width, gy + 1, gridLineColor);
        }

        // 4. Subtle Floating Starfield / Cyber Motes
        renderAmbientMotes(graphics, elapsed);

        // 5. Dynamic Scanning Laser / Hologram Wave
        int scanY = (int) ((elapsed * 0.035f) % (this.height + 120)) - 60;
        if (scanY > -40 && scanY < this.height + 40) {
            graphics.fillGradient(0, scanY, this.width, scanY + 20, 0x0038BDF8, 0x0B38BDF8);
            graphics.fill(0, scanY + 20, this.width, scanY + 21, 0x1838BDF8);
            graphics.fillGradient(0, scanY + 21, this.width, scanY + 40, 0x0B38BDF8, 0x0038BDF8);
        }

        // 6. Header Background Glass Bar with Animated Sweep
        int headerBarHeight = this.headerHeight - 4;
        graphics.fill(0, 0, this.width, headerBarHeight, 0xDD030712);

        float sweep = (elapsed % 2800L) / 2800.0f;
        int sweepX = (int) (sweep * (this.width + 160)) - 80;
        graphics.fill(0, headerBarHeight - 1, this.width, headerBarHeight, 0xFF1E293B);
        graphics.fillGradient(Math.max(0, sweepX - 80), headerBarHeight - 2, Math.min(this.width, sweepX + 80), headerBarHeight, 0x0038BDF8, 0xFF38BDF8);

        // 7. Footer Background Glass Bar
        int footerY = this.height - this.footerHeight;
        graphics.fill(0, footerY, this.width, this.height, 0xDD030712);
        graphics.fill(0, footerY, this.width, footerY + 1, 0xFF1E293B);
        graphics.fillGradient(0, footerY + 1, this.width, footerY + 3, 0x3338BDF8, 0x0038BDF8);

        // 8. List Area Container Frame & Corner Tech Accents
        if (this.list != null) {
            int left = this.list.getRowLeft() - 6;
            int right = this.list.getRowLeft() + this.list.getRowWidth() + 6;
            int top = this.list.getY();
            int bottom = this.list.getY() + this.list.getHeight();

            graphics.fill(left, top, right, bottom, 0x22000000);
            graphics.fill(left - 1, top, left, bottom, 0x2838BDF8);
            graphics.fill(right, top, right + 1, bottom, 0x2838BDF8);

            renderCornerBrackets(graphics, left - 3, top - 2, right + 3, bottom + 2, 0x8838BDF8);
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        // 9. Mod Logo Badge Card (Responsive position & size)
        int logoX = 12;
        int logoY = 8;
        int logoSize = 24;

        graphics.fill(logoX - 1, logoY - 1, logoX + logoSize + 1, logoY + logoSize + 1, 0xFF38BDF8);
        graphics.fill(logoX, logoY, logoX + logoSize, logoY + logoSize, 0xEE0F172A);

        if (modLogo == null && modId != null) {
            modLogo = getLogo(modId);
        }

        if (modLogo != null) {
            try {
                graphics.blit(modLogo, logoX + 2, logoY + 2, 0.0F, 0.0F, logoSize - 4, logoSize - 4, logoSize - 4, logoSize - 4);
            } catch (Exception e) {
                renderFallbackLogoBadge(graphics, logoX, logoY, logoSize);
            }
        } else {
            renderFallbackLogoBadge(graphics, logoX, logoY, logoSize);
        }

        // 10. Mod Title & Platform Loader Tag (Responsively bounded to prevent overlap with searchBox)
        int textStartX = logoX + logoSize + 8;
        int maxHeaderTitleWidth = Math.max(50, this.searchBoxX - textStartX - 8);

        String titleStr = "[ " + this.title.getString().toUpperCase(Locale.ROOT) + " ]";
        String loaderName = Services.HELPER.getPlatformName().toUpperCase(Locale.ROOT);
        String loaderBadge = "[ " + loaderName + " ]";

        int totalTitleBadgeWidth = this.font.width(titleStr) + 6 + this.font.width(loaderBadge);

        if (totalTitleBadgeWidth <= maxHeaderTitleWidth) {
            graphics.drawString(this.font, titleStr, textStartX, 10, 0x38BDF8);
            graphics.drawString(this.font, loaderBadge, textStartX + this.font.width(titleStr) + 6, 10, 0x10B981);
        } else if (this.font.width(titleStr) <= maxHeaderTitleWidth) {
            graphics.drawString(this.font, titleStr, textStartX, 10, 0x38BDF8);
        } else {
            graphics.drawString(this.font, truncate(titleStr, maxHeaderTitleWidth), textStartX, 10, 0x38BDF8);
        }

        // Subtitle Info (Responsively truncated)
        String subtitle;
        if (configMap.size() > 1) {
            subtitle = "TAB: " + activeTab.toUpperCase(Locale.ROOT) + " (" + (list != null ? list.children().size() : 0) + " OPTS)";
        } else {
            subtitle = "TOTAL OPTIONS: " + (list != null ? list.children().size() : 0);
        }
        graphics.drawString(this.font, truncate(subtitle, maxHeaderTitleWidth), textStartX, 22, 0x64748B);

        // Search Match Tag if searching (Rendered cleanly below/beside search box)
        if (!filterText.isEmpty() && list != null) {
            String matchTag = "[ " + list.children().size() + " MATCHES ]";
            int matchTagW = this.font.width(matchTag);
            int matchTagX = Math.max(textStartX, this.searchBoxX + this.searchBoxWidth - matchTagW);
            if (configMap.size() <= 1) {
                graphics.drawString(this.font, matchTag, matchTagX, 32, 0xF59E0B);
            }
        }

        // 11. Modified Changes Status Badge in Footer (Positioned cleanly between Reset and Save buttons)
        int statusLeft = this.resetBtnEndX + 8;
        int statusRight = this.saveBtnX - 8;
        int availableStatusWidth = statusRight - statusLeft;

        if (availableStatusWidth >= 80) {
            if (modifiedCount > 0) {
                String pendingStr = availableStatusWidth >= 140 ? "[ MODIFIED: " + modifiedCount + " UNSAVED ]" : "[ " + modifiedCount + " UNSAVED ]";
                int badgeW = this.font.width(pendingStr) + 8;
                int badgeX = statusLeft + (availableStatusWidth - badgeW) / 2;
                graphics.fill(badgeX, this.height - 27, badgeX + badgeW, this.height - 11, 0x33F59E0B);
                graphics.fill(badgeX, this.height - 27, badgeX + 2, this.height - 11, 0xFFF59E0B);
                graphics.drawString(this.font, pendingStr, badgeX + 4, this.height - 22, 0xF59E0B);
            } else {
                String cleanStr = availableStatusWidth >= 140 ? "[ STATUS: SYNCHRONIZED ]" : "[ SYNCED ]";
                int textW = this.font.width(cleanStr);
                int textX = statusLeft + (availableStatusWidth - textW) / 2;
                graphics.drawString(this.font, cleanStr, textX, this.height - 22, 0x34D399);
            }
        } else if (modifiedCount > 0) {
            // Compact dot badge on very small widths
            graphics.fill(statusLeft + 4, this.height - 22, statusLeft + 10, this.height - 16, 0xFFF59E0B);
        }

        // 12. Floating Rich Tooltip Overlay (Renders above all components)
        if (hoveredTooltip != null && !hoveredTooltip.isEmpty()) {
            graphics.renderComponentTooltip(this.font, hoveredTooltip, mouseX, mouseY);
        }
    }

    private void renderAmbientMotes(GuiGraphics g, long elapsed) {
        int moteCount = 36;
        for (int i = 0; i < moteCount; i++) {
            float speed = 0.012f + ((i * 37) % 10) * 0.002f;
            float rawY = (i * 47.0f - (elapsed * speed)) % this.height;
            int y = (int) (rawY < 0 ? rawY + this.height : rawY);

            float wave = (float) Math.sin((elapsed * 0.0012f) + i * 1.5f);
            int x = (int) (((i * 89.0f) + wave * 14.0f) % this.width);
            if (x < 0) x += this.width;

            float pulse = 0.35f + 0.65f * (float) Math.abs(Math.sin((elapsed * 0.0025f) + i));
            int alpha = (int) (pulse * 90);

            int colorBase = (i % 3 == 0) ? 0x38BDF8 : ((i % 3 == 1) ? 0x34D399 : 0x818CF8);
            int color = (alpha << 24) | (colorBase & 0x00FFFFFF);

            g.fill(x, y, x + (i % 2 == 0 ? 2 : 1), y + (i % 2 == 0 ? 2 : 1), color);
        }
    }

    private void renderCornerBrackets(GuiGraphics g, int left, int top, int right, int bottom, int color) {
        int bracketSize = 6;
        // Top-Left
        g.fill(left, top, left + bracketSize, top + 1, color);
        g.fill(left, top, left + 1, top + bracketSize, color);
        // Top-Right
        g.fill(right - bracketSize, top, right, top + 1, color);
        g.fill(right - 1, top, right, top + bracketSize, color);
        // Bottom-Left
        g.fill(left, bottom - 1, left + bracketSize, bottom, color);
        g.fill(left, bottom - bracketSize, left + 1, bottom, color);
        // Bottom-Right
        g.fill(right - bracketSize, bottom - 1, right, bottom, color);
        g.fill(right - 1, bottom - bracketSize, right, bottom, color);
    }

    private void renderFallbackLogoBadge(GuiGraphics graphics, int logoX, int logoY, int logoSize) {
        String cleanLetters = this.title.getString().replaceAll("[^A-Za-z0-9]", "");
        String letter = cleanLetters.isEmpty() ? "M" : cleanLetters.substring(0, 1).toUpperCase(Locale.ROOT);
        int letterX = logoX + (logoSize - this.font.width(letter)) / 2;
        int letterY = logoY + (logoSize - 8) / 2;
        graphics.drawString(this.font, letter, letterX, letterY, 0x38BDF8);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        String clean = str.replace("_", " ").replace("-", " ");
        String[] parts = clean.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (sb.length() > 0) sb.append(" ");
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase(Locale.ROOT));
        }
        return sb.toString();
    }

    /* ========================================================================= */
    /* UI Custom Modern Widgets (Clean Typography & Zero Emojis)                 */
    /* ========================================================================= */

    public static class CoolButton extends Button {
        private final int accentColor;

        public CoolButton(int x, int y, int width, int height, Component message, int accentColor, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
            this.accentColor = accentColor;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.isHoveredOrFocused();

            int bgTop = hovered ? 0xEE1E293B : 0xCC0F172A;
            int bgBottom = hovered ? 0xEE0F172A : 0xCC020617;
            graphics.fillGradient(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgTop, bgBottom);

            int borderAlpha = hovered ? 0xFF000000 : 0x77000000;
            int border = borderAlpha | (accentColor & 0x00FFFFFF);
            renderOutline(graphics, this.getX(), this.getY(), this.width, this.height, border);

            if (hovered) {
                graphics.fill(this.getX() + 2, this.getY() + this.height - 2, this.getX() + this.width - 2, this.getY() + this.height, (0xFF000000) | (accentColor & 0x00FFFFFF));
            }

            int textColor = hovered ? 0xFFFFFF : 0xE2E8F0;
            int textX = this.getX() + (this.width - Minecraft.getInstance().font.width(this.getMessage())) / 2;
            int textY = this.getY() + (this.height - 8) / 2;
            graphics.drawString(Minecraft.getInstance().font, this.getMessage(), textX, textY, textColor);
        }

        private static void renderOutline(GuiGraphics g, int x, int y, int w, int h, int color) {
            g.fill(x, y, x + w, y + 1, color);
            g.fill(x, y + h - 1, x + w, y + h, color);
            g.fill(x, y, x + 1, y + h, color);
            g.fill(x + w - 1, y, x + w, y + h, color);
        }
    }

    public static class CoolTabButton extends Button {
        private final boolean activeTab;

        public CoolTabButton(int x, int y, int width, int height, Component message, boolean activeTab, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
            this.activeTab = activeTab;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.isHoveredOrFocused();

            int bgTop = activeTab ? 0xF01E293B : (hovered ? 0xAA1E293B : 0x660F172A);
            int bgBottom = activeTab ? 0xF00F172A : (hovered ? 0xAA0F172A : 0x66020617);
            graphics.fillGradient(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgTop, bgBottom);

            int borderColor = activeTab ? 0xFF38BDF8 : (hovered ? 0x9994A3B8 : 0x44475569);
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, borderColor);
            graphics.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + this.height, borderColor);
            graphics.fill(this.getX() + this.width - 1, this.getY(), this.getX() + this.width, this.getY() + this.height, borderColor);

            if (activeTab) {
                graphics.fill(this.getX(), this.getY() + this.height - 2, this.getX() + this.width, this.getY() + this.height, 0xFF38BDF8);
            }

            int textColor = activeTab ? 0x38BDF8 : (hovered ? 0xFFFFFF : 0x94A3B8);
            int textX = this.getX() + (this.width - Minecraft.getInstance().font.width(this.getMessage())) / 2;
            int textY = this.getY() + (this.height - 8) / 2;
            graphics.drawString(Minecraft.getInstance().font, this.getMessage(), textX, textY, textColor);
        }
    }

    public static class CoolToggleWidget extends AbstractButton {
        private boolean state;
        private final Consumer<Boolean> onToggle;

        public CoolToggleWidget(int x, int y, int width, int height, boolean initialState, Consumer<Boolean> onToggle) {
            super(x, y, width, height, Component.empty());
            this.state = initialState;
            this.onToggle = onToggle;
            updateMessage();
        }

        public void setState(boolean state) {
            this.state = state;
            updateMessage();
        }

        public boolean getState() {
            return this.state;
        }

        private void updateMessage() {
            this.setMessage(Component.literal(state ? "ON" : "OFF"));
        }

        @Override
        public void onPress() {
            this.state = !this.state;
            updateMessage();
            this.onToggle.accept(this.state);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.isHoveredOrFocused();

            int trackBg = state ? (hovered ? 0xF0059669 : 0xCC10B981) : (hovered ? 0xF0334155 : 0xAA1E293B);
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, trackBg);

            int trackBorder = state ? (hovered ? 0xFF6EE7B7 : 0xFF34D399) : (hovered ? 0xFF94A3B8 : 0xFF475569);
            renderOutline(graphics, this.getX(), this.getY(), this.width, this.height, trackBorder);

            int thumbWidth = Math.max(12, (this.width / 2) - 2);
            int thumbX = state ? (this.getX() + this.width - thumbWidth - 2) : (this.getX() + 2);
            int thumbY = this.getY() + 2;
            int thumbH = this.height - 4;

            // Slider thumb
            graphics.fill(thumbX, thumbY, thumbX + thumbWidth, thumbY + thumbH, 0xFFFFFFFF);

            // Status label inside track (only if enough width)
            if (this.width >= 40) {
                int textX = state ? (this.getX() + 4) : (this.getX() + this.width - 20);
                int textY = this.getY() + (this.height - 8) / 2;
                int textColor = state ? 0xD1FAE5 : 0x94A3B8;
                graphics.drawString(Minecraft.getInstance().font, state ? "ON" : "OFF", textX, textY, textColor);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }

        private static void renderOutline(GuiGraphics g, int x, int y, int w, int h, int color) {
            g.fill(x, y, x + w, y + 1, color);
            g.fill(x, y + h - 1, x + w, y + h, color);
            g.fill(x, y, x + 1, y + h, color);
            g.fill(x + w - 1, y, x + w, y + h, color);
        }
    }

    public static class CoolSliderWidget extends AbstractSliderButton {
        private final double min;
        private final double max;
        private final boolean isInteger;
        private final Consumer<Double> onApply;

        public CoolSliderWidget(int x, int y, int width, int height, double min, double max, double current, boolean isInteger, Consumer<Double> onApply) {
            super(x, y, width, height, Component.empty(), (current - min) / (max - min));
            this.min = min;
            this.max = max;
            this.isInteger = isInteger;
            this.onApply = onApply;
            this.value = Mth.clamp((current - min) / (max - min), 0.0, 1.0);
            updateMessage();
        }

        public double getRealValue() {
            double val = min + this.value * (max - min);
            return isInteger ? Math.round(val) : Math.round(val * 100.0) / 100.0;
        }

        public void setRealValue(double val) {
            this.value = Mth.clamp((val - min) / (max - min), 0.0, 1.0);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            if (isInteger) {
                this.setMessage(Component.literal(String.valueOf((int) Math.round(getRealValue()))));
            } else {
                this.setMessage(Component.literal(String.format(Locale.ROOT, "%.2f", getRealValue())));
            }
        }

        @Override
        protected void applyValue() {
            this.onApply.accept(getRealValue());
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.isHoveredOrFocused();

            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xCC0F172A);

            int progressWidth = (int) (this.value * (this.width - 4));
            if (progressWidth > 0) {
                graphics.fillGradient(this.getX() + 2, this.getY() + 2, this.getX() + 2 + progressWidth, this.getY() + this.height - 2, 0xAA0284C7, 0xEE38BDF8);
            }

            int border = hovered ? 0xFF38BDF8 : 0x88475569;
            renderOutline(graphics, this.getX(), this.getY(), this.width, this.height, border);

            int thumbX = this.getX() + 2 + (int) (this.value * Math.max(1, this.width - 8));
            graphics.fill(thumbX, this.getY() + 1, thumbX + 4, this.getY() + this.height - 1, 0xFFFFFFFF);

            int textX = this.getX() + (this.width - Minecraft.getInstance().font.width(this.getMessage())) / 2;
            int textY = this.getY() + (this.height - 8) / 2;
            graphics.drawString(Minecraft.getInstance().font, this.getMessage(), textX, textY, hovered ? 0xFFFFFF : 0xE2E8F0);
        }

        private static void renderOutline(GuiGraphics g, int x, int y, int w, int h, int color) {
            g.fill(x, y, x + w, y + 1, color);
            g.fill(x, y + h - 1, x + w, y + h, color);
            g.fill(x, y, x + 1, y + h, color);
            g.fill(x + w - 1, y, x + w, y + h, color);
        }
    }

    public static class CoolCycleWidget<T extends Enum<T>> extends AbstractButton {
        private final T[] values;
        private int currentIndex;
        private final Consumer<T> onCycle;

        public CoolCycleWidget(int x, int y, int width, int height, Class<T> enumClass, T current, Consumer<T> onCycle) {
            super(x, y, width, height, Component.empty());
            this.values = enumClass.getEnumConstants();
            this.currentIndex = current != null ? current.ordinal() : 0;
            this.onCycle = onCycle;
            updateMessage();
        }

        public void setValue(T val) {
            if (val != null) {
                this.currentIndex = val.ordinal();
                updateMessage();
            }
        }

        public T getValue() {
            return values[currentIndex];
        }

        private void updateMessage() {
            this.setMessage(Component.literal(values[currentIndex].name()));
        }

        @Override
        public void onPress() {
            this.currentIndex = (this.currentIndex + 1) % values.length;
            updateMessage();
            this.onCycle.accept(values[currentIndex]);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.isHoveredOrFocused();

            int bgTop = hovered ? 0xEE1E293B : 0xCC0F172A;
            int bgBottom = hovered ? 0xEE0F172A : 0xCC020617;
            graphics.fillGradient(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgTop, bgBottom);

            int border = hovered ? 0xFF38BDF8 : 0x88475569;
            renderOutline(graphics, this.getX(), this.getY(), this.width, this.height, border);

            if (this.width >= 60) {
                graphics.drawString(Minecraft.getInstance().font, "<", this.getX() + 4, this.getY() + (this.height - 8) / 2, 0x64748B);
                graphics.drawString(Minecraft.getInstance().font, ">", this.getX() + this.width - 8, this.getY() + (this.height - 8) / 2, 0x64748B);
            }

            String label = values[currentIndex].name();
            String visibleLabel = truncate(label, Math.max(20, this.width - 16));
            int textX = this.getX() + (this.width - Minecraft.getInstance().font.width(visibleLabel)) / 2;
            int textY = this.getY() + (this.height - 8) / 2;
            graphics.drawString(Minecraft.getInstance().font, visibleLabel, textX, textY, hovered ? 0x38BDF8 : 0xE2E8F0);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }

        private static void renderOutline(GuiGraphics g, int x, int y, int w, int h, int color) {
            g.fill(x, y, x + w, y + 1, color);
            g.fill(x, y + h - 1, x + w, y + h, color);
            g.fill(x, y, x + 1, y + h, color);
            g.fill(x + w - 1, y, x + w, y + h, color);
        }
    }

    public static class CoolResetButton extends Button {
        public CoolResetButton(int x, int y, OnPress onPress) {
            super(x, y, 20, 20, Component.literal("R"), onPress, DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.isHoveredOrFocused();
            int bg = hovered ? 0xEE334155 : 0x881E293B;
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bg);

            int border = hovered ? 0xFFF59E0B : 0x5564748B;
            renderOutline(graphics, this.getX(), this.getY(), this.width, this.height, border);

            int textColor = hovered ? 0xF59E0B : 0x94A3B8;
            int textX = this.getX() + (this.width - Minecraft.getInstance().font.width(this.getMessage())) / 2;
            int textY = this.getY() + (this.height - 8) / 2;
            graphics.drawString(Minecraft.getInstance().font, this.getMessage(), textX, textY, textColor);
        }

        private static void renderOutline(GuiGraphics g, int x, int y, int w, int h, int color) {
            g.fill(x, y, x + w, y + 1, color);
            g.fill(x, y + h - 1, x + w, y + h, color);
            g.fill(x, y, x + 1, y + h, color);
            g.fill(x + w - 1, y, x + w, y + h, color);
        }
    }

    /* ========================================================================= */
    /* Container List & Entry Implementations                                    */
    /* ========================================================================= */

    private static class ConfigList extends ContainerObjectSelectionList<ConfigEntry> {

        public ConfigList(Minecraft mc, int width, int height, int y, int itemHeight) {
            super(mc, width, height, y, itemHeight);
        }

        public void clear() {
            this.clearEntries();
        }

        public void addConfigEntry(ConfigEntry entry) {
            this.addEntry(entry);
        }

        public int getModifiedCount() {
            int count = 0;
            for (ConfigEntry entry : children()) {
                if (entry.isModified()) {
                    count++;
                }
            }
            return count;
        }

        public void saveAll() {
            for (ConfigEntry entry : children()) {
                if (entry.isModified()) {
                    entry.save();
                }
            }
        }

        public void resetActiveTabToDefaults() {
            for (ConfigEntry entry : children()) {
                entry.resetToDefault();
            }
        }

        @Override
        public int getRowWidth() {
            return Math.min(this.width - 24, 560);
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getRowLeft() + this.getRowWidth() + 4;
        }
    }

    private static abstract class ConfigEntry extends ContainerObjectSelectionList.Entry<ConfigEntry> {
        public abstract boolean isModified();
        public abstract void save();
        public abstract void resetToDefault();
    }

    private static class GroupEntry extends ConfigEntry {
        private final String groupName;

        public GroupEntry(String groupName) {
            this.groupName = groupName;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            int bannerY = top + 5;
            int bannerH = height - 8;

            graphics.fillGradient(left, bannerY, left + width, bannerY + bannerH, 0xEE1E293B, 0xAA0F172A);
            graphics.fill(left, bannerY, left + 4, bannerY + bannerH, 0xFF38BDF8);
            graphics.fill(left, bannerY, left + width, bannerY + 1, 0x5538BDF8);
            graphics.fill(left, bannerY + bannerH - 1, left + width, bannerY + bannerH, 0x5538BDF8);

            String title = "// " + capitalize(groupName).toUpperCase(Locale.ROOT);
            graphics.drawString(Minecraft.getInstance().font, truncate(title, width - 20), left + 12, bannerY + (bannerH - 8) / 2, 0x38BDF8);
        }

        @Override public boolean isModified() { return false; }
        @Override public void save() {}
        @Override public void resetToDefault() {}
        @Override public List<? extends GuiEventListener> children() { return Collections.emptyList(); }
        @Override public List<? extends NarratableEntry> narratables() { return Collections.emptyList(); }
    }

    private static class BoolEntry extends ConfigEntry {
        private final ConfigBool config;
        private final CoolToggleWidget toggle;
        private final CoolResetButton resetBtn;
        private final String comments;
        private final BetterConfigScreen screen;

        public BoolEntry(ConfigBool config, BetterConfigScreen screen) {
            this.config = config;
            this.screen = screen;
            this.comments = String.join(" ", config.getComments());
            this.toggle = new CoolToggleWidget(0, 0, 60, 20, config.get(), state -> screen.updateModifiedCount());
            this.resetBtn = new CoolResetButton(0, 0, btn -> {
                resetToDefault();
                screen.updateModifiedCount();
            });
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            renderEntryBackground(graphics, left, top, width, height, isMouseOver, isModified());

            // Type Badge [BOOL]
            int badgeW = renderTypeBadge(graphics, "[BOOL]", 0x10B981, left + 8, top + 7);

            int toggleW = Math.min(60, Math.max(44, (width - 100) / 4));
            int resetX = left + width - 24;
            int toggleX = resetX - toggleW - 4;

            this.toggle.setWidth(toggleW);
            this.toggle.setX(toggleX);
            this.toggle.setY(top + (height - 20) / 2);
            this.toggle.render(graphics, mouseX, mouseY, partialTick);

            this.resetBtn.setX(resetX);
            this.resetBtn.setY(top + (height - 20) / 2);
            this.resetBtn.render(graphics, mouseX, mouseY, partialTick);

            int textLeft = left + 8 + badgeW + 6;
            int maxTextWidth = Math.max(40, toggleX - textLeft - 6);
            int labelY = top + (comments.isEmpty() ? (height - 8) / 2 : 6);

            graphics.drawString(Minecraft.getInstance().font, truncate(formatName(config.getName()), maxTextWidth), textLeft, labelY, 0xF1F5F9);

            if (!comments.isEmpty()) {
                graphics.drawString(Minecraft.getInstance().font, truncate(comments, maxTextWidth), textLeft, labelY + 12, 0x64748B);
            }

            if (isMouseOver && mouseX < toggleX) {
                screen.setTooltip(buildTooltip(config.getName(), comments, String.valueOf(config.getDefaultValue()), null, isModified()));
            }
        }

        @Override public boolean isModified() { return toggle.getState() != config.get(); }
        @Override public void save() { config.set(toggle.getState()); }
        @Override public void resetToDefault() { toggle.setState(config.getDefaultValue()); }
        @Override public List<? extends GuiEventListener> children() { return Arrays.asList(toggle, resetBtn); }
        @Override public List<? extends NarratableEntry> narratables() { return Arrays.asList(toggle, resetBtn); }
    }

    private static class IntEntry extends ConfigEntry {
        private final ConfigInt config;
        private final CoolSliderWidget slider;
        private final CoolResetButton resetBtn;
        private final String comments;
        private final BetterConfigScreen screen;

        public IntEntry(ConfigInt config, BetterConfigScreen screen) {
            this.config = config;
            this.screen = screen;
            this.comments = String.join(" ", config.getComments());
            this.slider = new CoolSliderWidget(0, 0, 90, 20, config.getMin(), config.getMax(), config.get(), true, val -> screen.updateModifiedCount());
            this.resetBtn = new CoolResetButton(0, 0, btn -> {
                resetToDefault();
                screen.updateModifiedCount();
            });
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            renderEntryBackground(graphics, left, top, width, height, isMouseOver, isModified());

            int badgeW = renderTypeBadge(graphics, "[INT]", 0x38BDF8, left + 8, top + 7);

            int sliderW = Math.min(100, Math.max(65, (width - 120) / 3));
            int resetX = left + width - 24;
            int sliderX = resetX - sliderW - 4;

            this.slider.setWidth(sliderW);
            this.slider.setX(sliderX);
            this.slider.setY(top + (height - 20) / 2);
            this.slider.render(graphics, mouseX, mouseY, partialTick);

            this.resetBtn.setX(resetX);
            this.resetBtn.setY(top + (height - 20) / 2);
            this.resetBtn.render(graphics, mouseX, mouseY, partialTick);

            int textLeft = left + 8 + badgeW + 6;
            int maxTextWidth = Math.max(40, sliderX - textLeft - 6);
            int labelY = top + (comments.isEmpty() ? (height - 8) / 2 : 6);

            graphics.drawString(Minecraft.getInstance().font, truncate(formatName(config.getName()), maxTextWidth), textLeft, labelY, 0xF1F5F9);

            if (!comments.isEmpty()) {
                graphics.drawString(Minecraft.getInstance().font, truncate(comments, maxTextWidth), textLeft, labelY + 12, 0x64748B);
            }

            if (isMouseOver && mouseX < sliderX) {
                String range = config.getMin() + " -> " + config.getMax();
                screen.setTooltip(buildTooltip(config.getName(), comments, String.valueOf(config.getDefaultValue()), range, isModified()));
            }
        }

        @Override
        public boolean isModified() {
            return (int) Math.round(slider.getRealValue()) != config.get();
        }

        @Override
        public void save() {
            config.set((int) Math.round(slider.getRealValue()));
        }

        @Override public void resetToDefault() { slider.setRealValue(config.getDefaultValue()); }
        @Override public List<? extends GuiEventListener> children() { return Arrays.asList(slider, resetBtn); }
        @Override public List<? extends NarratableEntry> narratables() { return Arrays.asList(slider, resetBtn); }
    }

    private static class FloatEntry extends ConfigEntry {
        private final ConfigFloat config;
        private final CoolSliderWidget slider;
        private final CoolResetButton resetBtn;
        private final String comments;
        private final BetterConfigScreen screen;

        public FloatEntry(ConfigFloat config, BetterConfigScreen screen) {
            this.config = config;
            this.screen = screen;
            this.comments = String.join(" ", config.getComments());
            this.slider = new CoolSliderWidget(0, 0, 90, 20, config.getMin(), config.getMax(), config.get(), false, val -> screen.updateModifiedCount());
            this.resetBtn = new CoolResetButton(0, 0, btn -> {
                resetToDefault();
                screen.updateModifiedCount();
            });
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            renderEntryBackground(graphics, left, top, width, height, isMouseOver, isModified());

            int badgeW = renderTypeBadge(graphics, "[NUM]", 0x818CF8, left + 8, top + 7);

            int sliderW = Math.min(100, Math.max(65, (width - 120) / 3));
            int resetX = left + width - 24;
            int sliderX = resetX - sliderW - 4;

            this.slider.setWidth(sliderW);
            this.slider.setX(sliderX);
            this.slider.setY(top + (height - 20) / 2);
            this.slider.render(graphics, mouseX, mouseY, partialTick);

            this.resetBtn.setX(resetX);
            this.resetBtn.setY(top + (height - 20) / 2);
            this.resetBtn.render(graphics, mouseX, mouseY, partialTick);

            int textLeft = left + 8 + badgeW + 6;
            int maxTextWidth = Math.max(40, sliderX - textLeft - 6);
            int labelY = top + (comments.isEmpty() ? (height - 8) / 2 : 6);

            graphics.drawString(Minecraft.getInstance().font, truncate(formatName(config.getName()), maxTextWidth), textLeft, labelY, 0xF1F5F9);

            if (!comments.isEmpty()) {
                graphics.drawString(Minecraft.getInstance().font, truncate(comments, maxTextWidth), textLeft, labelY + 12, 0x64748B);
            }

            if (isMouseOver && mouseX < sliderX) {
                String range = String.format(Locale.ROOT, "%.2f -> %.2f", config.getMin(), config.getMax());
                screen.setTooltip(buildTooltip(config.getName(), comments, String.valueOf(config.getDefaultValue()), range, isModified()));
            }
        }

        @Override
        public boolean isModified() {
            return Math.abs(slider.getRealValue() - config.get()) > 1e-5;
        }

        @Override
        public void save() {
            config.set(slider.getRealValue());
        }

        @Override public void resetToDefault() { slider.setRealValue(config.getDefaultValue()); }
        @Override public List<? extends GuiEventListener> children() { return Arrays.asList(slider, resetBtn); }
        @Override public List<? extends NarratableEntry> narratables() { return Arrays.asList(slider, resetBtn); }
    }

    private static class StringEntry extends ConfigEntry {
        private final ConfigString config;
        private final EditBox input;
        private final CoolResetButton resetBtn;
        private final String comments;
        private final BetterConfigScreen screen;

        public StringEntry(ConfigString config, BetterConfigScreen screen) {
            this.config = config;
            this.screen = screen;
            this.comments = String.join(" ", config.getComments());
            this.input = new EditBox(Minecraft.getInstance().font, 0, 0, 90, 20, Component.empty());
            this.input.setValue(config.get() != null ? config.get() : "");
            this.input.setResponder(val -> screen.updateModifiedCount());
            this.resetBtn = new CoolResetButton(0, 0, btn -> {
                resetToDefault();
                screen.updateModifiedCount();
            });
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            renderEntryBackground(graphics, left, top, width, height, isMouseOver, isModified());

            int badgeW = renderTypeBadge(graphics, "[STR]", 0xF59E0B, left + 8, top + 7);

            int inputW = Math.min(100, Math.max(65, (width - 120) / 3));
            int resetX = left + width - 24;
            int inputX = resetX - inputW - 4;

            this.input.setWidth(inputW);
            this.input.setX(inputX);
            this.input.setY(top + (height - 20) / 2);
            this.input.render(graphics, mouseX, mouseY, partialTick);

            this.resetBtn.setX(resetX);
            this.resetBtn.setY(top + (height - 20) / 2);
            this.resetBtn.render(graphics, mouseX, mouseY, partialTick);

            int textLeft = left + 8 + badgeW + 6;
            int maxTextWidth = Math.max(40, inputX - textLeft - 6);
            int labelY = top + (comments.isEmpty() ? (height - 8) / 2 : 6);

            graphics.drawString(Minecraft.getInstance().font, truncate(formatName(config.getName()), maxTextWidth), textLeft, labelY, 0xF1F5F9);

            if (!comments.isEmpty()) {
                graphics.drawString(Minecraft.getInstance().font, truncate(comments, maxTextWidth), textLeft, labelY + 12, 0x64748B);
            }

            if (isMouseOver && mouseX < inputX) {
                screen.setTooltip(buildTooltip(config.getName(), comments, "\"" + config.getDefaultValue() + "\"", null, isModified()));
            }
        }

        @Override public boolean isModified() { return !input.getValue().equals(config.get()); }
        @Override public void save() { config.set(input.getValue()); }
        @Override public void resetToDefault() { input.setValue(config.getDefaultValue() != null ? config.getDefaultValue() : ""); }
        @Override public List<? extends GuiEventListener> children() { return Arrays.asList(input, resetBtn); }
        @Override public List<? extends NarratableEntry> narratables() { return Arrays.asList(input, resetBtn); }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static class EnumEntry<T extends Enum<T>> extends ConfigEntry {
        private final ConfigEnum<T> config;
        private final CoolCycleWidget<T> cycleWidget;
        private final CoolResetButton resetBtn;
        private final String comments;
        private final BetterConfigScreen screen;

        public EnumEntry(ConfigEnum<T> config, BetterConfigScreen screen) {
            this.config = config;
            this.screen = screen;
            this.comments = String.join(" ", config.getComments());
            Class<T> enumClass = (Class<T>) config.get().getClass();
            this.cycleWidget = new CoolCycleWidget<>(0, 0, 90, 20, enumClass, config.get(), val -> screen.updateModifiedCount());
            this.resetBtn = new CoolResetButton(0, 0, btn -> {
                resetToDefault();
                screen.updateModifiedCount();
            });
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            renderEntryBackground(graphics, left, top, width, height, isMouseOver, isModified());

            int badgeW = renderTypeBadge(graphics, "[ENUM]", 0xC084FC, left + 8, top + 7);

            int widgetW = Math.min(100, Math.max(65, (width - 120) / 3));
            int resetX = left + width - 24;
            int widgetX = resetX - widgetW - 4;

            this.cycleWidget.setWidth(widgetW);
            this.cycleWidget.setX(widgetX);
            this.cycleWidget.setY(top + (height - 20) / 2);
            this.cycleWidget.render(graphics, mouseX, mouseY, partialTick);

            this.resetBtn.setX(resetX);
            this.resetBtn.setY(top + (height - 20) / 2);
            this.resetBtn.render(graphics, mouseX, mouseY, partialTick);

            int textLeft = left + 8 + badgeW + 6;
            int maxTextWidth = Math.max(40, widgetX - textLeft - 6);
            int labelY = top + (comments.isEmpty() ? (height - 8) / 2 : 6);

            graphics.drawString(Minecraft.getInstance().font, truncate(formatName(config.getName()), maxTextWidth), textLeft, labelY, 0xF1F5F9);

            if (!comments.isEmpty()) {
                graphics.drawString(Minecraft.getInstance().font, truncate(comments, maxTextWidth), textLeft, labelY + 12, 0x64748B);
            }

            if (isMouseOver && mouseX < widgetX) {
                screen.setTooltip(buildTooltip(config.getName(), comments, String.valueOf(config.getDefaultValue()), "Cycle Options", isModified()));
            }
        }

        @Override public boolean isModified() { return cycleWidget.getValue() != config.get(); }
        @Override public void save() { config.set(cycleWidget.getValue()); }
        @Override public void resetToDefault() { cycleWidget.setValue((T) config.getDefaultValue()); }
        @Override public List<? extends GuiEventListener> children() { return Arrays.asList(cycleWidget, resetBtn); }
        @Override public List<? extends NarratableEntry> narratables() { return Arrays.asList(cycleWidget, resetBtn); }
    }

    private static int renderTypeBadge(GuiGraphics g, String label, int color, int x, int y) {
        int textW = Minecraft.getInstance().font.width(label);
        int w = textW + 4;
        int h = 11;
        g.fill(x, y, x + w, y + h, 0x44000000);
        g.fill(x, y, x + 1, y + h, color);
        g.drawString(Minecraft.getInstance().font, label, x + 2, y + 2, color);
        return w;
    }

    private static void renderEntryBackground(GuiGraphics g, int left, int top, int width, int height, boolean hovered, boolean modified) {
        int bg = hovered ? 0x661E293B : 0x33111827;
        g.fill(left, top, left + width, top + height, bg);

        if (modified) {
            g.fill(left, top, left + 3, top + height, 0xFFF59E0B);
            g.fill(left + width - 2, top, left + width, top + height, 0x66F59E0B);
        } else if (hovered) {
            g.fill(left, top, left + 3, top + height, 0xFF38BDF8);
        }
    }

    private static List<Component> buildTooltip(String name, String comments, String defaultValue, String range, boolean modified) {
        List<Component> list = new ArrayList<>();
        list.add(Component.literal(formatName(name)).withColor(0x38BDF8));
        if (!comments.isEmpty()) {
            list.add(Component.literal(comments).withColor(0x94A3B8));
        }
        list.add(Component.literal("Key: " + name).withColor(0x64748B));
        if (defaultValue != null) {
            list.add(Component.literal("Default: " + defaultValue).withColor(0x34D399));
        }
        if (range != null) {
            list.add(Component.literal("Range: " + range).withColor(0x818CF8));
        }
        if (modified) {
            list.add(Component.literal("[ UNSAVED MODIFICATION ]").withColor(0xF59E0B));
        }
        return list;
    }

    private static String formatName(String raw) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (i > 0 && Character.isUpperCase(c) && !Character.isUpperCase(raw.charAt(i - 1))) {
                result.append(" ");
            }
            result.append(c);
        }
        return capitalize(result.toString());
    }

    private static String truncate(String text, int maxWidth) {
        if (text == null) return "";
        if (maxWidth <= 0) return "";
        if (Minecraft.getInstance().font.width(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "..";
        int targetWidth = maxWidth - Minecraft.getInstance().font.width(ellipsis);
        if (targetWidth <= 0) return ellipsis;
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Minecraft.getInstance().font.width(sb.toString() + c) > targetWidth) {
                break;
            }
            sb.append(c);
        }
        return sb + ellipsis;
    }
}
