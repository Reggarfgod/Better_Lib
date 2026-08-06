package com.reggarf.mods.better_lib.villager.datagen;

import com.reggarf.mods.better_lib.villager.ProfessionDefinition;
import com.reggarf.mods.better_lib.villager.VillagerLibRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.world.entity.ai.village.poi.PoiType;

import java.util.concurrent.CompletableFuture;

public class PoiTypeTagsProvider extends TagsProvider<PoiType> {

    public PoiTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.POINT_OF_INTEREST_TYPE, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        TagBuilder builder = getOrCreateRawBuilder(PoiTypeTags.ACQUIRABLE_JOB_SITE);

        for (ProfessionDefinition def : VillagerLibRegistry.getAllDefinitions()) {
            Identifier id = Identifier.fromNamespaceAndPath(def.modId(), def.path());
            builder.add(TagEntry.element(id));
        }
    }
}