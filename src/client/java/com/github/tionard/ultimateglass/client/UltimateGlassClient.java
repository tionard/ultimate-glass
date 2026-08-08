package com.github.tionard.ultimateglass.client;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;

import com.github.tionard.ultimateglass.UltimateGlass;
import com.github.tionard.ultimateglass.config.UltimateGlassServerConfig;
import com.github.tionard.ultimateglass.item.GlaziersToolTier;
import com.github.tionard.ultimateglass.network.RotationAxisPayload;
import com.github.tionard.ultimateglass.network.ShiftPlacementModePayload;
import com.github.tionard.ultimateglass.network.ToolCraftingConfigPayload;
import com.github.tionard.ultimateglass.placement.ShiftPlacementMode;
import com.github.tionard.ultimateglass.registry.UltimateGlassBlocks;
import com.github.tionard.ultimateglass.rotation.RotationAxisState;

public final class UltimateGlassClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(UltimateGlass.MOD_ID, "general")
    );

    private static final KeyMapping CHANGE_ROTATION_AXIS = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.ultimateglass.change_rotation_axis",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_V,
                    CATEGORY
            )
    );

    private static final KeyMapping TOGGLE_SHIFT_PLACEMENT_MODE = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.ultimateglass.toggle_shift_placement_mode",
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
        UltimateGlassBlocks.edgePanes().forEach(block ->
                FluidRenderingRegistry.setBlockTransparency(block, true));
        UltimateGlassBlocks.centeredPanes().forEach(block ->
                FluidRenderingRegistry.setBlockTransparency(block, true));

        ClientPlayNetworking.registerGlobalReceiver(
                ToolCraftingConfigPayload.TYPE,
                (payload, context) -> context.client().execute(() ->
                        UltimateGlassClientConfig.applyServerCraftingConfig(
                                payload.copperEnabled(),
                                payload.ironEnabled(),
                                payload.diamondEnabled()
                        )
                )
        );

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                UltimateGlassServerConfig.load()
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != currentPlayer) {
                currentPlayer = client.player;
                selectedAxis = RotationAxisState.DEFAULT_AXIS;
                if (currentPlayer != null && client.getConnection() != null) {
                    syncShiftPlacementMode();
                }
            }

            while (CHANGE_ROTATION_AXIS.consumeClick()) {
                if (client.player == null || client.getConnection() == null) {
                    continue;
                }

                selectedAxis = RotationAxisState.next(selectedAxis);
                ClientPlayNetworking.send(new RotationAxisPayload(RotationAxisState.ordinal(selectedAxis)));
                client.player.sendSystemMessage(Component.translatable(axisMessageKey(selectedAxis)));
            }

            while (TOGGLE_SHIFT_PLACEMENT_MODE.consumeClick()) {
                if (client.player == null || client.getConnection() == null) {
                    continue;
                }

                ShiftPlacementMode mode = UltimateGlassClientConfig.toggleShiftPlacementMode();
                syncShiftPlacementMode();
                client.player.sendSystemMessage(Component.translatable(shiftModeMessageKey(mode)));
            }
        });
    }

    public static void syncShiftPlacementMode() {
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() == null) {
            return;
        }

        ClientPlayNetworking.send(new ShiftPlacementModePayload(
                UltimateGlassClientConfig.shiftPlacementMode().ordinal()
        ));
    }

    public static void requestCraftingToggle(GlaziersToolTier tier) {
        boolean enabled = !UltimateGlassClientConfig.isCraftingEnabled(tier);
        UltimateGlassClientConfig.setCraftingEnabledLocally(tier, enabled);

        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() == null || client.player == null) {
            UltimateGlassServerConfig.apply(
                    UltimateGlassServerConfig.copperCraftingEnabled(),
                    UltimateGlassServerConfig.ironCraftingEnabled(),
                    UltimateGlassServerConfig.diamondCraftingEnabled(),
                    true
            );
            return;
        }

        ClientPlayNetworking.send(new ToolCraftingConfigPayload(
                UltimateGlassServerConfig.copperCraftingEnabled(),
                UltimateGlassServerConfig.ironCraftingEnabled(),
                UltimateGlassServerConfig.diamondCraftingEnabled()
        ));
    }

    private static String axisMessageKey(Direction.Axis axis) {
        return switch (axis) {
            case X -> "message.ultimateglass.rotation_axis_x";
            case Y -> "message.ultimateglass.rotation_axis_y";
            case Z -> "message.ultimateglass.rotation_axis_z";
        };
    }

    private static String shiftModeMessageKey(ShiftPlacementMode mode) {
        return switch (mode) {
            case FACE -> "message.ultimateglass.shift_mode_face";
            case NEAR -> "message.ultimateglass.shift_mode_near";
        };
    }
}
