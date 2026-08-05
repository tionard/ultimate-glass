package com.github.tionard.ultimateglass.item;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.github.tionard.ultimateglass.block.EdgePaneBlock;
import com.github.tionard.ultimateglass.registry.UltimateGlassBlocks;

public final class GlaziersToolItem extends Item {
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
            return togglePanePosition(context, state, block, player);
        }

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

    private static InteractionResult togglePanePosition(
            UseOnContext context,
            BlockState state,
            Block block,
            Player player
    ) {
        Level level = context.getLevel();
        Block vanillaPane = UltimateGlassBlocks.vanillaFor(block);

        if (vanillaPane != null) {
            if (!level.isClientSide()) {
                level.setBlockAndUpdate(context.getClickedPos(), vanillaPane.defaultBlockState());
            }
            return InteractionResult.SUCCESS;
        }

        EdgePaneBlock edgePane = UltimateGlassBlocks.edgeFor(block);
        if (edgePane == null) {
            return InteractionResult.PASS;
        }

        Direction facing = context.getClickedFace().getAxis().isHorizontal()
                ? context.getClickedFace()
                : player.getDirection().getOpposite();

        if (!level.isClientSide()) {
            level.setBlockAndUpdate(
                    context.getClickedPos(),
                    edgePane.defaultBlockState().setValue(EdgePaneBlock.FACING, facing)
            );
        }

        return InteractionResult.SUCCESS;
    }

    public static ItemStack collectedStack(Block block) {
        Block vanillaPane = UltimateGlassBlocks.vanillaFor(block);
        if (vanillaPane != null) {
            return new ItemStack(vanillaPane.asItem());
        }

        if (UltimateGlassBlocks.edgeFor(block) != null || isVanillaGlassBlock(block)) {
            return new ItemStack(block.asItem());
        }

        return ItemStack.EMPTY;
    }

    private static boolean isVanillaGlassBlock(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        if (!"minecraft".equals(id.getNamespace())) {
            return false;
        }

        String path = id.getPath();
        return "glass".equals(path) || path.endsWith("_stained_glass");
    }
}
