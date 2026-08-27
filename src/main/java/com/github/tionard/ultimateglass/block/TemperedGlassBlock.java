package com.github.tionard.ultimateglass.block;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.github.tionard.ultimateglass.config.UltimateGlassServerConfig;
import com.github.tionard.ultimateglass.glass.GlassFamilyBlock;
import com.github.tionard.ultimateglass.glass.GlassForm;
import com.github.tionard.ultimateglass.glass.GlassVariant;
import com.github.tionard.ultimateglass.pane.PaneFrame;
import com.github.tionard.ultimateglass.pane.PaneMaterial;
import com.github.tionard.ultimateglass.registry.UltimateGlassSmartItems;

/** A full glass block upgraded through the same tempering progression as panes. */
public final class TemperedGlassBlock extends Block implements GlassFamilyBlock {
    private final GlassVariant variant;

    public TemperedGlassBlock(PaneMaterial material, Properties properties) {
        super(properties);
        this.variant = new GlassVariant(material, GlassForm.BLOCK, true, PaneFrame.NONE);
    }

    @Override
    public GlassVariant glassVariant() {
        return variant;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        return adjacentState.getBlock() == this || super.skipRendering(state, adjacentState, direction);
    }

    @Override
    protected VoxelShape getVisualShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context
    ) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return variant.material() != PaneMaterial.TINTED && super.propagatesSkylightDown(state);
    }

    @Override
    protected int getLightDampening(BlockState state) {
        return variant.material() == PaneMaterial.TINTED ? 15 : super.getLightDampening(state);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops;
        if (!UltimateGlassServerConfig.temperedPanesAlwaysDrop()) {
            drops = super.getDrops(state, builder);
        } else {
            ItemStack stack = UltimateGlassSmartItems.stackForBlock(this);
            drops = stack.isEmpty() ? List.of() : List.of(stack);
        }
        return UltimateGlassSmartItems.modernizeDrops(this, drops);
    }

    @Override
    protected ItemStack getCloneItemStack(
            LevelReader level, BlockPos pos, BlockState state, boolean includeData
    ) {
        return UltimateGlassSmartItems.stackForBlock(this);
    }
}
