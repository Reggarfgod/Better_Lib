package com.reggarf.mods.better_lib.villager;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;

import java.util.Optional;

/**
 * Tag keys for modded villager trades, mirroring vanilla {@code VillagerTradeTags}
 * layout ({@code <profession>/level_<n>}).
 */
public final class VillagerLibTradeTags {

    private VillagerLibTradeTags() {
    }

    public static ResourceKey<PoiType> poiKey(ProfessionDefinition def) {
        return ResourceKey.create(
                Registries.POINT_OF_INTEREST_TYPE,
                Identifier.fromNamespaceAndPath(def.modId(), def.path())
        );
    }

    public static Identifier tradeTagId(ProfessionDefinition def, int level) {
        return Identifier.fromNamespaceAndPath(def.modId(), def.path() + "/level_" + level);
    }

    public static Identifier tradeId(ProfessionDefinition def, int level, int index) {
        return Identifier.fromNamespaceAndPath(
                def.modId(), def.path() + "/level_" + level + "/trade_" + index
        );
    }

    public static Identifier tradeSetId(ProfessionDefinition def, int level) {
        return Identifier.fromNamespaceAndPath(def.modId(), def.path() + "/level_" + level);
    }

    public static Identifier tradeSetRandomSequence(ProfessionDefinition def, int level) {
        return Identifier.fromNamespaceAndPath(
                def.modId(), "trade_set/" + def.path() + "/level_" + level
        );
    }

    public static Optional<Holder<PoiType>> resolvePoiHolder(ProfessionDefinition def) {
        String cacheKey = def.modId() + ":" + def.path();
        Holder<PoiType> cached = VillagerLibRuntime.getCachedPoiHolder(cacheKey);
        if (cached != null) {
            return Optional.of(cached);
        }

        ResourceKey<PoiType> key = poiKey(def);
        return BuiltInRegistries.POINT_OF_INTEREST_TYPE.get(key.identifier())
                .map(holder -> (Holder<PoiType>) holder);
    }
}
