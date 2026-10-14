package com.reggarf.mods.better_lib.configs.format;

public class ConfigFormatHandlers {

    public static ConfigFormatHandler getHandler(ConfigFormat format) {
        if (format == null) return JsonConfigHandler.INSTANCE;
        return switch (format) {
            case JSON -> JsonConfigHandler.INSTANCE;
            case TOML -> TomlConfigHandler.INSTANCE;
            case YAML -> YamlConfigHandler.INSTANCE;
            case XML -> XmlConfigHandler.INSTANCE;
            case INI -> IniConfigHandler.INSTANCE;
        };
    }
}
