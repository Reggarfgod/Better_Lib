package com.reggarf.mods.better_lib.platform;

import com.reggarf.mods.better_lib.configs.ui.BaseConfigScreen;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NeoForgeModHelper implements ModPlatformHelper {

    @Override
    public List<ModEntry> getLoadedMods() {
        List<ModEntry> list = new ArrayList<>();
        for (var mod : ModList.get().getMods()) {
            String id = mod.getModId();
            String name = mod.getDisplayName();
            String version = mod.getVersion().toString();
            String desc = mod.getDescription();
            boolean hasConfig = false;

            ModContainer container = ModList.get().getModContainerById(id).orElse(null);
            if (container != null) {
                hasConfig = container.getCustomExtension(IConfigScreenFactory.class).isPresent() || hasModConfig(id);
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
        return ModConfigAdapter.hasConfigs(modId);
    }

    @Override
    public Screen createModConfigScreen(String modId, Screen parent) {
        return ModConfigAdapter.createScreenForMod(modId, parent);
    }

    @Override
    public void populateConfigs(String modId, BaseConfigScreen screen) {
        ModConfigAdapter.populate(modId, screen);
    }

    @Override
    public Screen openConfigScreen(String modId, Screen parent) {
        ModContainer container = ModList.get().getModContainerById(modId).orElse(null);
        if (container != null) {
            IConfigScreenFactory factory = container.getCustomExtension(IConfigScreenFactory.class).orElse(null);
            if (factory != null) {
                return factory.createScreen(container, parent);
            }
        }
        if (hasModConfig(modId)) {
            return createModConfigScreen(modId, parent);
        }
        return null;
    }
}
