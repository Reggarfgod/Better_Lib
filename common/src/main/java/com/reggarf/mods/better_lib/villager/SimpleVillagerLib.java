package com.reggarf.mods.better_lib.villager;

import com.reggarf.mods.better_lib.Constants;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

/**
 * Per-mod entry point. Usage (see ModOreTrader.java for the full example):
 *
 * <pre>{@code
 * public static final SimpleVillagerLib VILLAGERS = new SimpleVillagerLib("mymod");
 *
 * VILLAGERS.profession("ore_trader")
 *     .workstation(() -> Blocks.REDSTONE_LAMP)
 *     .maxVillagers(1)
 *     .searchRange(1)
 *     .workSound(SoundEvents.VILLAGER_WORK_TOOLSMITH)
 *     .enabledIf(() -> true)
 *     .trade(1, SimpleTrade.create().cost(Items.EMERALD, 5).result(Items.IRON_INGOT, 6)
 *             .maxTrades(12).xp(2).priceMultiplier(0.05f))
 *     .register();
 *
 * VILLAGERS.register(); // finalizes: registers PoiType + VillagerProfession now,
 *                        // and hands the trade data to datagen for later.
 * }</pre>
 */
public final class SimpleVillagerLib {

    private final String modId;
    private final List<ProfessionDefinition> professions = new ArrayList<>();

    public SimpleVillagerLib(String modId) {
        this.modId = modId;
    }

    public ProfessionBuilder profession(String path) {
        return new ProfessionBuilder(path);
    }

    /**
     * Call once, after every .profession(...).register() call.
     * Makes this lib's profession and trade data visible to datagen and to the
     * platform loader (NeoForge RegisterEvent registers POI + profession).
     */
    public void register() {
        Constants.LOG.info("[VillagerLib] register() called for modId={}, professions={}", modId, professions.size());
        VillagerLibRegistry.registerLib(this);
    }

    List<ProfessionDefinition> getProfessions() {
        return professions;
    }

    public final class ProfessionBuilder {
        private final String path;
        private Supplier<Block> workstation;
        private int maxVillagers = 1;
        private int searchRange = 1;
        private SoundEvent workSound;
        private Supplier<Boolean> enabledIf = () -> true;
        private final Map<Integer, List<SimpleTrade>> tradesByLevel = new TreeMap<>();

        private ProfessionBuilder(String path) {
            this.path = path;
        }

        public ProfessionBuilder workstation(Supplier<Block> block) {
            this.workstation = block;
            return this;
        }

        public ProfessionBuilder maxVillagers(int max) {
            this.maxVillagers = max;
            return this;
        }

        public ProfessionBuilder searchRange(int range) {
            this.searchRange = range;
            return this;
        }

        public ProfessionBuilder workSound(SoundEvent sound) {
            this.workSound = sound;
            return this;
        }

        public ProfessionBuilder enabledIf(Supplier<Boolean> condition) {
            this.enabledIf = condition;
            return this;
        }

        public ProfessionBuilder trade(int level, SimpleTrade trade) {
            tradesByLevel.computeIfAbsent(level, k -> new ArrayList<>()).add(trade);
            return this;
        }

        /** Finishes this one profession and hands it back to the parent lib. */
        public SimpleVillagerLib register() {
            if (workstation == null) {
                throw new IllegalStateException("Profession '" + path + "' is missing workstation(...)");
            }
            if (!Boolean.TRUE.equals(enabledIf.get())) {
                return SimpleVillagerLib.this;
            }
            professions.add(new ProfessionDefinition(
                    modId, path, workstation, maxVillagers, searchRange, workSound, tradesByLevel));
            return SimpleVillagerLib.this;
        }
    }
}
