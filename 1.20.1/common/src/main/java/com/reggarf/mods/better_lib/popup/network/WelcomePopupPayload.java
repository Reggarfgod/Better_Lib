package com.reggarf.mods.better_lib.popup.network;

import com.reggarf.mods.better_lib.Better_lib;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record WelcomePopupPayload(int delayTicks) {

    public static final ResourceLocation ID = new ResourceLocation(Better_lib.MODID, "welcome_popup");

    public WelcomePopupPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(delayTicks);
    }
}
