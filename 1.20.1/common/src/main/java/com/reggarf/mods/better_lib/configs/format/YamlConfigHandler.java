package com.reggarf.mods.better_lib.configs.format;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class YamlConfigHandler implements ConfigFormatHandler {
    public static final YamlConfigHandler INSTANCE = new YamlConfigHandler();

    @Override
    public ConfigFormat getFormat() {
        return ConfigFormat.YAML;
    }

    private static class IndentLevel {
        final int indent;
        final Map<String, Object> map;

        IndentLevel(int indent, Map<String, Object> map) {
            this.indent = indent;
            this.map = map;
        }
    }

    @Override
    public Map<String, Object> read(File file) throws Exception {
        Map<String, Object> root = new LinkedHashMap<>();
        if (!file.exists()) return root;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            Deque<IndentLevel> stack = new ArrayDeque<>();
            stack.push(new IndentLevel(-1, root));

            String line;
            while ((line = reader.readLine()) != null) {
                int lineIndent = countIndent(line);
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                while (stack.size() > 1 && stack.peek().indent >= lineIndent) {
                    stack.pop();
                }

                Map<String, Object> currentMap = stack.peek().map;

                int colonIdx = trimmed.indexOf(':');
                if (colonIdx != -1) {
                    String key = trimmed.substring(0, colonIdx).trim();
                    String rawVal = trimmed.substring(colonIdx + 1).trim();
                    rawVal = stripInlineComment(rawVal);

                    if (rawVal.isEmpty()) {
                        Map<String, Object> childMap = new LinkedHashMap<>();
                        currentMap.put(key, childMap);
                        stack.push(new IndentLevel(lineIndent, childMap));
                    } else {
                        currentMap.put(key, parseYamlValue(rawVal));
                    }
                }
            }
        }
        return root;
    }

    private int countIndent(String line) {
        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == ' ') {
                count++;
            } else if (c == '\t') {
                count += 2;
            } else {
                break;
            }
        }
        return count;
    }

    private String stripInlineComment(String value) {
        if (value.startsWith("\"")) {
            int closeQuote = value.indexOf('"', 1);
            if (closeQuote != -1) {
                return value.substring(0, closeQuote + 1).trim();
            }
        } else if (value.startsWith("'")) {
            int closeQuote = value.indexOf('\'', 1);
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

    private Object parseYamlValue(String raw) {
        if (raw.equalsIgnoreCase("true") || raw.equalsIgnoreCase("yes") || raw.equalsIgnoreCase("on")) return Boolean.TRUE;
        if (raw.equalsIgnoreCase("false") || raw.equalsIgnoreCase("no") || raw.equalsIgnoreCase("off")) return Boolean.FALSE;
        if ((raw.startsWith("\"") && raw.endsWith("\"")) || (raw.startsWith("'") && raw.endsWith("'"))) {
            if (raw.length() >= 2) {
                return raw.substring(1, raw.length() - 1);
            }
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
            writeYaml(writer, data, "", 0, comments);
        }
    }

    @SuppressWarnings("unchecked")
    private void writeYaml(BufferedWriter writer, Map<String, Object> data, String prefix, int indent, Map<String, String> comments) throws IOException {
        String indentStr = "  ".repeat(indent);
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String fullKey = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            String comment = comments != null ? comments.get(fullKey) : null;
            if (comment != null && !comment.trim().isEmpty()) {
                for (String cLine : comment.split("\n")) {
                    writer.write(indentStr + "# " + cLine + "\n");
                }
            }

            if (entry.getValue() instanceof Map) {
                writer.write(indentStr + entry.getKey() + ":\n");
                writeYaml(writer, (Map<String, Object>) entry.getValue(), fullKey, indent + 1, comments);
            } else {
                writer.write(indentStr + entry.getKey() + ": " + formatYamlValue(entry.getValue()) + "\n");
            }
        }
    }

    private String formatYamlValue(Object val) {
        if (val == null) return "null";
        if (val instanceof Boolean || val instanceof Number) {
            return val.toString();
        }
        String s = val.toString();
        if (s.contains(":") || s.contains("#") || s.contains("\n") || s.startsWith(" ") || s.endsWith(" ")) {
            return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
        }
        return s;
    }
}
