package com.reggarf.mods.better_lib.configs.format;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TomlConfigHandler implements ConfigFormatHandler {
    public static final TomlConfigHandler INSTANCE = new TomlConfigHandler();

    @Override
    public ConfigFormat getFormat() {
        return ConfigFormat.TOML;
    }

    @Override
    public Map<String, Object> read(File file) throws Exception {
        Map<String, Object> root = new LinkedHashMap<>();
        if (!file.exists()) return root;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            Map<String, Object> currentMap = root;
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("[") && line.endsWith("]")) {
                    String sectionPath = line.substring(1, line.length() - 1).trim();
                    currentMap = getOrCreateSection(root, sectionPath);
                    continue;
                }

                int eqIdx = line.indexOf('=');
                if (eqIdx != -1) {
                    String key = line.substring(0, eqIdx).trim();
                    String rawVal = line.substring(eqIdx + 1).trim();
                    rawVal = stripInlineComment(rawVal);
                    Object parsed = parseTomlValue(rawVal);
                    currentMap.put(key, parsed);
                }
            }
        }
        return root;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getOrCreateSection(Map<String, Object> root, String path) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = root;
        for (String part : parts) {
            Object next = current.get(part);
            if (!(next instanceof Map)) {
                Map<String, Object> newMap = new LinkedHashMap<>();
                current.put(part, newMap);
                current = newMap;
            } else {
                current = (Map<String, Object>) next;
            }
        }
        return current;
    }

    private String stripInlineComment(String value) {
        if (value.startsWith("\"")) {
            int closeQuote = value.indexOf('"', 1);
            if (closeQuote != -1) {
                return value.substring(0, closeQuote + 1).trim();
            }
        }
        int hashIdx = value.indexOf('#');
        if (hashIdx != -1) {
            return value.substring(0, hashIdx).trim();
        }
        return value;
    }

    private Object parseTomlValue(String raw) {
        if (raw.equalsIgnoreCase("true")) return Boolean.TRUE;
        if (raw.equalsIgnoreCase("false")) return Boolean.FALSE;
        if (raw.startsWith("\"") && raw.endsWith("\"") && raw.length() >= 2) {
            return unescapeString(raw.substring(1, raw.length() - 1));
        }
        try {
            if (raw.contains(".")) {
                return Double.parseDouble(raw);
            } else {
                return Long.parseLong(raw);
            }
        } catch (NumberFormatException ignored) {}
        return raw;
    }

    @Override
    public void write(File file, Map<String, Object> data, Map<String, String> comments) throws Exception {
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writeSection(writer, data, "", comments);
        }
    }

    @SuppressWarnings("unchecked")
    private void writeSection(BufferedWriter writer, Map<String, Object> data, String prefix, Map<String, String> comments) throws IOException {
        // 1. Write scalar properties first
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() instanceof Map) continue;
            String fullKey = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            writeComment(writer, comments != null ? comments.get(fullKey) : null, "");
            writer.write(entry.getKey() + " = " + formatTomlValue(entry.getValue()) + "\n");
        }

        // 2. Write subsections
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() instanceof Map) {
                String subPrefix = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                writer.write("\n");
                writeComment(writer, comments != null ? comments.get(subPrefix) : null, "");
                writer.write("[" + subPrefix + "]\n");
                writeSection(writer, (Map<String, Object>) entry.getValue(), subPrefix, comments);
            }
        }
    }

    private void writeComment(BufferedWriter writer, String comment, String indent) throws IOException {
        if (comment == null || comment.trim().isEmpty()) return;
        for (String line : comment.split("\n")) {
            writer.write(indent + "# " + line + "\n");
        }
    }

    private String formatTomlValue(Object val) {
        if (val == null) return "\"\"";
        if (val instanceof Boolean || val instanceof Number) {
            return val.toString();
        }
        return "\"" + escapeString(val.toString()) + "\"";
    }

    private String escapeString(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private String unescapeString(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
    }
}
