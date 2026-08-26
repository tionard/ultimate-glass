package com.github.tionard.ultimateglass.block;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

import com.github.tionard.ultimateglass.glass.GlassFamilyBlock;
import com.github.tionard.ultimateglass.glass.GlassVariant;
import com.github.tionard.ultimateglass.pane.PaneMaterial;

/** A breakable framed pane with ordinary vanilla connection and placement behaviour. */
public class FramedVanillaPaneBlock extends IronBarsBlock implements GlassFamilyBlock {
    private final GlassVariant variant;

    public FramedVanillaPaneBlock(GlassVariant variant, Properties properties) {
        super(properties);
        this.variant = variant;
    }

    @Override
    public GlassVariant glassVariant() {
        return variant;
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
        if (variant.material() != PaneMaterial.TINTED) {
            return super.getDrops(state, builder);
        }
        ItemStack stack = new ItemStack(asItem());
        return stack.isEmpty() ? List.of() : List.of(stack);
    }
}
