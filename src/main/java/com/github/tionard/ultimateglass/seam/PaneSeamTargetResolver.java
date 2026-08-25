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
                .filter(candidate -> candidate.axis() == clickedFace.getAxis())
                .findFirst()
                .orElseGet(() -> geometry.planes().stream()
                        .min(Comparator.comparingDouble(candidate -> planeDistance(candidate, hit)))
                        .orElseThrow());

        Direction boundary = java.util.Arrays.stream(Direction.values())
                .filter(direction -> direction.getAxis() != plane.axis())
                .min(Comparator.comparingDouble(direction -> boundaryDistance(direction, hit)))
                .orElseThrow();
        return new PaneSeamTarget(plane, boundary);
    }

    private static double planeDistance(PanePlane plane, Vec3 hit) {
        double coordinate = coordinate(hit, plane.axis());
        if (plane.isCentered()) {
            return Math.abs(coordinate - 0.5D);
        }
        return plane.edgeDirection().getAxisDirection() == Direction.AxisDirection.NEGATIVE
                ? coordinate
                : 1.0D - coordinate;
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
