package com.reggarf.mods.better_lib.config.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.capitalize;

public class BetterConfigScreen extends Screen {

    private final Screen parent;
    private final Object config;
    private final List<BetterEntryBuilder> entries;
    private final String configName;
    private final ResourceLocation background;
    private final List<WidgetData> widgetData = new ArrayList<>();
    private ConfigScrollArea scrollArea;

    public BetterConfigScreen(Screen parent, Component title, Object config, List<BetterEntryBuilder> entries, String name, String bgTexture) {
        super(title);
        this.parent = parent;
        this.config = config;
        this.entries = entries;
        this.configName = name;
        this.background = bgTexture != null ? ResourceLocation.tryParse(bgTexture) : null;
    }

    @Override
    protected void init() {
        widgetData.clear();

        int panelWidth = 250;
        int panelHeight = height - 180;
        int centerX = width / 2;
        int panelX = centerX - (panelWidth / 2);
        int panelY = 90;

        scrollArea = new ConfigScrollArea(panelX, panelY, panelWidth, panelHeight);

        String modid = (configName != null && !configName.isEmpty()) ? configName : "better_lib";
        int y = 0;

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

                        @Override
                        protected void applyValue() {}
                    };
                    widget.setTooltip(Tooltip.create(tooltipText));
                }
                case "text" -> {
                    widget = new EditBox(font, panelX + 25, 0, 200, 20, label);
                    ((EditBox) widget).setValue(data.value().toString());
                    widget.setTooltip(Tooltip.create(tooltipText));
                }
                case "dropdown" -> {
                    String[] options = data.dropdownValues();
                    String current = data.value().toString();
                    int index = 0;
                    for (int i = 0; i < options.length; i++)
                        if (options[i].equals(current)) index = i;
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
                    widgetData.add(new WidgetData(fieldName, "dropdown", widget, options, currentIndex));
                }
                case "color" -> {
                    int color = (Integer) data.value();
                    widget = Button.builder(
                                    Component.literal("Color: #" + Integer.toHexString(color).toUpperCase()),
                                    btn -> {
                                        int newColor = 0xFF000000 | (int) (Math.random() * 0xFFFFFF);
                                        btn.setMessage(Component.literal("Color: #" + Integer.toHexString(newColor).toUpperCase()));
                                        btn.setFGColor(newColor);
                                    })
                            .pos(panelX + 25, 0)
                            .size(200, 20)
                            .build();
                    widget.setFGColor(color);
                    widget.setTooltip(Tooltip.create(tooltipText));
                }
            }

            if (widget != null) {
                scrollArea.addEntry(widget, 28);

                boolean alreadyAdded = widgetData.stream().anyMatch(w -> w.fieldName().equals(fieldName));
                if (!alreadyAdded) {
                    widgetData.add(new WidgetData(fieldName, data.type(), widget));
                }
            }

            y += 28;
        }

        addRenderableWidget(scrollArea);

        addRenderableWidget(Button.builder(Component.literal("💾 Save & Close"), b -> onSave())
                .pos(centerX - 105, this.height - 50)
                .size(100, 20)
                .build());

        addRenderableWidget(Button.builder(Component.literal("✖ Cancel"), b -> this.minecraft.setScreen(parent))
                .pos(centerX + 5, this.height - 50)
                .size(100, 20)
                .build());
    }

    private void onSave() {
        try {
            Class<?> configClass = config.getClass();
            for (WidgetData data : widgetData) {
                var field = configClass.getDeclaredField(data.fieldName());
                field.setAccessible(true);
                Object value = null;
                switch (data.type()) {
                    case "bool" -> value = ((Checkbox) data.widget()).selected();
                    case "slider" -> {
                        AbstractSliderButton slider = (AbstractSliderButton) data.widget();
                        String numberPart = slider.getMessage().getString().replaceAll("[^0-9-]", "");
                        value = Integer.parseInt(numberPart);
                    }
                    case "text" -> value = ((EditBox) data.widget()).getValue();
                    case "dropdown" -> value = data.options()[data.selectedIndex()[0]];
                    case "color" -> value = ((Button) data.widget()).getFGColor();
                }
                if (value != null) field.set(config, value);
            }
            saveConfigToFile(config, configName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.minecraft.setScreen(parent);
    }

    private void saveConfigToFile(Object config, String name) {
        try {
            java.io.File dir = new java.io.File(Minecraft.getInstance().gameDirectory, "config");
            if (!dir.exists()) dir.mkdirs();
            java.io.File file = new java.io.File(dir, name + ".json");

            com.google.gson.JsonObject root = new com.google.gson.JsonObject();
            String modid = (name != null && !name.isEmpty()) ? name : "better_lib";
            Class<?> clazz = config.getClass();

            com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();

            for (var field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object val = field.get(config);
                com.google.gson.JsonObject entryObj = new com.google.gson.JsonObject();

                if (val instanceof java.util.List<?> list) {
                    entryObj.add("value", gson.toJsonTree(list));
                } else if (val != null && val.getClass().isArray()) {
                    entryObj.add("value", gson.toJsonTree(val));
                } else if (val instanceof Number n) {
                    entryObj.addProperty("value", n);
                } else if (val instanceof Boolean b) {
                    entryObj.addProperty("value", b);
                } else if (val != null) {
                    entryObj.addProperty("value", String.valueOf(val));
                } else {
                    entryObj.add("value", com.google.gson.JsonNull.INSTANCE);
                }

                root.add(field.getName(), entryObj);
            }

            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                gson.toJson(root, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return scrollArea.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
                || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int centerX = this.width / 2;

        if (background != null) {
            RenderCompat.enableBlendSafe();
            RenderCompat.setShaderTextureSafe(background);
            RenderCompat.blitSafe(graphics, background, 0, 0, this.width, this.height, this.width, this.height);
        } else {
            graphics.fillGradient(0, 0, width, height, 0xFF0C0C0C, 0xFF202020);
        }

        int boxWidth = 260;
        int boxHeight = height - 160;
        int boxY = 80;
        int padding = 10;

        graphics.fill(centerX - (boxWidth / 2) - padding, boxY - padding,
                centerX + (boxWidth / 2) + padding, boxY + boxHeight + padding,
                0xAA000000);

        super.render(graphics, mouseX, mouseY, delta);

        graphics.fill(centerX - (boxWidth / 2) - padding, 35,
                centerX + (boxWidth / 2) + padding, 65, 0xAA000000);

        graphics.fill(centerX - (boxWidth / 2) - padding, height - 60,
                centerX + (boxWidth / 2) + padding, height - 20, 0xAA000000);

        String modid = (configName != null && !configName.isEmpty()) ? configName : "better_lib";
        String titleKey = "config." + modid + ".title";
        Component title = I18n.exists(titleKey)
                ? Component.translatable(titleKey)
                : Component.literal(capitalize(modid) + " Config");

        TitleCompat.drawCenteredTitleSafe(graphics, this.font, title, centerX, 45, 0xFFFFFF);
    }

    /**
     * Cross-version title rendering compatibility.
     * Tries legacy drawCenteredString() first (1.21.4 and below),
     * then falls back to new textRenderer-based method (1.21.5+).
     */
    private static class TitleCompat {
        public static void drawCenteredTitleSafe(GuiGraphics graphics, net.minecraft.client.gui.Font font, Component title, int centerX, int y, int color) {
            // Try legacy centered text (1.21.4 and below)
            try {
                graphics.drawCenteredString(font, title, centerX, y, color);
                return;
            } catch (Throwable ignored) {}

            // 1.21.5+ and NeoForge mappings compatibility
            try {
                float textWidth = font.width(title);
                float x = centerX - (textWidth / 2f);

                // Try the new float-based signature (1.21.5+)
                try {
                    var method = GuiGraphics.class.getMethod(
                            "drawString",
                            net.minecraft.client.gui.Font.class,
                            net.minecraft.network.chat.FormattedText.class,
                            float.class,
                            float.class,
                            int.class,
                            boolean.class
                    );
                    method.invoke(graphics, font, (net.minecraft.network.chat.FormattedText) title, x, (float) y, color, false);
                    return;
                } catch (NoSuchMethodException ignored2) {}

                // Try fallback: int-based variant (older mappings)
                try {
                    var method = GuiGraphics.class.getMethod(
                            "drawString",
                            net.minecraft.client.gui.Font.class,
                            net.minecraft.network.chat.Component.class,
                            int.class,
                            int.class,
                            int.class,
                            boolean.class
                    );
                    method.invoke(graphics, font, title, (int) x, y, color, false);
                    return;
                } catch (NoSuchMethodException ignored3) {}

                // Final fallback: manual render (safe even if reflection fails)
                graphics.drawString(font, title.getString(), (int) x, y, color, false);
            } catch (Throwable ignored) {
                // Skip drawing entirely if everything fails
            }
        }
    }



    /**
     * Universal RenderSystem + GuiGraphics compatibility layer.
     */
    private static class RenderCompat {
        public static void enableBlendSafe() {
            try {
                RenderSystem.class.getMethod("defaultBlendFunc").invoke(null);
            } catch (NoSuchMethodException e) {
                try {
                    RenderSystem.class.getMethod("enableBlend").invoke(null);
                } catch (Exception ignored) {}
            } catch (Exception ignored) {}
        }

        public static void setShaderTextureSafe(ResourceLocation texture) {
            try {
                RenderSystem.class.getMethod("setShaderTexture", ResourceLocation.class)
                        .invoke(null, texture);
            } catch (NoSuchMethodException e) {
                try {
                    RenderSystem.class.getMethod("setShaderTexture", int.class, ResourceLocation.class)
                            .invoke(null, 0, texture);
                } catch (Exception ignored) {}
            } catch (Exception ignored) {}
        }

        public static void blitSafe(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height, int texWidth, int texHeight) {
            try {
                // 1.21.8+ (int-based)
                GuiGraphics.class.getMethod("blit", ResourceLocation.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class)
                        .invoke(graphics, texture, x, y, 0, 0, width, height, texWidth, texHeight);
            } catch (NoSuchMethodException e) {
                try {
                    // 1.21.4 and below (float-based)
                    GuiGraphics.class.getMethod("blit", ResourceLocation.class, int.class, int.class, float.class, float.class, int.class, int.class, int.class, int.class)
                            .invoke(graphics, texture, x, y, 0.0f, 0.0f, width, height, texWidth, texHeight);
                } catch (Exception ignored) {}
            } catch (Exception ignored) {}
        }
    }

    // --- Utility methods ---
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

    private record WidgetData(String fieldName, String type, Object widget, String[] options, int[] selectedIndex) {
        public WidgetData(String fieldName, String type, Object widget) {
            this(fieldName, type, widget, new String[0], new int[]{0});
        }
    }
    /**
     * Lightweight scrollable area using the same logic as ScrollableTextList.
     */
    private static class ConfigScrollArea extends AbstractWidget {
        private final List<Entry> entries = new ArrayList<>();
        private int scrollOffset = 0;
        private final int entrySpacing = 28;

        // Drag support
        private boolean draggingNumeric = false;
        private EditBox draggedBox = null;
        private double lastMouseX = 0;
        private int baseValue = 0;

        public ConfigScrollArea(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty());
        }

        public void addEntry(AbstractWidget widget, int heightStep) {
            entries.add(new Entry(widget, heightStep));
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int startY = getY() - scrollOffset;
            int visibleBottom = getY() + getHeight();

            for (Entry entry : entries) {
                AbstractWidget widget = entry.widget();
                int widgetY = startY;
                if (widgetY + entry.height() > getY() && widgetY < visibleBottom) {
                    widget.setY(widgetY);
                    widget.render(graphics, mouseX, mouseY, partialTick);
                }
                startY += entry.height();
            }

            // Scrollbar
            int contentHeight = entries.size() * entrySpacing;
            if (contentHeight > this.height) {
                int scrollbarWidth = 6;
                int scrollbarX = getX() + getWidth() - scrollbarWidth - 2;
                int scrollbarY = getY();
                int visibleHeight = this.height;
                float progress = (float) scrollOffset / (float) (contentHeight - visibleHeight);
                int thumbHeight = Math.max(16, (int) ((float) visibleHeight * visibleHeight / contentHeight));
                int thumbY = scrollbarY + (int) ((visibleHeight - thumbHeight) * progress);
                graphics.fill(scrollbarX, scrollbarY, scrollbarX + scrollbarWidth, scrollbarY + visibleHeight, 0x44000000);
                graphics.fill(scrollbarX, thumbY, scrollbarX + scrollbarWidth, thumbY + thumbHeight, 0xAAFFFFFF);
            }

            // If currently dragging, update value smoothly
            if (draggingNumeric && draggedBox != null) {
                double currentMouseX = Minecraft.getInstance().mouseHandler.xpos() / Minecraft.getInstance().getWindow().getGuiScaledWidth() * Minecraft.getInstance().getWindow().getScreenWidth();
                double diff = currentMouseX - lastMouseX;
                if (Math.abs(diff) > 0.5) {
                    int delta = (int) (diff / 3); // Adjust sensitivity
                    int newValue = baseValue + delta;
                    draggedBox.setValue(String.valueOf(newValue));
                }
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            for (Entry entry : entries) {
                AbstractWidget widget = entry.widget();
                boolean inside = mouseX >= widget.getX() && mouseX <= widget.getX() + widget.getWidth() &&
                        mouseY >= widget.getY() && mouseY <= widget.getY() + widget.getHeight();
                if (inside) {
                    if (widget.mouseClicked(mouseX, mouseY, button)) {
                        if (widget instanceof EditBox editBox) {
                            editBox.setFocused(true);
                            try {
                                // Start dragging if the value is numeric
                                Integer.parseInt(editBox.getValue());
                                draggingNumeric = true;
                                draggedBox = editBox;
                                lastMouseX = mouseX;
                                baseValue = Integer.parseInt(editBox.getValue());
                            } catch (NumberFormatException ignored) {}
                        } else {
                            widget.setFocused(true);
                        }
                        return true;
                    }
                } else {
                    widget.setFocused(false);
                }
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            for (Entry entry : entries) {
                entry.widget().mouseReleased(mouseX, mouseY, button);
            }
            draggingNumeric = false;
            draggedBox = null;
            return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            // Don't rely on Minecraft's built-in dragging
            return draggingNumeric || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            for (Entry entry : entries) {
                if (entry.widget().keyPressed(keyCode, scanCode, modifiers)) return true;
            }
            return false;
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            for (Entry entry : entries) {
                if (entry.widget().charTyped(codePoint, modifiers)) return true;
            }
            return false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            int contentHeight = entries.size() * entrySpacing;
            int maxScroll = Math.max(0, contentHeight - this.height);
            scrollOffset = Mth.clamp(scrollOffset - (int) (scrollY * 20), 0, maxScroll);
            return true;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

        private record Entry(AbstractWidget widget, int height) {}
    }

}
