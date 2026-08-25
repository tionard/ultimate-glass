package com.github.tionard.ultimateglass.seam;

import net.minecraft.core.Direction;

import com.github.tionard.ultimateglass.pane.PanePlane;

/** Implemented by pane block entities capable of persisting manual boundary choices. */
public interface PaneSeamSource {
    PaneSeamData seamData();

    void markSeamsChanged();

    default PaneSeamOverride seamOverride(PanePlane plane, Direction boundary) {
        return seamData().get(plane, boundary);
    }

    default boolean setSeamOverride(
            PanePlane plane,
            Direction boundary,
            PaneSeamOverride override
    ) {
        if (!seamData().set(plane, boundary, override)) {
            return false;
        }
        markSeamsChanged();
        return true;
    }

    default boolean resetSeamOverrides() {
        if (!seamData().reset()) {
            return false;
        }
        markSeamsChanged();
        return true;
    }
}
