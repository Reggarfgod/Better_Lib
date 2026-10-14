package com.reggarf.mods.better_lib.popup.api;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

public class WelcomePopupButton {

    public enum Style {
        PRIMARY,
        SECONDARY,
        ACTION
    }

    private final Component label;
    @Nullable
    private final String url;
    private final Style style;
    @Nullable
    private final String description;
    @Nullable
    private final Runnable customAction;

    public WelcomePopupButton(Component label, @Nullable String url, Style style, @Nullable String description, @Nullable Runnable customAction) {
        this.label = label;
        this.url = url;
        this.style = style;
        this.description = description;
        this.customAction = customAction;
    }

    public static WelcomePopupButton primary(String label, String url, @Nullable String description) {
        return new WelcomePopupButton(Component.literal(label), url, Style.PRIMARY, description, null);
    }

    public static WelcomePopupButton secondary(String label, String url, @Nullable String description) {
        return new WelcomePopupButton(Component.literal(label), url, Style.SECONDARY, description, null);
    }

    public static WelcomePopupButton action(String label, String url, @Nullable String description) {
        return new WelcomePopupButton(Component.literal(label), url, Style.ACTION, description, null);
    }

    public static WelcomePopupButton custom(String label, Style style, Runnable action) {
        return new WelcomePopupButton(Component.literal(label), null, style, null, action);
    }

    public static WelcomePopupButton reportBug(String url) {
        return new WelcomePopupButton(Component.literal("\u26A0 Report Bug"), url, Style.PRIMARY, "(GitHub / Issues)", null);
    }

    public static WelcomePopupButton discord(String url) {
        return new WelcomePopupButton(Component.literal("\uD83D\uDCAC Discord"), url, Style.ACTION, "(Community)", null);
    }

    public static WelcomePopupButton github(String url) {
        return new WelcomePopupButton(Component.literal("\u2B50 GitHub"), url, Style.SECONDARY, "(Source / Issues)", null);
    }

    public static WelcomePopupButton changelog(String url) {
        return new WelcomePopupButton(Component.literal("\uD83D\uDCDD Changelog"), url, Style.SECONDARY, "(What's New)", null);
    }

    public Component getLabel() {
        return label;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    public Style getStyle() {
        return style;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @Nullable
    public Runnable getCustomAction() {
        return customAction;
    }
}
