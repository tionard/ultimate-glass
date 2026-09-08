package com.github.tionard.ultimateglass.pane;

import java.util.Objects;

import net.minecraft.resources.Identifier;

/** Uniform material identity, including equivalent legacy and component-backed wood frames. */
public final class PaneCombination {
    private PaneCombination() {
    }

    public static boolean matches(
            PaneAppearance first, Identifier firstWood,
            PaneAppearance second, Identifier secondWood
    ) {
        return first.material() == second.material()
                && Objects.equals(frameId(first.frame(), firstWood), frameId(second.frame(), secondWood));
    }

    private static Identifier frameId(PaneFrame frame, Identifier dynamicWood) {
        if (!frame.isFramed()) {
            return null;
        }
        return frame.isDynamic() ? dynamicWood
                : Identifier.withDefaultNamespace(frame.path() + "_planks");
    }
}
