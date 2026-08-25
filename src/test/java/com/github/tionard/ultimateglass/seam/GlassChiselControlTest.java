package com.github.tionard.ultimateglass.seam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

final class GlassChiselControlTest {
    @Test
    void releaseUsesPairedModeAndWholePaneResetPayloads() throws IOException {
        String payload = source("src/main/java/com/github/tionard/ultimateglass/network/PaneSeamEditPayload.java");
        String networking = source("src/main/java/com/github/tionard/ultimateglass/network/UltimateGlassNetworking.java");

        assertTrue(payload.contains("boolean resetAll"));
        assertTrue(payload.contains("boolean singleEdge"));
        assertTrue(networking.contains("seams.resetSeamOverrides()"));
        assertTrue(networking.contains("if (!payload.singleEdge())"));
        assertTrue(networking.contains("PaneSeamCounterpart.of"));
    }

    @Test
    void chiselModeDefaultsToPairedAndUsesConfigurableVBinding() throws IOException {
        String client = source("src/client/java/com/github/tionard/ultimateglass/client/UltimateGlassClient.java");
        String config = source("src/client/java/com/github/tionard/ultimateglass/client/UltimateGlassClientConfig.java");

        assertEquals(2, client.split("GLFW.GLFW_KEY_V", -1).length - 1);
        assertFalse(client.contains("GLFW.GLFW_KEY_B"));
        assertTrue(client.contains("TOGGLE_GLASS_CHISEL_MODE.consumeClick()"));
        assertTrue(client.contains("isGlassChiselActive(client.player)"));
        assertTrue(client.contains("isGlaziersToolActive(client.player)"));
        assertTrue(config.contains("singleEdgeChiselMode = false"));
    }

    @Test
    void serverCanDisableGlassChiselThroughItsSyncedConfig() throws IOException {
        String config = source("src/main/java/com/github/tionard/ultimateglass/config/UltimateGlassServerConfig.java");
        String screen = source("src/client/java/com/github/tionard/ultimateglass/client/UltimateGlassConfigScreen.java");
        String networking = source("src/main/java/com/github/tionard/ultimateglass/network/UltimateGlassNetworking.java");

        assertTrue(config.contains("glassChiselEnabled = true"));
        assertTrue(screen.contains("requestGlassChiselToggle()"));
        assertTrue(networking.contains("!UltimateGlassServerConfig.glassChiselEnabled()"));
    }

    @Test
    void glassChiselActionsDoNotWriteChatMessages() throws IOException {
        String networking = source("src/main/java/com/github/tionard/ultimateglass/network/UltimateGlassNetworking.java");
        String client = source("src/client/java/com/github/tionard/ultimateglass/client/UltimateGlassClient.java");

        assertFalse(networking.contains("sendSystemMessage"));
        assertFalse(client.contains("message.ultimateglass.chisel"));
    }

    @Test
    void releaseUsesTheNewIdentityRecipeAndCrispSprite() throws IOException {
        String recipe = source("src/main/resources/data/ultimateglass/recipe/glass_chisel.json");
        BufferedImage sprite = ImageIO.read(Path.of(
                "src/main/resources/assets/ultimateglass/textures/item/glass_chisel.png"
        ).toFile());

        assertTrue(recipe.contains("minecraft:crafting_shapeless"));
        assertTrue(recipe.contains("minecraft:emerald"));
        assertTrue(recipe.contains("minecraft:string"));
        assertTrue(recipe.contains("minecraft:stick"));
        assertTrue(recipe.contains("ultimateglass:glass_chisel"));
        assertEquals(16, sprite.getWidth());
        assertEquals(16, sprite.getHeight());
    }

    private static String source(String path) throws IOException {
        return Files.readString(Path.of(path));
    }
}
