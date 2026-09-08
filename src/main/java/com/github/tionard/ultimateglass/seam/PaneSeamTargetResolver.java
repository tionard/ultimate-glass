package com.github.tionard.ultimateglass.seam;

import java.util.Comparator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import com.github.tionard.ultimateglass.pane.PaneGeometry;
import com.github.tionard.ultimateglass.pane.PanePlane;

/** Resolves a pane click to the nearest editable boundary of the clicked sheet. */
public final class PaneSeamTargetResolver {
    private PaneSeamTargetResolver() {
    }

    public static PaneSeamTarget resolve(
            PaneGeometry geometry,
            BlockPos pos,
            Vec3 worldHit,
            Direction clickedFace
    ) {
        Vec3 hit = worldHit.subtract(pos.getX(), pos.getY(), pos.getZ());
        PanePlane plane = geometry.planes().stream()
                // Parallel sheets have the same normal: identify the sheet at the hit,
                // not the first enum value on that axis. At shared corners, the clicked
                // face normal breaks ties. A distant perpendicular sheet must not steal
                // clicks on the thin rim of a nearer pane.
                .min(Comparator.comparingDouble((PanePlane candidate) -> planeDistance(candidate, hit))
                        .thenComparingInt(candidate -> candidate.axis() == clickedFace.getAxis() ? 0 : 1)
                        .thenComparingDouble(candidate -> planeCenterDistance(candidate, hit)))
                .orElseThrow();

        Direction boundary = java.util.Arrays.stream(Direction.values())
                .filter(direction -> direction.getAxis() != plane.axis())
                .min(Comparator.comparingDouble(direction -> boundaryDistance(direction, hit)))
                .orElseThrow();
        return new PaneSeamTarget(plane, boundary);
    }

    private static double planeDistance(PanePlane plane, Vec3 hit) {
        double coordinate = coordinate(hit, plane.axis());
        double min = plane.shape().min(plane.axis());
        double max = plane.shape().max(plane.axis());
        // Include the full pane thickness, with tolerance for ray-hit rounding.
        return Math.max(0.0D, Math.max(min - coordinate, coordinate - max) - 1.0E-7D);
    }

    private static double planeCenterDistance(PanePlane plane, Vec3 hit) {
        double center = (plane.shape().min(plane.axis()) + plane.shape().max(plane.axis())) / 2.0D;
        return Math.abs(coordinate(hit, plane.axis()) - center);
    }

    private static double boundaryDistance(Direction direction, Vec3 hit) {
        double coordinate = coordinate(hit, direction.getAxis());
        return direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE
                ? coordinate
                : 1.0D - coordinate;
    }

    private static double coordinate(Vec3 point, Direction.Axis axis) {
        return switch (axis) {
            case X -> point.x;
            case Y -> point.y;
            case Z -> point.z;
        };
    }
}
