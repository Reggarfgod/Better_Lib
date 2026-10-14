package com.reggarf.mods.better_lib.popup.network;

import com.reggarf.mods.better_lib.Better_lib;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record WelcomePopupPayload(int delayTicks) implements CustomPacketPayload {

    public static final Type<WelcomePopupPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Better_lib.MODID, "welcome_popup"));

    public static final StreamCodec<ByteBuf, WelcomePopupPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            WelcomePopupPayload::delayTicks,
            WelcomePopupPayload::new
    );

    public WelcomePopupPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(delayTicks);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
