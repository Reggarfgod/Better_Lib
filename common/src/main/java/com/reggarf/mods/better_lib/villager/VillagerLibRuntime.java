package com.reggarf.mods.better_lib.villager;

import com.google.common.collect.ImmutableSet;
import com.reggarf.mods.better_lib.Constants;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Builds PoiType and VillagerProfession objects for 26.1+ trade-set based
 * villager professions. Actual registry insertion is done by the platform
 * loader (NeoForge RegisterEvent, Fabric registry callback, etc.).
 */
public final class VillagerLibRuntime {

    private static final Map<String, Holder<PoiType>> POI_HOLDERS = new HashMap<>();

    private VillagerLibRuntime() {
    }

    public static Optional<PoiType> createPoiType(ProfessionDefinition def) {
        Block workstationBlock = def.workstation().get();
        Optional<Holder<PoiType>> existing = PoiTypes.forState(workstationBlock.defaultBlockState());
        if (existing.isPresent()) {
            Constants.LOG.error(
                    "[VillagerLib] Skipping profession '{}' — workstation block {} is already a POI for {}",
                    def.path(), workstationBlock, existing.get()
            );
            return Optional.empty();
        }

        ImmutableSet<BlockState> matchingStates =
                ImmutableSet.copyOf(workstationBlock.getStateDefinition().getPossibleStates());
        return Optional.of(new PoiType(matchingStates, def.maxVillagers(), def.searchRange()));
    }

    public static void storePoiHolder(ProfessionDefinition def, Holder<PoiType> holder) {
        POI_HOLDERS.put(def.modId() + ":" + def.path(), holder);
    }

    static Holder<PoiType> getCachedPoiHolder(String cacheKey) {
        return POI_HOLDERS.get(cacheKey);
    }

    public static Optional<VillagerProfession> createProfession(ProfessionDefinition def) {
        Optional<Holder<PoiType>> poiHolder = VillagerLibTradeTags.resolvePoiHolder(def);
        if (poiHolder.isEmpty()) {
            Constants.LOG.error("[VillagerLib] No POI registered for profession '{}'", def.path());
            return Optional.empty();
        }

        Holder<PoiType> poi = poiHolder.get();

        Int2ObjectMap<ResourceKey<TradeSet>> tradeSetsByLevel = new Int2ObjectOpenHashMap<>();
        for (Map.Entry<Integer, java.util.List<SimpleTrade>> level : def.tradesByLevel().entrySet()) {
            tradeSetsByLevel.put(
                    level.getKey().intValue(),
                    ResourceKey.create(
                            Registries.TRADE_SET,
                            VillagerLibTradeTags.tradeSetId(def, level.getKey())
                    )
            );
        }

        Predicate<Holder<PoiType>> poiPredicate = holder -> holder.value() == poi.value();

        VillagerProfession profession = new VillagerProfession(
                Component.translatable("entity.villager." + def.modId() + "." + def.path()),
                poiPredicate,
                poiPredicate,
                ImmutableSet.<Item>of(),
                ImmutableSet.<Block>of(),
                def.workSound(),
                tradeSetsByLevel
        );

        return Optional.of(profession);
    }

    public static Identifier professionId(ProfessionDefinition def) {
        return Identifier.fromNamespaceAndPath(def.modId(), def.path());
    }
}
