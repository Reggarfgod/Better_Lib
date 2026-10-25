package com.reggarf.mods.better_lib.popup.api;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WelcomePopupData {

    private final String title;
    @Nullable
    private final String subtitle;
    private final int accentColor;
    @Nullable
    private final PopupTheme theme;
    @Nullable
    private final ResourceLocation icon;
    private final List<Component> descriptionLines;
    @Nullable
    private final WelcomePopupHighlight highlight;
    private final List<WelcomePopupButton> buttons;

    private WelcomePopupData(Builder builder) {
        this.title = builder.title;
        this.subtitle = builder.subtitle;
        this.accentColor = builder.accentColor;
        this.theme = builder.theme;
        this.icon = builder.icon;
        this.descriptionLines = Collections.unmodifiableList(new ArrayList<>(builder.descriptionLines));
        this.highlight = builder.highlight;
        this.buttons = Collections.unmodifiableList(new ArrayList<>(builder.buttons));
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTitle() {
        return title;
    }

    @Nullable
    public String getSubtitle() {
        return subtitle;
    }

    public int getAccentColor() {
        return accentColor;
    }

    @Nullable
    public PopupTheme getTheme() {
        return theme;
    }

    @Nullable
    public ResourceLocation getIcon() {
        return icon;
    }

    public List<Component> getDescriptionLines() {
        return descriptionLines;
    }

    @Nullable
    public WelcomePopupHighlight getHighlight() {
        return highlight;
    }

    public List<WelcomePopupButton> getButtons() {
        return buttons;
    }

    public static class Builder {
        private String title = "Welcome";
        private String subtitle = null;
        private int accentColor = 0xFF5CB85C;
        private PopupTheme theme = null;
        private ResourceLocation icon = null;
        private final List<Component> descriptionLines = new ArrayList<>();
        private WelcomePopupHighlight highlight = null;
        private final List<WelcomePopupButton> buttons = new ArrayList<>();

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder subtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public Builder accentColor(int accentColor) {
            this.accentColor = accentColor;
            return this;
        }

        public Builder theme(PopupTheme theme) {
            this.theme = theme;
            return this;
        }

        public Builder icon(ResourceLocation icon) {
            this.icon = icon;
            return this;
        }

        public Builder addText(String text) {
            return addText(text, 0xFFFFFFFF);
        }

        public Builder addText(String text, int colorRgb) {
            this.descriptionLines.add(Component.literal(text)
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(colorRgb))));
            return this;
        }

        public Builder addBlankLine() {
            this.descriptionLines.add(Component.literal(""));
            return this;
        }

        public Builder highlightBox(String label, String value) {
            this.highlight = WelcomePopupHighlight.of(label, value);
            return this;
        }

        public Builder highlightBox(String label, String value, String badge) {
            this.highlight = WelcomePopupHighlight.of(label, value, badge);
            return this;
        }

        public Builder highlightBox(String label, String value, String badge, String tooltip) {
            this.highlight = WelcomePopupHighlight.of(label, value, badge, tooltip);
            return this;
        }

        public Builder addButton(WelcomePopupButton button) {
            this.buttons.add(button);
            return this;
        }

        public Builder addButton(String label, String url, WelcomePopupButton.Style style, String description) {
            this.buttons.add(new WelcomePopupButton(Component.literal(label), url, style, description, null));
            return this;
        }

        public Builder addDiscord(String url) {
            this.buttons.add(WelcomePopupButton.discord(url));
            return this;
        }

        public Builder addBugReport(String url) {
            this.buttons.add(WelcomePopupButton.reportBug(url));
            return this;
        }

        public Builder addGitHub(String url) {
            this.buttons.add(WelcomePopupButton.github(url));
            return this;
        }

        public Builder addChangelog(String url) {
            this.buttons.add(WelcomePopupButton.changelog(url));
            return this;
        }

        public WelcomePopupData build() {
            return new WelcomePopupData(this);
        }
    }
}
