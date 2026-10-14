package com.reggarf.mods.better_lib.villagers;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * An immutable descriptor capturing everything SimpleVillagerLib registered
 * for a profession:
 *
 *   - the registered VillagerProfession holder
 *   - the per-level trade listing suppliers
 *   - the runtime enabled condition (from .enabledIf(...))
 *
 * Handed to the loader platform via registerTradeSource so the loader can
 * hook trades up however it needs to (TradeOfferHelper on Fabric,
 * VillagerTradesEvent on Forge/NeoForge).
 */
public record ProfessionEntry(
        String name,
        Holder<VillagerProfession> profession,
        Map<Integer, List<Supplier<ItemListing>>> trades,
        BooleanSupplier enabled
) {}
