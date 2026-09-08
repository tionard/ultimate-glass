package com.github.tionard.ultimateglass.item;

import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;

import com.github.tionard.ultimateglass.block.CompositePaneBlock;
import com.github.tionard.ultimateglass.block.CenteredPaneBlock;
import com.github.tionard.ultimateglass.block.EdgePaneBlock;
import com.github.tionard.ultimateglass.block.entity.CompositePaneBlockEntity;
import com.github.tionard.ultimateglass.block.entity.DynamicFrameBlockEntity;
import com.github.tionard.ultimateglass.block.entity.PaneFrameSource;
import com.github.tionard.ultimateglass.config.UltimateGlassServerConfig;
import com.github.tionard.ultimateglass.pane.PaneAppearance;
import com.github.tionard.ultimateglass.pane.CompositePaneGeometry;
import com.github.tionard.ultimateglass.pane.PaneFrame;
import com.github.tionard.ultimateglass.pane.PaneMaterial;
import com.github.tionard.ultimateglass.pane.PaneCombination;
import com.github.tionard.ultimateglass.registry.UltimateGlassBlocks;
import com.github.tionard.ultimateglass.registry.UltimateGlassComponents;
import com.github.tionard.ultimateglass.placement.PanePlacementResolver;

/** Common pane item behavior, including installation into stair and slab host blocks. */
public class TemperedPaneItem extends BlockItem {
    public TemperedPaneItem(Block block, Properties properties) {
        super(block, properties);
    }

    /** Allows a component-backed item to select its material-specific internal pane block. */
    protected Block placementBlock(ItemStack stack) {
        return getBlock();
    }

    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        Block target = placementBlock(context.getItemInHand());
        BlockState state = target == null ? null : target.getStateForPlacement(context);
        return state != null && canPlace(context, state) ? state : null;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult edgeResult = installEdgePane(context);
        if (edgeResult != InteractionResult.PASS) {
            return edgeResult;
        }
        InteractionResult compositeResult = installComposite(context);
        return compositeResult == InteractionResult.PASS
                ? super.useOn(context)
                : compositeResult;
    }

    private InteractionResult installEdgePane(UseOnContext context) {
        if (!(placementBlock(context.getItemInHand()) instanceof EdgePaneBlock incoming)) {
            return InteractionResult.PASS;
        }
        // Try the existing assembly first. An occupied face falls through to ordinary
        // adjacent placement, so extending a window still works normally.
        InteractionResult clicked = insertEdgePane(context, context.getClickedPos(),
                PanePlacementResolver.resolveComposite(context), incoming.appearance());
        if (clicked != InteractionResult.PASS) {
            return clicked;
        }
        BlockPlaceContext adjacent = new BlockPlaceContext(context);
        if (!adjacent.getClickedPos().equals(context.getClickedPos())) {
            return insertEdgePane(context, adjacent.getClickedPos(),
                    PanePlacementResolver.resolve(adjacent), incoming.appearance());
        }
        return InteractionResult.PASS;
    }

    private InteractionResult insertEdgePane(
            UseOnContext context, BlockPos pos, Direction face, PaneAppearance incoming
    ) {
        Level level = context.getLevel();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof EdgePaneBlock existing)
                || EdgePaneBlock.hasPaneOnFace(state, face)) {
            return InteractionResult.PASS;
        }
        Identifier existingWood = level.getBlockEntity(pos) instanceof PaneFrameSource frame
                ? frame.frameBlockId() : DynamicFrameBlockEntity.DEFAULT_FRAME;
        if (!PaneCombination.matches(existing.appearance(), existingWood, incoming,
                dynamicFrameId(context.getItemInHand(), incoming.frame()))) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        if (player == null || player.isSpectator() || !player.getAbilities().mayBuild
                || context.getItemInHand().isEmpty() || !level.mayInteract(player, pos)
                || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand())) {
            return InteractionResult.FAIL;
        }
        BlockState updated = EdgePaneBlock.addPane(state, face);
        if (!level.isUnobstructed(updated, pos, CollisionContext.of(player))) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            // Same block type: retain water, frame identity, and the seam block entity.
            if (!level.setBlock(pos, updated, Block.UPDATE_ALL)) {
                return InteractionResult.FAIL;
            }
            CenteredPaneBlock.refreshConnectionsAround(level, pos);
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, pos, context.getItemInHand());
            }
            var sound = updated.getSoundType();
            level.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS,
                    (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, updated));
            context.getItemInHand().consume(1, player);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult installComposite(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!UltimateGlassServerConfig.experimentalCompositesEnabled()) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockState hostState = level.getBlockState(context.getClickedPos());
        if (!isSupportedHost(hostState) || level.getBlockEntity(context.getClickedPos()) != null) {
            return InteractionResult.PASS;
        }

        if (player.isSpectator() || !player.getAbilities().mayBuild) {
            return InteractionResult.FAIL;
        }

        PaneAppearance appearance = paneAppearance(context.getItemInHand());
        if (appearance == null) {
            return InteractionResult.PASS;
        }

        Direction paneFacing = PanePlacementResolver.resolveComposite(context);
        if (paneFacing.getAxis() == Direction.Axis.Y) {
            return InteractionResult.PASS;
        }
        if (CompositePaneGeometry.exposedPaneShape(
                hostState.getShape(level, context.getClickedPos()),
                paneFacing
        ).isEmpty()) {
            return InteractionResult.PASS;
        }
        boolean waterlogged = hostState.hasProperty(BlockStateProperties.WATERLOGGED)
                ? hostState.getValue(BlockStateProperties.WATERLOGGED)
                : level.getFluidState(context.getClickedPos()).is(net.minecraft.world.level.material.Fluids.WATER);
        BlockState compositeState = UltimateGlassBlocks.COMPOSITE_PANE.defaultBlockState()
                .setValue(CompositePaneBlock.WATERLOGGED, waterlogged)
                .setValue(CompositePaneBlock.TINTED, appearance.material() == PaneMaterial.TINTED);

        if (!level.isClientSide()) {
            level.setBlockAndUpdate(context.getClickedPos(), compositeState);
            if (!(level.getBlockEntity(context.getClickedPos())
                    instanceof CompositePaneBlockEntity composite)) {
                level.setBlockAndUpdate(context.getClickedPos(), hostState);
                return InteractionResult.FAIL;
            }

            composite.setComposite(
                    hostState,
                    appearance,
                    paneFacing,
                    dynamicFrameId(context.getItemInHand(), appearance.frame())
            );
            EdgePaneBlock.refreshConnectionsAround(level, context.getClickedPos());
            CenteredPaneBlock.refreshConnectionsAround(level, context.getClickedPos());
            if (!player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }

    private PaneAppearance paneAppearance(ItemStack stack) {
        UltimateGlassBlocks.PaneFamily family = UltimateGlassBlocks.familyFor(
                placementBlock(stack)
        );
        return family == null ? null : family.appearance();
    }

    static boolean isSupportedHost(BlockState state) {
        if (state.getBlock() instanceof SlabBlock) {
            return !state.hasProperty(BlockStateProperties.SLAB_TYPE)
                    || state.getValue(BlockStateProperties.SLAB_TYPE) != SlabType.DOUBLE;
        }
        return state.getBlock() instanceof StairBlock;
    }

    private static Identifier dynamicFrameId(ItemStack stack, PaneFrame frame) {
        return frame.isDynamic()
                ? stack.getOrDefault(
                        UltimateGlassComponents.FRAME_BLOCK,
                        DynamicFrameBlockEntity.DEFAULT_FRAME
                )
                : DynamicFrameBlockEntity.DEFAULT_FRAME;
    }
}
