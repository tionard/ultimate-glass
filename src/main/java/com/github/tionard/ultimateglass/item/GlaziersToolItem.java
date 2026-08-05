package com.github.tionard.ultimateglass.item;

import java.util.Set;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import com.github.tionard.ultimateglass.block.EdgePaneBlock;
import com.github.tionard.ultimateglass.registry.UltimateGlassBlocks;

public final class GlaziersToolItem extends Item {
    private static final Set<Block> GLASS_BLOCKS = Set.of(
            Blocks.GLASS,
            Blocks.WHITE_STAINED_GLASS,
            Blocks.ORANGE_STAINED_GLASS,
            Blocks.MAGENTA_STAINED_GLASS,
            Blocks.LIGHT_BLUE_STAINED_GLASS,
            Blocks.YELLOW_STAINED_GLASS,
            Blocks.LIME_STAINED_GLASS,
            Blocks.PINK_STAINED_GLASS,
            Blocks.GRAY_STAINED_GLASS,
            Blocks.LIGHT_GRAY_STAINED_GLASS,
            Blocks.CYAN_STAINED_GLASS,
            Blocks.PURPLE_STAINED_GLASS,
            Blocks.BLUE_STAINED_GLASS,
            Blocks.BROWN_STAINED_GLASS,
            Blocks.GREEN_STAINED_GLASS,
            Blocks.RED_STAINED_GLASS,
            Blocks.BLACK_STAINED_GLASS
    );

    public GlaziersToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isSpectator() || !player.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockState state = level.getBlockState(context.getClickedPos());
        Block block = state.getBlock();

        if (player.isShiftKeyDown()) {
            if (block instanceof EdgePaneBlock) {
                if (!level.isClientSide()) {
                    level.setBlockAndUpdate(
                            context.getClickedPos(),
                            state.setValue(
                                    EdgePaneBlock.FACING,
                                    EdgePaneBlock.rotateClockwise(state.getValue(EdgePaneBlock.FACING))
                            )
                    );
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        ItemStack collected = collectedStack(block);
        if (collected.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            level.setBlockAndUpdate(context.getClickedPos(), Blocks.AIR.defaultBlockState());
            if (!player.addItem(collected)) {
                player.drop(collected, false);
            }
        }

        return InteractionResult.SUCCESS;
    }

    private static ItemStack collectedStack(Block block) {
        Block vanillaPane = UltimateGlassBlocks.vanillaFor(block);
        if (vanillaPane != null) {
            return new ItemStack(vanillaPane.asItem());
        }

        if (UltimateGlassBlocks.edgeFor(block) != null || GLASS_BLOCKS.contains(block)) {
            return new ItemStack(block.asItem());
        }

        return ItemStack.EMPTY;
    }
}
