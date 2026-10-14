package com.reggarf.mods.better_lib.configs.format;

import java.io.File;

public enum ConfigFormat {
    JSON("json"),
    TOML("toml"),
    YAML("yaml"),
    XML("xml"),
    INI("ini");

    private final String extension;

    ConfigFormat(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    public static ConfigFormat fromExtension(String ext) {
        if (ext != null) {
            String clean = ext.startsWith(".") ? ext.substring(1) : ext;
            if ("yml".equalsIgnoreCase(clean)) return YAML;
            for (ConfigFormat f : values()) {
                if (f.extension.equalsIgnoreCase(clean)) return f;
            }
        }
        return JSON;
    }

    public static ConfigFormat fromFile(File file) {
        if (file == null) return JSON;
        String name = file.getName();
        int idx = name.lastIndexOf('.');
        if (idx >= 0) {
            return fromExtension(name.substring(idx + 1));
        }
        return JSON;
    }
}
