package com.reggarf.mods.better_lib;

import com.mojang.logging.LogUtils;
import com.reggarf.mods.better_lib.config.core.BetterConfigManager;
import com.reggarf.mods.better_lib.config.core.BetterConfigScreenFactory;
import com.reggarf.mods.better_lib.config.gui.betterConfigScreenHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Better_lib.MODID)
public class Better_lib {
    public static final String MODID = "better_lib";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static DemoConfig CONFIG;
    public Better_lib(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();

        CONFIG = BetterConfigManager.register(DemoConfig.class);
        //OnlineMessageLib.registerPlugin(new MyModOnlineMessages());
       //DemoPlugin.register();
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> betterConfigScreenHandler.register("better_lib",
                    parent -> BetterConfigScreenFactory.from(DemoConfig.class, CONFIG, parent)));
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
