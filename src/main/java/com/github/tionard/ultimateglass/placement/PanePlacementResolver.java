package com.github.tionard.ultimateglass.placement;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;

import com.github.tionard.ultimateglass.block.CenteredPaneBlock;
import com.github.tionard.ultimateglass.block.EdgePaneBlock;

/** Resolves edge-pane placement from the clicked face, player position, and Shift modifier. */
public final class PanePlacementResolver {
    private static final double EPSILON = 1.0E-5;

    private PanePlacementResolver() {
    }

    public static Direction resolve(BlockPlaceContext context) {
        Player player = context.getPlayer();
        boolean shifted = player != null && player.isShiftKeyDown();

        if (shifted) {
            Direction copied = copyOrientation(context);
            if (copied != null) {
                return copied;
            }
        }

        Direction towardPlayer = directionTowardPlayerOnClickedFace(context);
        if (!shifted) {
            return towardPlayer.getOpposite();
        }

        ShiftPlacementMode mode = player == null
                ? ShiftPlacementModeState.DEFAULT_MODE
                : ShiftPlacementModeState.get(player);
        return mode == ShiftPlacementMode.FACE
                ? context.getClickedFace().getOpposite()
                : towardPlayer;
    }

    @Nullable
    private static Direction copyOrientation(BlockPlaceContext context) {
        BlockPos sourcePos = context.replacingClickedOnBlock()
                ? context.getClickedPos()
                : context.getClickedPos().relative(context.getClickedFace().getOpposite());
        BlockState source = context.getLevel().getBlockState(sourcePos);

        if (source.getBlock() instanceof EdgePaneBlock) {
            return source.getValue(EdgePaneBlock.FACING);
        }

        if (source.getBlock() instanceof CenteredPaneBlock) {
            return directionTowardPlayerAlongAxis(
                    context,
                    source.getValue(CenteredPaneBlock.AXIS)
            ).getOpposite();
        }

        if (source.getBlock() instanceof SlabBlock && source.hasProperty(BlockStateProperties.SLAB_TYPE)) {
            SlabType type = source.getValue(BlockStateProperties.SLAB_TYPE);
            return switch (type) {
                case TOP -> Direction.UP;
                case BOTTOM -> Direction.DOWN;
                case DOUBLE -> null;
            };
        }

        if (source.getBlock() instanceof TrapDoorBlock) {
            if (source.hasProperty(BlockStateProperties.OPEN) && source.getValue(BlockStateProperties.OPEN)) {
                return source.getValue(BlockStateProperties.HORIZONTAL_FACING);
            }
            return halfDirection(source);
        }

        if (source.getBlock() instanceof StairBlock) {
            if (context.getClickedFace().getAxis() == Direction.Axis.Y) {
                return halfDirection(source);
            }
            return source.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }

        if (source.hasProperty(BlockStateProperties.FACING)) {
            return source.getValue(BlockStateProperties.FACING);
        }

        if (source.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return source.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }

        if (source.hasProperty(BlockStateProperties.HALF)) {
            return halfDirection(source);
        }

        // AXIS-only blocks such as logs and pillars intentionally do not count as orientation sources.
        return null;
    }

    @Nullable
    private static Direction halfDirection(BlockState state) {
        if (!state.hasProperty(BlockStateProperties.HALF)) {
            return null;
        }
        return state.getValue(BlockStateProperties.HALF) == Half.TOP
                ? Direction.UP
                : Direction.DOWN;
    }

    private static Direction directionTowardPlayerOnClickedFace(BlockPlaceContext context) {
        Vec3 center = Vec3.atCenterOf(context.getClickedPos());
        Player player = context.getPlayer();
        Vec3 reference = player != null ? player.getEyePosition() : context.getClickLocation();
        Vec3 delta = reference.subtract(center);

        Direction.Axis excluded = context.getClickedFace().getAxis();
        Direction.Axis chosen = strongestPerpendicularAxis(delta, excluded);
        double component = component(delta, chosen);

        if (Math.abs(component) < EPSILON) {
            Vec3 clickDelta = context.getClickLocation().subtract(center);
            component = component(clickDelta, chosen);
        }

        if (Math.abs(component) < EPSILON) {
            component = 1.0;
        }

        return direction(chosen, component > 0.0);
    }

    private static Direction directionTowardPlayerAlongAxis(
            BlockPlaceContext context,
            Direction.Axis axis
    ) {
        Vec3 center = Vec3.atCenterOf(context.getClickedPos());
        Player player = context.getPlayer();
        Vec3 reference = player != null ? player.getEyePosition() : context.getClickLocation();
        double component = component(reference.subtract(center), axis);

        if (Math.abs(component) < EPSILON) {
            component = component(context.getClickLocation().subtract(center), axis);
        }
        if (Math.abs(component) < EPSILON) {
            component = 1.0;
        }

        return direction(axis, component > 0.0);
    }

    private static Direction.Axis strongestPerpendicularAxis(Vec3 delta, Direction.Axis excluded) {
        return switch (excluded) {
            case X -> Math.abs(delta.y) >= Math.abs(delta.z) ? Direction.Axis.Y : Direction.Axis.Z;
            case Y -> Math.abs(delta.x) >= Math.abs(delta.z) ? Direction.Axis.X : Direction.Axis.Z;
            case Z -> Math.abs(delta.x) >= Math.abs(delta.y) ? Direction.Axis.X : Direction.Axis.Y;
        };
    }

    private static double component(Vec3 vector, Direction.Axis axis) {
        return switch (axis) {
            case X -> vector.x;
            case Y -> vector.y;
            case Z -> vector.z;
        };
    }

    private static Direction direction(Direction.Axis axis, boolean positive) {
        return switch (axis) {
            case X -> positive ? Direction.EAST : Direction.WEST;
            case Y -> positive ? Direction.UP : Direction.DOWN;
            case Z -> positive ? Direction.SOUTH : Direction.NORTH;
        };
    }
}
