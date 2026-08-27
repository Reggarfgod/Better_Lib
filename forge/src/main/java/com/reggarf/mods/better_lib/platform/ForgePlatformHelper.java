package com.reggarf.mods.better_lib.platform;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.locating.IModFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class ForgePlatformHelper implements PlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
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
