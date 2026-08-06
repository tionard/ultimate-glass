package com.github.tionard.ultimateglass.item;

public enum GlaziersToolTier {
    COPPER(false, false),
    IRON(true, false),
    DIAMOND(true, true);

    private final boolean canTogglePanePosition;
    private final boolean silkTouchesGlass;

    GlaziersToolTier(boolean canTogglePanePosition, boolean silkTouchesGlass) {
        this.canTogglePanePosition = canTogglePanePosition;
        this.silkTouchesGlass = silkTouchesGlass;
    }

    public boolean canTogglePanePosition() {
        return canTogglePanePosition;
    }

    public boolean silkTouchesGlass() {
        return silkTouchesGlass;
    }
}
