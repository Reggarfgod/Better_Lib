package com.reggarf.mods.better_lib.platform;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.reggarf.mods.better_lib.Better_lib;
import com.reggarf.mods.better_lib.configs.ConfigBase;
import com.reggarf.mods.better_lib.configs.ui.BaseConfigScreen;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class ModConfigAdapter extends ConfigBase {

    private final String name;
    private final ModConfigSpec spec;
    private final ModConfig modConfig;
    private final List<Runnable> saveBindings;
    private final boolean isRoot;

    public ModConfigAdapter(String name, ModConfigSpec spec) {
        this(name, spec, null);
    }

    public ModConfigAdapter(String name, ModConfigSpec spec, ModConfig modConfig) {
        this.name = name;
        this.spec = spec;
        this.modConfig = modConfig;
        this.saveBindings = new ArrayList<>();
        this.isRoot = true;
        if (spec != null) {
            try {
                buildFromGroup(this, spec.getValues());
            } catch (Throwable t) {
                Better_lib.LOG.warn("Failed to build config from group: {}", t.getMessage());
            }
        }
    }

    private ModConfigAdapter(String name, ModConfigSpec spec, ModConfig modConfig, UnmodifiableConfig group, List<Runnable> sharedBindings) {
        this.name = name;
        this.spec = spec;
        this.modConfig = modConfig;
        this.saveBindings = sharedBindings;
        this.isRoot = false;
        if (group != null) {
            buildFromGroup(this, group);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    private void buildFromGroup(ConfigBase currentConfig, UnmodifiableConfig group) {
        if (group == null || spec == null) return;

        group.valueMap().forEach((key, obj) -> {
            if (obj instanceof UnmodifiableConfig subGroup) {
                ModConfigAdapter child = new ModConfigAdapter(key, spec, modConfig, subGroup, saveBindings);
                currentConfig.getChildren().add(child);
            } else if (obj instanceof ModConfigSpec.ConfigValue<?> cv) {
                try {
                    bindConfigValue(currentConfig, key, cv);
                } catch (Throwable t) {
                    Better_lib.LOG.warn("Failed to bind config value for {}: {}", key, t.getMessage());
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void bindConfigValue(ConfigBase targetConfig, String key, ModConfigSpec.ConfigValue<?> cv) {
        ModConfigSpec.ValueSpec vs = spec.getSpec().getRaw(cv.getPath());
        String comment = vs != null && vs.getComment() != null ? vs.getComment() : "";

        Object val = null;
        try {
            val = cv.get();
        } catch (Throwable t) {
            if (vs != null) {
                val = vs.getDefault();
            }
        }

        if (val == null) return;

        if (val instanceof Boolean b) {
            ConfigBool cb = targetConfig.b(b, key, comment);
            saveBindings.add(() -> {
                try {
                    ((ModConfigSpec.ConfigValue<Boolean>) cv).set(cb.get());
                } catch (Throwable ignored) {}
            });
        } else if (val instanceof Integer i) {
            int min = Integer.MIN_VALUE / 2;
            int max = Integer.MAX_VALUE / 2;
            if (vs != null && vs.getRange() != null) {
                if (vs.getRange().getMin() instanceof Number n) min = n.intValue();
                if (vs.getRange().getMax() instanceof Number n) max = n.intValue();
            }
            ConfigInt ci = targetConfig.i(i, min, max, key, comment);
            saveBindings.add(() -> {
                try {
                    ((ModConfigSpec.ConfigValue<Integer>) cv).set(ci.get());
                } catch (Throwable ignored) {}
            });
        } else if (val instanceof Number n) {
            float min = -100000.0f;
            float max = 100000.0f;
            if (vs != null && vs.getRange() != null) {
                if (vs.getRange().getMin() instanceof Number num) min = num.floatValue();
                if (vs.getRange().getMax() instanceof Number num) max = num.floatValue();
            }
            ConfigFloat cf = targetConfig.f(n.floatValue(), min, max, key, comment);
            final Object orig = val;
            saveBindings.add(() -> {
                try {
                    if (orig instanceof Double) {
                        ((ModConfigSpec.ConfigValue<Double>) cv).set(cf.get().doubleValue());
                    } else if (orig instanceof Float) {
                        ((ModConfigSpec.ConfigValue<Float>) cv).set(cf.get());
                    } else if (orig instanceof Long) {
                        ((ModConfigSpec.ConfigValue<Long>) cv).set(cf.get().longValue());
                    }
                } catch (Throwable ignored) {}
            });
        } else if (val instanceof String s) {
            ConfigString cs = targetConfig.s(s, key, comment);
            saveBindings.add(() -> {
                try {
                    ((ModConfigSpec.ConfigValue<String>) cv).set(cs.get());
                } catch (Throwable ignored) {}
            });
        } else if (val instanceof Enum<?> e) {
            ConfigEnum ce = targetConfig.e((Enum) e, key, comment);
            saveBindings.add(() -> {
                try {
                    ((ModConfigSpec.ConfigValue<Object>) cv).set(ce.get());
                } catch (Throwable ignored) {}
            });
        }
    }

    @Override
    public void save() {
        if (isRoot) {
            for (Runnable binding : saveBindings) {
                try {
                    binding.run();
                } catch (Throwable t) {
                    Better_lib.LOG.warn("Failed to apply save binding: {}", t.getMessage());
                }
            }
            if (spec != null && spec.isLoaded()) {
                try {
                    spec.save();
                } catch (Throwable t) {
                    Better_lib.LOG.warn("Failed to save ModConfigSpec: {}", t.getMessage());
                }
            }
        }
        if (children != null) {
            for (ConfigBase child : children) {
                try {
                    child.save();
                } catch (Throwable t) {
                    Better_lib.LOG.warn("Failed to save child config: {}", t.getMessage());
                }
            }
        }
    }

    @Override
    public void onReload() {
        if (isRoot && spec != null && spec.isLoaded()) {
            try {
                spec.afterReload();
            } catch (Throwable ignored) {}
        }
        if (children != null) {
            for (ConfigBase child : children) {
                try {
                    child.onReload();
                } catch (Throwable ignored) {}
            }
        }
    }

    public static boolean hasConfigs(String modId) {
        try {
            return !ModConfigs.getModConfigs(modId).isEmpty();
        } catch (Throwable t) {
            return false;
        }
    }

    public static void populate(String modId, BaseConfigScreen screen) {
        try {
            ModConfig clientCfg = null;
            ModConfig commonCfg = null;
            ModConfig serverCfg = null;

            for (ModConfig cfg : ModConfigs.getModConfigs(modId)) {
                if (cfg.getType() == ModConfig.Type.CLIENT) clientCfg = cfg;
                else if (cfg.getType() == ModConfig.Type.LOCAL) commonCfg = cfg;
                else if (cfg.getType() == ModConfig.Type.SYNCED) serverCfg = cfg;
            }

            ConfigBase client = (clientCfg != null && clientCfg.getSpec() instanceof ModConfigSpec cs) ? new ModConfigAdapter("client", cs, clientCfg) : null;
            ConfigBase common = (commonCfg != null && commonCfg.getSpec() instanceof ModConfigSpec cs) ? new ModConfigAdapter("common", cs, commonCfg) : null;
            ConfigBase server = (serverCfg != null && serverCfg.getSpec() instanceof ModConfigSpec cs) ? new ModConfigAdapter("server", cs, serverCfg) : null;

            screen.withConfigs(client, common, server);
        } catch (Throwable t) {
            Better_lib.LOG.warn("Failed to populate ModConfig for {}: {}", modId, t.getMessage());
        }
    }

    public static Screen createScreenForMod(String modId, Screen parent) {
        BaseConfigScreen screen = new BaseConfigScreen(parent, modId);
        populate(modId, screen);
        return screen;
    }
}
