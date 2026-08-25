package com.github.tionard.ultimateglass.seam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.core.Direction;

import com.github.tionard.ultimateglass.pane.PanePlane;

final class PaneSeamDataTest {
    @Test
    void everyBoundaryKeepsAnIndependentChoice() {
        PaneSeamData data = new PaneSeamData();
        data.set(PanePlane.EDGE_NORTH, Direction.UP, PaneSeamOverride.VISIBLE);
        data.set(PanePlane.EDGE_NORTH, Direction.EAST, PaneSeamOverride.SEAMLESS);

        assertEquals(
                PaneSeamOverride.VISIBLE,
                data.get(PanePlane.EDGE_NORTH, Direction.UP)
        );
        assertEquals(
                PaneSeamOverride.SEAMLESS,
                data.get(PanePlane.EDGE_NORTH, Direction.EAST)
        );

        data.set(PanePlane.EDGE_NORTH, Direction.UP, PaneSeamOverride.AUTOMATIC);
        assertEquals(
                PaneSeamOverride.AUTOMATIC,
                data.get(PanePlane.EDGE_NORTH, Direction.UP)
        );
        assertEquals(
                PaneSeamOverride.SEAMLESS,
                data.get(PanePlane.EDGE_NORTH, Direction.EAST)
        );
    }

    @Test
    void existingToolsRotateManualChoicesWithTheGlass() {
        PaneSeamData data = new PaneSeamData();
        data.set(PanePlane.EDGE_NORTH, Direction.UP, PaneSeamOverride.VISIBLE);
        data.set(PanePlane.EDGE_NORTH, Direction.WEST, PaneSeamOverride.SEAMLESS);

        data.rotateAround(Direction.Axis.Y);

        assertEquals(
                PaneSeamOverride.VISIBLE,
                data.get(PanePlane.EDGE_EAST, Direction.UP)
        );
        assertEquals(
                PaneSeamOverride.SEAMLESS,
                data.get(PanePlane.EDGE_EAST, Direction.NORTH)
        );
    }

    @Test
    void togglingPositionKeepsPrimaryPlaneChoices() {
        PaneSeamData data = new PaneSeamData();
        data.set(PanePlane.EDGE_NORTH, Direction.WEST, PaneSeamOverride.VISIBLE);

        data.remapPlane(PanePlane.EDGE_NORTH, PanePlane.CENTER_Z);

        assertEquals(
                PaneSeamOverride.AUTOMATIC,
                data.get(PanePlane.EDGE_NORTH, Direction.WEST)
        );
        assertEquals(
                PaneSeamOverride.VISIBLE,
                data.get(PanePlane.CENTER_Z, Direction.WEST)
        );
    }

    @Test
    void normalClickAlwaysChoosesTheOppositeVisibleResult() {
        assertEquals(
                PaneSeamOverride.SEAMLESS,
                PaneSeamOverride.AUTOMATIC.oppositeOfCurrent(false)
        );
        assertEquals(
                PaneSeamOverride.VISIBLE,
                PaneSeamOverride.AUTOMATIC.oppositeOfCurrent(true)
        );
        assertEquals(
                PaneSeamOverride.SEAMLESS,
                PaneSeamOverride.VISIBLE.oppositeOfCurrent(true)
        );
        assertEquals(
                PaneSeamOverride.VISIBLE,
                PaneSeamOverride.SEAMLESS.oppositeOfCurrent(false)
        );
    }

    @Test
    void shiftResetClearsEveryPlaneAndBoundaryAtOnce() {
        PaneSeamData data = new PaneSeamData();
        data.set(PanePlane.EDGE_NORTH, Direction.UP, PaneSeamOverride.VISIBLE);
        data.set(PanePlane.CENTER_X, Direction.SOUTH, PaneSeamOverride.SEAMLESS);

        assertFalse(data.isEmpty());
        assertTrue(data.reset());
        assertTrue(data.isEmpty());
        assertEquals(
                PaneSeamOverride.AUTOMATIC,
                data.get(PanePlane.EDGE_NORTH, Direction.UP)
        );
        assertEquals(
                PaneSeamOverride.AUTOMATIC,
                data.get(PanePlane.CENTER_X, Direction.SOUTH)
        );
        assertFalse(data.reset());
    }
}
