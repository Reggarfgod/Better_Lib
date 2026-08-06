package com.reggarf.mods.better_lib;

import com.reggarf.mods.better_lib.config.core.BetterConfigScreenFactory;
import com.reggarf.mods.better_lib.demo.DemoConfig;
import com.reggarf.mods.better_lib.gui.screen.BetterConfigScreenHandler;
import com.reggarf.mods.better_lib.villager.datagen.PoiTypeTagsProvider;
import com.reggarf.mods.better_lib.villager.datagen.TradeSetDataProvider;
import com.reggarf.mods.better_lib.villager.datagen.TradeTagsProvider;
import com.reggarf.mods.better_lib.villager.VillagerLibRegistry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;


@Mod(Constants.MODID)
public class Better_lib_neoforge {

    public Better_lib_neoforge(IEventBus eventBus) {
        Constants.LOG.info("Hello NeoForge world!");
        CommonClass.init();
        eventBus.addListener(VillagerLibNeoForgeRegistrar::onRegister);
        eventBus.addListener(GatherDataEvent.Client.class, event -> gatherData(event));
        eventBus.addListener(GatherDataEvent.Server.class, event -> gatherData(event));

    }

    @EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
    public final class BetterLibClient {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                BetterConfigScreenHandler.register("better_lib", parent ->
                        BetterConfigScreenFactory.from(DemoConfig.class, CommonClass.CONFIG, parent)
                );
            });
        }
    }

    private void gatherData(GatherDataEvent event) {
        Constants.LOG.info("[VillagerLib] gatherData fired!");

        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        var lookupProvider = event.getLookupProvider();

        RegistrySetBuilder registrySetBuilder = new RegistrySetBuilder()
                .add(Registries.VILLAGER_TRADE, VillagerLibRegistry::bootstrapTrades);

        generator.addProvider(true, new TradeTagsProvider(packOutput, lookupProvider));
        generator.addProvider(true, new PoiTypeTagsProvider(packOutput, lookupProvider));
        generator.addProvider(true, new TradeSetDataProvider(packOutput));
        generator.addProvider(
                true,
                new DatapackBuiltinEntriesProvider(
                        packOutput,
                        lookupProvider,
                        registrySetBuilder,
                        Set.of(Constants.MODID)
                )
        );

        Constants.LOG.info("[VillagerLib] gatherData: provider added");
    }
}