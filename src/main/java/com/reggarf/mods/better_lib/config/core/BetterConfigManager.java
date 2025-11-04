package com.reggarf.mods.better_lib.config.core;

import com.google.gson.*;
import com.reggarf.mods.better_lib.config.annotation.Config;
import com.reggarf.mods.better_lib.config.annotation.ConfigEntry;
import com.reggarf.mods.better_lib.config.api.ConfigData;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class BetterConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static <T extends ConfigData> T register(Class<T> clazz) {
        try {
            Config configAnnotation = clazz.getAnnotation(Config.class);
            if (configAnnotation == null)
                throw new IllegalArgumentException("Missing @Config annotation on " + clazz.getName());

            Path file = Path.of("config", configAnnotation.name() + ".json");
            Files.createDirectories(file.getParent());

            T configInstance = clazz.getDeclaredConstructor().newInstance();

            if (Files.exists(file)) {
                try (FileReader reader = new FileReader(file.toFile())) {
                    JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                    for (Field field : clazz.getDeclaredFields()) {
                        field.setAccessible(true);
                        String key = field.getName();

                        if (root.has(key)) {
                            JsonElement element = root.get(key);
                            if (element.isJsonObject() && element.getAsJsonObject().has("value")) {
                                element = element.getAsJsonObject().get("value");
                            }
                            setFieldValue(field, configInstance, element);
                        }
                    }
                }
            }

            // Always save to ensure structure exists
            save(configAnnotation.name(), configInstance);
            return configInstance;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load config for " + clazz.getName(), e);
        }
    }

    private static void setFieldValue(Field field, Object config, JsonElement element) throws IllegalAccessException {
        if (element == null || element.isJsonNull()) return;
        Class<?> type = field.getType();

        try {
            if (type == boolean.class || type == Boolean.class) {
                field.set(config, element.getAsBoolean());
            } else if (type == int.class || type == Integer.class) {
                field.set(config, element.getAsInt());
            } else if (type == double.class || type == Double.class) {
                field.set(config, element.getAsDouble());
            } else if (type == float.class || type == Float.class) {
                field.set(config, element.getAsFloat());
            } else if (type == long.class || type == Long.class) {
                field.set(config, element.getAsLong());
            } else if (type == String.class) {
                field.set(config, element.getAsString());
            } else if (List.class.isAssignableFrom(type)) {
                // ✅ Proper List support
                java.lang.reflect.Type genericType = field.getGenericType();
                List<?> list = GSON.fromJson(element, genericType);
                field.set(config, list);
            } else if (type.isArray()) {
                Object array = GSON.fromJson(element, type);
                field.set(config, array);
            } else {
                Object obj = GSON.fromJson(element, type);
                field.set(config, obj);
            }
        } catch (JsonSyntaxException e) {
            // ✅ Backward compatibility for old configs that stored lists as strings
            if (List.class.isAssignableFrom(type) && element.isJsonPrimitive()) {
                String s = element.getAsString();
                List<String> list = Arrays.asList(s.split(","));
                field.set(config, list);
                System.out.println("[BetterConfigManager] Converted old string list for field: " + field.getName());
            } else {
                throw e;
            }
        }
    }

    public static void save(String name, Object configInstance) {
        try {
            Path file = Path.of("config", name + ".json");
            Files.createDirectories(file.getParent());

            JsonObject root = new JsonObject();
            Class<?> clazz = configInstance.getClass();

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(configInstance);

                String category = "General";
                String description = "No description provided.";

                if (field.isAnnotationPresent(ConfigEntry.Category.class))
                    category = field.getAnnotation(ConfigEntry.Category.class).value();
                if (field.isAnnotationPresent(ConfigEntry.Description.class))
                    description = field.getAnnotation(ConfigEntry.Description.class).value();

                JsonObject entry = new JsonObject();

                // ✅ Properly serialize Lists and arrays as JSON
                if (value instanceof List<?> list) {
                    entry.add("value", GSON.toJsonTree(list));
                } else if (value != null && value.getClass().isArray()) {
                    entry.add("value", GSON.toJsonTree(value));
                } else if (value instanceof Number num) {
                    entry.addProperty("value", num);
                } else if (value instanceof Boolean bool) {
                    entry.addProperty("value", bool);
                } else if (value != null) {
                    entry.addProperty("value", String.valueOf(value));
                } else {
                    entry.add("value", JsonNull.INSTANCE);
                }

                entry.addProperty("category", category);
                entry.addProperty("description", description);
                root.add(field.getName(), entry);
            }

            try (FileWriter writer = new FileWriter(file.toFile())) {
                GSON.toJson(root, writer);
            }

            System.out.println("[BetterConfigManager] Saved config file: " + file);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
