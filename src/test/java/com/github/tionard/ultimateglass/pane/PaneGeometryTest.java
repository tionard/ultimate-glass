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
    void centeredGeometrySupportsEveryNonEmptyAxisSet() {
        for (int mask = 1; mask < 8; mask++) {
            Direction.Axis primary = Direction.Axis.values()[Integer.numberOfTrailingZeros(mask)];
            boolean connectFirst = (mask & (1 << PaneGeometry.firstPerpendicularAxis(primary).ordinal())) != 0;
            boolean connectSecond = (mask & (1 << PaneGeometry.secondPerpendicularAxis(primary).ordinal())) != 0;
            PaneGeometry geometry = PaneGeometry.centered(primary, connectFirst, connectSecond);

            assertEquals(Integer.bitCount(mask), geometry.planes().size());
            for (Direction.Axis axis : Direction.Axis.values()) {
                assertEquals(
                        (mask & (1 << axis.ordinal())) != 0,
                        geometry.hasCenteredPlane(axis)
                );
            }
        }
    }

    @Test
    void centeredRelativeFlagsHaveStableWorldAxisMapping() {
        assertEquals(Direction.Axis.Y, PaneGeometry.firstPerpendicularAxis(Direction.Axis.X));
        assertEquals(Direction.Axis.Z, PaneGeometry.secondPerpendicularAxis(Direction.Axis.X));
        assertEquals(Direction.Axis.X, PaneGeometry.firstPerpendicularAxis(Direction.Axis.Y));
        assertEquals(Direction.Axis.Z, PaneGeometry.secondPerpendicularAxis(Direction.Axis.Y));
        assertEquals(Direction.Axis.X, PaneGeometry.firstPerpendicularAxis(Direction.Axis.Z));
        assertEquals(Direction.Axis.Y, PaneGeometry.secondPerpendicularAxis(Direction.Axis.Z));
    }

    @Test
    void multiPlaneCenteredGeometryRotatesAsOneSet() {
        PaneGeometry xy = PaneGeometry.centered(Direction.Axis.X, true, false);
        PaneGeometry xz = xy.rotateAround(Direction.Axis.X);

        assertTrue(xz.hasCenteredPlane(Direction.Axis.X));
        assertTrue(xz.hasCenteredPlane(Direction.Axis.Z));
        assertFalse(xz.hasCenteredPlane(Direction.Axis.Y));

        PaneGeometry xyz = PaneGeometry.centered(Direction.Axis.X, true, true);
        for (Direction.Axis rotationAxis : Direction.Axis.values()) {
            assertEquals(xyz.planes(), xyz.rotateAround(rotationAxis).planes());
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
