package com.reggarf.mods.better_lib.message.event;

import com.reggarf.mods.better_lib.message.api.JoinMessagePlugin;
import com.reggarf.mods.better_lib.message.api.JoinMessagePlugins;

import com.reggarf.mods.better_lib.message.api.JoinMessageSet;
import net.minecraft.server.level.ServerPlayer;

public final class JoinMessageLogic {

    private JoinMessageLogic() {}

    public static void handle(ServerPlayer player) {

        for (JoinMessagePlugin plugin : JoinMessagePlugins.all()) {
            if (!plugin.enabled()) continue;

            // Namespaced, persistent player tag
            String tag = "better_lib:joinmsg_" + plugin.getModId();

            if (!player.getTags().contains(tag)) {

                for (JoinMessageSet set : plugin.getMessageSets()) {
                    set.sendTo(player);
                }

                player.addTag(tag);
            }
        }
    }
}
