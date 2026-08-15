package com.github.tionard.ultimateglass.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import com.github.tionard.ultimateglass.block.entity.DynamicFrameBlockEntity;
import com.github.tionard.ultimateglass.pane.PaneAppearance;
import com.github.tionard.ultimateglass.registry.UltimateGlassComponents;

public final class DynamicFramedCenteredPaneBlock extends CenteredPaneBlock
        implements EntityBlock, DynamicFramedPane {
    public DynamicFramedCenteredPaneBlock(
            net.minecraft.world.level.block.Block vanillaPane,
            PaneAppearance appearance,
            Properties properties
    ) {
        super(vanillaPane, appearance, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DynamicFrameBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(
            Level level, BlockPos pos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack
    ) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof DynamicFrameBlockEntity frame) {
            frame.setFrameBlockId(stack.getOrDefault(
                    UltimateGlassComponents.FRAME_BLOCK,
                    DynamicFrameBlockEntity.DEFAULT_FRAME
            ));
        }
        EdgePaneBlock.refreshConnectionsAround(level, pos);
        CenteredPaneBlock.refreshConnectionsAround(level, pos);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack stack = new ItemStack(asItem());
        copyFrame(level, pos, stack);
        return stack;
    }

    @Override
    protected java.util.List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        java.util.List<ItemStack> drops = super.getDrops(state, builder);
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof DynamicFrameBlockEntity frame) {
            drops.forEach(stack -> stack.set(UltimateGlassComponents.FRAME_BLOCK, frame.frameBlockId()));
        }
        return drops;
    }

    private static void copyFrame(BlockGetter level, BlockPos pos, ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof DynamicFrameBlockEntity frame) {
            stack.set(UltimateGlassComponents.FRAME_BLOCK, frame.frameBlockId());
        }
    }
}
