package com.github.tionard.ultimateglass.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import com.github.tionard.ultimateglass.UltimateGlass;
import com.github.tionard.ultimateglass.network.RotationAxisPayload;
import com.github.tionard.ultimateglass.registry.UltimateGlassItems;
import com.github.tionard.ultimateglass.rotation.RotationAxisState;

public final class UltimateGlassClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(UltimateGlass.MOD_ID, "general")
    );

    private static final KeyMapping CHANGE_ROTATION_AXIS = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.ultimateglass.change_rotation_axis",
                    InputConstants.Type.KEYSYM,
                    -1,
                    CATEGORY
            )
    );

    private static Direction.Axis selectedAxis = RotationAxisState.DEFAULT_AXIS;
    private static Player currentPlayer;

    @Override
    public void onInitializeClient() {
        UltimateGlassClientConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != currentPlayer) {
                currentPlayer = client.player;
                selectedAxis = RotationAxisState.DEFAULT_AXIS;
            }

            while (CHANGE_ROTATION_AXIS.consumeClick()) {
                if (client.player == null || client.getConnection() == null) {
                    continue;
                }

                selectedAxis = RotationAxisState.next(selectedAxis);
                ClientPlayNetworking.send(new RotationAxisPayload(RotationAxisState.ordinal(selectedAxis)));
                client.player.displayClientMessage(
                        Component.translatable(axisMessageKey(selectedAxis)),
                        true
                );
            }
        });

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (!UltimateGlassClientConfig.isToolEnabled() && isHoldingTool(player, hand)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        ClientPreAttackCallback.EVENT.register((client, player, clickCount) ->
                !UltimateGlassClientConfig.isToolEnabled()
                        && player.getMainHandItem().is(UltimateGlassItems.GLAZIERS_TOOL)
        );
    }

    private static String axisMessageKey(Direction.Axis axis) {
        return switch (axis) {
            case X -> "message.ultimateglass.rotation_axis_x";
            case Y -> "message.ultimateglass.rotation_axis_y";
            case Z -> "message.ultimateglass.rotation_axis_z";
        };
    }

    private static boolean isHoldingTool(Player player, net.minecraft.world.InteractionHand hand) {
        return player.getItemInHand(hand).is(UltimateGlassItems.GLAZIERS_TOOL);
    }
}
