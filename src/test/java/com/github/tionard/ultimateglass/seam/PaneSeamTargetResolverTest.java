package com.github.tionard.ultimateglass.seam;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import com.github.tionard.ultimateglass.pane.PaneGeometry;
import com.github.tionard.ultimateglass.pane.PanePlane;
import com.github.tionard.ultimateglass.pane.PanePlaneSet;

final class PaneSeamTargetResolverTest {
    @Test
    void everyParallelSheetCanBeSelectedFromBothSidesAlongEveryBoundary() {
        BlockPos pos = new BlockPos(-21, 67, 38);
        for (Direction face : Direction.values()) {
            PaneGeometry pair = PaneGeometry.edge(face, false, false, false, false, true);
            for (Direction boundary : Direction.values()) {
                if (boundary.getAxis() == face.getAxis()) {
                    continue;
                }
                for (boolean inside : new boolean[] {false, true}) {
                    assertEquals(new PaneSeamTarget(PanePlane.edge(face), boundary),
                            PaneSeamTargetResolver.resolve(pair, pos, hit(pos, face, boundary, inside),
                                    inside ? face.getOpposite() : face));
                }
            }
        }
    }

    @Test
    void everyInstalledSheetRemainsTargetableInAll63Assemblies() {
        for (int mask = 1; mask < 64; mask++) {
            PanePlaneSet planes = PanePlaneSet.EMPTY;
            for (Direction face : Direction.values()) {
                if ((mask & (1 << face.ordinal())) != 0) {
                    planes = planes.plus(PanePlane.edge(face));
                }
            }
            PaneGeometry geometry = PaneGeometry.of(planes);
            for (PanePlane plane : planes) {
                Direction face = plane.edgeDirection();
                for (Direction boundary : Direction.values()) {
                    if (boundary.getAxis() != face.getAxis()) {
                        for (boolean inside : new boolean[] {false, true}) {
                            assertEquals(new PaneSeamTarget(plane, boundary),
                                    PaneSeamTargetResolver.resolve(geometry, BlockPos.ZERO,
                                            hit(BlockPos.ZERO, face, boundary, inside),
                                            inside ? face.getOpposite() : face));
                        }
                    }
                }
            }
        }
    }

    @Test
    void thinRimClickDoesNotSelectADistantPerpendicularSheet() {
        PaneGeometry geometry = PaneGeometry.edge(Direction.NORTH, true, false, false, false, true);
        for (Direction face : new Direction[] {Direction.NORTH, Direction.SOUTH}) {
            double z = face == Direction.NORTH ? 1.0 / 16.0 : 15.0 / 16.0;
            assertEquals(new PaneSeamTarget(PanePlane.edge(face), Direction.DOWN),
                    PaneSeamTargetResolver.resolve(geometry, BlockPos.ZERO,
                            new Vec3(0.5, 0.0, z), Direction.DOWN));
        }
    }

    @Test
    void editingOneParallelBoundaryLeavesTheOtherSheetIndependent() {
        PaneGeometry pair = PaneGeometry.edge(Direction.NORTH, false, false, false, false, true);
        PaneSeamData seams = new PaneSeamData();
        var north = PaneSeamTargetResolver.resolve(pair, BlockPos.ZERO,
                hit(BlockPos.ZERO, Direction.NORTH, Direction.UP, true), Direction.SOUTH);
        var south = PaneSeamTargetResolver.resolve(pair, BlockPos.ZERO,
                hit(BlockPos.ZERO, Direction.SOUTH, Direction.UP, true), Direction.NORTH);
        seams.set(north.plane(), north.boundary(), PaneSeamOverride.SEAMLESS);
        assertEquals(PaneSeamOverride.AUTOMATIC, seams.get(south.plane(), south.boundary()));
        seams.set(south.plane(), south.boundary(), PaneSeamOverride.VISIBLE);
        assertEquals(PaneSeamOverride.SEAMLESS, seams.get(north.plane(), north.boundary()));
        assertEquals(PaneSeamOverride.VISIBLE, seams.get(south.plane(), south.boundary()));
    }

    private static Vec3 hit(BlockPos pos, Direction face, Direction boundary, boolean inside) {
        double[] coordinates = {0.5, 0.5, 0.5};
        boolean negative = face.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
        coordinates[face.getAxis().ordinal()] = inside
                ? (negative ? 2.0 / 16.0 : 14.0 / 16.0) : (negative ? 0.0 : 1.0);
        coordinates[boundary.getAxis().ordinal()] =
                boundary.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? 0.04 : 0.96;
        return new Vec3(pos.getX() + coordinates[0], pos.getY() + coordinates[1], pos.getZ() + coordinates[2]);
    }

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
