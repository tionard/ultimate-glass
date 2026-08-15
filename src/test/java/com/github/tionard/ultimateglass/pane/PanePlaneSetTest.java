package com.github.tionard.ultimateglass.pane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class PanePlaneSetTest {
    @Test
    void setIsImmutableAndDoesNotDuplicatePlanes() {
        PanePlaneSet original = PanePlaneSet.of(PanePlane.EDGE_NORTH);
        PanePlaneSet expanded = original
                .plus(PanePlane.EDGE_NORTH)
                .plus(PanePlane.EDGE_EAST);

        assertEquals(1, original.size());
        assertEquals(2, expanded.size());
        assertTrue(expanded.contains(PanePlane.EDGE_NORTH));
        assertTrue(expanded.contains(PanePlane.EDGE_EAST));
        assertFalse(expanded.contains(PanePlane.CENTER_Z));
    }
}
