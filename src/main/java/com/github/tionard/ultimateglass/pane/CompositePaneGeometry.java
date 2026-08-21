package com.github.tionard.ultimateglass.pane;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Boolean clipping shared by composite collision, outline, and regression tests. */
public final class CompositePaneGeometry {
    private CompositePaneGeometry() {
    }

    public static VoxelShape exposedPaneShape(
            VoxelShape hostShape,
            Direction paneFacing
    ) {
        return exposedPaneShape(hostShape, paneFacing, false);
    }

    public static VoxelShape exposedPaneShape(
            VoxelShape hostShape,
            Direction paneFacing,
            boolean centered
    ) {
        VoxelShape paneShape = centered
                ? PanePlane.centered(paneFacing.getAxis()).shape()
                : PanePlane.edge(paneFacing).shape();
        return Shapes.joinUnoptimized(paneShape, hostShape, BooleanOp.ONLY_FIRST).optimize();
    }

    /** Advances clockwise while skipping pane positions completely hidden by the host. */
    public static Direction nextAvailableFacing(
            VoxelShape hostShape,
            Direction currentFacing,
            boolean centered
    ) {
        if (currentFacing.getAxis() == Direction.Axis.Y) {
            throw new IllegalArgumentException("Composite stair/slab panes must be vertical");
        }

        if (centered) {
            Direction candidate = currentFacing.getAxis() == Direction.Axis.X
                    ? Direction.NORTH
                    : Direction.WEST;
            return exposedPaneShape(hostShape, candidate, true).isEmpty()
                    ? currentFacing
                    : candidate;
        }

        Direction candidate = currentFacing;
        for (int step = 0; step < 3; step++) {
            candidate = clockwise(candidate);
            if (!exposedPaneShape(hostShape, candidate, false).isEmpty()) {
                return candidate;
            }
        }
        return currentFacing;
    }

    private static Direction clockwise(Direction direction) {
        return switch (direction) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            case UP, DOWN -> throw new IllegalArgumentException(
                    "Composite stair/slab panes must be vertical"
            );
        };
    }
}
