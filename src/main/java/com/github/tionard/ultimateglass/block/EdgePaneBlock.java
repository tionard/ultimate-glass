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
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.github.tionard.ultimateglass.pane.PaneAppearance;
import com.github.tionard.ultimateglass.pane.PaneGeometry;
import com.github.tionard.ultimateglass.pane.PaneMaterial;
import com.github.tionard.ultimateglass.pane.PanePlane;
import com.github.tionard.ultimateglass.pane.UltimatePane;
import com.github.tionard.ultimateglass.placement.PanePlacementResolver;
import com.github.tionard.ultimateglass.config.UltimateGlassServerConfig;
import com.github.tionard.ultimateglass.item.GlaziersToolItem;
import com.github.tionard.ultimateglass.block.entity.PaneSeamBlockEntity;
import com.github.tionard.ultimateglass.registry.UltimateGlassSmartItems;

/** A glass pane aligned to one of the six outside faces of its block space. */
public class EdgePaneBlock extends Block implements EntityBlock, SimpleWaterloggedBlock, UltimatePane {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty CONNECT_TOP = BooleanProperty.create("connect_top");
    public static final BooleanProperty CONNECT_BOTTOM = BooleanProperty.create("connect_bottom");
    public static final BooleanProperty CONNECT_LEFT = BooleanProperty.create("connect_left");
    public static final BooleanProperty CONNECT_RIGHT = BooleanProperty.create("connect_right");
    // Retain the original property names so saved automatic corners load as installed panes.
    public static final BooleanProperty CONNECT_OPPOSITE = BooleanProperty.create("connect_opposite");

    private final Block vanillaPane;
    private final PaneAppearance appearance;

    public EdgePaneBlock(Block vanillaPane, PaneAppearance appearance, Properties properties) {
        super(properties);
        this.vanillaPane = vanillaPane;
        this.appearance = appearance;
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false)
                .setValue(CONNECT_TOP, false)
                .setValue(CONNECT_BOTTOM, false)
                .setValue(CONNECT_LEFT, false)
                .setValue(CONNECT_RIGHT, false)
                .setValue(CONNECT_OPPOSITE, false));
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
        return PaneGeometry.edge(
                state.getValue(FACING),
                state.getValue(CONNECT_TOP),
                state.getValue(CONNECT_BOTTOM),
                state.getValue(CONNECT_LEFT),
                state.getValue(CONNECT_RIGHT),
                state.getValue(CONNECT_OPPOSITE)
        );
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PaneSeamBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return shapeForState(state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForState(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForState(state);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        BlockState state = defaultBlockState()
                .setValue(FACING, PanePlacementResolver.resolve(context))
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
        var tool = builder.getOptionalParameter(LootContextParams.TOOL);
        boolean diamondTool = tool != null && tool.typeHolder().value() instanceof GlaziersToolItem glazier
                && glazier.tier().silkTouchesGlass();
        if (!UltimateGlassServerConfig.temperedPanesAlwaysDrop() && !diamondTool) {
            drops = super.getDrops(state, builder);
        } else {
            ItemStack drop = GlaziersToolItem.collectedStack(this);
            drops = drop.isEmpty() ? java.util.List.of() : java.util.List.of(drop);
        }
        java.util.List<ItemStack> modern = UltimateGlassSmartItems.modernizeDrops(this, drops);
        modern.forEach(stack -> stack.setCount(stack.getCount() * paneCount(state)));
        return modern;
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
        builder.add(
                FACING,
                WATERLOGGED,
                CONNECT_TOP,
                CONNECT_BOTTOM,
                CONNECT_LEFT,
                CONNECT_RIGHT,
                CONNECT_OPPOSITE
        );
    }

    public static Direction rotateAround(Direction direction, Direction.Axis axis) {
        return PanePlane.rotateDirection(direction, axis);
    }

    public static void refreshConnectionsAround(Level level, BlockPos changedPos) {
        // Edge faces are now permanent inventory, independent of neighbours.
        // Kept as a compatibility entry point for composite/frame placement callers.
    }

    public BlockState withConnections(BlockState state, BlockGetter level, BlockPos pos) {
        // In particular, do not erase saved 0.2.2 corner flags during chunk load updates.
        return state;
    }

    public static int paneCount(BlockState state) {
        return ((EdgePaneBlock) state.getBlock()).geometry(state).planes().size();
    }

    public static BlockState addPane(BlockState state, Direction direction) {
        PaneGeometry geometry = ((EdgePaneBlock) state.getBlock()).geometry(state);
        return withGeometry(state, state.getValue(FACING),
                PaneGeometry.of(geometry.planes().plus(PanePlane.edge(direction))));
    }

    public static BlockState rotateAssembly(BlockState state, Direction.Axis axis) {
        return withGeometry(state, rotateAround(state.getValue(FACING), axis),
                ((EdgePaneBlock) state.getBlock()).geometry(state).rotateAround(axis));
    }

    private static BlockState withGeometry(BlockState state, Direction facing, PaneGeometry geometry) {
        Direction top = PaneGeometry.localTop(facing);
        Direction bottom = top.getOpposite();
        Direction left = PaneGeometry.localLeft(facing);
        Direction right = left.getOpposite();

        return state
                .setValue(FACING, facing)
                .setValue(CONNECT_TOP, geometry.hasEdgePlane(top))
                .setValue(CONNECT_BOTTOM, geometry.hasEdgePlane(bottom))
                .setValue(CONNECT_LEFT, geometry.hasEdgePlane(left))
                .setValue(CONNECT_RIGHT, geometry.hasEdgePlane(right))
                .setValue(CONNECT_OPPOSITE, geometry.hasEdgePlane(facing.getOpposite()));
    }

    /** Returns whether this state contains a pane plane on the requested block face. */
    public static boolean hasPaneOnFace(BlockState state, Direction direction) {
        return state.getBlock() instanceof EdgePaneBlock pane
                && pane.geometry(state).hasEdgePlane(direction);
    }

    private static VoxelShape shapeForState(BlockState state) {
        return ((EdgePaneBlock) state.getBlock()).geometry(state).shape();
    }
}
