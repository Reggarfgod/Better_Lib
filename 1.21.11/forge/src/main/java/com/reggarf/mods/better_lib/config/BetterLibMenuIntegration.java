package com.reggarf.mods.better_lib.config;

import com.reggarf.mods.better_lib.configs.demo.BetterLibConfigScreen;
import com.reggarf.mods.better_lib.gui.screen.BetterConfigScreenHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BetterLibMenuIntegration {

    public static Screen createConfigScreen(Screen parent) {
        return BetterLibConfigScreen.ModScreen(parent);
    }

    public static void register() {
        BetterConfigScreenHandler.register("better_lib", BetterLibConfigScreen::ModScreen);
    }
}
