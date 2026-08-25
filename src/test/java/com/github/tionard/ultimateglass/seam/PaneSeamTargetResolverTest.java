package com.github.tionard.ultimateglass.seam;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import com.github.tionard.ultimateglass.pane.PaneGeometry;
import com.github.tionard.ultimateglass.pane.PanePlane;

final class PaneSeamTargetResolverTest {
    @Test
    void broadFaceClickSelectsItsNearestPaneEdge() {
        PaneSeamTarget target = PaneSeamTargetResolver.resolve(
                PaneGeometry.edge(Direction.NORTH, false, false, false, false),
                new BlockPos(10, 64, -3),
                new Vec3(10.04, 64.55, -2.99),
                Direction.SOUTH
        );

        assertEquals(PanePlane.EDGE_NORTH, target.plane());
        assertEquals(Direction.WEST, target.boundary());
    }

    @Test
    void connectedCornerUsesTheActuallyClickedPlane() {
        PaneGeometry corner = PaneGeometry.edge(
                Direction.NORTH, true, false, false, false
        );
        PaneSeamTarget target = PaneSeamTargetResolver.resolve(
                corner,
                BlockPos.ZERO,
                new Vec3(0.55, 0.99, 0.97),
                Direction.DOWN
        );

        assertEquals(PanePlane.EDGE_UP, target.plane());
        assertEquals(Direction.SOUTH, target.boundary());
    }

    @Test
    void centeredSheetSelectsAWorldBoundaryIndependently() {
        PaneSeamTarget target = PaneSeamTargetResolver.resolve(
                PaneGeometry.centered(Direction.Axis.X),
                BlockPos.ZERO,
                new Vec3(0.5, 0.45, 0.97),
                Direction.EAST
        );

        assertEquals(PanePlane.CENTER_X, target.plane());
        assertEquals(Direction.SOUTH, target.boundary());
    }
}
