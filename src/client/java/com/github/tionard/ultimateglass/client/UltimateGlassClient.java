package com.github.tionard.ultimateglass.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import com.github.tionard.ultimateglass.UltimateGlass;
import com.github.tionard.ultimateglass.registry.UltimateGlassItems;

public final class UltimateGlassClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(UltimateGlass.MOD_ID, "general")
    );

    private static final KeyMapping TOGGLE_TOOL = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.ultimateglass.toggle_tool",
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_G,
                    CATEGORY
            )
    );

    @Override
    public void onInitializeClient() {
        UltimateGlassClientConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE_TOOL.consumeClick()) {
                UltimateGlassClientConfig.toggleToolEnabled();
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

    private static boolean isHoldingTool(Player player, net.minecraft.world.InteractionHand hand) {
        return player.getItemInHand(hand).is(UltimateGlassItems.GLAZIERS_TOOL);
    }
}
