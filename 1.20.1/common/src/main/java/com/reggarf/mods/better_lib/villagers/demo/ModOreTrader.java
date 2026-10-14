package com.reggarf.mods.better_lib.villagers.demo;

import com.reggarf.mods.better_lib.Better_lib;
import com.reggarf.mods.better_lib.villagers.SimpleTrade;
import com.reggarf.mods.better_lib.villagers.SimpleVillagerLib;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * EXAMPLE: three villager professions built entirely from vanilla Minecraft
 *
 *   ore_trader        - Redstone Lamp   - ores/ingots
 *   arcane_trader      - Enchanting Table - xp/enchanting related goods
 *
 * None of these workstation blocks collide with vanilla professions
 * (blast furnace/armorer, smoker/butcher, cartography table/cartographer,
 * brewing stand/cleric, composter/farmer, barrel/fisherman, fletching
 * table/fletcher, cauldron/leatherworker, lectern/librarian, stonecutter/
 * mason, loom/shepherd, smithing table/toolsmith, grindstone/weaponsmith)
 * or with each other.
 */
public class ModOreTrader {

    public static final SimpleVillagerLib VILLAGERS =
            new SimpleVillagerLib(Better_lib.MODID);

    public static void register() {

        VILLAGERS.profession("ore_trader")
                .workstation(() -> Blocks.REDSTONE_LAMP)   // pure vanilla workstation block
                .maxVillagers(1)                           // max concurrent villagers on this job site
                .searchRange(1)                             // how far villagers search for it
                .workSound(SoundEvents.VILLAGER_WORK_TOOLSMITH)
                .enabledIf(() -> true)                      // no config flag needed - always on

                // LEVEL 1 - Novice
                .trade(1, SimpleTrade.create()
                        .cost(Items.EMERALD, 5)
                        .result(Items.IRON_INGOT, 6)
                        .maxTrades(12).xp(2).priceMultiplier(0.05f))
                .trade(1, SimpleTrade.create()
                        .cost(Items.COAL, 16)
                        .result(Items.EMERALD, 1)
                        .maxTrades(16).xp(2).priceMultiplier(0.05f))
                // TWO-COST TRADE
                .trade(1, SimpleTrade.create()
                        .cost(Items.EMERALD, 3)
                        .cost2(Items.COBBLESTONE, 32)
                        .result(Items.RAW_IRON, 6)
                        .maxTrades(12).xp(2).priceMultiplier(0.05f))

                // LEVEL 2 - Apprentice
                .trade(2, SimpleTrade.create()
                        .cost(Items.EMERALD, 6)
                        .result(Items.GOLD_INGOT, 4)
                        .maxTrades(10).xp(5).priceMultiplier(0.05f))
                .trade(2, SimpleTrade.create()
                        .cost(Items.RAW_IRON, 12)
                        .result(Items.EMERALD, 2)
                        .maxTrades(10).xp(5).priceMultiplier(0.05f))
                // TWO-COST TRADE
                .trade(2, SimpleTrade.create()
                        .cost(Items.EMERALD, 4)
                        .cost2(Items.RAW_COPPER, 16)
                        .result(Items.COPPER_INGOT, 12)
                        .maxTrades(10).xp(5).priceMultiplier(0.05f))

                // LEVEL 3 - Journeyman
                .trade(3, SimpleTrade.create()
                        .cost(Items.EMERALD, 8)
                        .cost2(Items.IRON_INGOT, 4)
                        .result(Items.ANVIL, 1)
                        .maxTrades(3).xp(10).priceMultiplier(0.05f))
                .trade(3, SimpleTrade.create()
                        .cost(Items.RAW_GOLD, 12)
                        .result(Items.EMERALD, 3)
                        .maxTrades(8).xp(10).priceMultiplier(0.05f))
                // TWO-COST TRADE
                .trade(3, SimpleTrade.create()
                        .cost(Items.EMERALD, 6)
                        .cost2(Items.COAL, 20)
                        .result(Items.IRON_BLOCK, 2)
                        .maxTrades(6).xp(10).priceMultiplier(0.05f))

                // LEVEL 4 - Expert
                .trade(4, SimpleTrade.create()
                        .cost(Items.EMERALD, 10)
                        .cost2(Items.GOLD_INGOT, 6)
                        .result(Items.BELL, 1)
                        .maxTrades(3).xp(15).priceMultiplier(0.05f))
                .trade(4, SimpleTrade.create()
                        .cost(Items.DIAMOND, 1)
                        .result(Items.EMERALD, 6)
                        .maxTrades(6).xp(15).priceMultiplier(0.05f))
                // TWO-COST TRADE
                .trade(4, SimpleTrade.create()
                        .cost(Items.EMERALD, 9)
                        .cost2(Items.LAPIS_LAZULI, 16)
                        .result(Items.GOLD_BLOCK, 1)
                        .maxTrades(4).xp(15).priceMultiplier(0.05f))

                // LEVEL 5 - Master
                .trade(5, SimpleTrade.create()
                        .cost(Items.EMERALD, 20)
                        .cost2(Items.DIAMOND, 3)
                        .result(Blocks.BEACON, 1)
                        .maxTrades(1).xp(30).priceMultiplier(0.1f))
                .trade(5, SimpleTrade.create()
                        .cost(Items.EMERALD, 15)
                        .result(Items.NETHERITE_SCRAP, 2)
                        .maxTrades(2).xp(30).priceMultiplier(0.1f))
                // TWO-COST TRADE
                .trade(5, SimpleTrade.create()
                        .cost(Items.EMERALD, 18)
                        .cost2(Items.GOLD_INGOT, 12)
                        .result(Items.NETHERITE_INGOT, 1)
                        .maxTrades(1).xp(30).priceMultiplier(0.1f))

                .register();

        // ARCANE_TRADER
        VILLAGERS.profession("arcane_trader")
                .workstation(() -> Blocks.ENCHANTING_TABLE)
                .maxVillagers(1)
                .searchRange(1)
                .workSound(SoundEvents.VILLAGER_WORK_LIBRARIAN)
                .enabledIf(() -> true)

                // LEVEL 1
                .trade(1, SimpleTrade.create()
                        .cost(Items.EMERALD, 3)
                        .result(Items.EXPERIENCE_BOTTLE, 6)
                        .maxTrades(12).xp(2).priceMultiplier(0.05f))
                .trade(1, SimpleTrade.create()
                        .cost(Items.LAPIS_LAZULI, 10)
                        .result(Items.EMERALD, 1)
                        .maxTrades(16).xp(2).priceMultiplier(0.05f))
                // TWO-COST TRADE
                .trade(1, SimpleTrade.create()
                        .cost(Items.EMERALD, 4)
                        .cost2(Items.BOOK, 4)
                        .result(Items.BOOKSHELF, 2)
                        .maxTrades(10).xp(2).priceMultiplier(0.05f))

                // LEVEL 2
                .trade(2, SimpleTrade.create()
                        .cost(Items.EMERALD, 5)
                        .result(Items.ENDER_PEARL, 3)
                        .maxTrades(10).xp(5).priceMultiplier(0.05f))
                .trade(2, SimpleTrade.create()
                        .cost(Items.REDSTONE, 24)
                        .result(Items.EMERALD, 2)
                        .maxTrades(10).xp(5).priceMultiplier(0.05f))
                // TWO-COST TRADE
                .trade(2, SimpleTrade.create()
                        .cost(Items.EMERALD, 6)
                        .cost2(Items.GLOWSTONE_DUST, 8)
                        .result(Items.GLASS_BOTTLE, 8)
                        .maxTrades(8).xp(5).priceMultiplier(0.05f))

                // LEVEL 3 - two-cost trade example
                .trade(3, SimpleTrade.create()
                        .cost(Items.EMERALD, 7)
                        .cost2(Items.BOOK, 3)
                        .result(Items.ENCHANTED_BOOK, 1)
                        .maxTrades(4).xp(10).priceMultiplier(0.05f))
                .trade(3, SimpleTrade.create()
                        .cost(Items.EMERALD, 6)
                        .result(Items.AMETHYST_SHARD, 8)
                        .maxTrades(8).xp(10).priceMultiplier(0.05f))

                // LEVEL 4
                .trade(4, SimpleTrade.create()
                        .cost(Items.EMERALD, 10)
                        .cost2(Items.DIAMOND, 1)
                        .result(Items.ENCHANTED_BOOK, 1)
                        .maxTrades(3).xp(15).priceMultiplier(0.05f))
                .trade(4, SimpleTrade.create()
                        .cost(Items.ENDER_PEARL, 6)
                        .result(Items.EMERALD, 4)
                        .maxTrades(6).xp(15).priceMultiplier(0.05f))

                // LEVEL 5
                .trade(5, SimpleTrade.create()
                        .cost(Items.EMERALD, 20)
                        .cost2(Items.NETHER_STAR, 1)
                        .result(Items.BEACON, 1)
                        .maxTrades(1).xp(30).priceMultiplier(0.1f))
                .trade(5, SimpleTrade.create()
                        .cost(Items.EMERALD, 25)
                        .result(Items.TOTEM_OF_UNDYING, 1)
                        .maxTrades(1).xp(30).priceMultiplier(0.1f))

                .register();

        VILLAGERS.register();
    }
}
