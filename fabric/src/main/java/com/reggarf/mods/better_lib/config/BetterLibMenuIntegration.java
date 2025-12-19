package com.reggarf.mods.better_lib.config;



import com.reggarf.mods.better_lib.CommonClass;
import com.reggarf.mods.better_lib.config.core.BetterConfigScreenFactory;
import com.reggarf.mods.better_lib.demo.DemoConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BetterLibMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent ->
                BetterConfigScreenFactory.from(
                        DemoConfig.class,
                        CommonClass.CONFIG,
                        parent
                );
    }
}
