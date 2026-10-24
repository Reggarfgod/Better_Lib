package com.reggarf.mods.better_lib.datagen;

import com.reggarf.mods.better_lib.CommonClass;
import com.reggarf.mods.better_lib.Constants;
import com.reggarf.mods.better_lib.villager.DynamicRegistryProvider;
import com.reggarf.mods.better_lib.villager.VillagerLibRegistry;
import com.reggarf.mods.better_lib.villager.datagen.PoiTypeTagsProvider;
import com.reggarf.mods.better_lib.villager.datagen.TradeSetDataProvider;
import com.reggarf.mods.better_lib.villager.datagen.TradeTagsProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

public class BetterLibFabricDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        Constants.LOG.info("[VillagerLib] Fabric datagen starting");
        CommonClass.init();

        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(DynamicRegistryProvider::new);
        pack.addProvider(TradeTagsProvider::new);
        pack.addProvider(PoiTypeTagsProvider::new);
        pack.addProvider((output, registries) -> new TradeSetDataProvider(output));
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registrySetBuilder) {
        registrySetBuilder.add(Registries.VILLAGER_TRADE, VillagerLibRegistry::bootstrapTrades);
    }
}
