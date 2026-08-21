package com.github.tionard.ultimateglass.placement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

final class PanePlacementResolverTest {
    @Test
    void normalPlacementUsesTheClosestClickedEdgeOnHorizontalFaces() {
        assertEquals(Direction.NORTH, PanePlacementResolver.closestEdge(Direction.UP, 0.5, 1.0, 0.05));
        assertEquals(Direction.SOUTH, PanePlacementResolver.closestEdge(Direction.DOWN, 0.5, 0.0, 0.95));
        assertEquals(Direction.WEST, PanePlacementResolver.closestEdge(Direction.UP, 0.05, 1.0, 0.5));
        assertEquals(Direction.EAST, PanePlacementResolver.closestEdge(Direction.UP, 0.95, 1.0, 0.5));
    }

    @Test
    void normalPlacementUsesTheClosestClickedEdgeOnVerticalFaces() {
        assertEquals(Direction.DOWN, PanePlacementResolver.closestEdge(Direction.NORTH, 0.5, 0.05, 0.0));
        assertEquals(Direction.UP, PanePlacementResolver.closestEdge(Direction.SOUTH, 0.5, 0.95, 1.0));
        assertEquals(Direction.NORTH, PanePlacementResolver.closestEdge(Direction.WEST, 0.0, 0.5, 0.05));
        assertEquals(Direction.SOUTH, PanePlacementResolver.closestEdge(Direction.EAST, 1.0, 0.5, 0.95));
    }

    @Test
    void worldCoordinatesAreReducedToTheClickedFaceCell() {
        assertEquals(
                Direction.EAST,
                PanePlacementResolver.resolve(
                        false, Direction.UP, new Vec3(12.95, 65.0, -3.5), false
                )
        );
    }

    @Test
    void shiftedOrdinaryPaneLiesAgainstTheClickedFace() {
        assertEquals(
                Direction.DOWN,
                PanePlacementResolver.resolve(true, Direction.UP, Vec3.ZERO, false)
        );
        assertEquals(
                Direction.EAST,
                PanePlacementResolver.resolve(true, Direction.WEST, Vec3.ZERO, false)
        );
    }

    @Test
    void shiftedCompositeUsesTheSameFaceInsideTheClickedHostCell() {
        assertEquals(
                Direction.WEST,
                PanePlacementResolver.resolve(true, Direction.WEST, Vec3.ZERO, true)
        );
    }
}
