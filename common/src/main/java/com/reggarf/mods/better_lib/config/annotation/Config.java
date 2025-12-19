package com.reggarf.mods.better_lib.config.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Config {

    /** Mod ID used as config folder name */
    String modid();

    /** Config file name (without .json) */
    String name();

    String background() default "minecraft:textures/block/stone.png";
}
