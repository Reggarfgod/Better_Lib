package com.reggarf.mods.better_lib.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotations used to describe configurable fields
 * for automatic GUI generation via BetterEntryBuilder.
 */
public class ConfigEntry {

    /**
     * Groups related settings under a named category.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Category {
        String value();
    }

    /**
     * Adds a tooltip shown when hovering over a config entry.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Tooltip {
        String value() default "";
    }

    /**
     * Marks an integer field to use a slider entry.
     * The slider will range from {@code min()} to {@code max()}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface BoundedDiscrete {
        int min();
        int max();
    }

    /**
     * Marks an integer field to use a color picker entry.
     * If {@code alpha()} is true, includes transparency channel.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ColorField {
        boolean alpha() default false;
    }

    /**
     * Marks a String field to use a dropdown selector.
     * The dropdown displays all given {@code values()}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Dropdown {
        String[] values();
    }
    /**
     * Adds a human-readable description for a config entry.
     * Can be used to display tooltips or context text in config GUIs.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Description {
        String value();
    }
    /**
     * Marks a String or boolean field as a simple toggle or text field entry.
     * Optional, used for clarity.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface EntryType {
        Type value() default Type.AUTO;
        enum Type {
            AUTO, BOOLEAN, TEXT, COLOR, DROPDOWN, SLIDER
        }
    }
}
