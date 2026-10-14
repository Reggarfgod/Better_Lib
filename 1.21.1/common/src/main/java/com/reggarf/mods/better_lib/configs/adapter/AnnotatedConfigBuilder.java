package com.reggarf.mods.better_lib.configs.adapter;

import com.reggarf.mods.better_lib.config.annotation.ConfigEntry.BoundedDiscrete;
import com.reggarf.mods.better_lib.config.annotation.ConfigEntry.Category;
import com.reggarf.mods.better_lib.config.annotation.ConfigEntry.ColorField;
import com.reggarf.mods.better_lib.config.annotation.ConfigEntry.Description;
import com.reggarf.mods.better_lib.config.annotation.ConfigEntry.Dropdown;
import com.reggarf.mods.better_lib.configs.ConfigBase.ConfigBool;
import com.reggarf.mods.better_lib.configs.ConfigBase.ConfigEnum;
import com.reggarf.mods.better_lib.configs.ConfigBase.ConfigFloat;
import com.reggarf.mods.better_lib.configs.ConfigBase.ConfigInt;
import com.reggarf.mods.better_lib.configs.ConfigBase.ConfigString;
import com.reggarf.mods.better_lib.configs.ui.BaseConfigScreen;
import com.reggarf.mods.better_lib.configs.ui.ConfigScreen.ConfigType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnnotatedConfigBuilder {

	public static class ConfigSections {
		public final SectionConfig client = new SectionConfig("client");
		public final SectionConfig common = new SectionConfig("common");
		public final SectionConfig server = new SectionConfig("server");
	}

	public static ConfigSections buildSections(Class<?> configClass, Object configInstance) {
		ConfigSections sections = new ConfigSections();
		Map<String, DynamicConfigCategory> categoryMap = new LinkedHashMap<>();

		for (Field field : configClass.getDeclaredFields()) {
			field.setAccessible(true);
			try {
				Object value = field.get(configInstance);
				String fieldName = field.getName();

				Category catAnnotation = field.getAnnotation(Category.class);
				String categoryName = catAnnotation != null && !catAnnotation.value().isEmpty()
						? catAnnotation.value()
						: "General Settings";

				DynamicConfigCategory category = categoryMap.computeIfAbsent(categoryName, k -> new DynamicConfigCategory(k, configInstance));

				Description descAnnotation = field.getAnnotation(Description.class);
				String description = descAnnotation != null ? descAnnotation.value() : "";

				List<String> commentList = new ArrayList<>();
				if (!description.isEmpty()) {
					commentList.add(description);
				}

				if (field.isAnnotationPresent(Dropdown.class)) {
					Dropdown dropdown = field.getAnnotation(Dropdown.class);
					commentList.add("[@cui:Dropdown:" + String.join(",", dropdown.values()) + "]");
				}

				if (field.isAnnotationPresent(ColorField.class)) {
					commentList.add("[@cui:ColorField:true]");
					commentList.add("[@cui:IntDisplay:#]");
				}

				String[] comments = commentList.toArray(new String[0]);

				if (value instanceof Boolean bVal) {
					ConfigBool boolVal = category.b(bVal, fieldName, comments);
					category.addBinding(new DynamicConfigCategory.FieldBinding<>(field, boolVal));
				} else if (value instanceof Integer iVal) {
					int min = Integer.MIN_VALUE;
					int max = Integer.MAX_VALUE;
					if (field.isAnnotationPresent(BoundedDiscrete.class)) {
						BoundedDiscrete bounded = field.getAnnotation(BoundedDiscrete.class);
						min = bounded.min();
						max = bounded.max();
					}
					ConfigInt intVal = category.i(iVal, min, max, fieldName, comments);
					category.addBinding(new DynamicConfigCategory.FieldBinding<>(field, intVal));
				} else if (value instanceof Float fVal) {
					ConfigFloat floatVal = category.f(fVal, -Float.MAX_VALUE, Float.MAX_VALUE, fieldName, comments);
					category.addBinding(new DynamicConfigCategory.FieldBinding<>(field, floatVal));
				} else if (value instanceof Double dVal) {
					ConfigFloat floatVal = category.f(dVal.floatValue(), -Float.MAX_VALUE, Float.MAX_VALUE, fieldName, comments);
					category.addBinding(new DynamicConfigCategory.FieldBinding<>(field, floatVal));
				} else if (value instanceof String sVal) {
					ConfigString stringVal = category.s(sVal, fieldName, comments);
					category.addBinding(new DynamicConfigCategory.FieldBinding<>(field, stringVal));
				} else if (value != null && value.getClass().isEnum()) {
					@SuppressWarnings("unchecked")
					Enum enumVal = (Enum) value;
					ConfigEnum enumConfig = category.e(enumVal, fieldName, comments);
					category.addBinding(new DynamicConfigCategory.FieldBinding<>(field, enumConfig));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Sort each category into client, common, or server
		for (Map.Entry<String, DynamicConfigCategory> entry : categoryMap.entrySet()) {
			String name = entry.getKey();
			DynamicConfigCategory cat = entry.getValue();
			ConfigType type = BaseConfigScreen.classifyCategory(name);
			switch (type) {
				case CLIENT -> sections.client.addCategory(cat);
				case SERVER -> sections.server.addCategory(cat);
				case COMMON -> sections.common.addCategory(cat);
			}
		}

		return sections;
	}

	public static Map<String, DynamicConfigCategory> buildCategories(Class<?> configClass, Object configInstance) {
		ConfigSections sections = buildSections(configClass, configInstance);
		Map<String, DynamicConfigCategory> map = new LinkedHashMap<>();
		if (sections.client.getChildren() != null) {
			for (var c : sections.client.getChildren()) {
				if (c instanceof DynamicConfigCategory dc) map.put(dc.getName(), dc);
			}
		}
		if (sections.common.getChildren() != null) {
			for (var c : sections.common.getChildren()) {
				if (c instanceof DynamicConfigCategory dc) map.put(dc.getName(), dc);
			}
		}
		if (sections.server.getChildren() != null) {
			for (var c : sections.server.getChildren()) {
				if (c instanceof DynamicConfigCategory dc) map.put(dc.getName(), dc);
			}
		}
		return map;
	}
}
