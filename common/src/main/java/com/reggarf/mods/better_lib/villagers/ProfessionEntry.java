package com.reggarf.mods.better_lib.villagers;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerTrades;


import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Internal record linking a registered profession to its trades and enabled
 * state. Not intended for direct use by mod authors - go through
 * VillagerProfessionBuilder instead. Handed to whichever platform impl is
 * active so it can apply the trades however that loader expects.
 */
public record ProfessionEntry(
        String name,
        Holder<VillagerProfession> profession,
        Map<Integer, List<Supplier<VillagerTrades.ItemListing>>> trades,
        BooleanSupplier enabled
) {
}
