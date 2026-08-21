package com.github.tionard.ultimateglass.item;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;

import com.github.tionard.ultimateglass.block.CompositePaneBlock;
import com.github.tionard.ultimateglass.block.CenteredPaneBlock;
import com.github.tionard.ultimateglass.block.EdgePaneBlock;
import com.github.tionard.ultimateglass.block.entity.CompositePaneBlockEntity;
import com.github.tionard.ultimateglass.block.entity.DynamicFrameBlockEntity;
import com.github.tionard.ultimateglass.config.UltimateGlassServerConfig;
import com.github.tionard.ultimateglass.pane.PaneAppearance;
import com.github.tionard.ultimateglass.pane.CompositePaneGeometry;
import com.github.tionard.ultimateglass.pane.PaneFrame;
import com.github.tionard.ultimateglass.pane.PaneMaterial;
import com.github.tionard.ultimateglass.registry.UltimateGlassBlocks;
import com.github.tionard.ultimateglass.registry.UltimateGlassComponents;
import com.github.tionard.ultimateglass.placement.PanePlacementResolver;

/** Common pane item behavior, including installation into stair and slab host blocks. */
public class TemperedPaneItem extends BlockItem {
    public TemperedPaneItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult compositeResult = installComposite(context);
        return compositeResult == InteractionResult.PASS
                ? super.useOn(context)
                : compositeResult;
    }

    private InteractionResult installComposite(UseOnContext context) {
        if (!UltimateGlassServerConfig.experimentalCompositesEnabled()) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockState hostState = level.getBlockState(context.getClickedPos());
        if (!isSupportedHost(hostState) || level.getBlockEntity(context.getClickedPos()) != null) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        if (player == null || player.isSpectator() || !player.getAbilities().mayBuild) {
            return InteractionResult.FAIL;
        }

        PaneAppearance appearance = paneAppearance();
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

    private PaneAppearance paneAppearance() {
        UltimateGlassBlocks.PaneFamily family = UltimateGlassBlocks.familyFor(getBlock());
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
