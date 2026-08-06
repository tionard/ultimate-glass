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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import com.github.tionard.ultimateglass.block.EdgePaneBlock;
import com.github.tionard.ultimateglass.registry.UltimateGlassBlocks;
import com.github.tionard.ultimateglass.rotation.RotationAxisState;

public final class GlaziersToolItem extends Item {
    private static final float GLASS_MINING_SPEED = 6.0F;

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
            return togglePanePosition(context, state, block);
        }

        if (block instanceof EdgePaneBlock) {
            if (!level.isClientSide()) {
                Direction rotated = EdgePaneBlock.rotateAround(
                        state.getValue(EdgePaneBlock.FACING),
                        RotationAxisState.get(player)
                );
                level.setBlockAndUpdate(
                        context.getClickedPos(),
                        state.setValue(EdgePaneBlock.FACING, rotated)
                );
                EdgePaneBlock.refreshConnectionsAround(level, context.getClickedPos());
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return collectedStack(state.getBlock()).isEmpty() ? 1.0F : GLASS_MINING_SPEED;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return !collectedStack(state.getBlock()).isEmpty() || super.isCorrectToolForDrops(stack, state);
    }

    private static InteractionResult togglePanePosition(
            UseOnContext context,
            BlockState state,
            Block block
    ) {
        Level level = context.getLevel();
        Block vanillaPane = UltimateGlassBlocks.vanillaFor(block);

        if (vanillaPane != null) {
            if (!level.isClientSide()) {
                BlockState centered = vanillaPane.defaultBlockState();
                if (centered.hasProperty(BlockStateProperties.WATERLOGGED)
                        && state.getValue(EdgePaneBlock.WATERLOGGED)) {
                    centered = centered.setValue(BlockStateProperties.WATERLOGGED, true);
                }
                level.setBlockAndUpdate(context.getClickedPos(), centered);
                EdgePaneBlock.refreshConnectionsAround(level, context.getClickedPos());
            }
            return InteractionResult.SUCCESS;
        }

        EdgePaneBlock edgePane = UltimateGlassBlocks.edgeFor(block);
        if (edgePane == null) {
            return InteractionResult.PASS;
        }

        boolean waterlogged = state.hasProperty(BlockStateProperties.WATERLOGGED)
                && state.getValue(BlockStateProperties.WATERLOGGED);

        if (!level.isClientSide()) {
            level.setBlockAndUpdate(
                    context.getClickedPos(),
                    edgePane.defaultBlockState()
                            .setValue(EdgePaneBlock.FACING, context.getClickedFace())
                            .setValue(EdgePaneBlock.WATERLOGGED, waterlogged)
            );
            EdgePaneBlock.refreshConnectionsAround(level, context.getClickedPos());
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
