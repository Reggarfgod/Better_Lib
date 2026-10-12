package com.reggarf.mods.better_lib.configs.adapter;

import com.reggarf.mods.better_lib.configs.ConfigBase;
import java.util.ArrayList;

public class SectionConfig extends ConfigBase {

	private final String name;

	public SectionConfig(String name) {
		this.name = name;
		this.children = new ArrayList<>();
		this.allValues = new ArrayList<>();
	}

	@Override
	public String getName() {
		return name;
	}

	public void addCategory(ConfigBase category) {
		if (this.children == null) {
			this.children = new ArrayList<>();
		}
		if (!this.children.contains(category)) {
			this.children.add(category);
		}
	}
}
