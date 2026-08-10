package com.github.tionard.ultimateglass.client;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

import com.github.tionard.ultimateglass.UltimateGlass;
import com.github.tionard.ultimateglass.config.UltimateGlassServerConfig;
import com.github.tionard.ultimateglass.item.GlaziersToolTier;
import com.github.tionard.ultimateglass.placement.ShiftPlacementMode;

public final class UltimateGlassClientConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("ultimate-glass-client.json");

    private static ShiftPlacementMode shiftPlacementMode = ShiftPlacementMode.FACE;
    private static volatile boolean seamlessConnectedPanes = true;

    private UltimateGlassClientConfig() {
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data != null) {
                if (data.shiftPlacementMode != null) {
                    shiftPlacementMode = data.shiftPlacementMode;
                }
                seamlessConnectedPanes = data.seamlessConnectedPanes == null
                        || data.seamlessConnectedPanes;
            }
        } catch (IOException | RuntimeException exception) {
            UltimateGlass.LOGGER.warn("Could not read client configuration; using defaults", exception);
            shiftPlacementMode = ShiftPlacementMode.FACE;
            seamlessConnectedPanes = true;
        }
    }

    public static ShiftPlacementMode shiftPlacementMode() {
        return shiftPlacementMode;
    }

    public static ShiftPlacementMode toggleShiftPlacementMode() {
        shiftPlacementMode = shiftPlacementMode.next();
        save();
        return shiftPlacementMode;
    }

    public static boolean seamlessConnectedPanes() {
        return seamlessConnectedPanes;
    }

    public static boolean toggleSeamlessConnectedPanes() {
        seamlessConnectedPanes = !seamlessConnectedPanes;
        save();
        return seamlessConnectedPanes;
    }

    public static boolean isCraftingEnabled(GlaziersToolTier tier) {
        return UltimateGlassServerConfig.isCraftingEnabled(tier);
    }

    public static void applyServerCraftingConfig(boolean copper, boolean iron, boolean diamond) {
        UltimateGlassServerConfig.apply(copper, iron, diamond, false);
    }

    public static void setCraftingEnabledLocally(GlaziersToolTier tier, boolean enabled) {
        boolean copper = UltimateGlassServerConfig.copperCraftingEnabled();
        boolean iron = UltimateGlassServerConfig.ironCraftingEnabled();
        boolean diamond = UltimateGlassServerConfig.diamondCraftingEnabled();
        switch (tier) {
            case COPPER -> copper = enabled;
            case IRON -> iron = enabled;
            case DIAMOND -> diamond = enabled;
        }
        UltimateGlassServerConfig.apply(copper, iron, diamond, false);
    }

    private static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(new ConfigData(shiftPlacementMode, seamlessConnectedPanes), writer);
            }
        } catch (IOException exception) {
            UltimateGlass.LOGGER.warn("Could not save client configuration", exception);
        }
    }

    private static final class ConfigData {
        private ShiftPlacementMode shiftPlacementMode = ShiftPlacementMode.FACE;
        private Boolean seamlessConnectedPanes = true;

        private ConfigData() {
        }

        private ConfigData(ShiftPlacementMode shiftPlacementMode, boolean seamlessConnectedPanes) {
            this.shiftPlacementMode = shiftPlacementMode;
            this.seamlessConnectedPanes = seamlessConnectedPanes;
        }
    }
}
