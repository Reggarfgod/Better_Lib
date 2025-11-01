package com.reggarf.mods.better_lib.message.event;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.network.chat.TextColor.fromRgb;

/**
 * Represents a collection of messages (text and clickable links)
 * for one mod, with automatic color parsing and clickable styles.
 */
public class OnlineMessageSet {
    private final List<Component> messages = new ArrayList<>();

    public OnlineMessageSet addText(String text, String colorHex) {
        messages.add(Component.literal(text).setStyle(Style.EMPTY.withColor(parseColor(colorHex))));
        return this;
    }

    public OnlineMessageSet addLink(String label, String url, String colorHex, String description) {
        ClickEvent clickEvent = createUrlClickEvent(url);
        Component comp = Component.literal(" - ")
                .append(Component.literal(label)
                        .setStyle(Style.EMPTY
                                .withClickEvent(clickEvent)
                                .withColor(parseColor(colorHex))
                                .withUnderlined(true)))
                .append(Component.literal(description != null && !description.isEmpty() ? " " + description : ""));
        messages.add(comp);
        return this;
    }

    public OnlineMessageSet addBlankLine() {
        messages.add(Component.literal(""));
        return this;
    }

    /** Sends all messages to a player */
    public void sendTo(ServerPlayer player) {
        for (Component msg : messages) {
            player.sendSystemMessage(msg);
        }
    }

    /** Unique hash of this message set (used for detecting changes). */
    public String hash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            StringBuilder sb = new StringBuilder();
            for (Component msg : messages) {
                sb.append(msg.getString());
            }
            byte[] hash = digest.digest(sb.toString().getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            return String.valueOf(messages.hashCode());
        }
    }

    private TextColor parseColor(String hex) {
        try {
            if (hex == null) return fromRgb(0xFFFFFF);
            if (hex.startsWith("#")) hex = hex.substring(1);
            int rgb = Integer.parseInt(hex, 16);
            return fromRgb(rgb);
        } catch (Exception e) {
            return fromRgb(0xFFFFFF);
        }
    }

    private ClickEvent createUrlClickEvent(String url) {
        try {
            // Try using the new 1.21.5+ method: ClickEvent.OpenUrl(URI)
            Class<?> openUrlClass = Class.forName("net.minecraft.network.chat.ClickEvent$OpenUrl");
            Constructor<?> ctor = openUrlClass.getConstructor(URI.class);
            return (ClickEvent) ctor.newInstance(URI.create(url));
        } catch (Throwable ignored) {
            // Fallback for older versions
            return new ClickEvent(ClickEvent.Action.OPEN_URL, url);
        }
    }
}
