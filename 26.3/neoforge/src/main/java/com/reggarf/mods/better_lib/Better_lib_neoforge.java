package com.reggarf.mods.better_lib;

import com.reggarf.mods.better_lib.configs.demo.BetterLibConfigScreen;
import com.reggarf.mods.better_lib.gui.screen.BetterConfigScreenHandler;
import com.reggarf.mods.better_lib.popup.manager.WelcomePopupManager;
import com.reggarf.mods.better_lib.popup.network.WelcomePopupPayload;
import com.reggarf.mods.better_lib.villager.VillagerLibRegistry;
import com.reggarf.mods.better_lib.villager.datagen.PoiTypeTagsProvider;
import com.reggarf.mods.better_lib.villager.datagen.TradeSetDataProvider;
import com.reggarf.mods.better_lib.villager.datagen.TradeTagsProvider;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Set;

@Mod(Better_lib.MODID)
public class Better_lib_neoforge {

    public Better_lib_neoforge(IEventBus modBus, ModContainer modContainer) {
        Better_lib.LOG.info("Hello NeoForge world!");
        CommonClass.init();

        modBus.addListener(VillagerLibNeoForgeRegistrar::onRegister);
        modBus.addListener(GatherDataEvent.Client.class, event -> gatherData(event));
        modBus.addListener(GatherDataEvent.Server.class, event -> gatherData(event));

        modBus.addListener(this::registerPayloads);

        if (FMLEnvironment.getDist().isClient()) {
            modContainer.registerExtensionPoint(
                    IConfigScreenFactory.class,
                    (mc, parent) -> BetterLibConfigScreen.ModScreen(parent)
            );
            modBus.addListener(this::onClientSetup);
        }
    }

    private void gatherData(GatherDataEvent event) {
        Better_lib.LOG.info("[VillagerLib] gatherData fired!");

        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        var reloadableLookupProvider = event.getReloadableLookupProvider();
        var worldLookupProvider = event.getWorldLookupProvider();

        RegistrySetBuilder registrySetBuilder = new RegistrySetBuilder()
                .add(Registries.VILLAGER_TRADE, VillagerLibRegistry::bootstrapTrades);

        generator.addProvider(true, new TradeTagsProvider(packOutput, reloadableLookupProvider));
        generator.addProvider(true, new PoiTypeTagsProvider(packOutput, worldLookupProvider));
        generator.addProvider(true, new TradeSetDataProvider(packOutput));
        generator.addProvider(
                true,
                DatapackBuiltinEntriesProvider.forReloadableLayer(
                        packOutput,
                        Better_lib.MODID,
                        worldLookupProvider,
                        reloadableLookupProvider,
                        registrySetBuilder,
                        Set.of(Better_lib.MODID)
                )
        );

        Better_lib.LOG.info("[VillagerLib] gatherData: provider added");
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToClient(
                WelcomePopupPayload.TYPE,
                WelcomePopupPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (FMLEnvironment.getDist().isClient()) {
                        context.enqueueWork(() -> {
                            WelcomePopupManager.triggerPopup(payload.delayTicks());
                        });
                    }
                }
        );
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BetterConfigScreenHandler.register("better_lib", BetterLibConfigScreen::ModScreen);
        });
    }
}
