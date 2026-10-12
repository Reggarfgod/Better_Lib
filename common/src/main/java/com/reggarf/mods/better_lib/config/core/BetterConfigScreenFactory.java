package com.reggarf.mods.better_lib.config.core;

import com.reggarf.mods.better_lib.config.annotation.Config;
import com.reggarf.mods.better_lib.config.helper.BetterConfigBuilder;
import com.reggarf.mods.better_lib.configs.ConfigBase;
import com.reggarf.mods.better_lib.configs.adapter.AnnotatedConfigBuilder;
import com.reggarf.mods.better_lib.configs.ui.BaseConfigScreen;
import com.reggarf.mods.better_lib.configs.ui.ConfigScreen;
import com.reggarf.mods.better_lib.configs.ui.SubMenuConfigScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Builds a Create-style config screen from annotated config classes or ConfigBase instances.
 */
public class BetterConfigScreenFactory {

    /**
     * Creates a Create-style BaseConfigScreen showing the 1st page:
     * Client Settings, Common Settings, Server Settings.
     */
    public static Screen from(Class<?> configClass, Object configInstance, Screen parent) {
        Config annotation = configClass.getAnnotation(Config.class);
        String modid = annotation != null && !annotation.modid().isEmpty() ? annotation.modid() : "better_lib";

        AnnotatedConfigBuilder.ConfigSections sections = AnnotatedConfigBuilder.buildSections(configClass, configInstance);

        BaseConfigScreen screen = new BaseConfigScreen(parent, modid);
        screen.withConfigs(sections.client, sections.common, sections.server);
        return screen;
    }

    /**
     * Creates legacy BetterConfigScreen if specifically requested.
     */
    public static Screen fromLegacy(Class<?> configClass, Object configInstance, Screen parent) {
        Config annotation = configClass.getAnnotation(Config.class);
        if (annotation == null)
            throw new IllegalArgumentException("Missing @Config annotation on " + configClass.getName());

        String name = annotation.name().isEmpty() ? configClass.getSimpleName() : annotation.name();
        String background = annotation.background();

        BetterConfigBuilder builder = BetterConfigBuilder.create(Component.literal(name))
                .setParent(parent)
                .setBackground(background);

        builder.autoBuildFrom(configInstance);

        return builder.build(configInstance, name);
    }

    /**
     * Creates a Create-style BaseConfigScreen for client, common, and server configs.
     */
    public static Screen fromCreate(Screen parent, String modID, ConfigBase client, ConfigBase common, ConfigBase server) {
        return new BaseConfigScreen(parent, modID).withConfigs(client, common, server);
    }

    /**
     * Creates a Create-style SubMenuConfigScreen for an individual ConfigBase category.
     */
    public static Screen fromCreate(Screen parent, ConfigScreen.ConfigType type, ConfigBase config) {
        return new SubMenuConfigScreen(parent, type, config);
    }
}
