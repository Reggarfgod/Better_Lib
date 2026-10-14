package com.reggarf.mods.better_lib.villagers;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.Optional;

/**
 * An ItemListing (pure vanilla style) that supports one or two cost items,
 * a result, max uses, xp, and a demand-based price multiplier.
 */
final class SimpleItemListing implements ItemListing {

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
    public MerchantOffer getOffer(net.minecraft.server.level.ServerLevel serverLevel, Entity trader, RandomSource random) {
        return new MerchantOffer(cost, Optional.ofNullable(cost2), result.copy(),
                maxTrades, xp, priceMultiplier);
    }
}
