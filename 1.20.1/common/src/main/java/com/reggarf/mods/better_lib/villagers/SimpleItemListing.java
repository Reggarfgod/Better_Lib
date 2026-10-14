package com.reggarf.mods.better_lib.villagers;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

final class SimpleItemListing implements VillagerTrades.ItemListing {

    private final ItemStack cost;
    private final ItemStack cost2; // may be null - single-cost trade
    private final ItemStack result;
    private final int maxTrades;
    private final int xp;
    private final float priceMultiplier;

    SimpleItemListing(ItemStack cost, ItemStack cost2, ItemStack result,
                       int maxTrades, int xp, float priceMultiplier) {
        this.cost = cost;
        this.cost2 = cost2;
        this.result = result;
        this.maxTrades = maxTrades;
        this.xp = xp;
        this.priceMultiplier = priceMultiplier;
    }

    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        if (cost2 == null || cost2.isEmpty()) {
            return new MerchantOffer(cost.copy(), result.copy(), maxTrades, xp, priceMultiplier);
        }
        return new MerchantOffer(cost.copy(), cost2.copy(), result.copy(), maxTrades, xp, priceMultiplier);
    }
}
