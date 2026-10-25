package com.reggarf.mods.better_lib;

import com.reggarf.mods.better_lib.villager.ProfessionDefinition;
import com.reggarf.mods.better_lib.villager.VillagerLibRegistry;
import com.reggarf.mods.better_lib.villager.VillagerLibRuntime;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

/**
 * POI types and villager professions registration for Forge 26.1+.
 */
@Mod.EventBusSubscriber(modid = Better_lib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class VillagerLibForgeRegistrar {

    private VillagerLibForgeRegistrar() {
    }

    @SubscribeEvent
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
