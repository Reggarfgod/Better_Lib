package com.reggarf.mods.better_lib.demo;

import com.reggarf.mods.better_lib.popup.api.PopupTheme;
import com.reggarf.mods.better_lib.popup.api.WelcomePopupButton;
import com.reggarf.mods.better_lib.popup.api.WelcomePopupData;
import com.reggarf.mods.better_lib.popup.api.WelcomePopupPlugin;
import com.reggarf.mods.better_lib.popup.api.WelcomePopupPlugins;

public class DemoCrystalsPlugin implements WelcomePopupPlugin {

    @Override
    public String getModId() {
        return "magic_crystals";
    }

    @Override
    public String getModName() {
        return "Magic Crystals";
    }

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public int getAccentColor() {
        return 0xFFFFB300; // Warm crystal amber / gold
    }

    @Override
    public PopupTheme getTheme() {
        return PopupTheme.CYBER;
    }

    @Override
    public WelcomePopupData getPopupData() {
        return WelcomePopupData.builder()
                .title("Magic Crystals v2.0")
                .subtitle("ARCANE CRYSTALS & POWER")
                .theme(PopupTheme.CYBER)
                .accentColor(0xFFFFB300)
                .addText("Harness the power of ancient crystals across the world!", 0xFFFFFFFF)
                .addText("Explore mystical biomes, craft powerful crystals, and unleash magic.", 0xFFB0B5B8)
                .addBlankLine()
                .highlightBox("COMMUNITY SERVER", "crystals.bettermods.net", "ONLINE", "Official multiplayer server")
                .addButton("Discord", "https://discord.gg/bettermods", WelcomePopupButton.Style.ACTION, "Community & Guides")
                .addButton("Report Bugs", "https://github.com/Reggarfgod/Better_Lib/issues", WelcomePopupButton.Style.PRIMARY, "Report Issues")
                .addButton("Wiki & Guides", "https://betterlib.com/crystals-wiki", WelcomePopupButton.Style.SECONDARY, "Crystal Recipes")
                .build();
    }

    public static void register() {
        WelcomePopupPlugins.register(new DemoCrystalsPlugin());
    }
}
