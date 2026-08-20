package com.github.tionard.ultimateglass.placement;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;

/** Resolves normal stair-like placement and the exact clicked-face Shift override. */
public final class PanePlacementResolver {
    private PanePlacementResolver() {
    }

    public static Direction resolve(BlockPlaceContext context) {
        Player player = context.getPlayer();
        return resolve(
                player != null && player.isShiftKeyDown(),
                context.getClickedFace(),
                horizontalFacing(player),
                false
        );
    }

    /** Composite panes occupy the clicked host cell, so its clicked face is not inverted. */
    public static Direction resolveComposite(UseOnContext context) {
        Player player = context.getPlayer();
        return resolve(
                player != null && player.isShiftKeyDown(),
                context.getClickedFace(),
                horizontalFacing(player),
                true
        );
    }

    static Direction resolve(
            boolean shifted,
            Direction clickedFace,
            Direction playerFacing,
            boolean occupiesClickedCell
    ) {
        if (!shifted) {
            return playerFacing;
        }
        return occupiesClickedCell ? clickedFace : clickedFace.getOpposite();
    }

    private static Direction horizontalFacing(Player player) {
        return player == null ? Direction.NORTH : player.getDirection();
    }
}
