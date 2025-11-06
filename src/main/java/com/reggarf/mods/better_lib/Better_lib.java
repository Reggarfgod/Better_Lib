package com.reggarf.mods.better_lib;

import com.mojang.logging.LogUtils;
import com.reggarf.mods.better_lib.config.core.BetterConfigManager;
import com.reggarf.mods.better_lib.config.core.BetterConfigScreenFactory;
import com.reggarf.mods.better_lib.config.gui.betterConfigScreenHandler;
import com.reggarf.mods.better_lib.message.util.OnlineMessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Better_lib.MODID)
public class Better_lib {
    public static final String MODID = "better_lib";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static DemoConfig CONFIG;

    public Better_lib() {

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onClientSetup);

        CONFIG = BetterConfigManager.register(DemoConfig.class);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new OnlineMessageHandler());
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> betterConfigScreenHandler.register("better_lib",
                parent -> BetterConfigScreenFactory.from(DemoConfig.class, CONFIG, parent)));
        LOGGER.info("Better_lib: Client setup complete, Minecraft user: {}", Minecraft.getInstance().getUser().getName());
    }

}
