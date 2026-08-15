package com.github.tionard.ultimateglass.pane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class PaneFrameTest {
    @Test
    void builtInFramesRemainFiniteAndDynamicFrameIsSeparate() {
        assertEquals(12, PaneFrame.woodFrames().size());
        assertFalse(PaneFrame.NONE.isFramed());
        assertTrue(PaneFrame.OAK.isFramed());
        assertTrue(PaneFrame.DYNAMIC.isDynamic());
        assertFalse(PaneFrame.woodFrames().contains(PaneFrame.DYNAMIC));
    }

    @Test
    void appearanceIncludesFrameIdentity() {
        PaneAppearance oak = new PaneAppearance(PaneMaterial.CLEAR, PaneFrame.OAK);
        PaneAppearance spruce = new PaneAppearance(PaneMaterial.CLEAR, PaneFrame.SPRUCE);
        assertTrue(oak.isFramed());
        assertFalse(oak.equals(spruce));
        assertEquals(PaneFrame.NONE, new PaneAppearance(PaneMaterial.CLEAR).frame());
    }
}
