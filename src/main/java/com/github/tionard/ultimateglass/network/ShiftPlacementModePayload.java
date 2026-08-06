package com.github.tionard.ultimateglass.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import com.github.tionard.ultimateglass.UltimateGlass;

public record ShiftPlacementModePayload(int modeOrdinal) implements CustomPacketPayload {
    public static final Type<ShiftPlacementModePayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(UltimateGlass.MOD_ID, "shift_placement_mode")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ShiftPlacementModePayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    ShiftPlacementModePayload::modeOrdinal,
                    ShiftPlacementModePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
