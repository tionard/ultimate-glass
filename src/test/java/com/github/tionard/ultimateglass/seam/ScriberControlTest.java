package com.github.tionard.ultimateglass.seam;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

final class ScriberControlTest {
    @Test
    void betaUsesPairedModeAndWholePaneResetPayloads() throws IOException {
        String payload = source("src/main/java/com/github/tionard/ultimateglass/network/PaneSeamEditPayload.java");
        String networking = source("src/main/java/com/github/tionard/ultimateglass/network/UltimateGlassNetworking.java");

        assertTrue(payload.contains("boolean resetAll"));
        assertTrue(payload.contains("boolean singleEdge"));
        assertTrue(networking.contains("seams.resetSeamOverrides()"));
        assertTrue(networking.contains("if (!payload.singleEdge())"));
        assertTrue(networking.contains("PaneSeamCounterpart.of"));
    }

    @Test
    void scriberModeDefaultsToPairedAndUsesConfigurableVBinding() throws IOException {
        String client = source("src/client/java/com/github/tionard/ultimateglass/client/UltimateGlassClient.java");
        String config = source("src/client/java/com/github/tionard/ultimateglass/client/UltimateGlassClientConfig.java");

        assertTrue(client.contains("GLFW.GLFW_KEY_V"));
        assertTrue(client.contains("GLFW.GLFW_KEY_B"));
        assertTrue(client.contains("TOGGLE_SCRIBER_MODE.consumeClick()"));
        assertTrue(config.contains("singleEdgeScriberMode = false"));
    }

    @Test
    void serverCanDisableScriberThroughItsSyncedConfig() throws IOException {
        String config = source("src/main/java/com/github/tionard/ultimateglass/config/UltimateGlassServerConfig.java");
        String screen = source("src/client/java/com/github/tionard/ultimateglass/client/UltimateGlassConfigScreen.java");
        String networking = source("src/main/java/com/github/tionard/ultimateglass/network/UltimateGlassNetworking.java");

        assertTrue(config.contains("manualSeamToolEnabled = true"));
        assertTrue(screen.contains("requestManualSeamToolToggle()"));
        assertTrue(networking.contains("!UltimateGlassServerConfig.manualSeamToolEnabled()"));
    }

    private static String source(String path) throws IOException {
        return Files.readString(Path.of(path));
    }
}
