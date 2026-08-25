package com.github.tionard.ultimateglass.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import com.github.tionard.ultimateglass.UltimateGlass;
import com.github.tionard.ultimateglass.seam.PaneSeamOverride;
import com.github.tionard.ultimateglass.seam.PaneSeamTarget;

/** Client-selected visual result for one validated pane boundary. */
public record PaneSeamEditPayload(
        BlockPos pos,
        int planeOrdinal,
        int boundaryOrdinal,
        int overrideOrdinal
) implements CustomPacketPayload {
    public static final Type<PaneSeamEditPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(UltimateGlass.MOD_ID, "pane_seam_edit")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, PaneSeamEditPayload> CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeBlockPos(payload.pos());
                        buffer.writeVarInt(payload.planeOrdinal());
                        buffer.writeVarInt(payload.boundaryOrdinal());
                        buffer.writeVarInt(payload.overrideOrdinal());
                    },
                    buffer -> new PaneSeamEditPayload(
                            buffer.readBlockPos(),
                            buffer.readVarInt(),
                            buffer.readVarInt(),
                            buffer.readVarInt()
                    )
            );

    public static PaneSeamEditPayload of(
            BlockPos pos,
            PaneSeamTarget target,
            PaneSeamOverride override
    ) {
        return new PaneSeamEditPayload(
                pos,
                target.plane().ordinal(),
                target.boundary().ordinal(),
                override.ordinal()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
