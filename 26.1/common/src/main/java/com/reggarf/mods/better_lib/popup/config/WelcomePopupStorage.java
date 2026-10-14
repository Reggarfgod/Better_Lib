package com.reggarf.mods.better_lib.popup.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.reggarf.mods.better_lib.Better_lib;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class WelcomePopupStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File GLOBAL_CONFIG_FILE = new File("config/better_lib_popups.json");

    private static boolean globalEnabled = true;
    @Nullable
    private static String userTheme = null;
    private static boolean globalLoaded = false;

    public static synchronized void ensureGlobalLoaded() {
        if (globalLoaded) return;
        globalLoaded = true;
        if (!GLOBAL_CONFIG_FILE.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(GLOBAL_CONFIG_FILE)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (json.has("globalEnabled")) {
                globalEnabled = json.get("globalEnabled").getAsBoolean();
            }
            if (json.has("theme") && !json.get("theme").isJsonNull()) {
                userTheme = json.get("theme").getAsString();
            }
        } catch (Exception e) {
            Better_lib.LOG.warn("Failed to load global welcome popup preferences: {}", e.getMessage());
        }
    }

    public static synchronized void saveGlobalConfig() {
        try {
            if (GLOBAL_CONFIG_FILE.getParentFile() != null && !GLOBAL_CONFIG_FILE.getParentFile().exists()) {
                GLOBAL_CONFIG_FILE.getParentFile().mkdirs();
            }
            JsonObject json = new JsonObject();
            json.addProperty("globalEnabled", globalEnabled);
            if (userTheme != null) {
                json.addProperty("theme", userTheme);
            }

            try (FileWriter writer = new FileWriter(GLOBAL_CONFIG_FILE)) {
                GSON.toJson(json, writer);
            }
        } catch (Exception e) {
            Better_lib.LOG.error("Failed to save global popup preferences: {}", e.getMessage());
        }
    }

    public static void ensureLoaded() {
        ensureGlobalLoaded();
    }

    public static boolean isGlobalEnabled() {
        ensureGlobalLoaded();
        return globalEnabled;
    }

    public static void setGlobalEnabled(boolean enabled) {
        ensureGlobalLoaded();
        globalEnabled = enabled;
        saveGlobalConfig();
    }

    @Nullable
    public static String getUserTheme() {
        ensureGlobalLoaded();
        return userTheme;
    }

    public static void setUserTheme(@Nullable String theme) {
        ensureGlobalLoaded();
        userTheme = theme;
        saveGlobalConfig();
    }

    public static boolean isModDismissed(@Nullable String modId) {
        return false;
    }

    public static void setModDismissed(@Nullable String modId, boolean dismissed) {
    }

    public static void dismissAll(Iterable<String> modIds) {
    }

    public static void resetAll() {
        ensureGlobalLoaded();
        globalEnabled = true;
        userTheme = null;
        saveGlobalConfig();
    }
}
