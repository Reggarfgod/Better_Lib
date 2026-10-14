package com.reggarf.mods.better_lib.platform;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Either;
import com.reggarf.mods.better_lib.villagers.ProfessionEntry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ForgeRegistryPlatform implements VillagerRegistryPlatform {

    private final Map<String, DeferredRegister<PoiType>> poiRegistries = new HashMap<>();
    private final Map<String, DeferredRegister<VillagerProfession>> professionRegistries = new HashMap<>();
    private final Set<String> flushed = new HashSet<>();

    private static final List<ProfessionEntry> ALL_ENTRIES = new ArrayList<>();

    @Override
    public Holder<PoiType> registerPoiType(String modid, String name, Supplier<Block> workstation,
                                           int maxVillagers, int searchRange) {
        DeferredRegister<PoiType> registry = poiRegistries.computeIfAbsent(modid,
                id -> DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, id));

        RegistryObject<PoiType> obj = registry.register(name, () ->
                new PoiType(ImmutableSet.copyOf(workstation.get().getStateDefinition().getPossibleStates()),
                        maxVillagers, searchRange));
        return new LazyHolder<>(obj);
    }

    @Override
    public Holder<VillagerProfession> registerProfession(String modid, String name,
                                                         Holder<PoiType> poi, SoundEvent workSound) {
        DeferredRegister<VillagerProfession> registry = professionRegistries.computeIfAbsent(modid,
                id -> DeferredRegister.create(Registries.VILLAGER_PROFESSION, id));

        RegistryObject<VillagerProfession> obj = registry.register(name, () -> new VillagerProfession(
                Component.literal(name),
                holder -> holder.value() == poi.value(),
                holder -> holder.value() == poi.value(),
                ImmutableSet.of(), ImmutableSet.of(), workSound,
                it.unimi.dsi.fastutil.ints.Int2ObjectMaps.emptyMap()));
        return new LazyHolder<>(obj);
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

        BusGroup modBus = FMLJavaModLoadingContext.get().getModBusGroup();

        DeferredRegister<PoiType> poi = poiRegistries.get(modid);
        DeferredRegister<VillagerProfession> professions = professionRegistries.get(modid);
        if (poi != null) {
            poi.register(modBus);
        }
        if (professions != null) {
            professions.register(modBus);
        }
    }

    /**
     * A {@link Holder} that defers resolving the backing {@link RegistryObject}'s holder until
     * it is actually used. Calling {@code RegistryObject.getHolder()} too early throws
     * "Registry is already frozen" because the vanilla registry hasn't been unlocked for writes
     * yet.
     */
    private static final class LazyHolder<T> implements Holder<T> {
        private final RegistryObject<T> object;
        private Holder<T> delegate;

        private LazyHolder(RegistryObject<T> object) {
            this.object = object;
        }

        private Holder<T> delegate() {
            if (delegate == null) {
                delegate = object.getHolder()
                        .orElseThrow(() -> new IllegalStateException(
                                "Registry entry '" + object.getId() + "' is not registered yet"));
            }
            return delegate;
        }

        @Override
        public net.minecraft.core.component.DataComponentMap components() {
            return delegate().components();
        }

        @Override
        public boolean areComponentsBound() {
            return delegate().areComponentsBound();
        }

        @Override
        public T value() {
            return delegate().value();
        }

        @Override
        public boolean isBound() {
            return delegate().isBound();
        }

        @Override
        public boolean is(Identifier location) {
            return delegate().is(location);
        }

        @Override
        public boolean is(ResourceKey<T> resourceKey) {
            return delegate().is(resourceKey);
        }

        @Override
        public boolean is(TagKey<T> tagKey) {
            return delegate().is(tagKey);
        }

        @Override
        public boolean is(Holder<T> other) {
            return delegate().is(other);
        }

        @Override
        public boolean is(Predicate<ResourceKey<T>> predicate) {
            return delegate().is(predicate);
        }

        @Override
        public Stream<TagKey<T>> tags() {
            return delegate().tags();
        }

        @Override
        public Either<ResourceKey<T>, T> unwrap() {
            return delegate().unwrap();
        }

        @Override
        public Optional<ResourceKey<T>> unwrapKey() {
            return delegate().unwrapKey();
        }

        @Override
        public Kind kind() {
            return delegate().kind();
        }

        @Override
        public boolean canSerializeIn(HolderOwner<T> owner) {
            return delegate().canSerializeIn(owner);
        }
    }
}
