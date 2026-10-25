package com.reggarf.mods.better_lib.configs.ui;

import com.reggarf.mods.better_lib.configs.ConfigBase;
import com.reggarf.mods.better_lib.configs.utility.Pair;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigHelper {

	public static final Pattern unitPattern = Pattern.compile("\\[(in .*)]");
	public static final Pattern annotationPattern = Pattern.compile("\\[@cui:([^:]*)(?::(.*))?]");

	public static final Map<String, ConfigChange> changes = new HashMap<>();

	public static <T> void setValue(String path, ConfigBase.CValue<T, ?> configValue, T value,
		Map<String, String> annotations) {
		if (value.equals(configValue.get())) {
			changes.remove(path);
		} else {
			changes.put(path, annotations == null ? new ConfigChange(value) : new ConfigChange(value, annotations));
		}
	}

	public static <T> T getValue(String path, ConfigBase.CValue<T, ?> configValue) {
		ConfigChange configChange = changes.get(path);
		if (configChange != null)
			return (T) configChange.value;
		else
			return configValue.get();
	}

	public static Pair<String, Map<String, String>> readMetadataFromComment(List<String> commentLines) {
		AtomicReference<String> unit = new AtomicReference<>();
		Map<String, String> annotations = new HashMap<>();

		commentLines.removeIf(line -> {
			if (line.trim().isEmpty()) {
				return true;
			}

			Matcher matcher = annotationPattern.matcher(line);
			if (matcher.matches()) {
				String annotation = matcher.group(1);
				String aValue = matcher.group(2);
				annotations.putIfAbsent(annotation, aValue);
				return true;
			}

			matcher = unitPattern.matcher(line);
			if (matcher.matches()) {
				unit.set(matcher.group(1));
			}

			return false;
		});

		return Pair.of(unit.get(), annotations);
	}

	public static class ConfigChange {
		public Object value;
		public Map<String, String> annotations;

		public ConfigChange(Object value) {
			this.value = value;
		}

		public ConfigChange(Object value, Map<String, String> annotations) {
			this.value = value;
			this.annotations = new HashMap<>();
			this.annotations.putAll(annotations);
		}
	}
}
