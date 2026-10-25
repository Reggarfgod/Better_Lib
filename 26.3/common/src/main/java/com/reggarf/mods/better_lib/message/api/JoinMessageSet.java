package com.reggarf.mods.better_lib.message.api;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.minecraft.network.chat.TextColor.fromRgb;

public class JoinMessageSet {

    public record TextEntry(String text, int colorRgb) {}
    public record LinkEntry(String label, String url, int colorRgb, String description) {}

    private final List<Component> messages = new ArrayList<>();
    private final List<TextEntry> textEntries = new ArrayList<>();
    private final List<LinkEntry> linkEntries = new ArrayList<>();

    public JoinMessageSet addText(String text, int colorRgb) {
        messages.add(Component.literal(text)
                .setStyle(Style.EMPTY.withColor(fromRgb(colorRgb))));
        textEntries.add(new TextEntry(text, colorRgb));
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
        linkEntries.add(new LinkEntry(label, url, colorRgb, description));
        return this;
    }

    public JoinMessageSet addBlankLine() {
        messages.add(Component.literal(""));
        textEntries.add(new TextEntry("", 0xFFFFFF));
        return this;
    }

    public void sendTo(ServerPlayer player) {
        for (Component msg : messages) {
            player.sendSystemMessage(msg);
        }
    }

    public List<Component> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public List<TextEntry> getTextEntries() {
        return Collections.unmodifiableList(textEntries);
    }

    public List<LinkEntry> getLinkEntries() {
        return Collections.unmodifiableList(linkEntries);
    }

    private ClickEvent createUrlClickEvent(String url) {
        return new ClickEvent.OpenUrl(URI.create(url));
    }
}
