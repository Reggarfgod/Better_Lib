package com.reggarf.mods.better_lib.message.api;

import com.reggarf.mods.better_lib.message.event.JoinMessageSet;
import java.util.List;

/**
 * Interface to be implemented by any mod that wants to add join messages.
 */
public interface JoinMessagePlugin {
    /**
     * @return The mod ID this plugin belongs to.
     */
    String getModId();

    /**
     * @return Whether to show the join messages for this plugin.
     */
    default boolean enabled() {
        return true;
    }

    /**
     * @return All message sets to display for this mod.
     */
    List<JoinMessageSet> getMessageSets();
}
