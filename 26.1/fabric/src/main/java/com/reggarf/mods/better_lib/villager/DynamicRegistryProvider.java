package com.reggarf.mods.better_lib.villager;

import com.reggarf.mods.better_lib.Constants;
import com.reggarf.mods.better_lib.datagen.BetterLibFabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.trading.VillagerTrade;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Writes {@code data/<modid>/villager_trade/...} JSON from the bootstrap
 * registry built in {@link BetterLibFabricDataGenerator#buildRegistry}.
 */
public class DynamicRegistryProvider extends FabricDynamicRegistryProvider {

    public DynamicRegistryProvider(
            FabricPackOutput output,
            CompletableFuture<HolderLookup.Provider> registries
    ) {
        super(output, registries);
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {
        HolderLookup.RegistryLookup<VillagerTrade> trades = registries.lookupOrThrow(Registries.VILLAGER_TRADE);

        for (ProfessionDefinition def : VillagerLibRegistry.getAllDefinitions()) {
            for (Map.Entry<Integer, List<SimpleTrade>> level : def.tradesByLevel().entrySet()) {
                List<SimpleTrade> tradeList = level.getValue();
                for (int i = 0; i < tradeList.size(); i++) {
                    ResourceKey<VillagerTrade> key = ResourceKey.create(
                            Registries.VILLAGER_TRADE,
                            VillagerLibTradeTags.tradeId(def, level.getKey(), i)
                    );
                    entries.add(trades, key);
                }
            }
        }
    }

    @Override
    public String getName() {
        return Constants.MOD_NAME + "/Villager Trades";
    }
}
