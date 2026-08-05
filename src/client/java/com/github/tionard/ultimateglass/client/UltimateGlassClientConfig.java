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

public final class UltimateGlassClientConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("ultimate-glass-client.json");

    private static boolean toolEnabled = true;

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
                toolEnabled = data.toolEnabled;
            }
        } catch (IOException | RuntimeException exception) {
            UltimateGlass.LOGGER.warn("Could not read client configuration; using defaults", exception);
            toolEnabled = true;
        }
    }

    public static boolean isToolEnabled() {
        return toolEnabled;
    }

    public static void setToolEnabled(boolean enabled) {
        toolEnabled = enabled;
        save();
    }

    public static boolean toggleToolEnabled() {
        setToolEnabled(!toolEnabled);
        return toolEnabled;
    }

    private static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(new ConfigData(toolEnabled), writer);
            }
        } catch (IOException exception) {
            UltimateGlass.LOGGER.warn("Could not save client configuration", exception);
        }
    }

    private static final class ConfigData {
        private boolean toolEnabled = true;

        private ConfigData() {
        }

        private ConfigData(boolean toolEnabled) {
            this.toolEnabled = toolEnabled;
        }
    }
}
