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
}
