package com.reggarf.mods.better_lib.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public abstract class ConfigBase {

	@Nullable
	public ModConfigSpec specification;

	protected int depth;
	protected List<CValue<?, ?>> allValues = new ArrayList<>();
	protected List<ConfigBase> children = new ArrayList<>();

	/**
	 * Convenience helper to instantiate and build a ConfigBase spec in 1 step.
	 *
	 * @param factory supplier producing the config instance
	 * @param <T>     config type
	 * @return initialized config instance with configured specification
	 */
	public static <T extends ConfigBase> T build(Supplier<T> factory) {
		Pair<T, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(builder -> {
			T config = factory.get();
			config.registerAll(builder);
			if (config.depth > 0) {
				builder.pop(config.depth);
				config.depth = 0;
			}
			return config;
		});
		T config = specPair.getLeft();
		config.specification = specPair.getRight();
		return config;
	}

	public List<CValue<?, ?>> getAllValues() {
		return Collections.unmodifiableList(allValues);
	}

	public boolean isLoaded() {
		try {
			return specification == null || specification.isLoaded();
		} catch (Throwable ignored) {
			return false;
		}
	}

	public void registerAll(final Builder builder) {
		for (CValue<?, ?> cValue : allValues) {
			cValue.register(builder);
		}
	}

	public void onLoad() {
		if (!children.isEmpty()) {
			children.forEach(ConfigBase::onLoad);
		}
	}

	public void onReload() {
		if (!children.isEmpty()) {
			children.forEach(ConfigBase::onReload);
		}
	}

	public abstract String getName();

	@FunctionalInterface
	protected interface IValueProvider<V, T extends ConfigValue<V>> extends Function<Builder, T> {
	}

	protected ConfigBool b(boolean current, String name, String... comment) {
		return new ConfigBool(name, current, comment);
	}

	protected ConfigFloat f(float current, float min, float max, String name, String... comment) {
		return new ConfigFloat(name, current, min, max, comment);
	}

	protected ConfigFloat f(float current, float min, String name, String... comment) {
		return f(current, min, Float.MAX_VALUE, name, comment);
	}

	protected ConfigInt i(int current, int min, int max, String name, String... comment) {
		return new ConfigInt(name, current, min, max, comment);
	}

	protected ConfigInt i(int current, int min, String name, String... comment) {
		return i(current, min, Integer.MAX_VALUE, name, comment);
	}

	protected ConfigInt i(int current, String name, String... comment) {
		return i(current, Integer.MIN_VALUE, Integer.MAX_VALUE, name, comment);
	}

	protected ConfigString s(String current, String name, String... comment) {
		return new ConfigString(name, current, comment);
	}

	protected <T extends Enum<T>> ConfigEnum<T> e(T defaultValue, String name, String... comment) {
		return new ConfigEnum<>(name, defaultValue, comment);
	}

	protected ConfigGroup group(int depth, String name, String... comment) {
		return new ConfigGroup(name, depth, comment);
	}

	protected <T extends ConfigBase> T nested(int depth, Supplier<T> constructor, String... comment) {
		T config = constructor.get();
		new ConfigGroup(config.getName(), depth, comment);
		new CValue<Boolean, BooleanValue>(config.getName(), false, builder -> {
			config.depth = depth;
			config.registerAll(builder);
			if (config.depth > depth) {
				builder.pop(config.depth - depth);
			}
			return null;
		});
		children.add(config);
		return config;
	}

	public class CValue<V, T extends ConfigValue<V>> {
		@Nullable
		protected ConfigValue<V> value;
		protected String name;
		protected V defaultValue;
		private final IValueProvider<V, T> provider;
		protected String[] comments;

		public CValue(String name, V defaultValue, IValueProvider<V, T> provider, String... comment) {
			this.name = name;
			this.defaultValue = defaultValue;
			this.comments = comment;
			this.provider = builder -> {
				addComments(builder, comment);
				return provider.apply(builder);
			};
			allValues.add(this);
		}

		public void addComments(Builder builder, String... comment) {
			if (comment.length > 0) {
				String[] comments = new String[comment.length + 1];
				comments[0] = ".";
				System.arraycopy(comment, 0, comments, 1, comment.length);
				builder.comment(comments);
			} else {
				builder.comment(".");
			}
		}

		public void register(Builder builder) {
			value = provider.apply(builder);
		}

		public V get() {
			if (value == null) {
				return defaultValue;
			}
			try {
				if (specification != null && !specification.isLoaded()) {
					return defaultValue;
				}
				return value.get();
			} catch (Throwable e) {
				return defaultValue;
			}
		}

		public void set(V value) {
			if (this.value == null) {
				this.defaultValue = value;
				return;
			}
			try {
				if (specification != null && !specification.isLoaded()) {
					this.defaultValue = value;
					return;
				}
				this.value.set(value);
				this.value.save();
			} catch (Throwable ignored) {
				this.defaultValue = value;
			}
		}

		@SuppressWarnings("unchecked")
		public void setRawValue(Object val) {
			set((V) val);
		}

		public String getName() {
			return name;
		}

		public V getDefaultValue() {
			return defaultValue;
		}

		public String[] getComments() {
			return comments != null ? comments : new String[0];
		}
	}

	public class ConfigGroup extends CValue<Boolean, BooleanValue> {
		private final int groupDepth;
		private final String[] comment;

		public ConfigGroup(String name, int depth, String... comment) {
			super(name, null, builder -> null, comment);
			this.groupDepth = depth;
			this.comment = comment;
		}

		@Override
		public void register(Builder builder) {
			if (depth > groupDepth) {
				builder.pop(depth - groupDepth);
			}
			depth = groupDepth;
			addComments(builder, comment);
			builder.push(getName());
			depth++;
		}
	}

	public class ConfigBool extends CValue<Boolean, BooleanValue> {
		public ConfigBool(String name, boolean def, String... comment) {
			super(name, def, builder -> builder.define(name, def), comment);
		}
	}

	public class ConfigString extends CValue<String, ConfigValue<String>> {
		public ConfigString(String name, String def, String... comment) {
			super(name, def, builder -> builder.define(name, def), comment);
		}
	}

	public class ConfigInt extends CValue<Integer, IntValue> {
		protected int min;
		protected int max;

		public ConfigInt(String name, int def, int min, int max, String... comment) {
			super(name, def, builder -> builder.defineInRange(name, def, min, max), comment);
			this.min = min;
			this.max = max;
		}

		public int getMin() {
			return min;
		}

		public int getMax() {
			return max;
		}
	}

	public class ConfigFloat extends CValue<Double, DoubleValue> {
		private final float min;
		private final float max;

		public ConfigFloat(String name, float def, float min, float max, String... comment) {
			super(name, (double) def, builder -> builder.defineInRange(name, def, min, max), comment);
			this.min = min;
			this.max = max;
		}

		public float getF() {
			return get().floatValue();
		}

		public float getMin() {
			return min;
		}

		public float getMax() {
			return max;
		}
	}

	public class ConfigEnum<T extends Enum<T>> extends CValue<T, EnumValue<T>> {
		private final T defaultValue;

		public ConfigEnum(String name, T defaultValue, String... comment) {
			super(name, defaultValue, builder -> builder.defineEnum(name, defaultValue), comment);
			this.defaultValue = defaultValue;
		}

		public T[] getEnumValues() {
			Class<T> enumClass = defaultValue.getDeclaringClass();
			return enumClass.getEnumConstants();
		}
	}
}
