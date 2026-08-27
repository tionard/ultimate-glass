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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.github.tionard.ultimateglass.pane.PaneAppearance;
import com.github.tionard.ultimateglass.pane.PaneConnectionQueries;
import com.github.tionard.ultimateglass.pane.PaneGeometry;
import com.github.tionard.ultimateglass.pane.PaneMaterial;
import com.github.tionard.ultimateglass.pane.PanePlane;
import com.github.tionard.ultimateglass.pane.UltimatePane;
import com.github.tionard.ultimateglass.config.UltimateGlassServerConfig;
import com.github.tionard.ultimateglass.item.GlaziersToolItem;
import com.github.tionard.ultimateglass.block.entity.PaneSeamBlockEntity;
import com.github.tionard.ultimateglass.registry.UltimateGlassSmartItems;

/** A full glass sheet centred in its block space on one of the three axes. */
public class CenteredPaneBlock extends Block implements EntityBlock, SimpleWaterloggedBlock, UltimatePane {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty CONNECT_FIRST = BooleanProperty.create("connect_first");
    public static final BooleanProperty CONNECT_SECOND = BooleanProperty.create("connect_second");

    private final Block vanillaPane;
    private final PaneAppearance appearance;

    public CenteredPaneBlock(Block vanillaPane, PaneAppearance appearance, Properties properties) {
        super(properties);
        this.vanillaPane = vanillaPane;
        this.appearance = appearance;
        registerDefaultState(defaultBlockState()
                .setValue(AXIS, Direction.Axis.Z)
                .setValue(WATERLOGGED, false)
                .setValue(CONNECT_FIRST, false)
                .setValue(CONNECT_SECOND, false));
    }

    @Override
    public Block vanillaPane() {
        return vanillaPane;
    }

    @Override
    public PaneAppearance appearance() {
        return appearance;
    }

    @Override
    public PaneGeometry geometry(BlockState state) {
        return PaneGeometry.centered(
                state.getValue(AXIS),
                state.getValue(CONNECT_FIRST),
                state.getValue(CONNECT_SECOND)
        );
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PaneSeamBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return shapeForState(state, level, pos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForState(state, level, pos);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForState(state, level, pos);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        BlockState state = defaultBlockState()
                .setValue(AXIS, context.getClickedFace().getAxis())
                .setValue(WATERLOGGED, fluid.is(Fluids.WATER));
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
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return appearance.material() != PaneMaterial.TINTED
                && super.propagatesSkylightDown(state);
    }

    @Override
    protected int getLightDampening(BlockState state) {
        return appearance.material() == PaneMaterial.TINTED
                ? 15
                : super.getLightDampening(state);
    }

    @Override
    protected java.util.List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        java.util.List<ItemStack> drops;
        if (!UltimateGlassServerConfig.temperedPanesAlwaysDrop()) {
            drops = super.getDrops(state, builder);
        } else {
            ItemStack drop = GlaziersToolItem.collectedStack(this);
            drops = drop.isEmpty() ? java.util.List.of() : java.util.List.of(drop);
        }
        return UltimateGlassSmartItems.modernizeDrops(this, drops);
    }

    @Override
    protected ItemStack getCloneItemStack(
            LevelReader level, BlockPos pos, BlockState state, boolean includeData
    ) {
        ItemStack stack = new ItemStack(asItem());
        UltimateGlassSmartItems.applyComponents(this, stack);
        return stack;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, WATERLOGGED, CONNECT_FIRST, CONNECT_SECOND);
    }

    /** Rotates the sheet normal 90 degrees around the selected world axis. */
    public static Direction.Axis rotateAround(Direction.Axis paneAxis, Direction.Axis rotationAxis) {
        return PanePlane.rotateAxis(paneAxis, rotationAxis);
    }

    /** Rotates the primary plane and every connected centered plane as one geometry. */
    public static BlockState rotateAround(BlockState state, Direction.Axis rotationAxis) {
        CenteredPaneBlock pane = (CenteredPaneBlock) state.getBlock();
        PaneGeometry rotated = pane.geometry(state).rotateAround(rotationAxis);
        Direction.Axis primary = PanePlane.rotateAxis(state.getValue(AXIS), rotationAxis);
        return state
                .setValue(AXIS, primary)
                .setValue(
                        CONNECT_FIRST,
                        rotated.hasCenteredPlane(PaneGeometry.firstPerpendicularAxis(primary))
                )
                .setValue(
                        CONNECT_SECOND,
                        rotated.hasCenteredPlane(PaneGeometry.secondPerpendicularAxis(primary))
                );
    }

    public BlockState withConnections(BlockState state, BlockGetter level, BlockPos pos) {
        Direction.Axis primary = state.getValue(AXIS);
        return state
                .setValue(CONNECT_FIRST, PaneConnectionQueries.hasCenteredConnection(
                        level,
                        pos,
                        state,
                        PaneGeometry.firstPerpendicularAxis(primary)
                ))
                .setValue(CONNECT_SECOND, PaneConnectionQueries.hasCenteredConnection(
                        level,
                        pos,
                        state,
                        PaneGeometry.secondPerpendicularAxis(primary)
                ));
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
                    if (!(candidate.getBlock() instanceof CenteredPaneBlock pane)) {
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

    private static VoxelShape shapeForState(BlockState state, BlockGetter level, BlockPos pos) {
        Direction.Axis primary = state.getValue(AXIS);
        VoxelShape shape = PaneGeometry.centered(primary).shape();
        for (Direction.Axis requestedAxis : Direction.Axis.values()) {
            if (requestedAxis == primary) {
                continue;
            }
            for (Direction sourceDirection : Direction.values()) {
                if (PaneConnectionQueries.hasCenteredConnectionFrom(
                        level, pos, state, requestedAxis, sourceDirection
                )) {
                    shape = Shapes.or(
                            shape,
                            PaneGeometry.centeredArm(requestedAxis, sourceDirection)
                    );
                }
            }
        }
        return shape.optimize();
    }
}
