package com.github.tionard.ultimateglass.glass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class SmartGlassKindTest {
    @Test
    void exposesSixInventoryFamiliesWithoutWoodOrColourProducts() {
        assertEquals(6, SmartGlassKind.values().length);
        assertEquals("tempered_glass_pane", SmartGlassKind.TEMPERED_PANE.itemPath());
        assertEquals(
                "framed_tempered_glass",
                SmartGlassKind.FRAMED_TEMPERED_BLOCK.itemPath()
        );
    }

    @Test
    void formTemperingAndFrameRemainStructuralInsteadOfComponents() {
        assertEquals(GlassForm.PANE, SmartGlassKind.FRAMED_PANE.form());
        assertFalse(SmartGlassKind.FRAMED_PANE.tempered());
        assertTrue(SmartGlassKind.FRAMED_PANE.framed());
        assertEquals(GlassForm.BLOCK, SmartGlassKind.TEMPERED_BLOCK.form());
        assertTrue(SmartGlassKind.TEMPERED_BLOCK.tempered());
        assertFalse(SmartGlassKind.TEMPERED_BLOCK.framed());
    }
}
