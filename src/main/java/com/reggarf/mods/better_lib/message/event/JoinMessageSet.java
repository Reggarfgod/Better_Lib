package com.reggarf.mods.better_lib.message.event;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.network.chat.TextColor.fromRgb;

public class JoinMessageSet {
    private final List<Component> messages = new ArrayList<>();

    public JoinMessageSet addText(String text, String colorHex) {
        messages.add(Component.literal(text).setStyle(Style.EMPTY.withColor(parseColor(colorHex))));
        return this;
    }

    public JoinMessageSet addLink(String label, String url, String colorHex, String description) {
        Component comp = Component.literal(" - ")
                .append(Component.literal(label)
                        .setStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                                .withColor(parseColor(colorHex))
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
}
