package com.reggarf.mods.better_lib.platform;

import com.google.common.collect.ImmutableSet;
import com.reggarf.mods.better_lib.villagers.ProfessionEntry;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PoiHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class FabricRegistryPlatform implements VillagerRegistryPlatform {

    private static final List<ProfessionEntry> ALL_ENTRIES = new ArrayList<>();

    @Override
    public Holder<PoiType> registerPoiType(String modid, String name, Supplier<Block> workstation,
                                           int maxVillagers, int searchRange) {

        Identifier id = Identifier.fromNamespaceAndPath(modid, name);

        PoiType registered = PoiHelper.register(
                id, maxVillagers, searchRange,
                ImmutableSet.copyOf(workstation.get().getStateDefinition().getPossibleStates()));

        return BuiltInRegistries.POINT_OF_INTEREST_TYPE.wrapAsHolder(registered);
    }

    @Override
    public Holder<VillagerProfession> registerProfession(String modid, String name,
                                                         Holder<PoiType> poi, SoundEvent workSound) {

        VillagerProfession profession = new VillagerProfession(
                Component.literal(name),
                holder -> holder.value() == poi.value(),
                holder -> holder.value() == poi.value(),
                ImmutableSet.of(), ImmutableSet.of(), workSound,
                it.unimi.dsi.fastutil.ints.Int2ObjectMaps.emptyMap());

        VillagerProfession registered = Registry.register(BuiltInRegistries.VILLAGER_PROFESSION,
                Identifier.fromNamespaceAndPath(modid, name), profession);

        return BuiltInRegistries.VILLAGER_PROFESSION.wrapAsHolder(registered);
    }

    @Override
    public void registerTradeSource(String modid, ProfessionEntry entry) {
        ALL_ENTRIES.add(entry);
    }

    @Override
    public void finishRegistration(String modid) {
    }
}
