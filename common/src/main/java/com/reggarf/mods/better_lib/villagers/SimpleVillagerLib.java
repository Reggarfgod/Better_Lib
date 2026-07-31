package com.reggarf.mods.better_lib.villagers;

import com.reggarf.mods.better_lib.platform.Services;

/**
 * Simple Villager
 * ----------------------------
 * A tiny fluent wrapper around villager profession + POI + trade
 * registration boilerplate that works unchanged on Fabric, Forge, and
 * NeoForge. Live in common/ and call it from your common mod init - each
 * loader's entrypoint just needs to invoke that common init at the right
 * time (mod constructor on Forge/NeoForge, onInitialize on Fabric).
 *
 * Usage (identical on every loader):
 *
 *   public static final SimpleVillagerLib VILLAGERS = new SimpleVillagerLib(MODID);
 *
 *   VILLAGERS.profession("andesite_worker")
 *       .workstation(() -> AllBlocks.BASIN.get())
 *       .maxVillagers(1)
 *       .searchRange(1)
 *       .workSound(SoundEvents.VILLAGER_WORK_ARMORER)
 *       .enabledIf(() -> CONFIG.common.ENABLE_ANDESITE_WORKER)
 *       .trade(1, SimpleTrade.create()
 *               .cost(Items.EMERALD, 9)
 *               .cost2(AllBlocks.COGWHEEL, 6)
 *               .result(AllBlocks.GEARBOX, 2)
 *               .maxTrades(5).xp(3).priceMultiplier(0.07f))
 *       .register();
 *
 *   // once, after every profession(...)....register() call:
 *   VILLAGERS.register();
 *
 * No event bus is passed in anywhere - each loader module grabs whatever it
 * needs (mod event bus on Forge/NeoForge, nothing on Fabric) internally.
 */
public class SimpleVillagerLib {

    private final String modid;

    public SimpleVillagerLib(String modid) {
        this.modid = modid;
    }

    /** Start defining a new villager profession. Call .register() on the returned builder when done. */
    public VillagerProfessionBuilder profession(String name) {
        return new VillagerProfessionBuilder(this, name);
    }

    /**
     * Call once, after every profession(...)....register() call, from your
     * common mod init. Flushes any deferred registries the platform module
     * collected (Forge/NeoForge DeferredRegister.register(bus)); a no-op on
     * Fabric, where registration already happened immediately.
     */
    public void register() {
        Services.PLATFORM.finishRegistration(modid);
    }

    String getModId() {
        return modid;
    }
}
