package com.github.tionard.ultimateglass.interaction;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.github.tionard.ultimateglass.block.EdgePaneBlock;
import com.github.tionard.ultimateglass.registry.UltimateGlassBlocks;
import com.github.tionard.ultimateglass.registry.UltimateGlassItems;

public final class UltimateGlassInteractions {
    private UltimateGlassInteractions() {
    }

    public static void initialize() {
        AttackBlockCallback.EVENT.register((player, level, hand, pos, clickedFace) -> {
            if (!player.isShiftKeyDown()
                    || player.isSpectator()
                    || !player.getAbilities().mayBuild
                    || !player.getItemInHand(hand).is(UltimateGlassItems.GLAZIERS_TOOL)) {
                return InteractionResult.PASS;
            }

            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();
            Block vanillaPane = UltimateGlassBlocks.vanillaFor(block);

            if (vanillaPane != null) {
                if (!level.isClientSide()) {
                    level.setBlockAndUpdate(pos, vanillaPane.defaultBlockState());
                }
                return InteractionResult.SUCCESS;
            }

            EdgePaneBlock edgePane = UltimateGlassBlocks.edgeFor(block);
            if (edgePane == null) {
                return InteractionResult.PASS;
            }

            Direction facing = clickedFace.getAxis().isHorizontal()
                    ? clickedFace
                    : player.getDirection().getOpposite();

            if (!level.isClientSide()) {
                level.setBlockAndUpdate(
                        pos,
                        edgePane.defaultBlockState().setValue(EdgePaneBlock.FACING, facing)
                );
            }

            return InteractionResult.SUCCESS;
        });
    }
}
