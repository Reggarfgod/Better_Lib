package com.reggarf.mods.better_lib.popup.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class WelcomePopupPlugins {

    private static final Map<String, WelcomePopupPlugin> PLUGINS = new LinkedHashMap<>();

    public static void register(WelcomePopupPlugin plugin) {
        PLUGINS.put(plugin.getModId(), plugin);
    }

    public static void register(String modId, String modName, int accentColor, Consumer<WelcomePopupData.Builder> builderConsumer) {
        WelcomePopupData.Builder builder = WelcomePopupData.builder()
                .title("Welcome to " + modName)
                .accentColor(accentColor);
        builderConsumer.accept(builder);
        WelcomePopupData data = builder.build();

        register(new WelcomePopupPlugin() {
            @Override
            public String getModId() {
                return modId;
            }

            @Override
            public String getModName() {
                return modName;
            }

            @Override
            public int getAccentColor() {
                return accentColor;
            }

            @Override
            public WelcomePopupData getPopupData() {
                return data;
            }
        });
    }

    public static Collection<WelcomePopupPlugin> all() {
        return PLUGINS.values();
    }

    public static List<WelcomePopupPlugin> getEnabledPlugins() {
        List<WelcomePopupPlugin> result = new ArrayList<>();
        for (WelcomePopupPlugin plugin : PLUGINS.values()) {
            if (plugin.enabled()) {
                result.add(plugin);
            }
        }
        return result;
    }

    public static WelcomePopupPlugin get(String modId) {
        return PLUGINS.get(modId);
    }

    public static boolean hasAny() {
        return !PLUGINS.isEmpty();
    }
}
