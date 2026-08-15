package com.github.tionard.ultimateglass.interaction;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.github.tionard.ultimateglass.block.EdgePaneBlock;
import com.github.tionard.ultimateglass.block.CenteredPaneBlock;
import com.github.tionard.ultimateglass.item.GlaziersToolItem;
import com.github.tionard.ultimateglass.block.entity.DynamicFrameBlockEntity;
import com.github.tionard.ultimateglass.registry.UltimateGlassComponents;

public final class UltimateGlassInteractions {
    private UltimateGlassInteractions() {
    }

    public static void initialize() {
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (state.getBlock() instanceof EdgePaneBlock
                    || state.getBlock() instanceof CenteredPaneBlock) {
                EdgePaneBlock.refreshConnectionsAround(level, pos);
                CenteredPaneBlock.refreshConnectionsAround(level, pos);
            }

            if (player.getAbilities().instabuild
                    || !(player.getMainHandItem().getItem() instanceof GlaziersToolItem tool)
                    || !tool.tier().silkTouchesGlass()) {
                return;
            }

            ItemStack drop = GlaziersToolItem.collectedStack(state.getBlock());
            if (blockEntity instanceof DynamicFrameBlockEntity frame && !drop.isEmpty()) {
                drop.set(UltimateGlassComponents.FRAME_BLOCK, frame.frameBlockId());
            }
            if (!drop.isEmpty()) {
                Block.popResource(level, pos, drop);
            }
        });
    }
}
