package com.reggarf.mods.better_lib.config.gui;

import com.reggarf.mods.better_lib.config.annotation.ConfigEntry.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class BetterConfigBuilder {
    private final Component title;
    private final List<BetterEntryBuilder> entries = new ArrayList<>();
    private Screen parent;
    private String background = "minecraft:textures/block/stone.png";

    private BetterConfigBuilder(Component title) {
        this.title = title;
    }

    public static BetterConfigBuilder create(Component title) {
        return new BetterConfigBuilder(title);
    }

    public BetterConfigBuilder setParent(Screen parent) {
        this.parent = parent;
        return this;
    }

    public BetterConfigBuilder setBackground(String texture) {
        this.background = texture;
        return this;
    }

    public BetterEntryBuilder entryBuilder() {
        BetterEntryBuilder builder = new BetterEntryBuilder(this);
        entries.add(builder);
        return builder;
    }

    /**
     * Automatically generate entries from config fields using annotations.
     */
    public BetterConfigBuilder autoBuildFrom(Object config) {
        for (Field field : config.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(config);
                String fieldName = field.getName();

                Component label = Component.literal(fieldName);
                Tooltip tooltip = field.getAnnotation(Tooltip.class);
                if (tooltip != null) {
                    label = Component.literal(fieldName + " §7(" + tooltip.value() + ")");
                }

                // handle @BoundedDiscrete (int sliders)
                if (field.isAnnotationPresent(BoundedDiscrete.class)) {
                    BoundedDiscrete range = field.getAnnotation(BoundedDiscrete.class);
                    if (value instanceof Integer intVal) {
                        entryBuilder().startIntSlider(label, fieldName, intVal, range.min(), range.max());
                    }
                    continue;
                }

                // handle @Dropdown
                if (field.isAnnotationPresent(Dropdown.class)) {
                    Dropdown drop = field.getAnnotation(Dropdown.class);
                    if (value instanceof String strVal) {
                        entryBuilder().startDropdown(label, fieldName, strVal, drop.values());
                    }
                    continue;
                }

                // handle @ColorField
                if (field.isAnnotationPresent(ColorField.class)) {
                    if (value instanceof Integer intVal) {
                        entryBuilder().startColorPicker(label, fieldName, intVal);
                    }
                    continue;
                }

                // handle booleans
                if (value instanceof Boolean boolVal) {
                    entryBuilder().startBooleanToggle(label, fieldName, boolVal);
                    continue;
                }

                // handle strings
                if (value instanceof String strVal) {
                    entryBuilder().startTextField(label, fieldName, strVal);
                    continue;
                }

                // handle integers (without annotations)
                if (value instanceof Integer intVal) {
                    entryBuilder().startIntSlider(label, fieldName, intVal, 0, 100);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    public Screen build(Object config, String name) {
        return new BetterConfigScreen(parent, title, config, entries, name, background);
    }
}
