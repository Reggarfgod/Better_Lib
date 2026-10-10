package com.reggarf.mods.better_lib.popup.api;

import org.jetbrains.annotations.Nullable;

public class WelcomePopupHighlight {

    private final String label;
    private final String value;
    @Nullable
    private final String badge;
    @Nullable
    private final String tooltip;
    private final boolean copyable;

    public WelcomePopupHighlight(String label, String value, @Nullable String badge, @Nullable String tooltip, boolean copyable) {
        this.label = label;
        this.value = value;
        this.badge = badge;
        this.tooltip = tooltip;
        this.copyable = copyable;
    }

    public static WelcomePopupHighlight of(String label, String value) {
        return new WelcomePopupHighlight(label, value, null, null, true);
    }

    public static WelcomePopupHighlight of(String label, String value, @Nullable String badge) {
        return new WelcomePopupHighlight(label, value, badge, null, true);
    }

    public static WelcomePopupHighlight of(String label, String value, @Nullable String badge, @Nullable String tooltip) {
        return new WelcomePopupHighlight(label, value, badge, tooltip, true);
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    @Nullable
    public String getBadge() {
        return badge;
    }

    @Nullable
    public String getTooltip() {
        return tooltip;
    }

    public boolean isCopyable() {
        return copyable;
    }
}
