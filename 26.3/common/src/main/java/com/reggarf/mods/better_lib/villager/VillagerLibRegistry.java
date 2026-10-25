package com.reggarf.mods.better_lib.villager;

import com.reggarf.mods.better_lib.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.trading.VillagerTrade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class VillagerLibRegistry {

    private static final List<SimpleVillagerLib> LIBS = new ArrayList<>();

    private VillagerLibRegistry() {
    }

    static void registerLib(SimpleVillagerLib lib) {
        if (LIBS.contains(lib)) {
            return;
        }
        LIBS.add(lib);
        Constants.LOG.info("[VillagerLib] registerLib called, LIBS now has {} entries", LIBS.size());
    }

    public static void bootstrapTrades(BootstrapContext<VillagerTrade> context) {
        Constants.LOG.info("[VillagerLib] bootstrapTrades running, LIBS={}", LIBS.size());
        for (SimpleVillagerLib lib : LIBS) {
            for (ProfessionDefinition def : lib.getProfessions()) {
                Constants.LOG.info(
                        "[VillagerLib] bootstrapTrades: profession={}, levels={}",
                        def.path(), def.tradesByLevel().size()
                );
                for (Map.Entry<Integer, List<SimpleTrade>> level : def.tradesByLevel().entrySet()) {
                    List<SimpleTrade> trades = level.getValue();
                    for (int i = 0; i < trades.size(); i++) {
                        ResourceKey<VillagerTrade> key = tradeKey(def, level.getKey(), i);
                        context.register(key, trades.get(i).build());
                    }
                }
            }
        }
    }

    /** Flattened view of every profession from every registered lib — used by
     *  tag providers to tag all PoiTypes / VillagerTrades. */
    public static List<ProfessionDefinition> getAllDefinitions() {
        List<ProfessionDefinition> all = new ArrayList<>();
        for (SimpleVillagerLib lib : LIBS) {
            all.addAll(lib.getProfessions());
        }
        return all;
    }

    private static ResourceKey<VillagerTrade> tradeKey(ProfessionDefinition def, int level, int index) {
        return ResourceKey.create(
                Registries.VILLAGER_TRADE,
                VillagerLibTradeTags.tradeId(def, level, index)
        );
    }
}
