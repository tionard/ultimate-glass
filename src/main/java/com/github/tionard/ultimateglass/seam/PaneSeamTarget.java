package com.github.tionard.ultimateglass.seam;

import net.minecraft.core.Direction;

import com.github.tionard.ultimateglass.pane.PanePlane;

/** One independently editable outer boundary of a physical pane plane. */
public record PaneSeamTarget(PanePlane plane, Direction boundary) {
    public PaneSeamTarget {
        if (boundary.getAxis() == plane.axis()) {
            throw new IllegalArgumentException("Pane boundary must lie within its plane");
        }
    }

    public int bitIndex() {
        return plane.ordinal() * Direction.values().length + boundary.ordinal();
    }

    public long bit() {
        return 1L << bitIndex();
    }
}
