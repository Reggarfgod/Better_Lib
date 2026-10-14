package com.reggarf.mods.better_lib.villagers;

import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.level.ItemLike;

import java.util.Objects;

/**
 * Fluent builder for a single villager trade. Identical API to the original
 * NeoForge-only version - only the internals changed (now builds a plain
 * VillagerTrades.ItemListing via SimpleItemListing instead of Forge's
 * BasicItemListing), so every mod using SimpleTrade needs zero changes.
 *
 * Example:
 *   SimpleTrade.create()
 *       .cost(Items.EMERALD, 9)
 *       .cost2(AllBlocks.COGWHEEL, 6)     // optional - omit for a single-cost trade
 *       .result(AllBlocks.GEARBOX, 2)
 *       .maxTrades(5)
 *       .xp(3)
 *       .priceMultiplier(0.07f)
 */
public final class SimpleTrade {

    private ItemCost cost;
    private ItemCost cost2; // optional
    private ItemStack result;
    private int maxTrades = 5;
    private int xp = 5;
    private float priceMultiplier = 0.05f;

    private SimpleTrade() {
    }

    public static SimpleTrade create() {
        return new SimpleTrade();
    }

    /** The item(s) the villager wants from the player. Required. */
    public SimpleTrade cost(ItemLike item, int count) {
        this.cost = new ItemCost(item, count);
        return this;
    }

    /** Optional second cost item - omit entirely for a single-item trade. */
    public SimpleTrade cost2(ItemLike item, int count) {
        this.cost2 = new ItemCost(item, count);
        return this;
    }

    /** What the player receives. Required. */
    public SimpleTrade result(ItemLike item, int count) {
        this.result = new ItemStack(item, count);
        return this;
    }

    /** How many times this trade can be used before the villager restocks. Default 5. */
    public SimpleTrade maxTrades(int maxTrades) {
        this.maxTrades = maxTrades;
        return this;
    }

    /** Villager experience awarded per use. Default 5. */
    public SimpleTrade xp(int xp) {
        this.xp = xp;
        return this;
    }

    /** Demand-based price growth rate. Default 0.05f. */
    public SimpleTrade priceMultiplier(float priceMultiplier) {
        this.priceMultiplier = priceMultiplier;
        return this;
    }

    VillagerTrades.ItemListing build() {
        Objects.requireNonNull(cost, "SimpleTrade: cost(...) must be set");
        Objects.requireNonNull(result, "SimpleTrade: result(...) must be set");
        return new SimpleItemListing(cost, cost2, result, maxTrades, xp, priceMultiplier);
    }
}
