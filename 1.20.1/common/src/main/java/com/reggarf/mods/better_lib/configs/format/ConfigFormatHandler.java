package com.reggarf.mods.better_lib.configs.format;

import java.io.File;
import java.util.Map;

public interface ConfigFormatHandler {
    ConfigFormat getFormat();

    Map<String, Object> read(File file) throws Exception;

    void write(File file, Map<String, Object> data, Map<String, String> comments) throws Exception;
}
