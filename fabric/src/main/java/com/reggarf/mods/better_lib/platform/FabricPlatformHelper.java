package com.reggarf.mods.better_lib.platform;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class FabricPlatformHelper implements PlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public InputStream getModLogoInputStream(String modId) {
        if (modId == null || modId.isEmpty()) return null;
        try {
            Optional<ModContainer> containerOpt = FabricLoader.getInstance().getModContainer(modId);
            if (containerOpt.isPresent()) {
                ModContainer container = containerOpt.get();
                ModMetadata meta = container.getMetadata();
                for (int size : new int[]{128, 64, 32, 16, 256, 512}) {
                    Optional<String> iconPath = meta.getIconPath(size);
                    if (iconPath.isPresent() && !iconPath.get().isEmpty()) {
                        Optional<Path> path = container.findPath(iconPath.get());
                        if (path.isPresent() && Files.exists(path.get())) {
                            return Files.newInputStream(path.get());
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}
