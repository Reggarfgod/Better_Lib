package com.reggarf.mods.better_lib.configs.format;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonConfigHandler implements ConfigFormatHandler {
    public static final JsonConfigHandler INSTANCE = new JsonConfigHandler();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public ConfigFormat getFormat() {
        return ConfigFormat.JSON;
    }

    @Override
    public Map<String, Object> read(File file) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!file.exists()) return result;
        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (root != null && root.isJsonObject()) {
                parseJsonObject(root.getAsJsonObject(), result);
            }
        }
        return result;
    }

    private void parseJsonObject(JsonObject obj, Map<String, Object> target) {
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            JsonElement el = entry.getValue();
            if (el.isJsonObject()) {
                Map<String, Object> child = new LinkedHashMap<>();
                parseJsonObject(el.getAsJsonObject(), child);
                target.put(entry.getKey(), child);
            } else if (el.isJsonPrimitive()) {
                JsonPrimitive p = el.getAsJsonPrimitive();
                if (p.isBoolean()) {
                    target.put(entry.getKey(), p.getAsBoolean());
                } else if (p.isNumber()) {
                    target.put(entry.getKey(), p.getAsNumber());
                } else if (p.isString()) {
                    target.put(entry.getKey(), p.getAsString());
                }
            }
        }
    }

    @Override
    public void write(File file, Map<String, Object> data, Map<String, String> comments) throws Exception {
        JsonObject root = mapToJsonObject(data);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        }
    }

    @SuppressWarnings("unchecked")
    private JsonObject mapToJsonObject(Map<String, Object> data) {
        JsonObject obj = new JsonObject();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof Map) {
                obj.add(entry.getKey(), mapToJsonObject((Map<String, Object>) val));
            } else if (val instanceof Boolean b) {
                obj.addProperty(entry.getKey(), b);
            } else if (val instanceof Number n) {
                obj.addProperty(entry.getKey(), n);
            } else if (val != null) {
                obj.addProperty(entry.getKey(), val.toString());
            }
        }
        return obj;
    }
}
