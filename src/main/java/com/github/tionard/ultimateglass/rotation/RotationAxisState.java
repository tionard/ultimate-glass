package com.github.tionard.ultimateglass.rotation;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public final class RotationAxisState {
    public static final Direction.Axis DEFAULT_AXIS = Direction.Axis.Y;
    private static final Direction.Axis[] ORDER = {
            Direction.Axis.X,
            Direction.Axis.Y,
            Direction.Axis.Z
    };
    private static final Map<UUID, Direction.Axis> PLAYER_AXES = new ConcurrentHashMap<>();

    private RotationAxisState() {
    }

    public static Direction.Axis get(Player player) {
        return PLAYER_AXES.getOrDefault(player.getUUID(), DEFAULT_AXIS);
    }

    public static void set(Player player, Direction.Axis axis) {
        PLAYER_AXES.put(player.getUUID(), axis);
    }

    public static Direction.Axis fromOrdinal(int ordinal) {
        return ORDER[Math.floorMod(ordinal, ORDER.length)];
    }

    public static int ordinal(Direction.Axis axis) {
        return switch (axis) {
            case X -> 0;
            case Y -> 1;
            case Z -> 2;
        };
    }

    public static Direction.Axis next(Direction.Axis axis) {
        return fromOrdinal(ordinal(axis) + 1);
    }
}
