package com.reggarf.mods.better_lib.config;

public class ConfigAnnotations {

	public enum IntDisplay implements ConfigAnnotation {
		HEX("#"),
		ZERO_X("0x"),
		ZERO_B("0b");

		private final String value;

		IntDisplay(String value) {
			this.value = value;
		}

		@Override
		public String getName() {
			return "IntDisplay";
		}

		@Override
		public String getValue() {
			return value;
		}
	}

	public enum RequiresRestart implements ConfigAnnotation {
		CLIENT("client"),
		SERVER("server"),
		BOTH("both");

		private final String value;

		RequiresRestart(String value) {
			this.value = value;
		}

		@Override
		public String getName() {
			return "RequiresReload";
		}

		@Override
		public String getValue() {
			return value;
		}
	}

	public interface ConfigAnnotation {
		String getName();

		default String getValue() {
			return null;
		}

		default String asComment() {
			String comment = "[@cui:" + getName();
			String val = getValue();
			if (val != null) {
				comment += ":" + val;
			}
			return comment + "]";
		}
	}
}
