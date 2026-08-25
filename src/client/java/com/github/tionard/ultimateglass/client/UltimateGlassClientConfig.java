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

public final class UltimateGlassClientConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("ultimate-glass-client.json");

    private static volatile boolean seamlessConnectedPanes = true;
    private static volatile boolean singleEdgeScriberMode = false;

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
                seamlessConnectedPanes = data.seamlessConnectedPanes == null
                        || data.seamlessConnectedPanes;
                singleEdgeScriberMode = Boolean.TRUE.equals(data.singleEdgeScriberMode);
            }
        } catch (IOException | RuntimeException exception) {
            UltimateGlass.LOGGER.warn("Could not read client configuration; using defaults", exception);
            seamlessConnectedPanes = true;
            singleEdgeScriberMode = false;
        }
    }

    public static boolean seamlessConnectedPanes() {
        return seamlessConnectedPanes;
    }

    public static boolean toggleSeamlessConnectedPanes() {
        seamlessConnectedPanes = !seamlessConnectedPanes;
        save();
        return seamlessConnectedPanes;
    }

    public static boolean singleEdgeScriberMode() {
        return singleEdgeScriberMode;
    }

    public static boolean toggleSingleEdgeScriberMode() {
        singleEdgeScriberMode = !singleEdgeScriberMode;
        save();
        return singleEdgeScriberMode;
    }

    public static boolean isCraftingEnabled(GlaziersToolTier tier) {
        return UltimateGlassServerConfig.isCraftingEnabled(tier);
    }

    public static void applyServerConfig(
            boolean copper,
            boolean iron,
            boolean diamond,
            boolean experimentalComposites,
            boolean alwaysDropTemperedPanes,
            boolean temperedToVanillaRecipe,
            boolean manualSeamToolEnabled
    ) {
        UltimateGlassServerConfig.apply(
                copper,
                iron,
                diamond,
                experimentalComposites,
                alwaysDropTemperedPanes,
                temperedToVanillaRecipe,
                manualSeamToolEnabled,
                false
        );
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
        UltimateGlassServerConfig.apply(
                copper,
                iron,
                diamond,
                UltimateGlassServerConfig.experimentalCompositesEnabled(),
                UltimateGlassServerConfig.temperedPanesAlwaysDrop(),
                UltimateGlassServerConfig.temperedToVanillaRecipeEnabled(),
                UltimateGlassServerConfig.manualSeamToolEnabled(),
                false
        );
    }

    public static boolean experimentalCompositesEnabled() {
        return UltimateGlassServerConfig.experimentalCompositesEnabled();
    }

    public static void setExperimentalCompositesEnabledLocally(boolean enabled) {
        UltimateGlassServerConfig.apply(
                UltimateGlassServerConfig.copperCraftingEnabled(),
                UltimateGlassServerConfig.ironCraftingEnabled(),
                UltimateGlassServerConfig.diamondCraftingEnabled(),
                enabled,
                UltimateGlassServerConfig.temperedPanesAlwaysDrop(),
                UltimateGlassServerConfig.temperedToVanillaRecipeEnabled(),
                UltimateGlassServerConfig.manualSeamToolEnabled(),
                false
        );
    }

    public static boolean temperedPanesAlwaysDrop() {
        return UltimateGlassServerConfig.temperedPanesAlwaysDrop();
    }

    public static void setTemperedPanesAlwaysDropLocally(boolean enabled) {
        UltimateGlassServerConfig.apply(
                UltimateGlassServerConfig.copperCraftingEnabled(),
                UltimateGlassServerConfig.ironCraftingEnabled(),
                UltimateGlassServerConfig.diamondCraftingEnabled(),
                UltimateGlassServerConfig.experimentalCompositesEnabled(),
                enabled,
                UltimateGlassServerConfig.temperedToVanillaRecipeEnabled(),
                UltimateGlassServerConfig.manualSeamToolEnabled(),
                false
        );
    }

    public static boolean temperedToVanillaRecipeEnabled() {
        return UltimateGlassServerConfig.temperedToVanillaRecipeEnabled();
    }

    public static void setTemperedToVanillaRecipeEnabledLocally(boolean enabled) {
        UltimateGlassServerConfig.apply(
                UltimateGlassServerConfig.copperCraftingEnabled(),
                UltimateGlassServerConfig.ironCraftingEnabled(),
                UltimateGlassServerConfig.diamondCraftingEnabled(),
                UltimateGlassServerConfig.experimentalCompositesEnabled(),
                UltimateGlassServerConfig.temperedPanesAlwaysDrop(),
                enabled,
                UltimateGlassServerConfig.manualSeamToolEnabled(),
                false
        );
    }

    public static boolean manualSeamToolEnabled() {
        return UltimateGlassServerConfig.manualSeamToolEnabled();
    }

    public static void setManualSeamToolEnabledLocally(boolean enabled) {
        UltimateGlassServerConfig.apply(
                UltimateGlassServerConfig.copperCraftingEnabled(),
                UltimateGlassServerConfig.ironCraftingEnabled(),
                UltimateGlassServerConfig.diamondCraftingEnabled(),
                UltimateGlassServerConfig.experimentalCompositesEnabled(),
                UltimateGlassServerConfig.temperedPanesAlwaysDrop(),
                UltimateGlassServerConfig.temperedToVanillaRecipeEnabled(),
                enabled,
                false
        );
    }

    private static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(new ConfigData(seamlessConnectedPanes, singleEdgeScriberMode), writer);
            }
        } catch (IOException exception) {
            UltimateGlass.LOGGER.warn("Could not save client configuration", exception);
        }
    }

    private static final class ConfigData {
        private Boolean seamlessConnectedPanes = true;
        private Boolean singleEdgeScriberMode = false;

        private ConfigData() {
        }

        private ConfigData(boolean seamlessConnectedPanes, boolean singleEdgeScriberMode) {
            this.seamlessConnectedPanes = seamlessConnectedPanes;
            this.singleEdgeScriberMode = singleEdgeScriberMode;
        }
    }
}
