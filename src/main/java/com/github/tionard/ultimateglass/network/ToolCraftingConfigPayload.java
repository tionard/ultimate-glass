package com.github.tionard.ultimateglass.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import com.github.tionard.ultimateglass.UltimateGlass;
import com.github.tionard.ultimateglass.config.UltimateGlassServerConfig;

public record ToolCraftingConfigPayload(
        boolean copperEnabled,
        boolean ironEnabled,
        boolean diamondEnabled,
        boolean experimentalCompositesEnabled,
        boolean temperedPanesAlwaysDrop,
        boolean temperedToVanillaRecipeEnabled
) implements CustomPacketPayload {
    public static final Type<ToolCraftingConfigPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(UltimateGlass.MOD_ID, "tool_crafting_config")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ToolCraftingConfigPayload> CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeBoolean(payload.copperEnabled());
                        buffer.writeBoolean(payload.ironEnabled());
                        buffer.writeBoolean(payload.diamondEnabled());
                        buffer.writeBoolean(payload.experimentalCompositesEnabled());
                        buffer.writeBoolean(payload.temperedPanesAlwaysDrop());
                        buffer.writeBoolean(payload.temperedToVanillaRecipeEnabled());
                    },
                    buffer -> new ToolCraftingConfigPayload(
                            buffer.readBoolean(),
                            buffer.readBoolean(),
                            buffer.readBoolean(),
                            buffer.readBoolean(),
                            buffer.readBoolean(),
                            buffer.readBoolean()
                    )
            );

    public static ToolCraftingConfigPayload current() {
        return new ToolCraftingConfigPayload(
                UltimateGlassServerConfig.copperCraftingEnabled(),
                UltimateGlassServerConfig.ironCraftingEnabled(),
                UltimateGlassServerConfig.diamondCraftingEnabled(),
                UltimateGlassServerConfig.experimentalCompositesEnabled(),
                UltimateGlassServerConfig.temperedPanesAlwaysDrop(),
                UltimateGlassServerConfig.temperedToVanillaRecipeEnabled()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
