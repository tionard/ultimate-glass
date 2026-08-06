package com.github.tionard.ultimateglass.placement;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.entity.player.Player;

public final class ShiftPlacementModeState {
    public static final ShiftPlacementMode DEFAULT_MODE = ShiftPlacementMode.FACE;
    private static final Map<UUID, ShiftPlacementMode> PLAYER_MODES = new ConcurrentHashMap<>();

    private ShiftPlacementModeState() {
    }

    public static ShiftPlacementMode get(Player player) {
        return PLAYER_MODES.getOrDefault(player.getUUID(), DEFAULT_MODE);
    }

    public static void set(Player player, ShiftPlacementMode mode) {
        PLAYER_MODES.put(player.getUUID(), mode);
    }

    public static void remove(Player player) {
        PLAYER_MODES.remove(player.getUUID());
    }
}
