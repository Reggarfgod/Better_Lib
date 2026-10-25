package com.reggarf.mods.better_lib.configs;

import com.reggarf.mods.better_lib.configs.format.ConfigFormat;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Public developer API for third-party mods to register, manage, and create
 * modern configuration screens powered by Better_Lib.
 * <p>
 * Modeled after Cloth Config / AutoConfig, allowing developers to choose
 * their preferred serialization format (YAML, TOML, JSON) at registration time.
 */
public class BetterLibConfigApi {

    private static final Map<String, ModConfigEntry> MOD_CONFIGS = new ConcurrentHashMap<>();

    public static class ModConfigEntry {
        public final String modId;
        public ConfigFormat format;
        public ConfigBase client;
        public ConfigBase common;
        public ConfigBase server;
        public Identifier background;
        public Component title;

        public ModConfigEntry(String modId) {
            this(modId, null);
        }

        public ModConfigEntry(String modId, ConfigFormat format) {
            this.modId = modId != null ? modId.toLowerCase() : "unknown";
            this.format = format;
        }

        public ModConfigEntry format(ConfigFormat format) {
            this.format = format;
            if (client != null && format != null) client.setFormat(format);
            if (common != null && format != null) common.setFormat(format);
            if (server != null && format != null) server.setFormat(format);
            return this;
        }

        public ModConfigEntry client(ConfigBase client) {
            this.client = client;
            if (client != null) {
                if (client.getModId() == null || "better_lib".equals(client.getModId())) {
                    client.setModId(this.modId);
                }
                if (this.format != null) {
                    client.setFormat(this.format);
                }
            }
            return this;
        }

        public ModConfigEntry common(ConfigBase common) {
            this.common = common;
            if (common != null) {
                if (common.getModId() == null || "better_lib".equals(common.getModId())) {
                    common.setModId(this.modId);
                }
                if (this.format != null) {
                    common.setFormat(this.format);
                }
            }
            return this;
        }

        public ModConfigEntry server(ConfigBase server) {
            this.server = server;
            if (server != null) {
                if (server.getModId() == null || "better_lib".equals(server.getModId())) {
                    server.setModId(this.modId);
                }
                if (this.format != null) {
                    server.setFormat(this.format);
                }
            }
            return this;
        }

        public ModConfigEntry background(Identifier background) {
            this.background = background;
            return this;
        }

        public ModConfigEntry title(Component title) {
            this.title = title;
            return this;
        }

        public ModConfigEntry loadAll() {
            if (client != null) client.load();
            if (common != null) common.load();
            if (server != null) server.load();
            return this;
        }

        public ModConfigEntry saveAll() {
            if (client != null) client.save();
            if (common != null) common.save();
            if (server != null) server.save();
            return this;
        }
    }

    /**
     * Start registering configuration for a mod using fluent builder pattern.
     * Example (Cloth Config style):
     * <pre>
     * BetterLibConfigApi.register("mymod", ConfigFormat.YAML)
     *     .client(new MyClientConfig())
     *     .common(new MyCommonConfig())
     *     .loadAll();
     * </pre>
     */
    public static ModConfigEntry register(String modId) {
        return register(modId, (ConfigFormat) null);
    }

    /**
     * Register configuration for a mod with a specific format (YAML, TOML, JSON).
     */
    public static ModConfigEntry register(String modId, ConfigFormat format) {
        ModConfigEntry entry = new ModConfigEntry(modId, format);
        MOD_CONFIGS.put(entry.modId, entry);
        return entry;
    }

    /**
     * Direct registration for client, common, and server configs with default format.
     */
    public static ModConfigEntry register(String modId, ConfigBase client, ConfigBase common, ConfigBase server) {
        return register(modId, null, client, common, server);
    }

    /**
     * Direct registration for client, common, and server configs with chosen format (Cloth Config style).
     * Example:
     * <pre>
     * BetterLibConfigApi.register("mymod", ConfigFormat.YAML, client, common, server).loadAll();
     * </pre>
     */
    public static ModConfigEntry register(String modId, ConfigFormat format, ConfigBase client, ConfigBase common, ConfigBase server) {
        ModConfigEntry entry = new ModConfigEntry(modId, format)
                .client(client)
                .common(common)
                .server(server);
        if (format != null) {
            entry.format(format);
        }
        MOD_CONFIGS.put(entry.modId, entry);
        return entry;
    }

    public static ModConfigEntry get(String modId) {
        return modId != null ? MOD_CONFIGS.get(modId.toLowerCase()) : null;
    }

    public static boolean hasConfigs(String modId) {
        return modId != null && MOD_CONFIGS.containsKey(modId.toLowerCase());
    }

    public static Set<String> getRegisteredModIds() {
        return Collections.unmodifiableSet(MOD_CONFIGS.keySet());
    }
}
