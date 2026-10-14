package com.reggarf.mods.better_lib.popup.api;

import com.reggarf.mods.better_lib.message.api.JoinMessagePlugin;
import com.reggarf.mods.better_lib.message.api.JoinMessagePlugins;

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
        // Collect registered WelcomePopupPlugins + adapt any JoinMessagePlugin that doesn't have a popup plugin yet
        Map<String, WelcomePopupPlugin> combined = new LinkedHashMap<>(PLUGINS);

        for (JoinMessagePlugin jPlugin : JoinMessagePlugins.all()) {
            if (!combined.containsKey(jPlugin.getModId()) && jPlugin.enabled()) {
                combined.put(jPlugin.getModId(), new WelcomePopupPlugin() {
                    @Override
                    public String getModId() {
                        return jPlugin.getModId();
                    }

                    @Override
                    public boolean enabled() {
                        return jPlugin.enabled();
                    }

                    @Override
                    public WelcomePopupData getPopupData() {
                        return WelcomePopupData.fromJoinMessageSets(
                                "Welcome from " + jPlugin.getModId(),
                                0xFF5CB85C,
                                jPlugin.getMessageSets()
                        );
                    }
                });
            }
        }

        return combined.values();
    }

    public static List<WelcomePopupPlugin> getEnabledPlugins() {
        List<WelcomePopupPlugin> result = new ArrayList<>();
        for (WelcomePopupPlugin plugin : all()) {
            if (plugin.enabled()) {
                result.add(plugin);
            }
        }
        return result;
    }

    public static WelcomePopupPlugin get(String modId) {
        WelcomePopupPlugin direct = PLUGINS.get(modId);
        if (direct != null) return direct;

        JoinMessagePlugin jPlugin = JoinMessagePlugins.get(modId);
        if (jPlugin != null) {
            return new WelcomePopupPlugin() {
                @Override
                public String getModId() {
                    return jPlugin.getModId();
                }

                @Override
                public boolean enabled() {
                    return jPlugin.enabled();
                }

                @Override
                public WelcomePopupData getPopupData() {
                    return WelcomePopupData.fromJoinMessageSets(
                            "Welcome from " + jPlugin.getModId(),
                            0xFF5CB85C,
                            jPlugin.getMessageSets()
                    );
                }
            };
        }
        return null;
    }

    public static boolean hasAny() {
        return !PLUGINS.isEmpty() || !JoinMessagePlugins.all().isEmpty();
    }
}
