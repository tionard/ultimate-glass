package com.github.tionard.ultimateglass.pane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.Direction;

import org.junit.jupiter.api.Test;

final class PaneGeometryTest {
    @Test
    void everyEdgeOrientationMapsItsRelativeConnectionsToPhysicalPlanes() {
        for (Direction facing : Direction.values()) {
            PaneGeometry single = PaneGeometry.edge(facing, false, false, false, false);
            assertEquals(1, single.planes().size());
            assertTrue(single.hasEdgePlane(facing));

            PaneGeometry connected = PaneGeometry.edge(facing, true, true, true, true);
            assertEquals(5, connected.planes().size());
            assertTrue(connected.hasEdgePlane(facing));
            assertTrue(connected.hasEdgePlane(PaneGeometry.localTop(facing)));
            assertTrue(connected.hasEdgePlane(PaneGeometry.localTop(facing).getOpposite()));
            assertTrue(connected.hasEdgePlane(PaneGeometry.localLeft(facing)));
            assertTrue(connected.hasEdgePlane(PaneGeometry.localLeft(facing).getOpposite()));
            assertFalse(connected.hasEdgePlane(facing.getOpposite()));
        }
    }

    @Test
    void centeredGeometrySupportsAllThreeAxes() {
        for (Direction.Axis axis : Direction.Axis.values()) {
            PaneGeometry geometry = PaneGeometry.centered(axis);
            assertEquals(1, geometry.planes().size());
            assertTrue(geometry.hasCenteredPlane(axis));
            assertFalse(geometry.shape().isEmpty());
        }
    }

    @Test
    void ordinaryBlockStateGeometriesAreCached() {
        assertSame(
                PaneGeometry.edge(Direction.DOWN, true, false, true, true),
                PaneGeometry.edge(Direction.DOWN, true, false, true, true)
        );
        assertSame(PaneGeometry.centered(Direction.Axis.Z), PaneGeometry.centered(Direction.Axis.Z));
    }

    @Test
    void fourQuarterTurnsRestoreSingleAndMultiPlaneGeometry() {
        PaneGeometry geometry = PaneGeometry.edge(Direction.NORTH, true, false, true, false);

        for (Direction.Axis axis : Direction.Axis.values()) {
            PaneGeometry rotated = geometry;
            for (int turn = 0; turn < 4; turn++) {
                rotated = rotated.rotateAround(axis);
            }
            assertEquals(geometry.planes(), rotated.planes());
        }
    }

    @Test
    void knownRotationMappingsMatchExistingToolBehavior() {
        assertEquals(
                Direction.EAST,
                PanePlane.rotateDirection(Direction.NORTH, Direction.Axis.Y)
        );
        assertEquals(
                Direction.SOUTH,
                PanePlane.rotateDirection(Direction.UP, Direction.Axis.X)
        );
        assertEquals(
                Direction.Axis.Z,
                PanePlane.rotateAxis(Direction.Axis.Y, Direction.Axis.X)
        );
        assertEquals(
                Direction.Axis.X,
                PanePlane.rotateAxis(Direction.Axis.X, Direction.Axis.X)
        );
    }
}
