package com.reggarf.mods.better_lib.platform;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class FabricModHelper implements ModPlatformHelper {

    @Override
    public List<ModEntry> getLoadedMods() {
        List<ModEntry> list = new ArrayList<>();
        Map<?, ?> modMenuFactories = null;
        try {
            Class<?> mmClass = Class.forName("com.terraformersmc.modmenu.ModMenu");
            var field = mmClass.getField("configScreenFactories");
            modMenuFactories = (Map<?, ?>) field.get(null);
        } catch (Throwable ignored) {}

        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            String id = mod.getMetadata().getId();
            if (id.startsWith("fabric-api-lookup-api-v1") || id.startsWith("fabric-convention-tags-v2")) {
                continue;
            }
            String name = mod.getMetadata().getName();
            String version = mod.getMetadata().getVersion().getFriendlyString();
            String desc = mod.getMetadata().getDescription();

            boolean hasConfig = (modMenuFactories != null && modMenuFactories.containsKey(id));
            list.add(new ModEntry(id, name, version, desc, hasConfig));
        }

        list.sort(Comparator.comparing(a -> a.name().toLowerCase()));
        return list;
    }

    @Override
    public Screen openConfigScreen(String modId, Screen parent) {
        try {
            Class<?> mmClass = Class.forName("com.terraformersmc.modmenu.ModMenu");
            var method = mmClass.getMethod("getConfigScreen", String.class, Screen.class);
            return (Screen) method.invoke(null, modId, parent);
        } catch (Throwable ignored) {}
        return null;
    }
}
