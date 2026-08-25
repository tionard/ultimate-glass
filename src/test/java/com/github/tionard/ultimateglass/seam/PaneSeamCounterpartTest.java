package com.github.tionard.ultimateglass.seam;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import com.github.tionard.ultimateglass.pane.PanePlane;

final class PaneSeamCounterpartTest {
    @Test
    void mapsSharedSeamToTheNeighborsOppositeBoundary() {
        PaneSeamCounterpart counterpart = PaneSeamCounterpart.of(
                new BlockPos(4, 7, 9),
                new PaneSeamTarget(PanePlane.EDGE_NORTH, Direction.EAST)
        );

        assertEquals(new BlockPos(5, 7, 9), counterpart.pos());
        assertEquals(PanePlane.EDGE_NORTH, counterpart.target().plane());
        assertEquals(Direction.WEST, counterpart.target().boundary());
    }

    @Test
    void verticalSeamsKeepTheSamePhysicalPlane() {
        PaneSeamCounterpart counterpart = PaneSeamCounterpart.of(
                BlockPos.ZERO,
                new PaneSeamTarget(PanePlane.CENTER_Z, Direction.UP)
        );

        assertEquals(new BlockPos(0, 1, 0), counterpart.pos());
        assertEquals(PanePlane.CENTER_Z, counterpart.target().plane());
        assertEquals(Direction.DOWN, counterpart.target().boundary());
    }
}
