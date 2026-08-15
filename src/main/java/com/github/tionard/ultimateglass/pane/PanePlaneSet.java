package com.github.tionard.ultimateglass.pane;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.stream.Stream;

/** Compact immutable set of physical pane planes. */
public final class PanePlaneSet implements Iterable<PanePlane> {
    public static final PanePlaneSet EMPTY = new PanePlaneSet(0);

    private static final PanePlane[] VALUES = PanePlane.values();

    private final int mask;

    private PanePlaneSet(int mask) {
        this.mask = mask;
    }

    public static PanePlaneSet of(PanePlane first, PanePlane... rest) {
        int mask = bit(first);
        for (PanePlane plane : rest) {
            mask |= bit(plane);
        }
        return new PanePlaneSet(mask);
    }

    public PanePlaneSet plus(PanePlane plane) {
        int updated = mask | bit(plane);
        return updated == mask ? this : new PanePlaneSet(updated);
    }

    public boolean contains(PanePlane plane) {
        return (mask & bit(plane)) != 0;
    }

    public boolean isEmpty() {
        return mask == 0;
    }

    public int size() {
        return Integer.bitCount(mask);
    }

    public Stream<PanePlane> stream() {
        return Stream.of(VALUES).filter(this::contains);
    }

    @Override
    public void forEach(Consumer<? super PanePlane> action) {
        stream().forEach(action);
    }

    @Override
    public Iterator<PanePlane> iterator() {
        return new Iterator<>() {
            private int index;

            @Override
            public boolean hasNext() {
                while (index < VALUES.length && !contains(VALUES[index])) {
                    index++;
                }
                return index < VALUES.length;
            }

            @Override
            public PanePlane next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return VALUES[index++];
            }
        };
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof PanePlaneSet set && mask == set.mask;
    }

    @Override
    public int hashCode() {
        return mask;
    }

    @Override
    public String toString() {
        return stream().map(Enum::name).toList().toString();
    }

    private static int bit(PanePlane plane) {
        return 1 << plane.ordinal();
    }
}
