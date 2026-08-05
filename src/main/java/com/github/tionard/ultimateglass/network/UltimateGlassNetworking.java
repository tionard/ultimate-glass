package com.github.tionard.ultimateglass.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import com.github.tionard.ultimateglass.rotation.RotationAxisState;

public final class UltimateGlassNetworking {
    private UltimateGlassNetworking() {
    }

    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(RotationAxisPayload.TYPE, RotationAxisPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(
                RotationAxisPayload.TYPE,
                (payload, context) -> RotationAxisState.set(
                        context.player(),
                        RotationAxisState.fromOrdinal(payload.axisOrdinal())
                )
        );
    }
}
