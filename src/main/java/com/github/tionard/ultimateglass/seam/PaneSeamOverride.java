package com.github.tionard.ultimateglass.seam;

/** Player-selected rendering behavior for one boundary of one pane plane. */
public enum PaneSeamOverride {
    AUTOMATIC,
    VISIBLE,
    SEAMLESS;

    public boolean resolvesToSeamless(boolean automaticState) {
        return switch (this) {
            case AUTOMATIC -> automaticState;
            case VISIBLE -> false;
            case SEAMLESS -> true;
        };
    }

    public PaneSeamOverride oppositeOfCurrent(boolean automaticState) {
        return resolvesToSeamless(automaticState) ? VISIBLE : SEAMLESS;
    }
}
