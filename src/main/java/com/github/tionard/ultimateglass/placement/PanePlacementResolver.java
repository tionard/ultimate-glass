package com.github.tionard.ultimateglass.placement;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;

/** Resolves cursor-nearest edge placement and the exact clicked-face Shift override. */
public final class PanePlacementResolver {
    private PanePlacementResolver() {
    }

    public static Direction resolve(BlockPlaceContext context) {
        Player player = context.getPlayer();
        return resolve(
                player != null && player.isShiftKeyDown(),
                context.getClickedFace(),
                context.getClickLocation(),
                false
        );
    }

    /** Composite panes occupy the clicked host cell, so its clicked face is not inverted. */
    public static Direction resolveComposite(UseOnContext context) {
        Player player = context.getPlayer();
        return resolve(
                player != null && player.isShiftKeyDown(),
                context.getClickedFace(),
                context.getClickLocation(),
                true
        );
    }

    static Direction resolve(
            boolean shifted,
            Direction clickedFace,
            Vec3 clickLocation,
            boolean occupiesClickedCell
    ) {
        if (shifted) {
            return occupiesClickedCell ? clickedFace : clickedFace.getOpposite();
        }
        return closestEdge(clickedFace, local(clickLocation.x), local(clickLocation.y), local(clickLocation.z));
    }

    static Direction closestEdge(Direction clickedFace, double x, double y, double z) {
        return switch (clickedFace.getAxis()) {
            case X -> nearest(y, Direction.DOWN, Direction.UP, z, Direction.NORTH, Direction.SOUTH);
            case Y -> nearest(x, Direction.WEST, Direction.EAST, z, Direction.NORTH, Direction.SOUTH);
            case Z -> nearest(x, Direction.WEST, Direction.EAST, y, Direction.DOWN, Direction.UP);
        };
    }

    private static Direction nearest(
            double firstCoordinate,
            Direction firstMinimum,
            Direction firstMaximum,
            double secondCoordinate,
            Direction secondMinimum,
            Direction secondMaximum
    ) {
        double firstDistance = Math.min(firstCoordinate, 1.0 - firstCoordinate);
        double secondDistance = Math.min(secondCoordinate, 1.0 - secondCoordinate);
        if (firstDistance <= secondDistance) {
            return firstCoordinate <= 0.5 ? firstMinimum : firstMaximum;
        }
        return secondCoordinate <= 0.5 ? secondMinimum : secondMaximum;
    }

    private static double local(double coordinate) {
        return coordinate - Math.floor(coordinate);
    }
}
