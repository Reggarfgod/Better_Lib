package com.reggarf.mods.better_lib.config.gui;

import net.minecraft.network.chat.Component;

public class BetterEntryBuilder {
    private final BetterConfigBuilder parent;
    private Component label;
    private String id;
    private Object value;
    private String type = "int";
    private int min, max;
    private String[] dropdownValues = new String[0];

    public BetterEntryBuilder(BetterConfigBuilder parent) {
        this.parent = parent;
    }

    public BetterEntryBuilder startBooleanToggle(Component label, String id, boolean value) {
        this.type = "bool";
        this.label = label;
        this.id = id;
        this.value = value;
        return this;
    }

    public BetterEntryBuilder startIntSlider(Component label, String id, int value, int min, int max) {
        this.type = "slider";
        this.label = label;
        this.id = id;
        this.value = value;
        this.min = min;
        this.max = max;
        return this;
    }

    public BetterEntryBuilder startTextField(Component label, String id, String value) {
        this.type = "text";
        this.label = label;
        this.id = id;
        this.value = value;
        return this;
    }

    public BetterEntryBuilder startDropdown(Component label, String id, String value, String[] options) {
        this.type = "dropdown";
        this.label = label;
        this.id = id;
        this.value = value;
        this.dropdownValues = options != null ? options : new String[0];
        return this;
    }

    public BetterEntryBuilder startColorPicker(Component label, String id, int color) {
        this.type = "color";
        this.label = label;
        this.id = id;
        this.value = color;
        return this;
    }

    public EntryData build() {
        return new EntryData(id, label, value, type, min, max, dropdownValues);
    }

    public record EntryData(
            String id,
            Component label,
            Object value,
            String type,
            int min,
            int max,
            String[] dropdownValues
    ) {}
}
