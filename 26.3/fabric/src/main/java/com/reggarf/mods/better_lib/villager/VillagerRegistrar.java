package com.reggarf.mods.better_lib.villager;

import com.google.common.collect.ImmutableSet;
import com.reggarf.mods.better_lib.Constants;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * POI types and villager professions are code registries in 26.1+. On Fabric they
 * are registered during mod initialization. POIs must be registered with
 * {@link PoiTypes#register} so workstation block states are indexed for job-site
 * detection; plain {@link Registry#register} is not enough.
 */
public final class VillagerRegistrar {

    private VillagerRegistrar() {
    }

    public static void register() {
        registerAllPois();
        registerAllProfessions();
    }

    private static void registerAllPois() {
        for (ProfessionDefinition def : VillagerLibRegistry.getAllDefinitions()) {
            Block workstationBlock = def.workstation().get();
            Optional<Holder<PoiType>> existing = PoiTypes.forState(workstationBlock.defaultBlockState());
            if (existing.isPresent()) {
                Constants.LOG.error(
                        "[VillagerLib] Skipping profession '{}' — workstation block {} is already a POI for {}",
                        def.path(), workstationBlock, existing.get()
                );
                continue;
            }

            ImmutableSet<BlockState> matchingStates =
                    ImmutableSet.copyOf(workstationBlock.getStateDefinition().getPossibleStates());
            PoiType poiType = PoiTypes.register(
                    BuiltInRegistries.POINT_OF_INTEREST_TYPE,
                    VillagerLibTradeTags.poiKey(def),
                    matchingStates,
                    def.maxVillagers(),
                    def.searchRange()
            );
            VillagerLibRuntime.storePoiHolder(
                    def,
                    BuiltInRegistries.POINT_OF_INTEREST_TYPE.wrapAsHolder(poiType)
            );
            Constants.LOG.info(
                    "[VillagerLib] Registered POI {} for profession '{}'",
                    VillagerLibRuntime.professionId(def), def.path()
            );
        }
    }

    private static void registerAllProfessions() {
        for (ProfessionDefinition def : VillagerLibRegistry.getAllDefinitions()) {
            VillagerLibRuntime.createProfession(def).ifPresent(profession -> {
                var id = VillagerLibRuntime.professionId(def);
                Registry.register(BuiltInRegistries.VILLAGER_PROFESSION, id, profession);
                Constants.LOG.info(
                        "[VillagerLib] Registered profession {} with {} trade levels",
                        id, def.tradesByLevel().size()
                );
            });
        }
    }
}
