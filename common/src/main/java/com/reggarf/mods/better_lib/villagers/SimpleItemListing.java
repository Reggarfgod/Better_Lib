package com.reggarf.mods.better_lib.villagers;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * A VillagerTrades.ItemListing (pure vanilla, present on every loader) that
 * supports one or two cost items, a result, max uses, xp, and a
 * demand-based price multiplier.
 *
 * This replaces Forge/NeoForge's BasicItemListing, which does the same thing
 * but doesn't exist on Fabric - reimplementing it here in common code means
 * SimpleTrade behaves identically on all three loaders.
 */
final class SimpleItemListing implements VillagerTrades.ItemListing {

    private final ItemCost cost;
    private final ItemCost cost2; // may be null - single-cost trade
    private final ItemStack result;
    private final int maxTrades;
    private final int xp;
    private final float priceMultiplier;

    SimpleItemListing(ItemCost cost, ItemCost cost2, ItemStack result,
                      int maxTrades, int xp, float priceMultiplier) {
        this.cost = cost;
        this.cost2 = cost2;
        this.result = result;
        this.maxTrades = maxTrades;
        this.xp = xp;
        this.priceMultiplier = priceMultiplier;
    }

    @Override
    public @Nullable MerchantOffer getOffer(ServerLevel level, Entity trader, RandomSource random) {
        return new MerchantOffer(cost, Optional.ofNullable(cost2), result.copy(),
                0, maxTrades, xp, priceMultiplier);
    }
}