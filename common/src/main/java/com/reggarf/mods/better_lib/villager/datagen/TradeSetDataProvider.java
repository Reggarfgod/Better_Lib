package com.reggarf.mods.better_lib.villager.datagen;

import com.google.gson.JsonObject;
import com.reggarf.mods.better_lib.villager.ProfessionDefinition;
import com.reggarf.mods.better_lib.villager.SimpleTrade;
import com.reggarf.mods.better_lib.villager.VillagerLibRegistry;
import com.reggarf.mods.better_lib.villager.VillagerLibTradeTags;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Writes trade_set JSON in the vanilla 26.1 format, referencing villager trade
 * tags ({@code #better_lib:ore_trader/level_1}) rather than listing every trade id.
 */
public class TradeSetDataProvider implements DataProvider {

    private final PackOutput output;

    public TradeSetDataProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (ProfessionDefinition def : VillagerLibRegistry.getAllDefinitions()) {
            for (Map.Entry<Integer, List<SimpleTrade>> level : def.tradesByLevel().entrySet()) {
                int levelNum = level.getKey();
                int tradeCount = level.getValue().size();
                int amount = Math.min(2, tradeCount);

                JsonObject json = new JsonObject();
                json.addProperty("amount", amount);
                json.addProperty(
                        "trades",
                        "#" + VillagerLibTradeTags.tradeTagId(def, levelNum)
                );
                json.addProperty("allow_duplicates", true);
                json.addProperty(
                        "random_sequence",
                        VillagerLibTradeTags.tradeSetRandomSequence(def, levelNum).toString()
                );

                Path path = output.createPathProvider(
                        PackOutput.Target.DATA_PACK,
                        "trade_set"
                ).json(VillagerLibTradeTags.tradeSetId(def, levelNum));

                futures.add(DataProvider.saveStable(cache, json, path));
            }
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "VillagerLib Trade Sets";
    }
}
