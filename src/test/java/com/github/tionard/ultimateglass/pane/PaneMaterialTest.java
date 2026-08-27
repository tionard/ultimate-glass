package com.github.tionard.ultimateglass.pane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class PaneMaterialTest {
    @Test
    void currentMaterialsRetainTheirVanillaPanePaths() {
        assertEquals("glass_pane", PaneMaterial.CLEAR.vanillaPanePath());
        assertEquals(
                "light_blue_stained_glass_pane",
                PaneMaterial.LIGHT_BLUE_STAINED.vanillaPanePath()
        );

        long currentMaterialCount = java.util.Arrays.stream(PaneMaterial.values())
                .filter(PaneMaterial::hasVanillaPane)
                .count();
        assertEquals(17L, currentMaterialCount);
    }

    @Test
    void tintedIsDistinctFromDecorativeColors() {
        assertEquals(PaneMaterial.Kind.TINTED, PaneMaterial.TINTED.kind());
        assertFalse(PaneMaterial.TINTED.hasVanillaPane());
        assertThrows(IllegalStateException.class, PaneMaterial.TINTED::vanillaPanePath);
        assertTrue(PaneMaterial.BLUE_STAINED.hasVanillaPane());
        assertEquals(PaneMaterial.Kind.STAINED, PaneMaterial.BLUE_STAINED.kind());
    }

    @Test
    void stackComponentIdentityRoundTripsEveryMaterial() {
        for (PaneMaterial material : PaneMaterial.values()) {
            assertEquals(material, PaneMaterial.fromComponentId(material.componentId()));
        }
    }
}
