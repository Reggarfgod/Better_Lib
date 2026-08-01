package com.reggarf.mods.better_lib.platform;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Either;
import com.reggarf.mods.better_lib.villagers.ProfessionEntry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.village.VillagerTradesEvent;
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

        RegistryObject<VillagerProfession> obj = registry.register(name, () -> new VillagerProfession(name,
                holder -> holder.value() == poi.value(),
                holder -> holder.value() == poi.value(),
                ImmutableSet.of(), ImmutableSet.of(), workSound));
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

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        DeferredRegister<PoiType> poi = poiRegistries.get(modid);
        DeferredRegister<VillagerProfession> professions = professionRegistries.get(modid);
        if (poi != null) {
            poi.register(modBus);
        }
        if (professions != null) {
            professions.register(modBus);
        }

        MinecraftForge.EVENT_BUS.addListener(this::onVillagerTrades);
    }

    private void onVillagerTrades(VillagerTradesEvent event) {
        for (ProfessionEntry entry : ALL_ENTRIES) {
            if (!entry.enabled().getAsBoolean()) {
                continue;
            }
            if (event.getType() != entry.profession().value()) {
                continue;
            }

            entry.trades().forEach((level, listingSuppliers) -> {
                List<VillagerTrades.ItemListing> levelTrades = event.getTrades().get(level);
                for (Supplier<VillagerTrades.ItemListing> supplier : listingSuppliers) {
                    levelTrades.add(supplier.get());
                }
            });
        }
    }

    /**
     * A {@link Holder} that defers resolving the backing {@link RegistryObject}'s holder until
     * it is actually used. Calling {@code RegistryObject.getHolder()} too early (e.g. during mod
     * construction, before Forge's RegisterEvent has fired for this registry) throws
     * "Registry is already frozen" because the vanilla registry hasn't been unlocked for writes
     * yet. By the time anything calls value()/is()/etc. on this wrapper - typically much later,
     * during actual gameplay - registration has long since completed.
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
        public T value() {
            return delegate().value();
        }

        @Override
        public boolean isBound() {
            return delegate().isBound();
        }

        @Override
        public boolean is(ResourceLocation location) {
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
            return Kind.REFERENCE;
        }

        @Override
        public boolean canSerializeIn(HolderOwner<T> owner) {
            return delegate().canSerializeIn(owner);
        }
    }
}