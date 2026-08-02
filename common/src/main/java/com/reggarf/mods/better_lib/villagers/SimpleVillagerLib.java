package com.reggarf.mods.better_lib.villagers;

import com.reggarf.mods.better_lib.platform.Services;
import com.reggarf.mods.better_lib.villagers.json.JsonVillagerLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 *   // JSON-defined professions are loaded through their own isolated entrypoint -
 *   // see loadJsonProfessions(...) below - so a bad/missing/empty json folder
 *   // can never take down the code-defined professions above, or vice versa.
 *   VILLAGERS.loadJsonProfessions(MyMod.class, "/villagers");
 *
 *   // once, after every profession(...)....register() call AND after
 *   // loadJsonProfessions(...):
 *   VILLAGERS.register();
 *
 * No event bus is passed in anywhere - each loader module grabs whatever it
 * needs (mod event bus on Forge/NeoForge, nothing on Fabric) internally.
 */
public class SimpleVillagerLib {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleVillagerLib.class);

    private final String modid;

    public SimpleVillagerLib(String modid) {
        this.modid = modid;
    }

    /** Start defining a new villager profession. Call .register() on the returned builder when done. */
    public VillagerProfessionBuilder profession(String name) {
        return new VillagerProfessionBuilder(this, name);
    }

    /**
     * Loads every *.json profession under the given classpath folder (see
     * JsonVillagerLoader.loadAll for the format), completely isolated from
     * your code-defined professions above. If the folder is missing, empty,
     * or a file in it is malformed, this logs an error and returns instead
     * of throwing - so a problem with your JSON professions can NEVER
     * prevent code-defined professions (registered separately via
     * .profession(...)....register()) from registering, and vice versa.
     *
     * Call this as its own step, separate from any .profession(...) chains -
     * order relative to them doesn't matter, since each path is independent.
     *
     * @param modClass   any class in your mod's jar, used to resolve the classpath folder
     * @param folderPath e.g. "/villagers" - resolved relative to your mod's resources root
     */
    public SimpleVillagerLib loadJsonProfessions(Class<?> modClass, String folderPath) {
        try {
            JsonVillagerLoader.loadAll(this, modClass, folderPath);
        } catch (Exception e) {
            // Deliberately swallowed (not rethrown): a bad/missing/empty json
            // folder must never stop code-defined professions elsewhere in
            // the same init method from registering.
            LOGGER.error("[{}] JSON villager professions failed to load from '{}' - "
                    + "code-defined professions are unaffected, but no JSON professions "
                    + "were registered this run", modid, folderPath, e);
        }
        return this;
    }

    /**
     * Loads a single JSON profession from a classpath resource, isolated the
     * same way as {@link #loadJsonProfessions(Class, String)} - a bad file
     * logs an error instead of throwing, so it can't take down anything
     * registered elsewhere.
     *
     * @param modClass     any class in your mod's jar, used to resolve the resource path
     * @param resourcePath e.g. "/villagers/andesite_worker.json"
     */
    public SimpleVillagerLib loadJsonProfession(Class<?> modClass, String resourcePath) {
        try {
            JsonVillagerLoader.loadResource(this, modClass, resourcePath);
        } catch (Exception e) {
            LOGGER.error("[{}] JSON villager profession failed to load from '{}' - "
                    + "other professions are unaffected", modid, resourcePath, e);
        }
        return this;
    }

    public void register() {
        Services.PLATFORM.finishRegistration(modid);
    }

    String getModId() {
        return modid;
    }
}