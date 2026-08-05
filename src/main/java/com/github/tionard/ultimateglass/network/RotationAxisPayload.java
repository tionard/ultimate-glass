package com.github.tionard.ultimateglass.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import com.github.tionard.ultimateglass.UltimateGlass;

public record RotationAxisPayload(int axisOrdinal) implements CustomPacketPayload {
    public static final Type<RotationAxisPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(UltimateGlass.MOD_ID, "rotation_axis")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RotationAxisPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            RotationAxisPayload::axisOrdinal,
            RotationAxisPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
