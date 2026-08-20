package com.github.tionard.ultimateglass.placement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

final class PanePlacementResolverTest {
    @Test
    void normalPlacementAlwaysUsesThePlayersStairLikeFacing() {
        assertEquals(
                Direction.EAST,
                PanePlacementResolver.resolve(false, Direction.UP, Direction.EAST, false)
        );
        assertEquals(
                Direction.NORTH,
                PanePlacementResolver.resolve(false, Direction.WEST, Direction.NORTH, true)
        );
    }

    @Test
    void shiftedOrdinaryPaneLiesAgainstTheClickedFace() {
        assertEquals(
                Direction.DOWN,
                PanePlacementResolver.resolve(true, Direction.UP, Direction.EAST, false)
        );
        assertEquals(
                Direction.EAST,
                PanePlacementResolver.resolve(true, Direction.WEST, Direction.NORTH, false)
        );
    }

    @Test
    void shiftedCompositeUsesTheSameFaceInsideTheClickedHostCell() {
        assertEquals(
                Direction.WEST,
                PanePlacementResolver.resolve(true, Direction.WEST, Direction.NORTH, true)
        );
    }
}
