package com.github.tionard.ultimateglass.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A full-height glass pane aligned to one outside edge of its block space.
 *
 * <p>The facing value points from the block centre toward the occupied edge.
 */
public final class EdgePaneBlock extends Block {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape NORTH_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0);
    private static final VoxelShape SOUTH_SHAPE = Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0);
    private static final VoxelShape WEST_SHAPE = Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0);
    private static final VoxelShape EAST_SHAPE = Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    private final Block vanillaPane;

    public EdgePaneBlock(Block vanillaPane, Properties properties) {
        super(properties);
        this.vanillaPane = vanillaPane;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    public Block vanillaPane() {
        return vanillaPane;
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return shapeFor(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(FACING));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, determineFacing(context));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    public static Direction determineFacing(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        if (clickedFace.getAxis().isHorizontal()) {
            return clickedFace.getOpposite();
        }

        BlockPos targetPos = context.getClickedPos();
        double localX = context.getClickLocation().x - targetPos.getX();
        double localZ = context.getClickLocation().z - targetPos.getZ();

        double westDistance = localX;
        double eastDistance = 1.0 - localX;
        double northDistance = localZ;
        double southDistance = 1.0 - localZ;

        double closest = Math.min(Math.min(westDistance, eastDistance), Math.min(northDistance, southDistance));
        if (closest == westDistance) {
            return Direction.WEST;
        }
        if (closest == eastDistance) {
            return Direction.EAST;
        }
        if (closest == northDistance) {
            return Direction.NORTH;
        }
        return Direction.SOUTH;
    }

    public static Direction rotateClockwise(Direction direction) {
        return switch (direction) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            default -> throw new IllegalArgumentException("Expected a horizontal direction, got " + direction);
        };
    }

    private static VoxelShape shapeFor(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> throw new IllegalArgumentException("Expected a horizontal direction, got " + direction);
        };
    }
}
