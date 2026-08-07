package com.reggarf.mods.better_lib.villager.json;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.reggarf.mods.better_lib.villager.SimpleTrade;
import com.reggarf.mods.better_lib.villager.SimpleVillagerLib;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * JSON format:
 * {
 *   "name": "andesite_worker",
 *   "workstation": "minecraft:smooth_stone",
 *   "max_villagers": 1,
 *   "search_range": 1,
 *   "work_sound": "minecraft:entity.villager.work_armorer",
 *   "enabled": true,
 *   "trades": {
 *     "1": [
 *       {
 *         "cost": { "item": "minecraft:emerald", "count": 9 },
 *         "cost2": { "item": "minecraft:cobblestone", "count": 6 },
 *         "result": { "item": "minecraft:iron_ingot", "count": 2 },
 *         "max_trades": 5,
 *         "xp": 3,
 *         "price_multiplier": 0.07
 *       }
 *     ],
 *     "2": [ { "cost": {...}, "result": {...} } ]
 *   }
 * }
 *
 * Notes:
 * - "cost2", "max_villagers", "search_range", "work_sound", "enabled",
 *   "max_trades", "xp", "price_multiplier", and "count" are all optional and
 *   fall back to SimpleTrade / ProfessionBuilder's usual defaults.
 * - "workstation" is resolved lazily (wrapped in a Supplier), same as the
 *   fluent workstation(...) call, so it's safe even if that block registers
 *   after this JSON is loaded.
 * - Item ids ("cost", "cost2", "result") are resolved immediately when the
 *   JSON is parsed, matching SimpleTrade's existing eager item lookup - so
 *   load JSON professions after your own items have been registered.
 * - Any missing required field or unresolvable registry id throws
 *   IllegalArgumentException with the profession name in the message, so
 *   bad JSON fails loudly at startup instead of silently no-opping.
 * - Call {@link SimpleVillagerLib#register()} yourself after loading JSON
 *   professions (e.g. after loadAll / loadResource). The underlying flush is
 *   guarded (registers each lib at most once), so this is safe even if you
 *   also register code-defined professions on the same lib elsewhere.
 */
public final class JsonVillagerLoader {

    private static final Gson GSON = new Gson();

    private JsonVillagerLoader() {
    }

    /**
     * Reads a single profession definition from a classpath resource (a JSON
     * file bundled in your mod jar, e.g. under src/main/resources). Call
     * {@link SimpleVillagerLib#register()} after loading to flush the lib.
     *
     * @param modClass any class in your mod's jar, used only to resolve the resource path
     */
    public static SimpleVillagerLib loadResource(SimpleVillagerLib lib, Class<?> modClass, String resourcePath) {
        try (InputStream stream = modClass.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalArgumentException(
                        "JsonVillagerLoader: resource '" + resourcePath + "' not found on classpath");
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                if (json == null || json.entrySet().isEmpty()) {
                    return lib;
                }
                load(lib, json);
            }
        } catch (IOException e) {
            throw new RuntimeException("JsonVillagerLoader: failed to read '" + resourcePath + "'", e);
        }
        return lib;
    }

    /**
     * Auto-discovers and loads every *.json file directly under a classpath
     * resource folder (non-recursive). Call {@link SimpleVillagerLib#register()}
     * after this to flush the lib. Works in dev (folder on disk) and in a built jar.
     *
     * @param modClass   any class in your mod's jar, used only to resolve the classpath
     * @param folderPath e.g. "/data/mymod/villagers" - relative to your mod's resources root
     */
    public static SimpleVillagerLib loadAll(SimpleVillagerLib lib, Class<?> modClass, String folderPath) {
        String normalized = folderPath.startsWith("/") ? folderPath.substring(1) : folderPath;
        if (!normalized.endsWith("/")) {
            normalized += "/";
        }

        List<String> jsonFileNames = new ArrayList<>();
        FileSystem openedFs = null;
        try {
            Path dirPath = null;

            URL folderUrl = modClass.getClassLoader().getResource(normalized);
            if (folderUrl != null) {
                URI uri = folderUrl.toURI();
                try {
                    dirPath = Paths.get(uri);
                } catch (FileSystemNotFoundException notMounted) {
                    openedFs = FileSystems.newFileSystem(uri, Collections.emptyMap());
                    dirPath = openedFs.provider().getPath(uri);
                }
            }

            if (dirPath == null) {
                URI codeUri = modClass.getProtectionDomain().getCodeSource().getLocation().toURI();
                Path codePath = Paths.get(codeUri);
                if (Files.isDirectory(codePath)) {
                    String codeStr = codePath.toString();
                    String marker = "classes" + File.separator + "java" + File.separator;
                    int idx = codeStr.indexOf(marker);
                    if (idx >= 0) {
                        String sourceSet = codeStr.substring(idx + marker.length());
                        Path resourcesRoot = Paths.get(
                                codeStr.substring(0, idx) + "resources" + File.separator + sourceSet);
                        Path candidate = resourcesRoot.resolve(normalized);
                        if (Files.isDirectory(candidate)) {
                            dirPath = candidate;
                        }
                    }
                }
            }

            if (dirPath == null) {
                throw new IllegalArgumentException(
                        "JsonVillagerLoader: folder '" + folderPath + "' not found on classpath");
            }

            try (Stream<Path> children = Files.list(dirPath)) {
                children.forEach(child -> {
                    String fileName = child.getFileName().toString();
                    if (fileName.endsWith(".json")) {
                        jsonFileNames.add(fileName);
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("JsonVillagerLoader: failed to scan folder '" + folderPath + "'", e);
        } finally {
            if (openedFs != null) {
                try {
                    openedFs.close();
                } catch (IOException ignored) {
                }
            }
        }

        for (String fileName : jsonFileNames) {
            try (InputStream stream = modClass.getResourceAsStream("/" + normalized + fileName);
                 InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                if (json == null || json.entrySet().isEmpty()) {
                    continue;
                }
                load(lib, json);
            } catch (IOException e) {
                throw new RuntimeException(
                        "JsonVillagerLoader: failed to read '" + normalized + fileName + "'", e);
            }
        }

        return lib;
    }

    /** Parses and registers one profession from an already-parsed JsonObject. */
    public static SimpleVillagerLib load(SimpleVillagerLib lib, JsonObject json) {
        String name = requireString(json, "name", "<unknown>");

        SimpleVillagerLib.ProfessionBuilder builder = lib.profession(name);

        String workstationId = requireString(json, "workstation", name);
        Supplier<Block> workstation = () -> resolve(BuiltInRegistries.BLOCK, workstationId, "block", name);
        builder.workstation(workstation);

        if (json.has("max_villagers")) {
            builder.maxVillagers(json.get("max_villagers").getAsInt());
        }
        if (json.has("search_range")) {
            builder.searchRange(json.get("search_range").getAsInt());
        }
        if (json.has("work_sound")) {
            String soundId = json.get("work_sound").getAsString();
            SoundEvent sound = resolve(BuiltInRegistries.SOUND_EVENT, soundId, "sound event", name);
            builder.workSound(sound);
        }
        if (json.has("enabled")) {
            boolean enabled = json.get("enabled").getAsBoolean();
            builder.enabledIf(() -> enabled);
        }

        if (json.has("trades")) {
            JsonObject tradesJson = json.getAsJsonObject("trades");
            for (Map.Entry<String, JsonElement> levelEntry : tradesJson.entrySet()) {
                int level;
                try {
                    level = Integer.parseInt(levelEntry.getKey());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "JsonVillagerLoader: profession '" + name + "' has non-integer trade level key '"
                                    + levelEntry.getKey() + "'");
                }

                JsonArray tradesArray = levelEntry.getValue().getAsJsonArray();
                for (JsonElement tradeElement : tradesArray) {
                    builder.trade(level, parseTrade(name, tradeElement.getAsJsonObject()));
                }
            }
        }

        return builder.register();
    }

    private static SimpleTrade parseTrade(String professionName, JsonObject tradeJson) {
        SimpleTrade trade = SimpleTrade.create();

        if (!tradeJson.has("cost")) {
            throw new IllegalArgumentException(
                    "JsonVillagerLoader: a trade in profession '" + professionName + "' is missing 'cost'");
        }
        applyCost(trade, professionName, tradeJson.getAsJsonObject("cost"), false);

        if (tradeJson.has("cost2")) {
            applyCost(trade, professionName, tradeJson.getAsJsonObject("cost2"), true);
        }

        if (!tradeJson.has("result")) {
            throw new IllegalArgumentException(
                    "JsonVillagerLoader: a trade in profession '" + professionName + "' is missing 'result'");
        }
        JsonObject resultJson = tradeJson.getAsJsonObject("result");
        String resultId = requireString(resultJson, "item", professionName);
        int resultCount = resultJson.has("count") ? resultJson.get("count").getAsInt() : 1;
        Item resultItem = resolve(BuiltInRegistries.ITEM, resultId, "item", professionName);
        trade.result(resultItem, resultCount);

        if (tradeJson.has("max_trades")) {
            trade.maxTrades(tradeJson.get("max_trades").getAsInt());
        }
        if (tradeJson.has("xp")) {
            trade.xp(tradeJson.get("xp").getAsInt());
        }
        if (tradeJson.has("price_multiplier")) {
            trade.priceMultiplier(tradeJson.get("price_multiplier").getAsFloat());
        }

        return trade;
    }

    private static void applyCost(SimpleTrade trade, String professionName, JsonObject costJson, boolean secondary) {
        String itemId = requireString(costJson, "item", professionName);
        int count = costJson.has("count") ? costJson.get("count").getAsInt() : 1;
        ItemLike item = resolve(BuiltInRegistries.ITEM, itemId, "item", professionName);
        if (secondary) {
            trade.cost2(item, count);
        } else {
            trade.cost(item, count);
        }
    }

    private static <T> T resolve(Registry<T> registry, String id, String kind, String professionName) {
        Identifier location;
        try {
            location = Identifier.parse(id);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "JsonVillagerLoader: profession '" + professionName + "' has an invalid " + kind
                            + " id '" + id + "'");
        }
        if (!registry.containsKey(location)) {
            throw new IllegalArgumentException(
                    "JsonVillagerLoader: profession '" + professionName + "' references unknown " + kind
                            + " '" + id + "'");
        }
        return registry.getValue(location);
    }

    private static String requireString(JsonObject json, String key, String professionName) {
        if (!json.has(key)) {
            throw new IllegalArgumentException(
                    "JsonVillagerLoader: profession '" + professionName + "' is missing required field '"
                            + key + "'");
        }
        return json.get(key).getAsString();
    }
}
