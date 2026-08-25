package com.github.tionard.ultimateglass.seam;

import com.mojang.serialization.Codec;

import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.github.tionard.ultimateglass.pane.PanePlane;

/** Compact tri-state seam overrides stored as two non-overlapping bit masks. */
public final class PaneSeamData {
    private static final String VISIBLE_KEY = "visible_pane_seams";
    private static final String SEAMLESS_KEY = "seamless_pane_seams";

    private long visibleMask;
    private long seamlessMask;

    public PaneSeamOverride get(PanePlane plane, Direction boundary) {
        long bit = new PaneSeamTarget(plane, boundary).bit();
        if ((visibleMask & bit) != 0L) {
            return PaneSeamOverride.VISIBLE;
        }
        if ((seamlessMask & bit) != 0L) {
            return PaneSeamOverride.SEAMLESS;
        }
        return PaneSeamOverride.AUTOMATIC;
    }

    public boolean set(PanePlane plane, Direction boundary, PaneSeamOverride override) {
        long bit = new PaneSeamTarget(plane, boundary).bit();
        long previousVisible = visibleMask;
        long previousSeamless = seamlessMask;
        visibleMask &= ~bit;
        seamlessMask &= ~bit;
        if (override == PaneSeamOverride.VISIBLE) {
            visibleMask |= bit;
        } else if (override == PaneSeamOverride.SEAMLESS) {
            seamlessMask |= bit;
        }
        return visibleMask != previousVisible || seamlessMask != previousSeamless;
    }

    public long visibleMask() {
        return visibleMask;
    }

    public long seamlessMask() {
        return seamlessMask;
    }

    public boolean isEmpty() {
        return visibleMask == 0L && seamlessMask == 0L;
    }

    public PaneSeamData copy() {
        PaneSeamData copy = new PaneSeamData();
        copy.visibleMask = visibleMask;
        copy.seamlessMask = seamlessMask;
        return copy;
    }

    public boolean copyFrom(PaneSeamData source) {
        long previousVisible = visibleMask;
        long previousSeamless = seamlessMask;
        visibleMask = source == null ? 0L : source.visibleMask;
        seamlessMask = source == null ? 0L : source.seamlessMask;
        return visibleMask != previousVisible || seamlessMask != previousSeamless;
    }

    /** Moves overrides between edge-bound and centered versions of the same physical plane. */
    public void remapPlane(PanePlane from, PanePlane to) {
        if (from == to) {
            return;
        }
        long oldVisible = visibleMask;
        long oldSeamless = seamlessMask;
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() == from.axis()) {
                continue;
            }
            long fromBit = new PaneSeamTarget(from, direction).bit();
            if ((oldVisible & fromBit) != 0L && direction.getAxis() != to.axis()) {
                set(to, direction, PaneSeamOverride.VISIBLE);
            }
            if ((oldSeamless & fromBit) != 0L && direction.getAxis() != to.axis()) {
                set(to, direction, PaneSeamOverride.SEAMLESS);
            }
            visibleMask &= ~fromBit;
            seamlessMask &= ~fromBit;
        }
    }

    /** Rotates every saved boundary together with a pane changed by a Glazier's Tool. */
    public void rotateAround(Direction.Axis axis) {
        long oldVisible = visibleMask;
        long oldSeamless = seamlessMask;
        visibleMask = 0L;
        seamlessMask = 0L;
        for (PanePlane plane : PanePlane.values()) {
            for (Direction direction : Direction.values()) {
                if (direction.getAxis() == plane.axis()) {
                    continue;
                }
                PaneSeamTarget oldTarget = new PaneSeamTarget(plane, direction);
                PanePlane rotatedPlane = plane.rotateAround(axis);
                Direction rotatedBoundary = PanePlane.rotateDirection(direction, axis);
                if ((oldVisible & oldTarget.bit()) != 0L) {
                    set(rotatedPlane, rotatedBoundary, PaneSeamOverride.VISIBLE);
                }
                if ((oldSeamless & oldTarget.bit()) != 0L) {
                    set(rotatedPlane, rotatedBoundary, PaneSeamOverride.SEAMLESS);
                }
            }
        }
    }

    public void load(ValueInput input) {
        visibleMask = input.read(VISIBLE_KEY, Codec.LONG).orElse(0L);
        seamlessMask = input.read(SEAMLESS_KEY, Codec.LONG).orElse(0L) & ~visibleMask;
    }

    public void save(ValueOutput output) {
        if (visibleMask != 0L) {
            output.store(VISIBLE_KEY, Codec.LONG, visibleMask);
        }
        if (seamlessMask != 0L) {
            output.store(SEAMLESS_KEY, Codec.LONG, seamlessMask);
        }
    }
}
