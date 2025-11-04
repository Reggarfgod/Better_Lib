package com.reggarf.mods.better_lib.config.core;

import com.reggarf.mods.better_lib.config.annotation.Config;
import com.reggarf.mods.better_lib.config.annotation.ConfigEntry.*;
import com.reggarf.mods.better_lib.config.gui.BetterConfigBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;

/**
 * Builds a BetterConfigScreen from annotated config classes.
 */
public class BetterConfigScreenFactory {

    /**
     * Creates a Screen from the config class and instance.
     */
    public static Screen from(Class<?> configClass, Object configInstance, Screen parent) {
        Config annotation = configClass.getAnnotation(Config.class);
        if (annotation == null)
            throw new IllegalArgumentException("Missing @Config annotation on " + configClass.getName());

        String name = annotation.name().isEmpty() ? configClass.getSimpleName() : annotation.name();
        String background = annotation.background();

        BetterConfigBuilder builder = BetterConfigBuilder.create(Component.literal(name))
                .setParent(parent)
                .setBackground(background);

        // If you prefer automatic generation from fields, call builder.autoBuildFrom(configInstance).
        // We'll auto-generate here for convenience:
        builder.autoBuildFrom(configInstance);

        return builder.build(configInstance, name);
    }
}
