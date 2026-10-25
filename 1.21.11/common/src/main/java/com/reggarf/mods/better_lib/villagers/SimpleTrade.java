package com.reggarf.mods.better_lib.villagers;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.level.ItemLike;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Fluent builder for a single villager trade. Identical API to the original
 * NeoForge-only version - only the internals changed (now builds a plain
 * ItemListing via SimpleItemListing instead of Forge's
 * BasicItemListing), so every mod using SimpleTrade needs zero changes.
 *
 * Cost and result construction is deferred until getOffer() time to prevent
 * "Components not bound yet" NullPointerExceptions during early mod initialization.
 */
public final class SimpleTrade {

    private Supplier<ItemCost> costSupplier;
    private Supplier<ItemCost> cost2Supplier; // optional
    private Supplier<ItemStack> resultSupplier;
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
        this.costSupplier = () -> new ItemCost(item, count);
        return this;
    }

    public SimpleTrade cost(Supplier<? extends ItemLike> item, int count) {
        this.costSupplier = () -> new ItemCost(item.get(), count);
        return this;
    }

    /** Optional second cost item - omit entirely for a single-item trade. */
    public SimpleTrade cost2(ItemLike item, int count) {
        this.cost2Supplier = () -> new ItemCost(item, count);
        return this;
    }

    public SimpleTrade cost2(Supplier<? extends ItemLike> item, int count) {
        this.cost2Supplier = () -> new ItemCost(item.get(), count);
        return this;
    }

    /** What the player receives. Required. */
    public SimpleTrade result(ItemLike item, int count) {
        this.resultSupplier = () -> new ItemStack(item, count);
        return this;
    }

    public SimpleTrade result(Supplier<? extends ItemLike> item, int count) {
        this.resultSupplier = () -> new ItemStack(item.get(), count);
        return this;
    }

    public SimpleTrade result(ItemStack stack) {
        this.resultSupplier = stack::copy;
        return this;
    }

    public SimpleTrade result(Supplier<ItemStack> stackSupplier) {
        this.resultSupplier = stackSupplier;
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

    ItemListing build() {
        Objects.requireNonNull(costSupplier, "SimpleTrade: cost(...) must be set");
        Objects.requireNonNull(resultSupplier, "SimpleTrade: result(...) must be set");
        return new SimpleItemListing(costSupplier, cost2Supplier, resultSupplier, maxTrades, xp, priceMultiplier);
    }
}
