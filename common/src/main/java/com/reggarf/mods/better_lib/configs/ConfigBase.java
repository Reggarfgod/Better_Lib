package com.reggarf.mods.better_lib.configs;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.reggarf.mods.better_lib.Better_lib;

public abstract class ConfigBase {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	protected int depth;
	protected List<CValue<?, ?>> allValues = new ArrayList<>();
	protected List<ConfigBase> children = new ArrayList<>();
	protected File configFile;

	public abstract String getName();

	public void setConfigFile(File file) {
		this.configFile = file;
	}

	public void load() {
		if (configFile != null && configFile.exists()) {
			try (FileReader reader = new FileReader(configFile)) {
				JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
				readFromJson(json);
			} catch (Exception e) {
				Better_lib.LOG.error("Failed to load config file: {}", configFile, e);
			}
		}
		if (children != null) {
			for (ConfigBase child : children) {
				child.load();
			}
		}
	}

	public void save() {
		if (configFile == null) {
			configFile = new File("config/better_lib/" + getName() + ".json");
		}
		try {
			if (configFile.getParentFile() != null && !configFile.getParentFile().exists()) {
				configFile.getParentFile().mkdirs();
			}
			JsonObject json = new JsonObject();
			writeToJson(json);
			try (FileWriter writer = new FileWriter(configFile)) {
				GSON.toJson(json, writer);
			}
		} catch (Exception e) {
			Better_lib.LOG.error("Failed to save config file: {}", configFile, e);
		}
		if (children != null) {
			for (ConfigBase child : children) {
				child.save();
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
		public void readFromJson(JsonElement element) {}

		@Override
		public void writeToJson(JsonObject object) {}
	}

	public class ConfigBool extends CValue<Boolean, Boolean> {
		public ConfigBool(String name, boolean def, String... comment) {
			super(name, def, comment);
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
		public void readFromJson(JsonElement element) {
			if (element.isJsonPrimitive()) {
				try {
					this.value = Enum.valueOf(defaultValue.getDeclaringClass(), element.getAsString());
				} catch (Exception ignored) {}
			}
		}

		@Override
		public void writeToJson(JsonObject object) {
			object.addProperty(name, value.name());
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
