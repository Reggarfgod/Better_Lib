package com.reggarf.mods.better_lib.demo;

import com.reggarf.mods.better_lib.Better_lib;
import com.reggarf.mods.better_lib.message.api.JoinMessageSet;
import com.reggarf.mods.better_lib.popup.api.WelcomePopupButton;
import com.reggarf.mods.better_lib.popup.api.WelcomePopupData;
import com.reggarf.mods.better_lib.popup.api.WelcomePopupPlugin;
import com.reggarf.mods.better_lib.popup.api.WelcomePopupPlugins;
import net.minecraft.resources.Identifier;

import java.util.List;

public class DemoPopupPlugin implements WelcomePopupPlugin {

    @Override
    public String getModId() {
        return Better_lib.MODID;
    }

    @Override
    public String getModName() {
        return "Better Lib";
    }

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public int getAccentColor() {
        return 0xFF5CB85C; // Elegant emerald green
    }

    @Override
    public Identifier getIcon() {
        return Identifier.fromNamespaceAndPath(Better_lib.MODID, "textures/gui/icon.png");
    }

    @Override
    public List<JoinMessageSet> getMessageSets() {
        return List.of(
                new JoinMessageSet()
                        .addText("Welcome to Better Lib!", 0x5CB85C)
                        .addBlankLine()
                        .addLink("Discord", "https://discord.gg/bettermods", 0x00AAFF, "(Community)")
                        .addLink("Report Bugs", "https://github.com/Reggarfgod/Better_Lib/issues", 0x5CB85C, "(Bug Tracker)")
                        .addLink("GitHub", "https://github.com/reggarf/better_lib", 0xAAAAAA, "(Source / Issues)"),

                new JoinMessageSet()
                        .addBlankLine()
                        .addText("New 2.0 Update \u2014 Welcome popup & mod list added!", 0x00FF66)
                        .addLink("Read Changelog", "https://betterlib.com/changelog", 0x00FFFF, "")
        );
    }

    @Override
    public WelcomePopupData getPopupData() {
        return WelcomePopupData.builder()
                .title("Welcome to Better Lib!")
                .subtitle("CORE LIBRARY & TOOLKIT")
                .accentColor(0xFF5CB85C)
                .addText("Thank you for using Better Lib!", 0xFFFFFFFF)
                .addText("A modern library providing rich UI, dynamic configs & seamless integrations.", 0xFFB0B5B8)
                .addBlankLine()
                .highlightBox("COMMUNITY DISCORD", "discord.gg/bettermods", "JOIN", "Join our Discord community")
                .addButton("Discord", "https://discord.gg/bettermods", WelcomePopupButton.Style.ACTION, "Community & Chat")
                .addButton("Report Bugs", "https://github.com/Reggarfgod/Better_Lib/issues", WelcomePopupButton.Style.PRIMARY, "Bug Reports & Issues")
                .addButton("GitHub", "https://github.com/reggarf/better_lib", WelcomePopupButton.Style.SECONDARY, "Source Code")
                .addButton("Changelog", "https://betterlib.com/changelog", WelcomePopupButton.Style.SECONDARY, "Latest Updates")
                .build();
    }

    public static void register() {
        WelcomePopupPlugins.register(new DemoPopupPlugin());
    }
}
