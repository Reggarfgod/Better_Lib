package com.reggarf.mods.better_lib.configs.adapter;

import com.reggarf.mods.better_lib.config.annotation.Config;
import com.reggarf.mods.better_lib.config.core.BetterConfigManager;
import com.reggarf.mods.better_lib.configs.ConfigBase;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DynamicConfigCategory extends ConfigBase {

	private final String name;
	private final Object targetInstance;
	private final List<FieldBinding<?>> bindings = new ArrayList<>();

	public DynamicConfigCategory(String name, Object targetInstance) {
		this.name = name;
		this.targetInstance = targetInstance;
	}

	@Override
	public String getName() {
		return name;
	}

	public void addBinding(FieldBinding<?> binding) {
		this.bindings.add(binding);
	}

	public void applyToTarget() {
		for (FieldBinding<?> binding : bindings) {
			binding.apply(targetInstance);
		}
	}

	@Override
	public void save() {
		super.save();
		applyToTarget();
		if (targetInstance != null) {
			Config cfg = targetInstance.getClass().getAnnotation(Config.class);
			String modid = cfg != null && !cfg.modid().isEmpty() ? cfg.modid() : "better_lib";
			String cfgName = cfg != null && !cfg.name().isEmpty() ? cfg.name() : targetInstance.getClass().getSimpleName();
			BetterConfigManager.save(modid, cfgName, targetInstance);
		}
	}

	public record FieldBinding<T>(Field field, CValue<T, ?> value) {
		public void apply(Object target) {
			try {
				field.setAccessible(true);
				field.set(target, value.get());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
