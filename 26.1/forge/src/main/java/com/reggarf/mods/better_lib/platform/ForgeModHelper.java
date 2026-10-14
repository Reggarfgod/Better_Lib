package com.reggarf.mods.better_lib.platform;

import com.reggarf.mods.better_lib.configs.ui.BaseConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ForgeModHelper implements ModPlatformHelper {

    @Override
    public List<ModEntry> getLoadedMods() {
        List<ModEntry> list = new ArrayList<>();
        for (var mod : ModList.getMods()) {
            String id = mod.getModId();
            String name = mod.getDisplayName();
            String version = mod.getVersion().toString();
            String desc = mod.getDescription();
            boolean hasConfig = false;

            ModContainer container = ModList.getModContainerById(id).orElse(null);
            if (container != null) {
                hasConfig = container.getCustomExtension(ConfigScreenHandler.ConfigScreenFactory.class).isPresent() || hasModConfig(id);
            } else {
                hasConfig = hasModConfig(id);
            }

            list.add(new ModEntry(id, name, version, desc, hasConfig));
        }
        list.sort(Comparator.comparing(a -> a.name().toLowerCase()));
        return list;
    }

    @Override
    public boolean hasModConfig(String modId) {
        return ForgeModConfigAdapter.hasConfigs(modId);
    }

    @Override
    public Screen createModConfigScreen(String modId, Screen parent) {
        return ForgeModConfigAdapter.createScreenForMod(modId, parent);
    }

    @Override
    public void populateConfigs(String modId, BaseConfigScreen screen) {
        ForgeModConfigAdapter.populate(modId, screen);
    }

    @Override
    public Screen openConfigScreen(String modId, Screen parent) {
        ModContainer container = ModList.getModContainerById(modId).orElse(null);
        if (container != null) {
            var factory = container.getCustomExtension(ConfigScreenHandler.ConfigScreenFactory.class).orElse(null);
            if (factory != null) {
                return factory.screenFunction().apply(Minecraft.getInstance(), parent);
            }
        }
        if (hasModConfig(modId)) {
            return createModConfigScreen(modId, parent);
        }
        return null;
    }
}
