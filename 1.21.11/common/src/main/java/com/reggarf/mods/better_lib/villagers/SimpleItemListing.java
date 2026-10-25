package com.reggarf.mods.better_lib.villagers;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * An ItemListing (pure vanilla style) that supports one or two cost items,
 * a result, max uses, xp, and a demand-based price multiplier.
 * Defers ItemCost and ItemStack creation until getOffer() is called, so
 * mod initialization is never subject to "Components not bound yet".
 */
final class SimpleItemListing implements ItemListing {

    private final Supplier<ItemCost> costSupplier;
    private final Supplier<ItemCost> cost2Supplier; // may be null - single-cost trade
    private final Supplier<ItemStack> resultSupplier;
    private final int maxTrades;
    private final int xp;
    private final float priceMultiplier;

    private ItemCost cachedCost;
    private ItemCost cachedCost2;
    private ItemStack cachedResult;

    SimpleItemListing(Supplier<ItemCost> costSupplier, Supplier<ItemCost> cost2Supplier, Supplier<ItemStack> resultSupplier,
                      int maxTrades, int xp, float priceMultiplier) {
        this.costSupplier = costSupplier;
        this.cost2Supplier = cost2Supplier;
        this.resultSupplier = resultSupplier;
        this.maxTrades = maxTrades;
        this.xp = xp;
        this.priceMultiplier = priceMultiplier;
    }

    @Override
    public MerchantOffer getOffer(net.minecraft.server.level.ServerLevel serverLevel, Entity trader, RandomSource random) {
        if (cachedCost == null && costSupplier != null) {
            cachedCost = costSupplier.get();
        }
        if (cachedCost2 == null && cost2Supplier != null) {
            cachedCost2 = cost2Supplier.get();
        }
        if (cachedResult == null && resultSupplier != null) {
            cachedResult = resultSupplier.get();
        }
        return new MerchantOffer(cachedCost, Optional.ofNullable(cachedCost2), cachedResult != null ? cachedResult.copy() : ItemStack.EMPTY,
                maxTrades, xp, priceMultiplier);
    }
}
