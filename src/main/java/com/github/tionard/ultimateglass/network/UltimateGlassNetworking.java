package com.github.tionard.ultimateglass.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.Permissions;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import com.github.tionard.ultimateglass.config.UltimateGlassServerConfig;
import com.github.tionard.ultimateglass.placement.ShiftPlacementMode;
import com.github.tionard.ultimateglass.placement.ShiftPlacementModeState;
import com.github.tionard.ultimateglass.rotation.RotationAxisState;

public final class UltimateGlassNetworking {
    private UltimateGlassNetworking() {
    }

    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(RotationAxisPayload.TYPE, RotationAxisPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ShiftPlacementModePayload.TYPE, ShiftPlacementModePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ToolCraftingConfigPayload.TYPE, ToolCraftingConfigPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ToolCraftingConfigPayload.TYPE, ToolCraftingConfigPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(
                RotationAxisPayload.TYPE,
                (payload, context) -> RotationAxisState.set(
                        context.player(),
                        RotationAxisState.fromOrdinal(payload.axisOrdinal())
                )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                ShiftPlacementModePayload.TYPE,
                (payload, context) -> ShiftPlacementModeState.set(
                        context.player(),
                        ShiftPlacementMode.fromOrdinal(payload.modeOrdinal())
                )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                ToolCraftingConfigPayload.TYPE,
                (payload, context) -> {
                    if (!context.player().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                        ServerPlayNetworking.send(context.player(), ToolCraftingConfigPayload.current());
                        return;
                    }

                    UltimateGlassServerConfig.apply(
                            payload.copperEnabled(),
                            payload.ironEnabled(),
                            payload.diamondEnabled(),
                            true
                    );
                    MinecraftServer server = context.player().level().getServer();
                    ToolCraftingConfigPayload current = ToolCraftingConfigPayload.current();
                    server.getPlayerList().getPlayers().forEach(
                            player -> ServerPlayNetworking.send(player, current)
                    );
                }
        );

        ServerPlayConnectionEvents.JOIN.register((listener, sender, server) ->
                ServerPlayNetworking.send(listener.player, ToolCraftingConfigPayload.current())
        );
        ServerPlayConnectionEvents.DISCONNECT.register((listener, server) ->
                ShiftPlacementModeState.remove(listener.player)
        );
    }
}
