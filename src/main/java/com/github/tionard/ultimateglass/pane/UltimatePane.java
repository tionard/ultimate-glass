package com.github.tionard.ultimateglass.pane;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Shared contract implemented by ordinary Ultimate pane geometries. */
public interface UltimatePane {
    PaneAppearance appearance();

    PaneGeometry geometry(BlockState state);

    Block vanillaPane();
}
