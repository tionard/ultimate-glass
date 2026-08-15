package com.github.tionard.ultimateglass.pane;

import java.util.Objects;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Geometry-independent description of every physical sheet in one pane block cell. */
public final class PaneGeometry {
    private static final int EDGE_CONNECTION_COMBINATIONS = 16;
    private static final PaneGeometry[][] EDGE_GEOMETRIES = createEdgeGeometries();
    private static final PaneGeometry[] CENTERED_GEOMETRIES = createCenteredGeometries();

    private final PanePlaneSet planes;
    private final VoxelShape shape;

    private PaneGeometry(PanePlaneSet planes) {
        this.planes = Objects.requireNonNull(planes, "planes");
        if (planes.isEmpty()) {
            throw new IllegalArgumentException("Pane geometry must contain at least one plane");
        }

        VoxelShape combined = Shapes.empty();
        for (PanePlane plane : planes) {
            combined = Shapes.or(combined, plane.shape());
        }
        shape = combined;
    }

    public static PaneGeometry of(PanePlaneSet planes) {
        for (PaneGeometry[] geometries : EDGE_GEOMETRIES) {
            for (PaneGeometry geometry : geometries) {
                if (geometry.planes.equals(planes)) {
                    return geometry;
                }
            }
        }
        for (PaneGeometry geometry : CENTERED_GEOMETRIES) {
            if (geometry != null && geometry.planes.equals(planes)) {
                return geometry;
            }
        }
        return new PaneGeometry(planes);
    }

    public static PaneGeometry edge(
            Direction facing,
            boolean connectTop,
            boolean connectBottom,
            boolean connectLeft,
            boolean connectRight
    ) {
        int connectionMask = (connectTop ? 1 : 0)
                | (connectBottom ? 2 : 0)
                | (connectLeft ? 4 : 0)
                | (connectRight ? 8 : 0);
        return EDGE_GEOMETRIES[facing.ordinal()][connectionMask];
    }

    public static PaneGeometry centered(Direction.Axis axis) {
        return centered(axis, false, false);
    }

    public static PaneGeometry centered(
            Direction.Axis primaryAxis,
            boolean connectFirst,
            boolean connectSecond
    ) {
        int mask = 1 << primaryAxis.ordinal();
        if (connectFirst) {
            mask |= 1 << firstPerpendicularAxis(primaryAxis).ordinal();
        }
        if (connectSecond) {
            mask |= 1 << secondPerpendicularAxis(primaryAxis).ordinal();
        }
        return CENTERED_GEOMETRIES[mask];
    }

    public PanePlaneSet planes() {
        return planes;
    }

    public boolean hasEdgePlane(Direction direction) {
        return planes.contains(PanePlane.edge(direction));
    }

    public boolean hasCenteredPlane(Direction.Axis axis) {
        return planes.contains(PanePlane.centered(axis));
    }

    public VoxelShape shape() {
        return shape;
    }

    public PaneGeometry rotateAround(Direction.Axis axis) {
        PanePlaneSet rotated = PanePlaneSet.EMPTY;
        for (PanePlane plane : planes) {
            rotated = rotated.plus(plane.rotateAround(axis));
        }
        return of(rotated);
    }

    public static Direction localTop(Direction facing) {
        return switch (facing) {
            case NORTH, EAST, SOUTH, WEST -> Direction.UP;
            case UP -> Direction.SOUTH;
            case DOWN -> Direction.NORTH;
        };
    }

    public static Direction localLeft(Direction facing) {
        return switch (facing) {
            case NORTH -> Direction.WEST;
            case EAST -> Direction.NORTH;
            case SOUTH -> Direction.EAST;
            case WEST -> Direction.SOUTH;
            case UP, DOWN -> Direction.WEST;
        };
    }

    /** Stable relative-axis mapping used by the two backward-compatible centered flags. */
    public static Direction.Axis firstPerpendicularAxis(Direction.Axis primaryAxis) {
        return switch (primaryAxis) {
            case X -> Direction.Axis.Y;
            case Y, Z -> Direction.Axis.X;
        };
    }

    /** Stable relative-axis mapping used by the two backward-compatible centered flags. */
    public static Direction.Axis secondPerpendicularAxis(Direction.Axis primaryAxis) {
        return switch (primaryAxis) {
            case X, Y -> Direction.Axis.Z;
            case Z -> Direction.Axis.Y;
        };
    }

    private static PaneGeometry[][] createEdgeGeometries() {
        Direction[] directions = Direction.values();
        PaneGeometry[][] geometries = new PaneGeometry[directions.length][EDGE_CONNECTION_COMBINATIONS];
        for (Direction facing : directions) {
            for (int connectionMask = 0; connectionMask < EDGE_CONNECTION_COMBINATIONS; connectionMask++) {
                geometries[facing.ordinal()][connectionMask] = createEdgeGeometry(facing, connectionMask);
            }
        }
        return geometries;
    }

    private static PaneGeometry createEdgeGeometry(Direction facing, int connectionMask) {
        PanePlaneSet planes = PanePlaneSet.of(PanePlane.edge(facing));
        Direction top = localTop(facing);
        Direction left = localLeft(facing);

        if ((connectionMask & 1) != 0) {
            planes = planes.plus(PanePlane.edge(top));
        }
        if ((connectionMask & 2) != 0) {
            planes = planes.plus(PanePlane.edge(top.getOpposite()));
        }
        if ((connectionMask & 4) != 0) {
            planes = planes.plus(PanePlane.edge(left));
        }
        if ((connectionMask & 8) != 0) {
            planes = planes.plus(PanePlane.edge(left.getOpposite()));
        }
        return new PaneGeometry(planes);
    }

    private static PaneGeometry[] createCenteredGeometries() {
        PaneGeometry[] geometries = new PaneGeometry[8];
        for (int mask = 1; mask < geometries.length; mask++) {
            PanePlaneSet planes = PanePlaneSet.EMPTY;
            for (Direction.Axis axis : Direction.Axis.values()) {
                if ((mask & (1 << axis.ordinal())) != 0) {
                    planes = planes.plus(PanePlane.centered(axis));
                }
            }
            geometries[mask] = new PaneGeometry(planes);
        }
        return geometries;
    }
}
