package com.github.tionard.ultimateglass.block;

import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;

/** A normal connected pane counterpart to vanilla tinted glass. */
public final class TintedGlassPaneBlock extends IronBarsBlock {
    public TintedGlassPaneBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return false;
    }

    @Override
    protected int getLightDampening(BlockState state) {
        return 15;
    }
}
