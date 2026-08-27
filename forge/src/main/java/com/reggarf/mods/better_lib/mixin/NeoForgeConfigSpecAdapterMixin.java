package com.reggarf.mods.better_lib.mixin;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "fuzs.forgeconfigapiport.forge.impl.neoforge.NeoForgeConfigSpecAdapter$1", remap = false)
public class NeoForgeConfigSpecAdapterMixin {

    @Shadow(remap = false)
    private CommentedConfig val$data;

    /**
     * @author BetterLib
     * @reason Fix Windows AccessDeniedException caused by REPLACE_ATOMIC on open FileConfig
     */
    @Overwrite(remap = false)
    public void save() {
        if (this.val$data instanceof FileConfig fileConfig) {
            fileConfig.save();
        }
    }
}
