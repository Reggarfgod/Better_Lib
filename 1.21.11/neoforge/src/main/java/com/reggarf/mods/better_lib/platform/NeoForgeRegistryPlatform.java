package com.reggarf.mods.better_lib.platform;

import com.google.common.collect.ImmutableSet;
import com.reggarf.mods.better_lib.villagers.ProfessionEntry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class NeoForgeRegistryPlatform implements VillagerRegistryPlatform {

    private final Map<String, DeferredRegister<PoiType>> poiRegistries = new HashMap<>();
    private final Map<String, DeferredRegister<VillagerProfession>> professionRegistries = new HashMap<>();
    private final Set<String> flushed = new HashSet<>();
    private boolean tradesListenerRegistered = false;

    private static final List<ProfessionEntry> ALL_ENTRIES = new ArrayList<>();

    @Override
    public Holder<PoiType> registerPoiType(String modid, String name, Supplier<Block> workstation,
                                            int maxVillagers, int searchRange) {
        DeferredRegister<PoiType> registry = poiRegistries.computeIfAbsent(modid,
                id -> DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, id));

        return registry.register(name, () ->
                new PoiType(ImmutableSet.copyOf(workstation.get().getStateDefinition().getPossibleStates()),
                        maxVillagers, searchRange));
    }

    @Override
    public Holder<VillagerProfession> registerProfession(String modid, String name,
                                                          Holder<PoiType> poi, SoundEvent workSound) {
        DeferredRegister<VillagerProfession> registry = professionRegistries.computeIfAbsent(modid,
                id -> DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION, id));

        ResourceKey<PoiType> poiKey = ResourceKey.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE.key(), Identifier.fromNamespaceAndPath(modid, name + "_poi"));
        return registry.register(name, () -> new VillagerProfession(
                Component.literal(name),
                holder -> (poiKey != null && holder.is(poiKey)) || (poi.isBound() && holder.value() == poi.value()),
                holder -> (poiKey != null && holder.is(poiKey)) || (poi.isBound() && holder.value() == poi.value()),
                ImmutableSet.of(), ImmutableSet.of(), workSound));
    }

    @Override
    public void registerTradeSource(String modid, ProfessionEntry entry) {
        ALL_ENTRIES.add(entry);
    }

    @Override
    public void finishRegistration(String modid) {
        if (!flushed.add(modid)) {
            return;
        }

        IEventBus modBus = ModList.get().getModContainerById(modid)
                .orElseThrow(() -> new IllegalStateException(
                        "No NeoForge mod container found for mod id '" + modid + "'"))
                .getEventBus();

        DeferredRegister<PoiType> poi = poiRegistries.get(modid);
        DeferredRegister<VillagerProfession> professions = professionRegistries.get(modid);
        if (poi != null) {
            poi.register(modBus);
        }
        if (professions != null) {
            professions.register(modBus);
        }

        if (!tradesListenerRegistered) {
            NeoForge.EVENT_BUS.addListener(this::onVillagerTrades);
            tradesListenerRegistered = true;
        }
    }

    private void onVillagerTrades(VillagerTradesEvent event) {
        for (ProfessionEntry entry : ALL_ENTRIES) {
            if (!entry.enabled().getAsBoolean()) {
                continue;
            }
            if (!entry.profession().is(event.getType())) {
                continue;
            }

            entry.trades().forEach((level, listingSuppliers) -> {
                List<VillagerTrades.ItemListing> levelTrades = event.getTrades().get(level);
                if (levelTrades != null) {
                    for (Supplier<com.reggarf.mods.better_lib.villagers.ItemListing> supplier : listingSuppliers) {
                        levelTrades.add(supplier.get()::getOffer);
                    }
                }
            });
        }
    }
}
