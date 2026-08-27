package com.reggarf.mods.better_lib.platform;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class NeoForgePlatformHelper implements PlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public InputStream getModLogoInputStream(String modId) {
        if (modId == null || modId.isEmpty()) return null;
        try {
            Optional<? extends ModContainer> containerOpt = ModList.get().getModContainerById(modId);
            if (containerOpt.isPresent()) {
                ModContainer container = containerOpt.get();
                IModInfo info = container.getModInfo();
                Optional<String> logoFile = info.getLogoFile();
                if (logoFile.isPresent() && !logoFile.get().isEmpty()) {
                    String file = logoFile.get();
                    IModFile modFile = info.getOwningFile().getFile();
                    Path path = modFile.findResource(file);
                    if (path != null && Files.exists(path)) {
                        return Files.newInputStream(path);
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}
