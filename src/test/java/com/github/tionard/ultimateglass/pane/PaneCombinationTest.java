package com.github.tionard.ultimateglass.pane;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

final class PaneCombinationTest {
    private static final Identifier OAK = Identifier.withDefaultNamespace("oak_planks");
    private static final Identifier BIRCH = Identifier.withDefaultNamespace("birch_planks");

    @Test
    void allGlassAndFrameIdentitiesRequireExactMatches() {
        for (PaneMaterial first : PaneMaterial.values()) {
            for (PaneMaterial second : PaneMaterial.values()) {
                for (PaneFrame firstFrame : PaneFrame.values()) {
                    for (PaneFrame secondFrame : PaneFrame.values()) {
                        boolean sameFrame = firstFrame == secondFrame
                                || (firstFrame == PaneFrame.OAK && secondFrame == PaneFrame.DYNAMIC)
                                || (firstFrame == PaneFrame.DYNAMIC && secondFrame == PaneFrame.OAK);
                        assertEquals(first == second && sameFrame, PaneCombination.matches(
                                new PaneAppearance(first, firstFrame), OAK,
                                new PaneAppearance(second, secondFrame), OAK));
                    }
                }
            }
        }
    }

    @Test
    void dynamicWoodIdentityIsNotJustTheDynamicFrameEnum() {
        var appearance = new PaneAppearance(PaneMaterial.CLEAR, PaneFrame.DYNAMIC);
        assertFalse(PaneCombination.matches(appearance, OAK, appearance, BIRCH));
        var moddedWood = Identifier.fromNamespaceAndPath("example", "willow_planks");
        assertTrue(PaneCombination.matches(appearance, moddedWood, appearance, moddedWood));
        assertFalse(PaneCombination.matches(appearance, OAK, appearance, moddedWood));
    }
}
