package com.reggarf.mods.better_lib.configs;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.reggarf.mods.better_lib.Better_lib;
import com.reggarf.mods.better_lib.configs.format.ConfigFormat;
import com.reggarf.mods.better_lib.configs.format.ConfigFormatHandler;
import com.reggarf.mods.better_lib.configs.format.ConfigFormatHandlers;

public abstract class ConfigBase {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	protected int depth;
	protected List<CValue<?, ?>> allValues = new ArrayList<>();
	protected List<ConfigBase> children = new ArrayList<>();
	protected File configFile;
	protected ConfigFormat format;
	protected String modId;

	public ConfigBase() {
		this(null, null);
	}

	public ConfigBase(String modId) {
		this(modId, null);
	}

	public ConfigBase(ConfigFormat format) {
		this(null, format);
	}

	public ConfigBase(String modId, ConfigFormat format) {
		this.modId = modId;
		this.format = format;
	}

	public abstract String getName();

	public String getModId() {
		return modId != null ? modId : "better_lib";
	}

	public void setModId(String modId) {
		this.modId = modId;
		if (children != null) {
			for (ConfigBase child : children) {
				child.setModId(modId);
			}
		}
	}

	public ConfigFormat getFormat() {
		if (this.format != null) {
			return this.format;
		}
		File existing = findExistingConfigFile();
		if (existing != null) {
			this.format = ConfigFormat.fromFile(existing);
			return this.format;
		}
		return ConfigFormat.YAML;
	}

	public void setFormat(ConfigFormat format) {
		this.format = format;
		if (children != null) {
			for (ConfigBase child : children) {
				child.setFormat(format);
			}
		}
	}

	public void setConfigFile(File file) {
		this.configFile = file;
		if (file != null) {
			this.format = ConfigFormat.fromFile(file);
		}
	}

	public File getConfigFile() {
		return resolveConfigFile();
	}

	public File getConfigDirectory() {
		String id = getModId();
		if (id == null || id.isEmpty()) {
			return new File("config");
		}
		return new File("config/" + id);
	}

	public File findExistingConfigFile() {
		File dir = getConfigDirectory();
		if (!dir.exists()) {
			return null;
		}
		String[] exts = {"yaml", "yml", "toml", "json", "json5", "xml", "ini"};
		for (String ext : exts) {
			File f = new File(dir, getName() + "." + ext);
			if (f.exists()) {
				return f;
			}
		}
		return null;
	}

	public File resolveConfigFile() {
		if (configFile != null) {
			return configFile;
		}
		File existing = findExistingConfigFile();
		if (existing != null) {
			this.format = ConfigFormat.fromFile(existing);
			this.configFile = existing;
			return existing;
		}
		ConfigFormat fmt = getFormat();
		String ext = (fmt != null) ? fmt.getExtension() : "yaml";
		File dir = getConfigDirectory();
		File preferred = new File(dir, getName() + "." + ext);
		this.configFile = preferred;
		return preferred;
	}

	public void load() {
		File file = resolveConfigFile();
		if (file != null && file.exists()) {
			try {
				ConfigFormatHandler handler = ConfigFormatHandlers.getHandler(getFormat());
				Map<String, Object> data = handler.read(file);
				applyFromMap(data);
			} catch (Exception e) {
				Better_lib.LOG.error("Failed to load config file: {}", file, e);
			}
		}
		if (children != null) {
			for (ConfigBase child : children) {
				child.load();
			}
		}
	}

	public void save() {
		File file = resolveConfigFile();
		try {
			if (file.getParentFile() != null && !file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			ConfigFormatHandler handler = ConfigFormatHandlers.getHandler(getFormat());
			Map<String, Object> data = exportToMap();
			Map<String, String> comments = exportComments();
			handler.write(file, data, comments);
		} catch (Exception e) {
			Better_lib.LOG.error("Failed to save config file: {}", file, e);
		}
		if (children != null) {
			for (ConfigBase child : children) {
				child.save();
			}
		}
	}

	public Map<String, Object> exportToMap() {
		Map<String, Object> map = new LinkedHashMap<>();
		for (CValue<?, ?> val : allValues) {
			if (val instanceof ConfigGroup) continue;
			map.put(val.getName(), val.getRawValue());
		}
		for (ConfigBase child : children) {
			map.put(child.getName(), child.exportToMap());
		}
		return map;
	}

	public Map<String, String> exportComments() {
		Map<String, String> commentsMap = new LinkedHashMap<>();
		exportCommentsInternal("", commentsMap);
		return commentsMap;
	}

	private void exportCommentsInternal(String prefix, Map<String, String> map) {
		for (CValue<?, ?> val : allValues) {
			if (val instanceof ConfigGroup) continue;
			String comment = val.getCommentString();
			if (comment != null && !comment.isEmpty()) {
				map.put(prefix + val.getName(), comment);
			}
		}
		for (ConfigBase child : children) {
			String childPrefix = prefix + child.getName() + ".";
			child.exportCommentsInternal(childPrefix, map);
		}
	}

	@SuppressWarnings("unchecked")
	public void applyFromMap(Map<String, Object> map) {
		if (map == null) return;
		for (CValue<?, ?> val : allValues) {
			if (map.containsKey(val.getName())) {
				val.setRawValue(map.get(val.getName()));
			}
		}
		for (ConfigBase child : children) {
			if (map.containsKey(child.getName())) {
				Object childObj = map.get(child.getName());
				if (childObj instanceof Map) {
					child.applyFromMap((Map<String, Object>) childObj);
				}
			}
		}
	}

	public void readFromJson(JsonObject json) {
		for (CValue<?, ?> val : allValues) {
			if (json.has(val.getName())) {
				val.readFromJson(json.get(val.getName()));
			}
		}
		for (ConfigBase child : children) {
			if (json.has(child.getName()) && json.get(child.getName()).isJsonObject()) {
				child.readFromJson(json.getAsJsonObject(child.getName()));
			}
		}
	}

	public void writeToJson(JsonObject json) {
		for (CValue<?, ?> val : allValues) {
			val.writeToJson(json);
		}
		for (ConfigBase child : children) {
			JsonObject childJson = new JsonObject();
			child.writeToJson(childJson);
			json.add(child.getName(), childJson);
		}
	}

	public List<CValue<?, ?>> getAllValues() {
		return allValues;
	}

	public List<ConfigBase> getChildren() {
		return children;
	}

	public void onLoad() {
		if (children != null)
			children.forEach(ConfigBase::onLoad);
	}

	public void onReload() {
		if (children != null)
			children.forEach(ConfigBase::onReload);
	}

	public ConfigBool b(boolean current, String name, String... comment) {
		return new ConfigBool(name, current, comment);
	}

	public ConfigFloat f(float current, float min, float max, String name, String... comment) {
		return new ConfigFloat(name, current, min, max, comment);
	}

	public ConfigFloat f(float current, float min, String name, String... comment) {
		return f(current, min, Float.MAX_VALUE, name, comment);
	}

	public ConfigInt i(int current, int min, int max, String name, String... comment) {
		return new ConfigInt(name, current, min, max, comment);
	}

	public ConfigInt i(int current, int min, String name, String... comment) {
		return i(current, min, Integer.MAX_VALUE, name, comment);
	}

	public ConfigInt i(int current, String name, String... comment) {
		return i(current, Integer.MIN_VALUE, Integer.MAX_VALUE, name, comment);
	}

	public ConfigString s(String current, String name, String... comment) {
		return new ConfigString(name, current, comment);
	}

	public <T extends Enum<T>> ConfigEnum<T> e(T defaultValue, String name, String... comment) {
		return new ConfigEnum<>(name, defaultValue, comment);
	}

	public ConfigGroup group(int depth, String name, String... comment) {
		return new ConfigGroup(name, depth, comment);
	}

	public <T extends ConfigBase> T nested(int depth, Supplier<T> constructor, String... comment) {
		T config = constructor.get();
		config.depth = depth;
		config.modId = this.getModId();
		config.format = this.getFormat();
		if (children == null)
			children = new ArrayList<>();
		children.add(config);
		return config;
	}

	public abstract class CValue<V, S> {
		protected V value;
		protected V defaultValue;
		protected String name;
		protected String[] comments;

		public CValue(String name, V defaultValue, String... comments) {
			this.name = name;
			this.defaultValue = defaultValue;
			this.value = defaultValue;
			this.comments = comments;
			allValues.add(this);
		}

		public V get() {
			return value;
		}

		public void set(V value) {
			this.value = value;
		}

		public V getDefault() {
			return defaultValue;
		}

		public String getName() {
			return name;
		}

		public String[] getComments() {
			return comments;
		}

		public String getCommentString() {
			if (comments == null || comments.length == 0) return "";
			return String.join("\n", comments);
		}

		public Object getRawValue() {
			if (value instanceof Enum<?> e) {
				return e.name();
			}
			return value;
		}

		public abstract void setRawValue(Object raw);

		public abstract void readFromJson(JsonElement element);
		public abstract void writeToJson(JsonObject object);
	}

	public class ConfigGroup extends CValue<Boolean, Boolean> {
		private int groupDepth;

		public ConfigGroup(String name, int depth, String... comment) {
			super(name, true, comment);
			this.groupDepth = depth;
		}

		public int getGroupDepth() {
			return groupDepth;
		}

		@Override
		public void setRawValue(Object raw) {}

		@Override
		public void readFromJson(JsonElement element) {}

		@Override
		public void writeToJson(JsonObject object) {}
	}

	public class ConfigBool extends CValue<Boolean, Boolean> {
		public ConfigBool(String name, boolean def, String... comment) {
			super(name, def, comment);
		}

		@Override
		public void setRawValue(Object raw) {
			if (raw instanceof Boolean b) {
				this.value = b;
			} else if (raw != null) {
				this.value = Boolean.parseBoolean(raw.toString().trim());
			}
		}

		@Override
		public void readFromJson(JsonElement element) {
			if (element.isJsonPrimitive()) {
				this.value = element.getAsBoolean();
			}
		}

		@Override
		public void writeToJson(JsonObject object) {
			object.addProperty(name, value);
		}
	}

	public class ConfigString extends CValue<String, String> {
		public ConfigString(String name, String def, String... comment) {
			super(name, def, comment);
		}

		@Override
		public void setRawValue(Object raw) {
			if (raw != null) {
				this.value = raw.toString();
			}
		}

		@Override
		public void readFromJson(JsonElement element) {
			if (element.isJsonPrimitive()) {
				this.value = element.getAsString();
			}
		}

		@Override
		public void writeToJson(JsonObject object) {
			object.addProperty(name, value);
		}
	}

	public class ConfigEnum<T extends Enum<T>> extends CValue<T, T> {
		public ConfigEnum(String name, T defaultValue, String... comment) {
			super(name, defaultValue, comment);
		}

		@Override
		public void setRawValue(Object raw) {
			if (raw != null) {
				try {
					this.value = Enum.valueOf(defaultValue.getDeclaringClass(), raw.toString().trim());
				} catch (Exception ignored) {}
			}
		}

		@Override
		public void readFromJson(JsonElement element) {
			if (element.isJsonPrimitive()) {
				try {
					this.value = Enum.valueOf(defaultValue.getDeclaringClass(), element.getAsString());
				} catch (Exception ignored) {}
			}
		}

		@Override
		public void writeToJson(JsonObject object) {
			object.addProperty(name, value != null ? value.name() : defaultValue.name());
		}
	}

	public class ConfigFloat extends CValue<Float, Float> {
		private final float min;
		private final float max;

		public ConfigFloat(String name, float current, float min, float max, String... comment) {
			super(name, current, comment);
			this.min = min;
			this.max = max;
		}

		public float getF() {
			return get();
		}

		public float getMin() {
			return min;
		}

		public float getMax() {
			return max;
		}

		@Override
		public void setRawValue(Object raw) {
			if (raw instanceof Number n) {
				this.value = Math.max(min, Math.min(max, n.floatValue()));
			} else if (raw != null) {
				try {
					this.value = Math.max(min, Math.min(max, Float.parseFloat(raw.toString().trim())));
				} catch (NumberFormatException ignored) {}
			}
		}

		@Override
		public void readFromJson(JsonElement element) {
			if (element.isJsonPrimitive()) {
				this.value = Math.max(min, Math.min(max, element.getAsFloat()));
			}
		}

		@Override
		public void writeToJson(JsonObject object) {
			object.addProperty(name, value);
		}
	}

	public class ConfigInt extends CValue<Integer, Integer> {
		private final int min;
		private final int max;

		public ConfigInt(String name, int current, int min, int max, String... comment) {
			super(name, current, comment);
			this.min = min;
			this.max = max;
		}

		public int getMin() {
			return min;
		}

		public int getMax() {
			return max;
		}

		@Override
		public void setRawValue(Object raw) {
			if (raw instanceof Number n) {
				this.value = Math.max(min, Math.min(max, n.intValue()));
			} else if (raw != null) {
				try {
					this.value = Math.max(min, Math.min(max, Integer.parseInt(raw.toString().trim())));
				} catch (NumberFormatException ignored) {}
			}
		}

		@Override
		public void readFromJson(JsonElement element) {
			if (element.isJsonPrimitive()) {
				this.value = Math.max(min, Math.min(max, element.getAsInt()));
			}
		}

		@Override
		public void writeToJson(JsonObject object) {
			object.addProperty(name, value);
		}
	}
}
