package com.github.tionard.ultimateglass.interaction;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import com.github.tionard.ultimateglass.item.GlaziersToolItem;
import com.github.tionard.ultimateglass.registry.UltimateGlassItems;

public final class UltimateGlassInteractions {
    private UltimateGlassInteractions() {
    }

    public static void initialize() {
        AttackBlockCallback.EVENT.register((player, level, hand, pos, clickedFace) -> {
            if (player.isSpectator()
                    || !player.getAbilities().mayBuild
                    || !player.getItemInHand(hand).is(UltimateGlassItems.GLAZIERS_TOOL)) {
                return InteractionResult.PASS;
            }

            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();
            ItemStack collected = GlaziersToolItem.collectedStack(block);

            if (collected.isEmpty()) {
                return InteractionResult.PASS;
            }

            if (!level.isClientSide()) {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                if (!player.addItem(collected)) {
                    player.drop(collected, false);
                }
            }

            return InteractionResult.SUCCESS;
        });
    }
}
