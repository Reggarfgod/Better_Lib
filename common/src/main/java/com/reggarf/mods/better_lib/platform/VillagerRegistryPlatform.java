package com.reggarf.mods.better_lib.platform;

import com.reggarf.mods.better_lib.villagers.ProfessionEntry;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * The only part of SimpleVillagerLib that differs between loaders:
 *
 *  - Fabric has no DeferredRegister; POI types / professions are registered
 *    immediately via Registry.register(...), and trades are wired up per
 *    level directly through Fabric API's TradeOfferHelper.
 *  - Forge / NeoForge use a DeferredRegister for POI types / professions,
 *    flushed onto the mod event bus, and trades are applied globally via a
 *    VillagerTradesEvent listener.
 *
 * common/ code never touches any of this directly - it only ever talks to
 * Services.PLATFORM. Each loader module implements this interface once and
 * publishes it via
 * META-INF/services/com.reggarf.mods.better_lib.villagers.platform.VillagerRegistryPlatform
 */
public interface VillagerRegistryPlatform {

    /** Register (or queue registration of) a POI type for the given workstation block. */
    Holder<PoiType> registerPoiType(String modid, String name, Supplier<Block> workstation,
                                    int maxVillagers, int searchRange);

    /** Register (or queue registration of) a villager profession tied to the given POI. */
    Holder<VillagerProfession> registerProfession(String modid, String name,
                                                  Holder<PoiType> poi, SoundEvent workSound);

    /**
     * Hand off a fully-built profession entry (trades + enabled flag) so the
     * platform can apply it whenever that loader generates villager trades.
     */
    void registerTradeSource(String modid, ProfessionEntry entry);

    /**
     * Called once, after every profession(...)....register() call for a
     * given mod id, so the platform can flush anything it collected along
     * the way (DeferredRegister.register(bus) on Forge/NeoForge; a no-op on
     * Fabric, where registration above already happened immediately).
     */
    void finishRegistration(String modid);
}