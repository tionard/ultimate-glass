package com.github.tionard.ultimateglass.block;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
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
import com.github.tionard.ultimateglass.glass.GlassVariant;
import com.github.tionard.ultimateglass.registry.UltimateGlassComponents;
import com.github.tionard.ultimateglass.registry.UltimateGlassSmartItems;

/** Data-backed full framed block used for every non-vanilla plank. */
public final class DynamicFramedGlassBlock extends FramedGlassBlock
        implements EntityBlock, DynamicFramedBlock {
    public DynamicFramedGlassBlock(GlassVariant variant, Properties properties) {
        super(variant, properties);
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
        Identifier frameId = stack.getOrDefault(
                UltimateGlassComponents.FRAME_BLOCK,
                DynamicFrameBlockEntity.DEFAULT_FRAME
        );
        if (level.getBlockEntity(pos) instanceof DynamicFrameBlockEntity frame) {
            frame.setFrameBlockId(frameId);
        }
        refreshModelsAround(level, pos);
    }

    @Override
    protected ItemStack getCloneItemStack(
            LevelReader level, BlockPos pos, BlockState state, boolean includeData
    ) {
        ItemStack stack = new ItemStack(asItem());
        UltimateGlassSmartItems.applyComponents(this, stack);
        copyFrame(level, pos, stack);
        return stack;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        drops.forEach(stack -> UltimateGlassSmartItems.applyComponents(this, stack));
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof DynamicFrameBlockEntity frame) {
            drops.forEach(stack -> stack.set(
                    UltimateGlassComponents.FRAME_BLOCK,
                    frame.frameBlockId()
            ));
        }
        return drops;
    }

    private static void copyFrame(BlockGetter level, BlockPos pos, ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof DynamicFrameBlockEntity frame) {
            stack.set(UltimateGlassComponents.FRAME_BLOCK, frame.frameBlockId());
        }
    }
}
