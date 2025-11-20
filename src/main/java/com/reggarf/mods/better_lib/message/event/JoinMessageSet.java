package com.reggarf.mods.better_lib.message.event;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.network.chat.TextColor.fromRgb;

public class JoinMessageSet {
    private final List<Component> messages = new ArrayList<>();

    public JoinMessageSet addText(String text, int colorRgb) {
        messages.add(Component.literal(text).setStyle(Style.EMPTY.withColor(fromRgb(colorRgb))));
        return this;
    }

    public JoinMessageSet addLink(String label, String url, int colorRgb, String description) {
        ClickEvent clickEvent = createUrlClickEvent(url);
        Component comp = Component.literal(" - ")
                .append(Component.literal(label)
                        .setStyle(Style.EMPTY
                                .withClickEvent(clickEvent)
                                .withColor(fromRgb(colorRgb))
                                .withUnderlined(true)))
                .append(Component.literal(description != null ? " " + description : ""));
        messages.add(comp);
        return this;
    }

    public JoinMessageSet addBlankLine() {
        messages.add(Component.literal(""));
        return this;
    }

    protected void sendTo(ServerPlayer player) {
        for (Component msg : messages) {
            player.sendSystemMessage(msg);
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
