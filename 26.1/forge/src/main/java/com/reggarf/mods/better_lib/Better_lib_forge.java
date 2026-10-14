package com.reggarf.mods.better_lib;

import com.reggarf.mods.better_lib.config.BetterLibMenuIntegration;
import com.reggarf.mods.better_lib.popup.manager.WelcomePopupManager;
import com.reggarf.mods.better_lib.popup.network.WelcomePopupPayload;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

@Mod(Better_lib.MODID)
public class Better_lib_forge {

    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(Identifier.fromNamespaceAndPath(Better_lib.MODID, "main"))
            .networkProtocolVersion(1)
            .clientAcceptedVersions((status, version) -> true)
            .serverAcceptedVersions((status, version) -> true)
            .simpleChannel();

    public Better_lib_forge() {
        Better_lib.LOG.info("Hello Forge world!");
        CommonClass.init();

        CHANNEL.messageBuilder(WelcomePopupPayload.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(WelcomePopupPayload::encode)
                .decoder(WelcomePopupPayload::new)
                .consumerMainThread((msg, ctx) -> {
                    ctx.setPacketHandled(true);
                    if (FMLEnvironment.dist.isClient()) {
                        WelcomePopupManager.triggerPopup(msg.delayTicks());
                    }
                })
                .add();
    }

    @Mod.EventBusSubscriber(modid = Better_lib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(BetterLibMenuIntegration::register);
        }
    }
}
