package com.reggarf.mods.better_lib.villager.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.reggarf.mods.better_lib.Constants;
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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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

    private static final Gson GSON = new GsonBuilder().setLenient().create();

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
                Constants.LOG.warn("[VillagerLib] Resource '{}' not found on classpath", resourcePath);
                return lib;
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                if (json == null || json.entrySet().isEmpty()) {
                    return lib;
                }
                load(lib, json);
            }
        } catch (Exception e) {
            Constants.LOG.error("[VillagerLib] Failed to read resource '{}'", resourcePath, e);
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

        List<String> jsonFileNames;
        try {
            jsonFileNames = collectJsonFileNames(modClass, normalized);
        } catch (Exception e) {
            Constants.LOG.warn("[VillagerLib] Failed to scan folder '{}': {}", folderPath, e.getMessage());
            return lib;
        }

        for (String fileName : jsonFileNames) {
            String fullPath = normalized + fileName;
            try (InputStream stream = modClass.getClassLoader().getResourceAsStream(fullPath)) {
                if (stream == null) {
                    continue;
                }
                try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    JsonObject json = GSON.fromJson(reader, JsonObject.class);
                    if (json == null || json.entrySet().isEmpty()) {
                        continue;
                    }
                    load(lib, json);
                }
            } catch (Exception e) {
                Constants.LOG.error("[VillagerLib] Failed to read '{}'", fullPath, e);
            }
        }

        return lib;
    }

    private static List<String> collectJsonFileNames(Class<?> modClass, String normalized) {
        List<String> jsonFileNames = new ArrayList<>();

        URL folderUrl = modClass.getClassLoader().getResource(normalized);
        if (folderUrl == null && normalized.endsWith("/")) {
            folderUrl = modClass.getClassLoader().getResource(normalized.substring(0, normalized.length() - 1));
        }

        if (folderUrl != null) {
            if ("file".equals(folderUrl.getProtocol())) {
                try (Stream<Path> children = Files.list(Paths.get(folderUrl.toURI()))) {
                    children.forEach(child -> addJsonFileName(jsonFileNames, child.getFileName().toString()));
                } catch (Exception e) {
                    Constants.LOG.warn("[VillagerLib] Failed to list file folder: {}", e.getMessage());
                }
                return jsonFileNames;
            }
            if ("jar".equals(folderUrl.getProtocol())) {
                try {
                    collectJsonFileNamesFromJarUrl(folderUrl, normalized, jsonFileNames);
                    if (!jsonFileNames.isEmpty()) {
                        return jsonFileNames;
                    }
                } catch (Exception e) {
                    Constants.LOG.warn("[VillagerLib] Failed to scan jar via FileSystem, falling back: {}", e.getMessage());
                }
            }
        }

        Path devDir = locateDevResourcesDir(modClass, normalized);
        if (devDir != null) {
            try (Stream<Path> children = Files.list(devDir)) {
                children.forEach(child -> addJsonFileName(jsonFileNames, child.getFileName().toString()));
            } catch (Exception ignored) {
            }
            if (!jsonFileNames.isEmpty()) {
                return jsonFileNames;
            }
        }

        collectJsonFileNamesFromCodeSourceJar(modClass, normalized, jsonFileNames);
        return jsonFileNames;
    }

    private static void collectJsonFileNamesFromJarUrl(URL folderUrl, String normalized, List<String> jsonFileNames)
            throws IOException, URISyntaxException {
        URI uri = folderUrl.toURI();
        String uriString = uri.toString();
        int separator = uriString.indexOf("!/");
        if (separator < 0) {
            throw new IOException("Invalid jar resource URL: " + uriString);
        }

        URI jarUri = URI.create(uriString.substring(0, separator));
        String entryPath = uriString.substring(separator + 1);
        if (!entryPath.startsWith("/")) {
            entryPath = "/" + entryPath;
        }
        if (entryPath.endsWith("/") && entryPath.length() > 1) {
            entryPath = entryPath.substring(0, entryPath.length() - 1);
        }

        FileSystem fileSystem = null;
        boolean shouldClose = false;
        try {
            try {
                fileSystem = FileSystems.getFileSystem(jarUri);
            } catch (FileSystemNotFoundException | IllegalArgumentException notFound) {
                try {
                    fileSystem = FileSystems.newFileSystem(jarUri, Collections.emptyMap());
                    shouldClose = true;
                } catch (FileSystemAlreadyExistsException alreadyExists) {
                    fileSystem = FileSystems.getFileSystem(jarUri);
                }
            }

            Path directory = fileSystem.getPath(entryPath);
            if (Files.exists(directory) && Files.isDirectory(directory)) {
                try (Stream<Path> children = Files.list(directory)) {
                    children.forEach(child -> addJsonFileName(jsonFileNames, child.getFileName().toString()));
                }
            }
        } finally {
            if (shouldClose && fileSystem != null) {
                try {
                    fileSystem.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static void collectJsonFileNamesFromCodeSourceJar(
            Class<?> modClass,
            String normalized,
            List<String> jsonFileNames
    ) {
        try {
            if (modClass.getProtectionDomain() == null
                    || modClass.getProtectionDomain().getCodeSource() == null
                    || modClass.getProtectionDomain().getCodeSource().getLocation() == null) {
                return;
            }

            URI codeUri = modClass.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path jarPath = resolveJarPath(codeUri);
            if (jarPath == null || !Files.isRegularFile(jarPath)) {
                return;
            }

            try (JarFile jarFile = new JarFile(jarPath.toFile())) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.isDirectory()) {
                        continue;
                    }
                    String entryName = entry.getName();
                    if (!entryName.startsWith(normalized) || !entryName.endsWith(".json")) {
                        continue;
                    }
                    String relative = entryName.substring(normalized.length());
                    if (!relative.contains("/")) {
                        addJsonFileName(jsonFileNames, relative);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static Path resolveJarPath(URI codeUri) {
        if ("jar".equals(codeUri.getScheme())) {
            String schemeSpecific = codeUri.getSchemeSpecificPart();
            int separator = schemeSpecific.indexOf('!');
            String jarLocation = separator >= 0 ? schemeSpecific.substring(0, separator) : schemeSpecific;
            return Paths.get(URI.create(jarLocation));
        }
        if (codeUri.getPath() != null && codeUri.getPath().endsWith(".jar")) {
            return Paths.get(codeUri);
        }
        return null;
    }

    private static Path locateDevResourcesDir(Class<?> modClass, String normalized) {
        if (modClass.getProtectionDomain() == null
                || modClass.getProtectionDomain().getCodeSource() == null
                || modClass.getProtectionDomain().getCodeSource().getLocation() == null) {
            return null;
        }

        try {
            Path codePath = Paths.get(modClass.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (!Files.isDirectory(codePath)) {
                return null;
            }

            String codeStr = codePath.toString();
            String marker = "classes" + File.separator + "java" + File.separator;
            int idx = codeStr.indexOf(marker);
            if (idx < 0) {
                return null;
            }

            String sourceSet = codeStr.substring(idx + marker.length());
            Path resourcesRoot = Paths.get(
                    codeStr.substring(0, idx) + "resources" + File.separator + sourceSet
            );
            Path candidate = resourcesRoot.resolve(normalized);
            return Files.isDirectory(candidate) ? candidate : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static void addJsonFileName(List<String> jsonFileNames, String fileName) {
        if (fileName.endsWith(".json")) {
            jsonFileNames.add(fileName);
        }
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
