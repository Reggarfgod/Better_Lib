package com.reggarf.mods.better_lib.villager;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Immutable snapshot of everything a dev configured through
 * SimpleVillagerLib.profession(...)....register().
 */
public record ProfessionDefinition(
        String modId,
        String path,
        Supplier<Block> workstation,
        int maxVillagers,
        int searchRange,
        @Nullable SoundEvent workSound,
        Map<Integer, List<SimpleTrade>> tradesByLevel
) {
    public String id() {
        return path;
    }
}
