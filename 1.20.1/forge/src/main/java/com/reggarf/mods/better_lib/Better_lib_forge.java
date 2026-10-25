package com.reggarf.mods.better_lib;

import com.reggarf.mods.better_lib.configs.demo.BetterLibConfigScreen;
import com.reggarf.mods.better_lib.gui.screen.BetterConfigScreenHandler;
import com.reggarf.mods.better_lib.popup.manager.WelcomePopupManager;
import com.reggarf.mods.better_lib.popup.network.WelcomePopupPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod(Better_lib.MODID)
public class Better_lib_forge {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Better_lib.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public Better_lib_forge() {
        Better_lib.LOG.info("Hello Forge world!");
        CommonClass.init();

        CHANNEL.registerMessage(
                0,
                WelcomePopupPayload.class,
                WelcomePopupPayload::encode,
                WelcomePopupPayload::new,
                (msg, ctxSupplier) -> {
                    var ctx = ctxSupplier.get();
                    ctx.enqueueWork(() -> {
                        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> WelcomePopupManager.triggerPopup(msg.delayTicks()));
                    });
                    ctx.setPacketHandled(true);
                }
        );
    }

    @Mod.EventBusSubscriber(modid = Better_lib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                BetterConfigScreenHandler.register("better_lib", BetterLibConfigScreen::ModScreen);
            });
        }
    }
}
