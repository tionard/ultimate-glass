package com.github.tionard.ultimateglass.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.Permissions;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import com.github.tionard.ultimateglass.config.UltimateGlassServerConfig;
import com.github.tionard.ultimateglass.rotation.RotationAxisState;

public final class UltimateGlassNetworking {
    private UltimateGlassNetworking() {
    }

    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(RotationAxisPayload.TYPE, RotationAxisPayload.CODEC);
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
                ToolCraftingConfigPayload.TYPE,
                (payload, context) -> {
                    MinecraftServer server = context.player().level().getServer();
                    boolean canEdit = context.player().permissions()
                            .hasPermission(Permissions.COMMANDS_GAMEMASTER)
                            || server.isSingleplayerOwner(context.player().nameAndId());
                    if (!canEdit) {
                        ServerPlayNetworking.send(context.player(), ToolCraftingConfigPayload.current());
                        return;
                    }

                    UltimateGlassServerConfig.apply(
                            payload.copperEnabled(),
                            payload.ironEnabled(),
                            payload.diamondEnabled(),
                            payload.experimentalCompositesEnabled(),
                            true
                    );
                    ToolCraftingConfigPayload current = ToolCraftingConfigPayload.current();
                    server.getPlayerList().getPlayers().forEach(
                            player -> ServerPlayNetworking.send(player, current)
                    );
                }
        );

        ServerPlayConnectionEvents.JOIN.register((listener, sender, server) ->
                ServerPlayNetworking.send(listener.player, ToolCraftingConfigPayload.current())
        );
    }
}
