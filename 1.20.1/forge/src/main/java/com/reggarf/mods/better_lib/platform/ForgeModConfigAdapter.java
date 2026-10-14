package com.reggarf.mods.better_lib.platform;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.reggarf.mods.better_lib.Better_lib;
import com.reggarf.mods.better_lib.configs.ConfigBase;
import com.reggarf.mods.better_lib.configs.ui.BaseConfigScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ForgeModConfigAdapter extends ConfigBase {

    private final String name;
    private final ForgeConfigSpec spec;
    private final ModConfig modConfig;
    private final List<Runnable> saveBindings;
    private final boolean isRoot;

    public ForgeModConfigAdapter(String name, ForgeConfigSpec spec) {
        this(name, spec, null);
    }

    public ForgeModConfigAdapter(String name, ForgeConfigSpec spec, ModConfig modConfig) {
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

    private ForgeModConfigAdapter(String name, ForgeConfigSpec spec, ModConfig modConfig, UnmodifiableConfig group, List<Runnable> sharedBindings) {
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
                ForgeModConfigAdapter child = new ForgeModConfigAdapter(key, spec, modConfig, subGroup, saveBindings);
                currentConfig.getChildren().add(child);
            } else if (obj instanceof ForgeConfigSpec.ConfigValue<?> cv) {
                try {
                    bindConfigValue(currentConfig, key, cv);
                } catch (Throwable t) {
                    Better_lib.LOG.warn("Failed to bind config value for {}: {}", key, t.getMessage());
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void bindConfigValue(ConfigBase targetConfig, String key, ForgeConfigSpec.ConfigValue<?> cv) {
        ForgeConfigSpec.ValueSpec vs = spec.getSpec().getRaw(cv.getPath());
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
                    ((ForgeConfigSpec.ConfigValue<Boolean>) cv).set(cb.get());
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
                    ((ForgeConfigSpec.ConfigValue<Integer>) cv).set(ci.get());
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
                        ((ForgeConfigSpec.ConfigValue<Double>) cv).set(cf.get().doubleValue());
                    } else if (orig instanceof Float) {
                        ((ForgeConfigSpec.ConfigValue<Float>) cv).set(cf.get());
                    } else if (orig instanceof Long) {
                        ((ForgeConfigSpec.ConfigValue<Long>) cv).set(cf.get().longValue());
                    }
                } catch (Throwable ignored) {}
            });
        } else if (val instanceof String s) {
            ConfigString cs = targetConfig.s(s, key, comment);
            saveBindings.add(() -> {
                try {
                    ((ForgeConfigSpec.ConfigValue<String>) cv).set(cs.get());
                } catch (Throwable ignored) {}
            });
        } else if (val instanceof Enum<?> e) {
            ConfigEnum ce = targetConfig.e((Enum) e, key, comment);
            saveBindings.add(() -> {
                try {
                    ((ForgeConfigSpec.ConfigValue<Object>) cv).set(ce.get());
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
                    Better_lib.LOG.warn("Failed to save ForgeConfigSpec: {}", t.getMessage());
                }
            } else if (modConfig != null) {
                try {
                    modConfig.save();
                } catch (Throwable t) {
                    Better_lib.LOG.warn("Failed to save ModConfig: {}", t.getMessage());
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
            for (Set<ModConfig> set : ConfigTracker.INSTANCE.configSets().values()) {
                for (ModConfig cfg : set) {
                    if (cfg.getModId().equalsIgnoreCase(modId)) {
                        return true;
                    }
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }

    public static void populate(String modId, BaseConfigScreen screen) {
        try {
            ModConfig clientCfg = null;
            ModConfig commonCfg = null;
            ModConfig serverCfg = null;

            for (Set<ModConfig> set : ConfigTracker.INSTANCE.configSets().values()) {
                for (ModConfig cfg : set) {
                    if (cfg.getModId().equalsIgnoreCase(modId)) {
                        if (cfg.getType() == ModConfig.Type.CLIENT) clientCfg = cfg;
                        else if (cfg.getType() == ModConfig.Type.COMMON) commonCfg = cfg;
                        else if (cfg.getType() == ModConfig.Type.SERVER) serverCfg = cfg;
                    }
                }
            }

            ConfigBase client = (clientCfg != null && clientCfg.getSpec() instanceof ForgeConfigSpec cs) ? new ForgeModConfigAdapter("client", cs, clientCfg) : null;
            ConfigBase common = (commonCfg != null && commonCfg.getSpec() instanceof ForgeConfigSpec cs) ? new ForgeModConfigAdapter("common", cs, commonCfg) : null;
            ConfigBase server = (serverCfg != null && serverCfg.getSpec() instanceof ForgeConfigSpec cs) ? new ForgeModConfigAdapter("server", cs, serverCfg) : null;

            screen.withConfigs(client, common, server);
        } catch (Throwable t) {
            Better_lib.LOG.warn("Failed to populate Forge ModConfig for {}: {}", modId, t.getMessage());
        }
    }

    public static Screen createScreenForMod(String modId, Screen parent) {
        BaseConfigScreen screen = new BaseConfigScreen(parent, modId);
        populate(modId, screen);
        return screen;
    }
}
