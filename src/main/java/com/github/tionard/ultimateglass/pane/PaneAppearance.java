package com.github.tionard.ultimateglass.pane;

import java.util.Objects;

/**
 * Geometry-independent pane appearance.
 */
public record PaneAppearance(PaneMaterial material, PaneFrame frame) {
    public PaneAppearance(PaneMaterial material) {
        this(material, PaneFrame.NONE);
    }

    public PaneAppearance {
        Objects.requireNonNull(material, "material");
        Objects.requireNonNull(frame, "frame");
    }

    public boolean isFramed() {
        return frame.isFramed();
    }
}
