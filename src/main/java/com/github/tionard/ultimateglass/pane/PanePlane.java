package com.github.tionard.ultimateglass.pane;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

/** One physical pane sheet at an outside face or at the centre of a block cell. */
public enum PanePlane {
    EDGE_NORTH(Direction.NORTH, Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0)),
    EDGE_EAST(Direction.EAST, Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0)),
    EDGE_SOUTH(Direction.SOUTH, Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0)),
    EDGE_WEST(Direction.WEST, Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0)),
    EDGE_DOWN(Direction.DOWN, Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0)),
    EDGE_UP(Direction.UP, Block.box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0)),
    CENTER_X(Direction.Axis.X, Block.box(7.0, 0.0, 0.0, 9.0, 16.0, 16.0)),
    CENTER_Y(Direction.Axis.Y, Block.box(0.0, 7.0, 0.0, 16.0, 9.0, 16.0)),
    CENTER_Z(Direction.Axis.Z, Block.box(0.0, 0.0, 7.0, 16.0, 16.0, 9.0));

    private final Position position;
    private final Direction.Axis axis;
    private final Direction edgeDirection;
    private final VoxelShape shape;

    PanePlane(Direction edgeDirection, VoxelShape shape) {
        this.position = Position.EDGE;
        this.axis = edgeDirection.getAxis();
        this.edgeDirection = edgeDirection;
        this.shape = shape;
    }

    PanePlane(Direction.Axis axis, VoxelShape shape) {
        this.position = Position.CENTERED;
        this.axis = axis;
        this.edgeDirection = null;
        this.shape = shape;
    }

    public static PanePlane edge(Direction direction) {
        return switch (direction) {
            case NORTH -> EDGE_NORTH;
            case EAST -> EDGE_EAST;
            case SOUTH -> EDGE_SOUTH;
            case WEST -> EDGE_WEST;
            case DOWN -> EDGE_DOWN;
            case UP -> EDGE_UP;
        };
    }

    public static PanePlane centered(Direction.Axis axis) {
        return switch (axis) {
            case X -> CENTER_X;
            case Y -> CENTER_Y;
            case Z -> CENTER_Z;
        };
    }

    public Position position() {
        return position;
    }

    public Direction.Axis axis() {
        return axis;
    }

    public boolean isEdge() {
        return position == Position.EDGE;
    }

    public boolean isCentered() {
        return position == Position.CENTERED;
    }

    public Direction edgeDirection() {
        if (edgeDirection == null) {
            throw new IllegalStateException(this + " is not an edge plane");
        }
        return edgeDirection;
    }

    public VoxelShape shape() {
        return shape;
    }

    public PanePlane rotateAround(Direction.Axis rotationAxis) {
        if (isEdge()) {
            return edge(rotateDirection(edgeDirection(), rotationAxis));
        }
        return centered(rotateAxis(axis, rotationAxis));
    }

    public static Direction rotateDirection(Direction direction, Direction.Axis axis) {
        return switch (axis) {
            case X -> switch (direction) {
                case UP -> Direction.SOUTH;
                case SOUTH -> Direction.DOWN;
                case DOWN -> Direction.NORTH;
                case NORTH -> Direction.UP;
                case EAST, WEST -> direction;
            };
            case Y -> switch (direction) {
                case NORTH -> Direction.EAST;
                case EAST -> Direction.SOUTH;
                case SOUTH -> Direction.WEST;
                case WEST -> Direction.NORTH;
                case UP, DOWN -> direction;
            };
            case Z -> switch (direction) {
                case UP -> Direction.WEST;
                case WEST -> Direction.DOWN;
                case DOWN -> Direction.EAST;
                case EAST -> Direction.UP;
                case NORTH, SOUTH -> direction;
            };
        };
    }

    public static Direction.Axis rotateAxis(
            Direction.Axis paneAxis,
            Direction.Axis rotationAxis
    ) {
        if (paneAxis == rotationAxis) {
            return paneAxis;
        }

        return switch (rotationAxis) {
            case X -> paneAxis == Direction.Axis.Y ? Direction.Axis.Z : Direction.Axis.Y;
            case Y -> paneAxis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
            case Z -> paneAxis == Direction.Axis.X ? Direction.Axis.Y : Direction.Axis.X;
        };
    }

    public enum Position {
        EDGE,
        CENTERED
    }
}
