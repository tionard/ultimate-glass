package com.github.tionard.ultimateglass.pane;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class PaneSeamPolicyTest {
    @Test
    void connectedSideKeepsOnePixelPerpendicularOuterCorner() {
        assertFalse(PaneSeamPolicy.shouldReplaceBoundary(true, 2, 1));
    }

    @Test
    void fullyConnectedCornerBecomesSeamless() {
        assertTrue(PaneSeamPolicy.shouldReplaceBoundary(true, 2, 2));
    }

    @Test
    void connectedOneBorderStripBecomesSeamless() {
        assertTrue(PaneSeamPolicy.shouldReplaceBoundary(true, 1, 1));
    }

    @Test
    void twoPixelInnerBoundaryDisappearsOnEitherConnection() {
        assertTrue(PaneSeamPolicy.shouldReplaceBoundary(false, 2, 1));
    }
}
