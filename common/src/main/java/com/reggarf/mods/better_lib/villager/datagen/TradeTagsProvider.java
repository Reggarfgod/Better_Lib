package com.reggarf.mods.better_lib.villager.datagen;

import com.reggarf.mods.better_lib.villager.ProfessionDefinition;
import com.reggarf.mods.better_lib.villager.SimpleTrade;
import com.reggarf.mods.better_lib.villager.VillagerLibRegistry;
import com.reggarf.mods.better_lib.villager.VillagerLibTradeTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.trading.VillagerTrade;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TradeTagsProvider extends TagsProvider<VillagerTrade> {

    public TradeTagsProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider
    ) {
        super(output, Registries.VILLAGER_TRADE, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (ProfessionDefinition def : VillagerLibRegistry.getAllDefinitions()) {
            for (Map.Entry<Integer, List<SimpleTrade>> level : def.tradesByLevel().entrySet()) {
                TagKey<VillagerTrade> tag = TagKey.create(
                        Registries.VILLAGER_TRADE,
                        VillagerLibTradeTags.tradeTagId(def, level.getKey())
                );
                TagBuilder builder = getOrCreateRawBuilder(tag);
                List<SimpleTrade> trades = level.getValue();
                for (int i = 0; i < trades.size(); i++) {
                    Identifier tradeId = VillagerLibTradeTags.tradeId(def, level.getKey(), i);
                    builder.add(TagEntry.element(tradeId));
                }
            }
        }
    }
}
