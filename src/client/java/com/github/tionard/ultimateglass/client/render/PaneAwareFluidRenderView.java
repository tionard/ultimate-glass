package com.github.tionard.ultimateglass.client.render;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

import com.github.tionard.ultimateglass.block.EdgePaneBlock;

/**
 * Presents each horizontal pane wall as vanilla glass to the fluid renderer. This selects
 * Minecraft's non-falling water overlay on that side while leaving genuinely open sides
 * and all lighting/tint data untouched.
 */
public record PaneAwareFluidRenderView(
        BlockAndTintGetter delegate,
        BlockPos panePos,
        BlockState paneState
) implements BlockAndTintGetter {
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return delegate.getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return isPaneBoundary(pos)
                ? Blocks.GLASS.defaultBlockState()
                : delegate.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return isPaneBoundary(pos)
                ? Blocks.GLASS.defaultBlockState().getFluidState()
                : delegate.getFluidState(pos);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return delegate.getLightEngine();
    }

    @Override
    public int getBrightness(LightLayer layer, BlockPos pos) {
        return delegate.getBrightness(layer, pos);
    }

    @Override
    public int getRawBrightness(BlockPos pos, int skyDarken) {
        return delegate.getRawBrightness(pos, skyDarken);
    }

    @Override
    public boolean canSeeSky(BlockPos pos) {
        return delegate.canSeeSky(pos);
    }

    @Override
    public CardinalLighting cardinalLighting() {
        return delegate.cardinalLighting();
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver resolver) {
        return delegate.getBlockTint(pos, resolver);
    }

    @Override
    public int getHeight() {
        return delegate.getHeight();
    }

    @Override
    public int getMinY() {
        return delegate.getMinY();
    }

    private boolean isPaneBoundary(BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (EdgePaneBlock.hasPaneOnFace(paneState, direction)
                    && pos.equals(panePos.relative(direction))) {
                return true;
            }
        }
        return false;
    }
}
