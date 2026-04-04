package com.reggarf.mods.better_lib.config.gui;

import com.reggarf.mods.better_lib.config.annotation.Config;
import com.reggarf.mods.better_lib.config.core.BetterConfigManager;
import com.reggarf.mods.better_lib.config.helper.BetterEntryBuilder;
import com.reggarf.mods.better_lib.config.helper.ConfigPermissionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.capitalize;

public class BetterConfigScreen extends Screen {

    private final Screen parent;
    private final Object config;
    private final List<BetterEntryBuilder> entries;
    private final String configName;
    //private final ResourceLocation background;
    private final List<WidgetData> widgetData = new ArrayList<>();
    private final Map<String, String> cachedTextValues = new HashMap<>();
    private ConfigScrollArea scrollArea;


    /* ================= ADDED ================= */
    private final Minecraft mc = Minecraft.getInstance();
    private boolean canEditServerConfig;
    /* ======================================== */

    public BetterConfigScreen(Screen parent, Component title, Object config, List<BetterEntryBuilder> entries, String name, String bgTexture) {
        super(title);
        this.parent = parent;
        this.config = config;
        this.entries = entries;
        this.configName = name;
        //this.background = bgTexture != null ? ResourceLocation.tryParse(bgTexture) : null;
    }


    @Override
    protected void init() {

        this.clearWidgets();

        this.canEditServerConfig =
                ConfigPermissionHelper.canClientEditServerConfig(mc);

        cachedTextValues.clear();
        for (WidgetData wd : widgetData) {
            if (wd.type().equals("text") && wd.widget() instanceof EditBox eb) {
                cachedTextValues.put(wd.fieldName(), eb.getValue());
            }
        }
        widgetData.clear();

        int panelWidth = 250;
        int panelHeight = height - 180;
        int centerX = width / 2;
        int panelX = centerX - (panelWidth / 2);
        int panelY = 90;

        scrollArea = new ConfigScrollArea(panelX, panelY, panelWidth, panelHeight);

        String modid = (configName != null && !configName.isEmpty()) ? configName : "assets/better_lib";

        for (BetterEntryBuilder entry : entries) {
            var data = entry.build();
            String fieldName = sanitizeFieldName(data.label().getString(), modid);
            String labelKey = "config." + modid + "." + fieldName;
            String descriptionKey = labelKey + ".tooltip";

            Component label = getLangOrFallback(labelKey);
            Component tooltipText = getTooltipText("General", descriptionKey);

            AbstractWidget widget = null;

            switch (data.type()) {
                case "bool" -> {
                    widget = Checkbox.builder(label, this.font)
                            .pos(panelX + 25, 0)
                            .selected((Boolean) data.value())
                            .build();
                    widget.setTooltip(Tooltip.create(tooltipText));
                }
                case "slider" -> {
                    int min = data.min();
                    int max = data.max();
                    int initial = (Integer) data.value();

                    widget = new AbstractSliderButton(panelX + 25, 0, 200, 20,
                            Component.literal(label.getString() + ": " + initial),
                            (initial - min) / (double) (max - min)) {
                        @Override
                        protected void updateMessage() {
                            int val = (int) (min + value * (max - min));
                            setMessage(Component.literal(label.getString() + ": " + val));
                        }
                        @Override protected void applyValue() {}
                    };
                    widget.setTooltip(Tooltip.create(tooltipText));
                }
                case "text" -> {
                    EditBox box = new EditBox(font, panelX + 25, 0, 200, 20, label);
                    box.setMaxLength(999999);
                    box.setValue(cachedTextValues.getOrDefault(fieldName, data.value().toString()));
                    widget = box;
                    widget.setTooltip(Tooltip.create(tooltipText));
                }
                case "dropdown" -> {
                    String[] options = data.dropdownValues();
                    int index = 0;
                    for (int i = 0; i < options.length; i++)
                        if (options[i].equals(data.value().toString())) index = i;
                    final int[] currentIndex = {index};

                    widget = Button.builder(
                                    Component.literal(label.getString() + ": " + options[currentIndex[0]]),
                                    btn -> {
                                        currentIndex[0] = (currentIndex[0] + 1) % options.length;
                                        btn.setMessage(Component.literal(label.getString() + ": " + options[currentIndex[0]]));
                                    })
                            .pos(panelX + 25, 0)
                            .size(200, 20)
                            .build();
                    widget.setTooltip(Tooltip.create(tooltipText));
                    widgetData.add(new WidgetData(
                            fieldName,
                            "dropdown",
                            widget,
                            options,
                            currentIndex,
                            new int[]{0} // colorHolder (unused here)
                    ));
                }
                case "color" -> {
                    int initialColor = (Integer) data.value();
                    int[] colorHolder = new int[]{ initialColor };

                    String hex = String.format("#%08X", initialColor);

                    Button btn = Button.builder(
                            Component.literal("Color: " + hex)
                                    .withStyle(s -> s.withColor(initialColor)),
                            b -> {
                                int newColor =
                                        ((int) (Math.random() * 0xFFFFFF)) | 0xFF000000;

                                colorHolder[0] = newColor; // ✅ STORE VALUE

                                String newHex = String.format("#%08X", newColor);

                                b.setMessage(
                                        Component.literal("Color: " + newHex)
                                                .withStyle(s -> s.withColor(newColor))
                                );
                            }
                    ).pos(panelX + 25, 0).size(200, 20).build();

                    widget = btn;

                    widgetData.add(new WidgetData(
                            fieldName,
                            "color",
                            btn,
                            new String[0],
                            new int[]{0},
                            colorHolder
                    ));
                }


            }

            if (widget != null) {

                if (!canEditServerConfig) {
                    widget.active = false;
                }

                scrollArea.addEntry(widget, 28);

                if (widgetData.stream().noneMatch(w -> w.fieldName().equals(fieldName))) {
                    widgetData.add(new WidgetData(fieldName, data.type(), widget));
                }
            }
        }

        addRenderableWidget(scrollArea);

        Button saveButton = Button.builder(Component.literal("💾 Save & Close"), b -> onSave())
                .pos(centerX - 105, this.height - 50)
                .size(100, 20)
                .build();

        saveButton.active = canEditServerConfig;

        addRenderableWidget(saveButton);

        addRenderableWidget(Button.builder(Component.literal("✖ Cancel"), b -> this.minecraft.setScreen(parent))
                .pos(centerX + 5, this.height - 50)
                .size(100, 20)
                .build());
    }

    private void onSave() {

        if (!canEditServerConfig) {
            this.minecraft.setScreen(parent);
            return;
        }

        try {
            Class<?> configClass = config.getClass();

            for (WidgetData data : widgetData) {
                Field field = configClass.getDeclaredField(data.fieldName());
                field.setAccessible(true);

                Object value = switch (data.type()) {

                    case "bool" ->
                            ((Checkbox) data.widget()).selected();

                    case "slider" -> {
                        String s = ((AbstractSliderButton) data.widget())
                                .getMessage()
                                .getString()
                                .replaceAll("[^0-9-]", "");
                        yield Integer.parseInt(s);
                    }

                    case "text" ->
                            ((EditBox) data.widget()).getValue();

                    case "dropdown" ->
                            data.options()[data.selectedIndex()[0]];

                    case "color" ->
                            data.colorHolder()[0];

                    default -> null;
                };


                if (value != null) {
                    field.set(config, value);
                }
            }

            BetterConfigManager.save(
                    getModIdFromConfig(),   // modid
                    getNameFromConfig(),    // config name
                    config
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

        this.minecraft.setScreen(parent);
    }

    private String getModIdFromConfig() {
        Config cfg = config.getClass().getAnnotation(Config.class);
        return cfg != null ? cfg.modid() : "unknown";
    }

    private String getNameFromConfig() {
        Config cfg = config.getClass().getAnnotation(Config.class);
        return cfg != null ? cfg.name() : config.getClass().getSimpleName();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return scrollArea.mouseClicked(event, doubleClick)
                || super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return scrollArea.mouseReleased(event)
                || super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        return scrollArea.mouseDragged(event, dx, dy)
                || super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return scrollArea.keyPressed(event)
                || super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return scrollArea.charTyped(event)
                || super.charTyped(event);
    }


    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {

        int centerX = graphics.guiWidth() / 2;
        int height = graphics.guiHeight();

        int boxWidth = 260;
        int boxHeight = height - 160;
        int boxY = 80;
        int padding = 10;

        /* ================= PANELS ================= */

        graphics.fill(centerX - (boxWidth / 2) - padding, boxY - padding,
                centerX + (boxWidth / 2) + padding, boxY + boxHeight + padding,
                0xAA000000);

        graphics.fill(centerX - (boxWidth / 2) - padding, 35,
                centerX + (boxWidth / 2) + padding, 65,
                0xAA000000);

        graphics.fill(centerX - (boxWidth / 2) - padding, height - 60,
                centerX + (boxWidth / 2) + padding, height - 20,
                0xAA000000);

        /* ================= WIDGETS ================= */

        for (GuiEventListener child : this.children()) {
            if (child instanceof AbstractWidget widget) {
                widget.extractRenderState(graphics, mouseX, mouseY, delta);
            }
        }

        /* ================= TITLE ================= */

        String modid = (configName != null && !configName.isEmpty()) ? configName : "better_lib";
        String titleKey = "config." + modid + ".title";

        Component title = Language.getInstance().has(titleKey)
                ? Component.translatable(titleKey)
                : Component.literal(capitalize(modid) + " Config");

        graphics.centeredText(this.font, title, centerX, 45, 0xFFFFFFFF);
    }

    private Component getLangOrFallback(String key) {
        return I18n.exists(key) ? Component.translatable(key) : Component.literal(key);
    }

    private Component getTooltipText(String category, String tooltipKey) {
        String text = I18n.exists(tooltipKey) ? I18n.get(tooltipKey) : tooltipKey;
        return Component.literal("§l" + category + "§r\n" + text);
    }

    private String sanitizeFieldName(String key, String modid) {
        key = key.trim();
        if (key.startsWith("config." + modid + ".")) {
            key = key.substring(("config." + modid + ".").length());
        }
        if (key.contains(".tooltip"))
            key = key.replace(".tooltip", "");
        return key.replaceAll("[^A-Za-z0-9_]", "").trim();
    }

    private record WidgetData(
            String fieldName,
            String type,
            Object widget,
            String[] options,
            int[] selectedIndex,
            int[] colorHolder
    ) {
        public WidgetData(String fieldName, String type, Object widget) {
            this(fieldName, type, widget, new String[0], new int[]{0}, new int[]{0});
        }
    }


    private static class ConfigScrollArea extends AbstractWidget {

        private final List<Entry> entries = new ArrayList<>();
        private int scrollOffset = 0;
        private final int entrySpacing = 28;

        private AbstractWidget focusedWidget;

        public ConfigScrollArea(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty());
        }

        public void addEntry(AbstractWidget widget, int heightStep) {
            entries.add(new Entry(widget, heightStep));
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int startY = getY() - scrollOffset;
            int visibleBottom = getY() + getHeight();

            for (Entry entry : entries) {
                AbstractWidget widget = entry.widget();
                int widgetY = startY;

                if (widgetY + entry.height() > getY() && widgetY < visibleBottom) {
                    widget.setX(getX() + 10);
                    widget.setY(widgetY);

                    // NEW SYSTEM
                    widget.extractRenderState(graphics, mouseX, mouseY, partialTick);
                }

                startY += entry.height();
            }
        }

        /* ================= INPUT ================= */

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            double mouseX = event.x();
            double mouseY = event.y();

            if (!this.isMouseOver(mouseX, mouseY)) {
                clearFocus();
                return false;
            }

            for (Entry entry : entries) {
                AbstractWidget widget = entry.widget();

                if (!widget.visible || !widget.active) continue;

                if (widget.isMouseOver(mouseX, mouseY)) {
                    setFocusedWidget(widget);
                    return widget.mouseClicked(event, doubleClick);
                }
            }

            clearFocus();
            return false;
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent event) {
            return focusedWidget != null && focusedWidget.mouseReleased(event);
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
            return focusedWidget != null && focusedWidget.mouseDragged(event, dx, dy);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            int contentHeight = entries.stream().mapToInt(Entry::height).sum();
            int maxScroll = Math.max(0, contentHeight - this.getHeight());

            scrollOffset = Mth.clamp(scrollOffset - (int)(scrollY * 20), 0, maxScroll);
            return true;
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            return focusedWidget != null && focusedWidget.keyPressed(event);
        }

        @Override
        public boolean charTyped(CharacterEvent event) {
            return focusedWidget != null && focusedWidget.charTyped(event);
        }

        private void setFocusedWidget(AbstractWidget widget) {
            if (focusedWidget != null)
                focusedWidget.setFocused(false);

            focusedWidget = widget;
            widget.setFocused(true);
        }

        private void clearFocus() {
            if (focusedWidget != null) {
                focusedWidget.setFocused(false);
                focusedWidget = null;
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {}

        private record Entry(AbstractWidget widget, int height) {}
    }

}