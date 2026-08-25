package com.github.tionard.ultimateglass.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

import com.github.tionard.ultimateglass.UltimateGlass;
import com.github.tionard.ultimateglass.item.GlaziersToolTier;

/** Authoritative crafting settings on a logical server, mirrored to connected clients. */
public final class UltimateGlassServerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("ultimate-glass-server.json");

    private static volatile boolean copperCraftingEnabled = true;
    private static volatile boolean ironCraftingEnabled = true;
    private static volatile boolean diamondCraftingEnabled = true;
    private static volatile boolean experimentalCompositesEnabled = false;
    private static volatile boolean temperedPanesAlwaysDrop = true;
    private static volatile boolean temperedToVanillaRecipeEnabled = false;
    private static volatile boolean glassChiselEnabled = true;

    private UltimateGlassServerConfig() {
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data != null) {
                apply(
                        data.copperCraftingEnabled,
                        data.ironCraftingEnabled,
                        data.diamondCraftingEnabled,
                        data.experimentalCompositesEnabled,
                        data.temperedPanesAlwaysDrop == null || data.temperedPanesAlwaysDrop,
                        Boolean.TRUE.equals(data.temperedToVanillaRecipeEnabled),
                        data.glassChiselEnabled == null || data.glassChiselEnabled,
                        false
                );
            }
        } catch (IOException | RuntimeException exception) {
            UltimateGlass.LOGGER.warn("Could not read server configuration; using defaults", exception);
            apply(true, true, true, false, true, false, true, false);
        }
    }

    public static boolean isCraftingEnabled(GlaziersToolTier tier) {
        return switch (tier) {
            case COPPER -> copperCraftingEnabled;
            case IRON -> ironCraftingEnabled;
            case DIAMOND -> diamondCraftingEnabled;
        };
    }

    public static boolean copperCraftingEnabled() {
        return copperCraftingEnabled;
    }

    public static boolean ironCraftingEnabled() {
        return ironCraftingEnabled;
    }

    public static boolean diamondCraftingEnabled() {
        return diamondCraftingEnabled;
    }

    public static boolean experimentalCompositesEnabled() {
        return experimentalCompositesEnabled;
    }

    public static boolean temperedPanesAlwaysDrop() {
        return temperedPanesAlwaysDrop;
    }

    public static boolean temperedToVanillaRecipeEnabled() {
        return temperedToVanillaRecipeEnabled;
    }

    public static boolean glassChiselEnabled() {
        return glassChiselEnabled;
    }

    public static void apply(
            boolean copper,
            boolean iron,
            boolean diamond,
            boolean experimentalComposites,
            boolean alwaysDropTemperedPanes,
            boolean temperedToVanillaRecipe,
            boolean chiselEnabled,
            boolean save
    ) {
        copperCraftingEnabled = copper;
        ironCraftingEnabled = iron;
        diamondCraftingEnabled = diamond;
        experimentalCompositesEnabled = experimentalComposites;
        temperedPanesAlwaysDrop = alwaysDropTemperedPanes;
        temperedToVanillaRecipeEnabled = temperedToVanillaRecipe;
        glassChiselEnabled = chiselEnabled;
        if (save) {
            save();
        }
    }

    private static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(new ConfigData(
                        copperCraftingEnabled,
                        ironCraftingEnabled,
                        diamondCraftingEnabled,
                        experimentalCompositesEnabled,
                        temperedPanesAlwaysDrop,
                        temperedToVanillaRecipeEnabled,
                        glassChiselEnabled
                ), writer);
            }
        } catch (IOException exception) {
            UltimateGlass.LOGGER.warn("Could not save server configuration", exception);
        }
    }

    private static final class ConfigData {
        private boolean copperCraftingEnabled = true;
        private boolean ironCraftingEnabled = true;
        private boolean diamondCraftingEnabled = true;
        private boolean experimentalCompositesEnabled = false;
        private Boolean temperedPanesAlwaysDrop = true;
        private Boolean temperedToVanillaRecipeEnabled = false;
        private Boolean glassChiselEnabled = true;

        private ConfigData() {
        }

        private ConfigData(
                boolean copper,
                boolean iron,
                boolean diamond,
                boolean experimentalComposites,
                boolean alwaysDropTemperedPanes,
                boolean temperedToVanillaRecipe,
                boolean chiselEnabled
        ) {
            this.copperCraftingEnabled = copper;
            this.ironCraftingEnabled = iron;
            this.diamondCraftingEnabled = diamond;
            this.experimentalCompositesEnabled = experimentalComposites;
            this.temperedPanesAlwaysDrop = alwaysDropTemperedPanes;
            this.temperedToVanillaRecipeEnabled = temperedToVanillaRecipe;
            this.glassChiselEnabled = chiselEnabled;
        }
    }
}
