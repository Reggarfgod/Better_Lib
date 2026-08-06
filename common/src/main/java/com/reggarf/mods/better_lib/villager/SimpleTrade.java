package com.reggarf.mods.better_lib.villager;

import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.ItemLike; // <-- matches TradeCost's constructor

import java.util.List;
import java.util.Optional;

public final class SimpleTrade {

    private ItemLike costItem;
    private int costCount = 1;

    private ItemLike cost2Item; // optional
    private int cost2Count = 1;

    private ItemLike resultItem;
    private int resultCount = 1;

    private int maxUses = 12;
    private int villagerXp = 1;
    private float priceMultiplier = 0.05F;

    private SimpleTrade() {
    }

    public static SimpleTrade create() {
        return new SimpleTrade();
    }

    public SimpleTrade cost(ItemLike item, int count) {
        this.costItem = item;
        this.costCount = count;
        return this;
    }

    public SimpleTrade cost2(ItemLike item, int count) {
        this.cost2Item = item;
        this.cost2Count = count;
        return this;
    }

    public SimpleTrade result(ItemLike item, int count) {
        this.resultItem = item;
        this.resultCount = count;
        return this;
    }

    public SimpleTrade maxTrades(int maxUses) {
        this.maxUses = maxUses;
        return this;
    }

    public SimpleTrade xp(int xp) {
        this.villagerXp = xp;
        return this;
    }

    public SimpleTrade priceMultiplier(float multiplier) {
        this.priceMultiplier = multiplier;
        return this;
    }

    public VillagerTrade build() {
        if (costItem == null || resultItem == null) {
            throw new IllegalStateException("SimpleTrade requires at least cost(...) and result(...)");
        }

        TradeCost primaryCost = new TradeCost(costItem, costCount);
        ItemStackTemplate resultTemplate = new ItemStackTemplate(resultItem.asItem(), resultCount);

        if (cost2Item != null) {
            TradeCost secondaryCost = new TradeCost(cost2Item, cost2Count);
            return new VillagerTrade(
                    primaryCost,
                    Optional.of(secondaryCost),
                    resultTemplate,
                    maxUses,
                    villagerXp,
                    priceMultiplier,
                    Optional.empty(),
                    List.of()
            );
        }

        return new VillagerTrade(
                primaryCost,
                resultTemplate,
                maxUses,
                villagerXp,
                priceMultiplier,
                Optional.empty(),
                List.of()
        );
    }
}