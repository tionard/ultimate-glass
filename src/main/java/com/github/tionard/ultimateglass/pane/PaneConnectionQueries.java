package com.github.tionard.ultimateglass.pane;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import com.github.tionard.ultimateglass.block.EdgePaneBlock;
import com.github.tionard.ultimateglass.block.CenteredPaneBlock;
import com.github.tionard.ultimateglass.block.DynamicFramedPane;
import com.github.tionard.ultimateglass.block.entity.DynamicFrameBlockEntity;

/** Stateless connection lookups shared by block-state and rendering code. */
public final class PaneConnectionQueries {
    private PaneConnectionQueries() {
    }

    /**
     * Follows a convex outside corner away from an edge pane. The first adjacent pane creates an
     * L shape; a second pane one step farther around the same corner can create a cube corner.
     */
    public static boolean hasOuterEdgeConnection(
            BlockGetter level,
            BlockPos pos,
            Direction facing,
            Direction wingFacing
    ) {
        BlockPos firstPos = pos.relative(facing.getOpposite());
        BlockState firstState = level.getBlockState(firstPos);
        if (!(firstState.getBlock() instanceof EdgePaneBlock)) {
            return false;
        }

        Direction firstFacing = firstState.getValue(EdgePaneBlock.FACING);
        if (firstFacing == wingFacing) {
            return true;
        }

        if (firstFacing.getAxis() == facing.getAxis()
                || wingFacing.getAxis() == facing.getAxis()
                || firstFacing.getAxis() == wingFacing.getAxis()) {
            return false;
        }

        BlockPos secondPos = firstPos.relative(firstFacing.getOpposite());
        BlockState secondState = level.getBlockState(secondPos);
        return secondState.getBlock() instanceof EdgePaneBlock
                && secondState.getValue(EdgePaneBlock.FACING) == wingFacing;
    }

    /** Exact-family continuation used by seamless rendering. */
    public static boolean hasMatchingContinuation(
            BlockGetter level,
            BlockPos pos,
            BlockState state,
            Direction neighborDirection,
            PanePlane plane
    ) {
        if (neighborDirection.getAxis() == plane.axis()) {
            return false;
        }

        BlockPos neighborPos = pos.relative(neighborDirection);
        BlockState neighbor = level.getBlockState(neighborPos);
        return samePaneVariant(level, pos, state, neighborPos, neighbor)
                && neighbor.getBlock() instanceof UltimatePane pane
                && pane.geometry(neighbor).planes().contains(plane);
    }

    /**
     * A derived centered plane is sourced only by a directly adjacent pane whose primary AXIS is
     * that plane. Derived flags are deliberately ignored so connections cannot sustain themselves.
     */
    public static boolean hasCenteredConnection(
            BlockGetter level,
            BlockPos pos,
            BlockState state,
            Direction.Axis requestedAxis
    ) {
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() == requestedAxis) {
                continue;
            }

            BlockPos neighborPos = pos.relative(direction);
            BlockState neighbor = level.getBlockState(neighborPos);
            if (samePaneVariant(level, pos, state, neighborPos, neighbor)
                    && neighbor.getBlock() instanceof CenteredPaneBlock
                    && neighbor.getValue(CenteredPaneBlock.AXIS) == requestedAxis) {
                return true;
            }
        }
        return false;
    }

    public static boolean samePaneVariant(
            BlockGetter level,
            BlockPos firstPos,
            BlockState first,
            BlockPos secondPos,
            BlockState second
    ) {
        if (first.getBlock() != second.getBlock()) {
            return false;
        }
        if (!(first.getBlock() instanceof DynamicFramedPane)) {
            return true;
        }
        if (!(level.getBlockEntity(firstPos) instanceof DynamicFrameBlockEntity firstFrame)
                || !(level.getBlockEntity(secondPos) instanceof DynamicFrameBlockEntity secondFrame)) {
            return false;
        }
        return firstFrame.frameBlockId().equals(secondFrame.frameBlockId());
    }
}
