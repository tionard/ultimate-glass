package com.github.tionard.ultimateglass.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

/** Protects the normal and centre-sampled surface variants used by seamless stained panes. */
final class GeneratedPaneModelTest {
    private static final Path MODEL_ROOT = Path.of(
            "build/generated/ultimateGlassPaneResources/assets/ultimateglass/models/block"
    );
    private static final Path GENERATED_ROOT = Path.of("build/generated/ultimateGlassPaneResources");

    @Test
    void singleEdgePaneProvidesInteriorSamplesForEveryBoundarySection() throws IOException {
        assertEquals(new PaneElementCounts(9, 8), paneElementCounts("edge_pane_shape_0_base.json"));
    }

    @Test
    void fullyConnectedEdgeGeometryDoesNotAddSamplesAtInCellJunctions() throws IOException {
        assertEquals(new PaneElementCounts(9, 4), paneElementCounts("edge_pane_shape_15_base.json"));
    }

    @Test
    void centeredPaneProvidesInteriorSamplesForEveryBoundarySection() throws IOException {
        assertEquals(new PaneElementCounts(9, 8), paneElementCounts("centered_pane_base.json"));
    }

    @Test
    void everyEdgeSeamReplacementSamplesThePaneTextureCenter() throws IOException {
        for (int mask = 0; mask < 16; mask++) {
            paneElementCounts("edge_pane_shape_" + mask + "_base.json");
        }
    }

    @Test
    void tintedFamilyGeneratesBothGeometriesAndNonLossyRecipe() throws IOException {
        Path assets = GENERATED_ROOT.resolve("assets/ultimateglass");
        assertTrue(Files.exists(assets.resolve("blockstates/edge_tinted_glass_pane.json")));
        assertTrue(Files.exists(assets.resolve("blockstates/centered_tinted_glass_pane.json")));

        Path recipeRoot = GENERATED_ROOT.resolve("data/ultimateglass/recipe");
        JsonObject recipe = readJson(recipeRoot.resolve("ultimate_tinted_glass_pane.json"));
        assertEquals("minecraft:crafting_shaped", recipe.get("type").getAsString());
        assertEquals(16, recipe.getAsJsonObject("result").get("count").getAsInt());
        assertFalse(Files.exists(recipeRoot.resolve(
                "tinted_glass_pane_from_ultimate_tinted_glass_pane.json")));
    }

    private static JsonObject readJson(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static PaneElementCounts paneElementCounts(String modelName) throws IOException {
        try (Reader reader = Files.newBufferedReader(MODEL_ROOT.resolve(modelName))) {
            JsonArray elements = JsonParser.parseReader(reader)
                    .getAsJsonObject()
                    .getAsJsonArray("elements");
            int normal = 0;
            int seamless = 0;
            for (JsonElement element : elements) {
                JsonObject faces = element.getAsJsonObject().getAsJsonObject("faces");
                JsonObject paneFace = faces.entrySet().stream()
                        .map(entry -> entry.getValue().getAsJsonObject())
                        .filter(face -> "#pane".equals(face.get("texture").getAsString()))
                        .findFirst()
                        .orElse(null);
                if (paneFace == null) {
                    continue;
                }

                if (paneFace.has("tintindex")) {
                    assertEquals(15, paneFace.get("tintindex").getAsInt());
                    assertEquals("[7,7,9,9]", paneFace.getAsJsonArray("uv").toString());
                    seamless++;
                } else {
                    normal++;
                }
            }
            return new PaneElementCounts(normal, seamless);
        }
    }

    private record PaneElementCounts(int normal, int seamless) {
    }
}
