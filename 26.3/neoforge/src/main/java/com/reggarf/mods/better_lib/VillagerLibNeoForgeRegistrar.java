package com.reggarf.mods.better_lib;

import com.reggarf.mods.better_lib.villager.ProfessionDefinition;
import com.reggarf.mods.better_lib.villager.VillagerLibRegistry;
import com.reggarf.mods.better_lib.villager.VillagerLibRuntime;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * POI types and villager professions are code registries in 26.1+. They must
 * be registered through {@link RegisterEvent} on the mod event bus.
 */
public final class VillagerLibNeoForgeRegistrar {

    private VillagerLibNeoForgeRegistrar() {
    }

    public static void onRegister(RegisterEvent event) {
        event.register(Registries.POINT_OF_INTEREST_TYPE, helper -> {
            for (ProfessionDefinition def : VillagerLibRegistry.getAllDefinitions()) {
                VillagerLibRuntime.createPoiType(def).ifPresent(poiType -> {
                    var id = VillagerLibRuntime.professionId(def);
                    helper.register(id, poiType);
                    Constants.LOG.info("[VillagerLib] Registered POI {} for profession '{}'", id, def.path());
                });
            }
        });

        event.register(Registries.VILLAGER_PROFESSION, helper -> {
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
}
