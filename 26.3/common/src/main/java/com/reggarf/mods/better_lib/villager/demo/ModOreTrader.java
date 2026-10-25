package com.reggarf.mods.better_lib.villager.demo;


import com.reggarf.mods.better_lib.Constants;
import com.reggarf.mods.better_lib.villager.SimpleTrade;
import com.reggarf.mods.better_lib.villager.SimpleVillagerLib;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * Unchanged from your original file — works as-is against SimpleVillagerLib.
 * See README.md for how the two register() calls map to runtime vs. datagen.
 */
public class ModOreTrader {

    public static final SimpleVillagerLib VILLAGERS =
            new SimpleVillagerLib(Constants.MODID);

    public static void register() {

        VILLAGERS.profession("ore_trader")
                .workstation(() -> Blocks.REDSTONE_LAMP)
                .maxVillagers(1)
                .searchRange(1)
                .workSound(SoundEvents.VILLAGER_WORK_TOOLSMITH)
                .enabledIf(() -> true)

                // ---------- Level 1 (Novice) ----------
                .trade(1, SimpleTrade.create()
                        .cost(Items.EMERALD, 5)
                        .result(Items.IRON_INGOT, 6)
                        .maxTrades(12).xp(2).priceMultiplier(0.05f))
                .trade(1, SimpleTrade.create()
                        .cost(Items.COAL, 16)
                        .result(Items.EMERALD, 1)
                        .maxTrades(16).xp(2).priceMultiplier(0.05f))
                .trade(1, SimpleTrade.create()
                        .cost(Items.EMERALD, 3)
                        .cost2(Items.COBBLESTONE, 32)
                        .result(Items.RAW_IRON, 6)
                        .maxTrades(12).xp(2).priceMultiplier(0.05f))
                .trade(1, SimpleTrade.create()
                        .cost(Items.RAW_IRON, 10)
                        .result(Items.EMERALD, 1)
                        .maxTrades(16).xp(2).priceMultiplier(0.05f))
                .trade(1, SimpleTrade.create()
                        .cost(Items.EMERALD, 2)
                        .result(Items.TORCH, 12)
                        .maxTrades(12).xp(2).priceMultiplier(0.05f))
                .trade(1, SimpleTrade.create()
                        .cost(Items.FLINT, 24)
                        .result(Items.EMERALD, 1)
                        .maxTrades(16).xp(2).priceMultiplier(0.05f))

                // ---------- Level 2 (Apprentice) ----------
                .trade(2, SimpleTrade.create()
                        .cost(Items.EMERALD, 6)
                        .result(Items.GOLD_INGOT, 4)
                        .maxTrades(10).xp(5).priceMultiplier(0.05f))
                .trade(2, SimpleTrade.create()
                        .cost(Items.RAW_IRON, 12)
                        .result(Items.EMERALD, 2)
                        .maxTrades(10).xp(5).priceMultiplier(0.05f))
                .trade(2, SimpleTrade.create()
                        .cost(Items.EMERALD, 4)
                        .cost2(Items.RAW_COPPER, 16)
                        .result(Items.COPPER_INGOT, 12)
                        .maxTrades(10).xp(5).priceMultiplier(0.05f))
                .trade(2, SimpleTrade.create()
                        .cost(Items.RAW_GOLD, 8)
                        .result(Items.EMERALD, 2)
                        .maxTrades(10).xp(5).priceMultiplier(0.05f))
                .trade(2, SimpleTrade.create()
                        .cost(Items.EMERALD, 5)
                        .result(Items.LANTERN, 2)
                        .maxTrades(8).xp(5).priceMultiplier(0.05f))
                .trade(2, SimpleTrade.create()
                        .cost(Items.REDSTONE, 24)
                        .result(Items.EMERALD, 2)
                        .maxTrades(10).xp(5).priceMultiplier(0.05f))

                // ---------- Level 3 (Journeyman) ----------
                .trade(3, SimpleTrade.create()
                        .cost(Items.EMERALD, 8)
                        .result(Items.DIAMOND, 1)
                        .maxTrades(8).xp(10).priceMultiplier(0.05f))
                .trade(3, SimpleTrade.create()
                        .cost(Items.LAPIS_LAZULI, 24)
                        .result(Items.EMERALD, 3)
                        .maxTrades(10).xp(10).priceMultiplier(0.05f))
                .trade(3, SimpleTrade.create()
                        .cost(Items.EMERALD, 6)
                        .cost2(Items.RAW_IRON, 24)
                        .result(Items.IRON_CHAIN, 4)
                        .maxTrades(8).xp(10).priceMultiplier(0.05f))
                .trade(3, SimpleTrade.create()
                        .cost(Items.RAW_COPPER, 20)
                        .result(Items.EMERALD, 3)
                        .maxTrades(10).xp(10).priceMultiplier(0.05f))
                .trade(3, SimpleTrade.create()
                        .cost(Items.EMERALD, 5)
                        .result(Items.IRON_PICKAXE, 1)
                        .maxTrades(6).xp(10).priceMultiplier(0.05f))
                .trade(3, SimpleTrade.create()
                        .cost(Items.QUARTZ, 20)
                        .result(Items.EMERALD, 3)
                        .maxTrades(10).xp(10).priceMultiplier(0.05f))

                // ---------- Level 4 (Expert) ----------
                .trade(4, SimpleTrade.create()
                        .cost(Items.EMERALD, 10)
                        .result(Items.DIAMOND, 2)
                        .maxTrades(6).xp(15).priceMultiplier(0.05f))
                .trade(4, SimpleTrade.create()
                        .cost(Items.DIAMOND, 3)
                        .result(Items.EMERALD, 8)
                        .maxTrades(6).xp(15).priceMultiplier(0.05f))
                .trade(4, SimpleTrade.create()
                        .cost(Items.EMERALD, 8)
                        .cost2(Items.GOLD_INGOT, 12)
                        .result(Items.GOLDEN_PICKAXE, 1)
                        .maxTrades(4).xp(15).priceMultiplier(0.05f))
                .trade(4, SimpleTrade.create()
                        .cost(Items.RAW_GOLD, 16)
                        .result(Items.EMERALD, 4)
                        .maxTrades(8).xp(15).priceMultiplier(0.05f))
                .trade(4, SimpleTrade.create()
                        .cost(Items.EMERALD, 7)
                        .result(Items.ANVIL, 1)
                        .maxTrades(4).xp(15).priceMultiplier(0.05f))
                .trade(4, SimpleTrade.create()
                        .cost(Items.AMETHYST_SHARD, 12)
                        .result(Items.EMERALD, 4)
                        .maxTrades(8).xp(15).priceMultiplier(0.05f))

                // ---------- Level 5 (Master) ----------
                .trade(5, SimpleTrade.create()
                        .cost(Items.EMERALD, 12)
                        .result(Items.NETHERITE_SCRAP, 1)
                        .maxTrades(4).xp(20).priceMultiplier(0.05f))
                .trade(5, SimpleTrade.create()
                        .cost(Items.DIAMOND, 5)
                        .result(Items.EMERALD, 12)
                        .maxTrades(4).xp(20).priceMultiplier(0.05f))
                .trade(5, SimpleTrade.create()
                        .cost(Items.EMERALD, 15)
                        .cost2(Items.IRON_INGOT, 20)
                        .result(Items.DIAMOND_PICKAXE, 1)
                        .maxTrades(3).xp(20).priceMultiplier(0.05f))
                .trade(5, SimpleTrade.create()
                        .cost(Items.RAW_IRON, 40)
                        .result(Items.EMERALD, 6)
                        .maxTrades(6).xp(20).priceMultiplier(0.05f))
                .trade(5, SimpleTrade.create()
                        .cost(Items.EMERALD, 10)
                        .result(Items.BELL, 1)
                        .maxTrades(3).xp(20).priceMultiplier(0.05f))
                .trade(5, SimpleTrade.create()
                        .cost(Items.ANCIENT_DEBRIS, 1)
                        .result(Items.EMERALD, 18)
                        .maxTrades(4).xp(20).priceMultiplier(0.05f))

                .register();

        VILLAGERS.register();
    }
}