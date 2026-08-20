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
import com.github.tionard.ultimateglass.client.render.SeamlessPaneModels;
import com.github.tionard.ultimateglass.item.GlaziersToolTier;
import com.github.tionard.ultimateglass.network.RotationAxisPayload;
import com.github.tionard.ultimateglass.network.ToolCraftingConfigPayload;
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

    private static Direction.Axis selectedAxis = RotationAxisState.DEFAULT_AXIS;
    private static Player currentPlayer;

    @Override
    public void onInitializeClient() {
        UltimateGlassClientConfig.load();
        SeamlessPaneModels.initialize();
        UltimateGlassBlocks.edgePanes().forEach(block ->
                FluidRenderingRegistry.setBlockTransparency(block, true));
        UltimateGlassBlocks.centeredPanes().forEach(block ->
                FluidRenderingRegistry.setBlockTransparency(block, true));
        FluidRenderingRegistry.setBlockTransparency(UltimateGlassBlocks.TINTED_GLASS_PANE, true);
        FluidRenderingRegistry.setBlockTransparency(UltimateGlassBlocks.COMPOSITE_PANE, true);

        ClientPlayNetworking.registerGlobalReceiver(
                ToolCraftingConfigPayload.TYPE,
                (payload, context) -> context.client().execute(() ->
                        UltimateGlassClientConfig.applyServerConfig(
                                payload.copperEnabled(),
                                payload.ironEnabled(),
                                payload.diamondEnabled(),
                                payload.experimentalCompositesEnabled()
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
            }

            while (CHANGE_ROTATION_AXIS.consumeClick()) {
                if (client.player == null || client.getConnection() == null) {
                    continue;
                }

                selectedAxis = RotationAxisState.next(selectedAxis);
                ClientPlayNetworking.send(new RotationAxisPayload(RotationAxisState.ordinal(selectedAxis)));
                client.player.sendSystemMessage(Component.translatable(axisMessageKey(selectedAxis)));
            }

        });
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
                    UltimateGlassServerConfig.experimentalCompositesEnabled(),
                    true
            );
            return;
        }

        ClientPlayNetworking.send(new ToolCraftingConfigPayload(
                UltimateGlassServerConfig.copperCraftingEnabled(),
                UltimateGlassServerConfig.ironCraftingEnabled(),
                UltimateGlassServerConfig.diamondCraftingEnabled(),
                UltimateGlassServerConfig.experimentalCompositesEnabled()
        ));
    }

    public static void requestExperimentalCompositesToggle() {
        boolean enabled = !UltimateGlassClientConfig.experimentalCompositesEnabled();
        UltimateGlassClientConfig.setExperimentalCompositesEnabledLocally(enabled);

        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() == null || client.player == null) {
            UltimateGlassServerConfig.apply(
                    UltimateGlassServerConfig.copperCraftingEnabled(),
                    UltimateGlassServerConfig.ironCraftingEnabled(),
                    UltimateGlassServerConfig.diamondCraftingEnabled(),
                    enabled,
                    true
            );
            return;
        }

        ClientPlayNetworking.send(ToolCraftingConfigPayload.current());
    }

    public static void toggleSeamlessConnectedPanes() {
        UltimateGlassClientConfig.toggleSeamlessConnectedPanes();

        Minecraft client = Minecraft.getInstance();
        if (client.level != null) {
            client.levelRenderer.invalidateCompiledGeometry(
                    client.level,
                    client.options,
                    client.gameRenderer.mainCamera(),
                    client.getBlockColors()
            );
        }
    }

    private static String axisMessageKey(Direction.Axis axis) {
        return switch (axis) {
            case X -> "message.ultimateglass.rotation_axis_x";
            case Y -> "message.ultimateglass.rotation_axis_y";
            case Z -> "message.ultimateglass.rotation_axis_z";
        };
    }

}
