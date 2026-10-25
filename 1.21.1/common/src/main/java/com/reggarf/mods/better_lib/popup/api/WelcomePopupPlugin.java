package com.reggarf.mods.better_lib.popup.api;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface WelcomePopupPlugin {

    /**
     * @return The mod ID this plugin belongs to.
     */
    String getModId();

    /**
     * @return User-friendly display name of the mod.
     */
    default String getModName() {
        return getModId();
    }

    /**
     * @return Whether the welcome popup is enabled for this mod.
     */
    default boolean enabled() {
        return true;
    }

    /**
     * @return The brand accent color used in borders, badges, and buttons.
     * Default: Zap/Emerald Green (0xFF5CB85C).
     */
    default int getAccentColor() {
        return 0xFF5CB85C;
    }

    /**
     * @return The visual theme used for this mod's popup.
     * Options: MODERN, CLASSIC, MINECRAFTY, GIRLISH, BOYISH, LGBT.
     */
    default PopupTheme getTheme() {
        return PopupTheme.MODERN;
    }

    /**
     * @return Optional custom mod icon texture. If null, a stylized letter badge is rendered.
     */
    @Nullable
    default ResourceLocation getIcon() {
        return null;
    }

    /**
     * @return The full popup content specification.
     */
    @Nullable
    default WelcomePopupData getPopupData() {
        return null;
    }
}
