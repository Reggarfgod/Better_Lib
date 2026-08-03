package com.reggarf.mods.better_lib.villagers;

import com.reggarf.mods.better_lib.platform.Services;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Fluent builder for a villager profession. Obtain one via
 * {@link SimpleVillagerLib#profession(String)}, configure it, add trades per
 * level with {@link #trade(int, SimpleTrade)}, then finish with
 * {@link #register()}. Identical API on every loader - the actual POI /
 * profession / trade registration is delegated to Services.PLATFORM, which
 * resolves to whichever loader module is on the classpath.
 */
public final class VillagerProfessionBuilder {

    private final SimpleVillagerLib lib;
    private final String name;

    private Supplier<Block> workstationBlock;
    private int maxVillagers = 1;
    private int searchRange = 1;
    private SoundEvent workSound = SoundEvents.VILLAGER_WORK_ARMORER;
    private BooleanSupplier enabled = () -> true;

    private final Map<Integer, List<Supplier<VillagerTrades.ItemListing>>> trades = new HashMap<>();

    VillagerProfessionBuilder(SimpleVillagerLib lib, String name) {
        this.lib = lib;
        this.name = name;
    }

    /** The block that acts as this profession's job site / workstation. Required. */
    public VillagerProfessionBuilder workstation(Supplier<Block> block) {
        this.workstationBlock = block;
        return this;
    }

    /** Max villagers that can claim this workstation at once ("max concurrent"). Default 1. */
    public VillagerProfessionBuilder maxVillagers(int max) {
        this.maxVillagers = max;
        return this;
    }

    /** How far (in blocks) villagers search for this workstation. Default 1. */
    public VillagerProfessionBuilder searchRange(int range) {
        this.searchRange = range;
        return this;
    }

    /** Sound played while the villager works. Default VILLAGER_WORK_ARMORER. */
    public VillagerProfessionBuilder workSound(SoundEvent sound) {
        this.workSound = sound;
        return this;
    }

    /** Gate the whole profession (and its trades) behind a config flag or any condition. Default: always enabled. */
    public VillagerProfessionBuilder enabledIf(BooleanSupplier condition) {
        this.enabled = condition;
        return this;
    }

    /**
     * Add a trade at the given villager level (1 = novice ... 5 = master).
     * Call this multiple times per level to add several trades to the same level.
     */
    public VillagerProfessionBuilder trade(int level, SimpleTrade trade) {
        trades.computeIfAbsent(level, k -> new ArrayList<>()).add(trade::build);
        return this;
    }

    /**
     * Finalizes this profession: registers the POI + profession through the
     * active platform, and hands the trades off to it. Returns the parent
     * lib for chaining. Remember to call {@link SimpleVillagerLib#register()}
     * once at the end, after every profession(...)....register() call.
     */
    public SimpleVillagerLib register() {
        Objects.requireNonNull(workstationBlock, "Profession '" + name + "': workstation(...) must be set");

        String modid = lib.getModId();

        Holder<PoiType> poi = Services.PLATFORM.registerPoiType(
                modid, name + "_poi", workstationBlock, maxVillagers, searchRange);

        Holder<VillagerProfession> profession = Services.PLATFORM.registerProfession(
                modid, name, poi, workSound);

        ProfessionEntry entry = new ProfessionEntry(name, profession, trades, enabled);
        Services.PLATFORM.registerTradeSource(modid, entry);

        return lib;
    }
}
