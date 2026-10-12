package com.reggarf.mods.better_lib.demo;

import com.reggarf.mods.better_lib.Better_lib;
import com.reggarf.mods.better_lib.message.api.JoinMessagePlugin;
import com.reggarf.mods.better_lib.message.api.JoinMessagePlugins;
import com.reggarf.mods.better_lib.message.api.JoinMessageSet;


import java.util.List;

public class DemoPlugin implements JoinMessagePlugin {

    @Override
    public String getModId() {
        return Better_lib.MODID; // Your mod ID
    }

    @Override
    public boolean enabled() {
        return true; // Set false to disable messages
    }

    @Override
    public List<JoinMessageSet> getMessageSets() {
        return List.of(
            new JoinMessageSet()
                .addText("Welcome to Magic Crystals!", 0xFFD700)
                .addBlankLine()
                .addLink("Discord", "https://discord.gg/bettermods", 0x00AAFF, "(Community)")
                .addLink("GitHub", "https://github.com/reggarf/better_lib", 0xAAAAAA, "(Source / Issues)"),

            new JoinMessageSet()
                .addBlankLine()
                .addText("New 2.0 Update — New message Added!", 0x00FF66)
                .addLink("Read Changelog", "https://betterlib.com/changelog", 0x00FFFF, "")
        );
    }

    public static void register() {
        JoinMessagePlugins.register(new DemoPlugin());
    }
}