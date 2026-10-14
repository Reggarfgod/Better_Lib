package com.reggarf.mods.better_lib.configs.utility;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class TooltipHelper {

	public static final int MAX_WIDTH_PER_LINE = 200;

	public static Style styleFromColor(ChatFormatting color) {
		return Style.EMPTY.applyFormat(color);
	}

	public static List<Component> cutStringTextComponent(String s, Palette palette) {
		return cutTextComponent(Component.literal(s), palette);
	}

	public static List<Component> cutTextComponent(Component c, Palette palette) {
		return cutTextComponent(c, palette.primary(), palette.highlight(), 0);
	}

	public static List<Component> cutTextComponent(Component c, Style primaryStyle, Style highlightStyle, int indent) {
		String s = c.getString();

		List<String> words = new LinkedList<>();
		BreakIterator iterator = BreakIterator.getLineInstance(java.util.Locale.getDefault());
		iterator.setText(s);
		int start = iterator.first();
		for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
			String word = s.substring(start, end);
			words.add(word);
		}

		Font font = Minecraft.getInstance().font;
		List<String> lines = new LinkedList<>();
		StringBuilder currentLine = new StringBuilder();
		int width = 0;
		for (String word : words) {
			int newWidth = font.width(word.replaceAll("_", ""));
			if (width + newWidth > MAX_WIDTH_PER_LINE) {
				if (width > 0) {
					lines.add(currentLine.toString());
					currentLine = new StringBuilder();
					width = 0;
				} else {
					lines.add(word);
					continue;
				}
			}
			currentLine.append(word);
			width += newWidth;
		}
		if (width > 0) {
			lines.add(currentLine.toString());
		}

		StringBuilder lineStart = new StringBuilder();
		for (int i = 0; i < indent; i++) lineStart.append(" ");
		MutableComponent startComp = Component.literal(lineStart.toString()).withStyle(primaryStyle);
		List<Component> formattedLines = new ArrayList<>(lines.size());
		Couple<Style> styles = Couple.create(highlightStyle, primaryStyle);

		boolean currentlyHighlighted = false;
		for (String string : lines) {
			MutableComponent currentComponent = startComp.plainCopy();
			String[] split = string.split("_");
			for (String part : split) {
				currentComponent.append(Component.literal(part).withStyle(styles.get(currentlyHighlighted)));
				currentlyHighlighted = !currentlyHighlighted;
			}
			formattedLines.add(currentComponent);
			currentlyHighlighted = !currentlyHighlighted;
		}

		return formattedLines;
	}

	public record Palette(Style primary, Style highlight) {
		public static final Palette ALL_GRAY = ofColors(ChatFormatting.GRAY, ChatFormatting.GRAY);
		public static final Palette GRAY_AND_GOLD = ofColors(ChatFormatting.GRAY, ChatFormatting.GOLD);
		public static final Palette GRAY_AND_RED = ofColors(ChatFormatting.GRAY, ChatFormatting.RED);
		public static final Palette GRAY_AND_WHITE = ofColors(ChatFormatting.GRAY, ChatFormatting.WHITE);

		public static Palette ofColors(ChatFormatting primary, ChatFormatting highlight) {
			return new Palette(styleFromColor(primary), styleFromColor(highlight));
		}
	}
}
