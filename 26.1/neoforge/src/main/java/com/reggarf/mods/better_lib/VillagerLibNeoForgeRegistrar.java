package com.reggarf.mods.better_lib;

import com.reggarf.mods.better_lib.villager.ProfessionDefinition;
import com.reggarf.mods.better_lib.villager.VillagerLibRegistry;
import com.reggarf.mods.better_lib.villager.VillagerLibRuntime;
import com.reggarf.mods.better_lib.villager.VillagerLibTradeTags;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * POI types and villager professions are code registries in 26.1+. They must
 * be registered through {@link RegisterEvent} on the mod event bus.
 *
 * <p>{@code VILLAGER_PROFESSION} can fire before {@code POINT_OF_INTEREST_TYPE},
 * so POI registration is forced immediately (not via a nested {@code event.register}
 * call, which is deferred) before professions are created.
 */
public final class VillagerLibNeoForgeRegistrar {

    private static boolean poisRegistered = false;

    private VillagerLibNeoForgeRegistrar() {
    }

    public static void onRegister(RegisterEvent event) {
        event.register(Registries.POINT_OF_INTEREST_TYPE, helper -> registerAllPois(helper));

        event.register(Registries.VILLAGER_PROFESSION, helper -> {
            ensurePoisRegistered();
            for (ProfessionDefinition def : VillagerLibRegistry.getAllDefinitions()) {
                VillagerLibRuntime.createProfession(def).ifPresent(profession -> {
                    var id = VillagerLibRuntime.professionId(def);
                    helper.register(id, profession);
                    Constants.LOG.info(
                            "[VillagerLib] Registered profession {} with {} trade levels",
                            id, def.tradesByLevel().size()
                    );
                });
            }
        });
    }

    private static void ensurePoisRegistered() {
        if (poisRegistered) {
            return;
        }
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
        poisRegistered = true;
    }

    private static void registerAllPois(RegisterEvent.RegisterHelper<PoiType> helper) {
        if (poisRegistered) {
            return;
        }
        for (ProfessionDefinition def : VillagerLibRegistry.getAllDefinitions()) {
            VillagerLibRuntime.createPoiType(def).ifPresent(poiType -> {
                var id = VillagerLibRuntime.professionId(def);
                helper.register(id, poiType);
                VillagerLibRuntime.storePoiHolder(
                        def,
                        BuiltInRegistries.POINT_OF_INTEREST_TYPE.wrapAsHolder(poiType)
                );
                Constants.LOG.info("[VillagerLib] Registered POI {} for profession '{}'", id, def.path());
            });
        }
        poisRegistered = true;
    }
}
