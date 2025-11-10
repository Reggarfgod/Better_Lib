package com.reggarf.mods.better_lib;

import com.mojang.logging.LogUtils;

import com.reggarf.mods.better_lib.config.core.BetterConfigManager;
import com.reggarf.mods.better_lib.config.core.BetterConfigScreenFactory;
import com.reggarf.mods.better_lib.config.gui.ConfigScreenHandler;
import com.reggarf.mods.better_lib.message.util.OnlineMessageHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Better_lib.MODID)
public class Better_lib {
    public static final String MODID = "better_lib";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static DemoConfig CONFIG;
    public Better_lib(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new OnlineMessageHandler());
        /// //////////////////////////////////
        //config register
        CONFIG = BetterConfigManager.register(DemoConfig.class);
        /// ////////////////////////

    }

    /// /////////////////////////////////////
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

     /// /////////////////////////////////////
    //config screen register
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
               event.enqueueWork(() -> {
            ConfigScreenHandler.register("better_lib", parent ->
                    BetterConfigScreenFactory.from(DemoConfig.class, CONFIG, parent)
            );
        });
      }
    }
}
