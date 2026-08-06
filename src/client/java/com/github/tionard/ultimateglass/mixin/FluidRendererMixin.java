package com.github.tionard.ultimateglass.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import com.github.tionard.ultimateglass.block.EdgePaneBlock;

/** Keeps waterlogged edge-pane water inside the pane planes during chunk rendering. */
@Mixin(FluidRenderer.class)
abstract class FluidRendererMixin {
    @Unique
    private static final float ULTIMATE_GLASS$PANE_THICKNESS = 2.0F / 16.0F;
    @Unique
    private static final float ULTIMATE_GLASS$RENDER_INSET = 0.001F;
    @Unique
    private static final ThreadLocal<RenderContext> ULTIMATE_GLASS$CONTEXT = new ThreadLocal<>();

    @Inject(method = "tesselate", at = @At("HEAD"))
    private void ultimateGlass$beginPaneFluidRender(
            BlockAndTintGetter level,
            BlockPos pos,
            FluidRenderer.Output output,
            BlockState blockState,
            FluidState fluidState,
            CallbackInfo callback
    ) {
        if (!(blockState.getBlock() instanceof EdgePaneBlock)
                || !blockState.getValue(EdgePaneBlock.WATERLOGGED)) {
            return;
        }

        float minX = EdgePaneBlock.hasPaneOnFace(blockState, Direction.WEST)
                ? ULTIMATE_GLASS$PANE_THICKNESS + ULTIMATE_GLASS$RENDER_INSET
                : 0.0F;
        float maxX = EdgePaneBlock.hasPaneOnFace(blockState, Direction.EAST)
                ? 1.0F - ULTIMATE_GLASS$PANE_THICKNESS - ULTIMATE_GLASS$RENDER_INSET
                : 1.0F;
        float minY = EdgePaneBlock.hasPaneOnFace(blockState, Direction.DOWN)
                ? ULTIMATE_GLASS$PANE_THICKNESS + ULTIMATE_GLASS$RENDER_INSET
                : 0.0F;
        float maxY = EdgePaneBlock.hasPaneOnFace(blockState, Direction.UP)
                ? 1.0F - ULTIMATE_GLASS$PANE_THICKNESS - ULTIMATE_GLASS$RENDER_INSET
                : 1.0F;
        float minZ = EdgePaneBlock.hasPaneOnFace(blockState, Direction.NORTH)
                ? ULTIMATE_GLASS$PANE_THICKNESS + ULTIMATE_GLASS$RENDER_INSET
                : 0.0F;
        float maxZ = EdgePaneBlock.hasPaneOnFace(blockState, Direction.SOUTH)
                ? 1.0F - ULTIMATE_GLASS$PANE_THICKNESS - ULTIMATE_GLASS$RENDER_INSET
                : 1.0F;

        float flatSurface = Float.NaN;
        if (ultimateGlass$isContainedSource(level, pos, blockState, fluidState)) {
            flatSurface = Math.min(
                    fluidState.getHeight(level, pos) - ULTIMATE_GLASS$RENDER_INSET,
                    maxY
            );
        }

        ULTIMATE_GLASS$CONTEXT.set(new RenderContext(
                level,
                pos.immutable(),
                blockState,
                fluidState,
                minX,
                maxX,
                minY,
                maxY,
                minZ,
                maxZ,
                flatSurface
        ));
    }

    @Inject(method = "tesselate", at = @At("RETURN"))
    private void ultimateGlass$endPaneFluidRender(
            BlockAndTintGetter level,
            BlockPos pos,
            FluidRenderer.Output output,
            BlockState blockState,
            FluidState fluidState,
            CallbackInfo callback
    ) {
        ULTIMATE_GLASS$CONTEXT.remove();
    }

    @Redirect(
            method = "tesselate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;getFlow(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private Vec3 ultimateGlass$ignoreFlowThroughPaneFaces(
            FluidState fluidState,
            BlockGetter level,
            BlockPos pos
    ) {
        RenderContext context = ULTIMATE_GLASS$CONTEXT.get();
        if (context == null) {
            return fluidState.getFlow(level, pos);
        }
        return fluidState.getFlow(new PaneBoundedFluidGetter(context), pos);
    }

    @ModifyArgs(
            method = "vertex",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;addVertex(FFFIFFIIFFF)V"
            )
    )
    private void ultimateGlass$clipPaneFluidVertex(Args args) {
        RenderContext context = ULTIMATE_GLASS$CONTEXT.get();
        if (context == null) {
            return;
        }

        float sectionX = context.pos().getX() & 15;
        float sectionY = context.pos().getY() & 15;
        float sectionZ = context.pos().getZ() & 15;
        float x = args.get(0);
        float y = args.get(1);
        float z = args.get(2);

        x = sectionX + ultimateGlass$clamp(x - sectionX, context.minX(), context.maxX());
        z = sectionZ + ultimateGlass$clamp(z - sectionZ, context.minZ(), context.maxZ());

        float localY = y - sectionY;
        if (!Float.isNaN(context.flatSurface()) && localY > 0.5F) {
            localY = context.flatSurface();
        }
        y = sectionY + ultimateGlass$clamp(localY, context.minY(), context.maxY());

        args.set(0, x);
        args.set(1, y);
        args.set(2, z);
    }

    @Unique
    private static boolean ultimateGlass$isContainedSource(
            BlockGetter level,
            BlockPos pos,
            BlockState blockState,
            FluidState fluidState
    ) {
        if (!fluidState.isSource()) {
            return false;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (EdgePaneBlock.hasPaneOnFace(blockState, direction)) {
                continue;
            }
            FluidState neighbor = level.getFluidState(pos.relative(direction));
            if (!fluidState.getType().isSame(neighbor.getType())) {
                return false;
            }
        }
        return true;
    }

    @Unique
    private static float ultimateGlass$clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    @Unique
    private record RenderContext(
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState blockState,
            FluidState fluidState,
            float minX,
            float maxX,
            float minY,
            float maxY,
            float minZ,
            float maxZ,
            float flatSurface
    ) {
    }

    @Unique
    private record PaneBoundedFluidGetter(RenderContext context) implements BlockGetter {
        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            return context.level().getBlockEntity(pos);
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return context.level().getBlockState(pos);
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (EdgePaneBlock.hasPaneOnFace(context.blockState(), direction)
                        && pos.equals(context.pos().relative(direction))) {
                    return context.fluidState();
                }
            }
            return context.level().getFluidState(pos);
        }

        @Override
        public int getHeight() {
            return context.level().getHeight();
        }

        @Override
        public int getMinY() {
            return context.level().getMinY();
        }
    }
}
