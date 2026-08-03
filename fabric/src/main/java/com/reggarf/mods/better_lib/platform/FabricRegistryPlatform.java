package com.reggarf.mods.better_lib.platform;

import com.google.common.collect.ImmutableSet;
import com.reggarf.mods.better_lib.villagers.ProfessionEntry;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.village.poi.PoiType;

import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.function.Supplier;

public class FabricRegistryPlatform implements VillagerRegistryPlatform {

    @Override
    public Holder<PoiType> registerPoiType(String modid, String name, Supplier<Block> workstation,
                                           int maxVillagers, int searchRange) {

        Set<BlockState> validStates =
                ImmutableSet.copyOf(workstation.get().getStateDefinition().getPossibleStates());

        Identifier id = Identifier.fromNamespaceAndPath(modid, name);

        PoiType registered = PointOfInterestHelper.register(
                id, maxVillagers, searchRange, validStates);

        return BuiltInRegistries.POINT_OF_INTEREST_TYPE.wrapAsHolder(registered);
    }

    @Override
    public Holder<VillagerProfession> registerProfession(String modid, String name,
                                                         Holder<PoiType> poi, SoundEvent workSound) {

        VillagerProfession profession = new VillagerProfession(Component.literal(name),
                holder -> holder.value() == poi.value(),
                holder -> holder.value() == poi.value(),
                ImmutableSet.of(), ImmutableSet.of(), workSound);

        VillagerProfession registered = Registry.register(BuiltInRegistries.VILLAGER_PROFESSION,
                Identifier.fromNamespaceAndPath(modid, name), profession);

        return BuiltInRegistries.VILLAGER_PROFESSION.wrapAsHolder(registered);
    }

    @Override
    public void registerTradeSource(String modid, ProfessionEntry entry) {
        ResourceKey<VillagerProfession> professionKey = entry.profession()
                .unwrapKey()
                .orElseThrow(() -> new IllegalStateException(
                        "Profession holder for '" + modid + "' has no resource key"));

        entry.trades().forEach((level, listingSuppliers) ->
                TradeOfferHelper.registerVillagerOffers(professionKey, level, tradesList -> {
                    if (!entry.enabled().getAsBoolean()) {
                        return;
                    }
                    for (Supplier<VillagerTrades.ItemListing> supplier : listingSuppliers) {
                        tradesList.add(supplier.get());
                    }
                }));
    }

    @Override
    public void finishRegistration(String modid) {
    }
}