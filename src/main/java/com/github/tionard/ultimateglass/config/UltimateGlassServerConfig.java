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
                apply(data.copperCraftingEnabled, data.ironCraftingEnabled, data.diamondCraftingEnabled, false);
            }
        } catch (IOException | RuntimeException exception) {
            UltimateGlass.LOGGER.warn("Could not read server configuration; using defaults", exception);
            apply(true, true, true, false);
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

    public static void apply(boolean copper, boolean iron, boolean diamond, boolean save) {
        copperCraftingEnabled = copper;
        ironCraftingEnabled = iron;
        diamondCraftingEnabled = diamond;
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
                        diamondCraftingEnabled
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

        private ConfigData() {
        }

        private ConfigData(boolean copper, boolean iron, boolean diamond) {
            this.copperCraftingEnabled = copper;
            this.ironCraftingEnabled = iron;
            this.diamondCraftingEnabled = diamond;
        }
    }
}
