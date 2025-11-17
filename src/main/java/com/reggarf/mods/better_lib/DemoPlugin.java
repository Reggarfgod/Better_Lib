//package com.reggarf.mods.better_lib;
//
//
//
//import com.reggarf.mods.better_lib.message.api.JoinMessagePlugin;
//import com.reggarf.mods.better_lib.message.api.JoinMessagePlugins;
//import com.reggarf.mods.better_lib.message.event.JoinMessageSet;
//
//import java.util.List;
//
//public class DemoPlugin implements JoinMessagePlugin {
//
//    @Override
//    public String getModId() {
//        return Better_lib.MODID; // Your mod ID
//    }
//
//    @Override
//    public boolean enabled() {
//        return true; // Set false to disable messages
//    }
//
//    @Override
//    public List<JoinMessageSet> getMessageSets() {
//        return List.of(
//            new JoinMessageSet()
//                .addText("Welcome to Magic Crystals!", "FFD700")
//                .addBlankLine()
//                .addLink("Discord", "https://discord.gg/bettermods", "00AAFF", "(Community)")
//                .addLink("GitHub", "https://github.com/reggarf/better_lib", "AAAAAA", "(Source / Issues)"),
//
//            new JoinMessageSet()
//                .addBlankLine()
//                .addText("New 2.0 Update — New message Added!", "00FF66")
//                .addLink("Read Changelog", "https://betterlib.com/changelog", "00FFFF", "")
//        );
//    }
//
//    public static void register() {
//        JoinMessagePlugins.register(new DemoPlugin());
//    }
//}