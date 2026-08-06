package com.github.tionard.ultimateglass.placement;

public enum ShiftPlacementMode {
    FACE,
    NEAR;

    private static final ShiftPlacementMode[] VALUES = values();

    public ShiftPlacementMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public static ShiftPlacementMode fromOrdinal(int ordinal) {
        return VALUES[Math.floorMod(ordinal, VALUES.length)];
    }
}
