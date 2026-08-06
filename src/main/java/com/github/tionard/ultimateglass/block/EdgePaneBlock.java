package com.github.tionard.ultimateglass.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.github.tionard.ultimateglass.placement.PanePlacementResolver;

/** A glass pane aligned to one of the six outside faces of its block space. */
public final class EdgePaneBlock extends Block implements SimpleWaterloggedBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty CONNECT_TOP = BooleanProperty.create("connect_top");
    public static final BooleanProperty CONNECT_BOTTOM = BooleanProperty.create("connect_bottom");
    public static final BooleanProperty CONNECT_LEFT = BooleanProperty.create("connect_left");
    public static final BooleanProperty CONNECT_RIGHT = BooleanProperty.create("connect_right");

    private static final VoxelShape NORTH_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0);
    private static final VoxelShape SOUTH_SHAPE = Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0);
    private static final VoxelShape WEST_SHAPE = Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0);
    private static final VoxelShape EAST_SHAPE = Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape DOWN_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
    private static final VoxelShape UP_SHAPE = Block.box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0);

    private final Block vanillaPane;

    public EdgePaneBlock(Block vanillaPane, Properties properties) {
        super(properties);
        this.vanillaPane = vanillaPane;
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false)
                .setValue(CONNECT_TOP, false)
                .setValue(CONNECT_BOTTOM, false)
                .setValue(CONNECT_LEFT, false)
                .setValue(CONNECT_RIGHT, false));
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
    protected VoxelShape getOcclusionShape(BlockState state) {
        return shapeFor(state.getValue(FACING));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        BlockState state = defaultBlockState()
                .setValue(FACING, PanePlacementResolver.resolve(context))
                .setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
        return withConnections(state, context.getLevel(), context.getClickedPos());
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack
    ) {
        super.setPlacedBy(level, pos, state, placer, stack);
        refreshConnectionsAround(level, pos);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess scheduledTicks,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
    ) {
        if (state.getValue(WATERLOGGED)) {
            scheduledTicks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        BlockState updated = withConnections(state, level, pos);
        return super.updateShape(updated, level, scheduledTicks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(
                FACING,
                WATERLOGGED,
                CONNECT_TOP,
                CONNECT_BOTTOM,
                CONNECT_LEFT,
                CONNECT_RIGHT
        );
    }

    public static Direction rotateAround(Direction direction, Direction.Axis axis) {
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

    public static void refreshConnectionsAround(Level level, BlockPos changedPos) {
        if (level.isClientSide()) {
            return;
        }

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos candidatePos = changedPos.offset(x, y, z);
                    BlockState candidate = level.getBlockState(candidatePos);
                    if (!(candidate.getBlock() instanceof EdgePaneBlock pane)) {
                        continue;
                    }

                    BlockState updated = pane.withConnections(candidate, level, candidatePos);
                    if (updated != candidate) {
                        level.setBlockAndUpdate(candidatePos, updated);
                    }
                }
            }
        }
    }

    public BlockState withConnections(BlockState state, BlockGetter level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        Direction top = localTop(facing);
        Direction bottom = top.getOpposite();
        Direction left = localLeft(facing);
        Direction right = left.getOpposite();

        return state
                .setValue(CONNECT_TOP, hasOuterConnection(level, pos, facing, top))
                .setValue(CONNECT_BOTTOM, hasOuterConnection(level, pos, facing, bottom))
                .setValue(CONNECT_LEFT, hasOuterConnection(level, pos, facing, left))
                .setValue(CONNECT_RIGHT, hasOuterConnection(level, pos, facing, right));
    }

    private static boolean hasOuterConnection(
            BlockGetter level,
            BlockPos pos,
            Direction facing,
            Direction edge
    ) {
        BlockPos diagonal = pos.relative(facing).relative(edge);
        BlockState other = level.getBlockState(diagonal);
        return other.getBlock() instanceof EdgePaneBlock
                && other.getValue(FACING) == edge.getOpposite();
    }

    private static Direction localTop(Direction facing) {
        return switch (facing) {
            case NORTH, EAST, SOUTH, WEST -> Direction.UP;
            case UP -> Direction.SOUTH;
            case DOWN -> Direction.NORTH;
        };
    }

    private static Direction localLeft(Direction facing) {
        return switch (facing) {
            case NORTH -> Direction.WEST;
            case EAST -> Direction.NORTH;
            case SOUTH -> Direction.EAST;
            case WEST -> Direction.SOUTH;
            case UP, DOWN -> Direction.WEST;
        };
    }

    private static VoxelShape shapeFor(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case DOWN -> DOWN_SHAPE;
            case UP -> UP_SHAPE;
        };
    }
}
