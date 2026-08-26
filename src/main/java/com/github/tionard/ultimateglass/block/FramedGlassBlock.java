package com.github.tionard.ultimateglass.block;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.github.tionard.ultimateglass.config.UltimateGlassServerConfig;
import com.github.tionard.ultimateglass.glass.GlassFamilyBlock;
import com.github.tionard.ultimateglass.glass.GlassVariant;
import com.github.tionard.ultimateglass.pane.PaneMaterial;
import com.github.tionard.ultimateglass.registry.UltimateGlassSmartItems;

/** Full framed glass block. Its model removes borders only beside an identical neighbour. */
public class FramedGlassBlock extends Block implements GlassFamilyBlock {
    private final GlassVariant variant;

    public FramedGlassBlock(GlassVariant variant, Properties properties) {
        super(properties);
        if (!variant.isFramed()) {
            throw new IllegalArgumentException("FramedGlassBlock requires a frame");
        }
        this.variant = variant;
    }

    @Override
    public GlassVariant glassVariant() {
        return variant;
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
        if ((!variant.tempered() || !UltimateGlassServerConfig.temperedPanesAlwaysDrop())
                && variant.material() != PaneMaterial.TINTED) {
            drops = super.getDrops(state, builder);
        } else {
            ItemStack stack = new ItemStack(asItem());
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

    public static void refreshModelsAround(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
            BlockPos neighbour = pos.relative(direction);
            BlockState neighbourState = level.getBlockState(neighbour);
            level.sendBlockUpdated(
                    neighbour, neighbourState, neighbourState, Block.UPDATE_ALL
            );
        }
    }
}
