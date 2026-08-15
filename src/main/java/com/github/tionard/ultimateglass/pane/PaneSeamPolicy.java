package com.github.tionard.ultimateglass.pane;

/** Decides when a generated pane boundary section is replaced by its seamless glass sample. */
public final class PaneSeamPolicy {
    private PaneSeamPolicy() {
    }

    public static boolean shouldReplaceBoundary(
            boolean preservePerpendicularOuterEdge,
            int borderCount,
            int continuingBorderCount
    ) {
        if (borderCount < 0
                || continuingBorderCount < 0
                || continuingBorderCount > borderCount) {
            throw new IllegalArgumentException("Invalid pane boundary counts");
        }
        if (borderCount == 0) {
            return false;
        }
        return preservePerpendicularOuterEdge
                ? continuingBorderCount == borderCount
                : continuingBorderCount > 0;
    }
}
