package com.reggarf.mods.better_lib.configs.gui;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class ScreenOpener {

	private static final Deque<Screen> backStack = new ArrayDeque<>();
	private static Screen backSteppedFrom = null;

	public static void open(Screen screen) {
		open(Minecraft.getInstance().screen, screen);
	}

	public static void open(Screen current, Screen toOpen) {
		backSteppedFrom = null;
		if (current != null) {
			if (backStack.size() >= 15)
				backStack.pollLast();
			backStack.push(current);
		} else {
			backStack.clear();
		}

		openScreen(toOpen);
	}

	public static void clearStack() {
		backStack.clear();
	}

	public static List<Screen> getScreenHistory() {
		return new ArrayList<>(backStack);
	}

	public static Screen getPreviouslyRenderedScreen() {
		return backSteppedFrom != null ? backSteppedFrom : backStack.peek();
	}

	private static void openScreen(Screen screen) {
		Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(screen));
	}
}
