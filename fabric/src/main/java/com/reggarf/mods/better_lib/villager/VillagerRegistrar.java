package com.reggarf.mods.better_lib.villager;

import com.reggarf.mods.better_lib.Constants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * POI types and villager professions are code registries in 26.1+. On Fabric they
 * are registered directly during mod initialization, with POIs registered before
 * professions so {@link VillagerLibRuntime#createProfession} can resolve holders.
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
            VillagerLibRuntime.createPoiType(def).ifPresent(poiType -> {
                var id = VillagerLibRuntime.professionId(def);
                Registry.register(
                        BuiltInRegistries.POINT_OF_INTEREST_TYPE,
                        VillagerLibTradeTags.poiKey(def),
                        poiType
                );
                VillagerLibRuntime.storePoiHolder(
                        def,
                        BuiltInRegistries.POINT_OF_INTEREST_TYPE.wrapAsHolder(poiType)
                );
                Constants.LOG.info("[VillagerLib] Registered POI {} for profession '{}'", id, def.path());
            });
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
