package com.github.tionard.ultimateglass.pane;

import java.util.Objects;

/**
 * Geometry-independent pane appearance. Frame and decoration data can be added here as those
 * systems are introduced, while ordinary panes continue deriving the value from block identity.
 */
public record PaneAppearance(PaneMaterial material) {
    public PaneAppearance {
        Objects.requireNonNull(material, "material");
    }
}
