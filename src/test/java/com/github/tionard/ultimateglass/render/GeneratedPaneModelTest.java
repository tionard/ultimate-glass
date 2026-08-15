package com.github.tionard.ultimateglass.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        for (int mask : new int[] {1, 2, 4}) {
            assertEquals(
                    new PaneElementCounts(9, 8),
                    paneElementCounts("centered_pane_shape_" + mask + "_base.json")
            );
        }
    }

    @Test
    void centeredJunctionModelsTrimEveryTransparentIntersection() throws IOException {
        for (int mask : new int[] {3, 5, 6}) {
            assertEquals(
                    new PaneElementCounts(24, 20),
                    paneElementCounts("centered_pane_shape_" + mask + "_base.json")
            );
        }
        assertEquals(
                new PaneElementCounts(48, 36),
                paneElementCounts("centered_pane_shape_7_base.json")
        );

        for (int mask = 1; mask < 8; mask++) {
            assertNoOverlappingNonSeamFaces("centered_pane_shape_" + mask + "_base.json");
        }
    }

    @Test
    void centeredBlockStateMapsAllPrimaryAndRelativeConnectionStates() throws IOException {
        JsonObject variants = readJson(GENERATED_ROOT.resolve(
                "assets/ultimateglass/blockstates/centered_glass_pane.json"
        )).getAsJsonObject("variants");
        assertEquals(12, variants.size());

        assertCenteredVariant(variants, "x", false, false, 1);
        assertCenteredVariant(variants, "x", true, false, 3);
        assertCenteredVariant(variants, "x", false, true, 5);
        assertCenteredVariant(variants, "x", true, true, 7);
        assertCenteredVariant(variants, "y", false, false, 2);
        assertCenteredVariant(variants, "y", true, false, 3);
        assertCenteredVariant(variants, "y", false, true, 6);
        assertCenteredVariant(variants, "y", true, true, 7);
        assertCenteredVariant(variants, "z", false, false, 4);
        assertCenteredVariant(variants, "z", true, false, 5);
        assertCenteredVariant(variants, "z", false, true, 6);
        assertCenteredVariant(variants, "z", true, true, 7);
    }

    @Test
    void everyEdgeSeamReplacementSamplesThePaneTextureCenter() throws IOException {
        for (int mask = 0; mask < 16; mask++) {
            paneElementCounts("edge_pane_shape_" + mask + "_base.json");
        }
    }

    @Test
    void tintedFamilyGeneratesVanillaPaneAndTemperedCookingProgression() throws IOException {
        Path assets = GENERATED_ROOT.resolve("assets/ultimateglass");
        assertTrue(Files.exists(assets.resolve("blockstates/tinted_glass_pane.json")));
        assertTrue(Files.exists(assets.resolve("blockstates/edge_tinted_glass_pane.json")));
        assertTrue(Files.exists(assets.resolve("blockstates/centered_tinted_glass_pane.json")));
        assertTrue(Files.exists(assets.resolve(
                "models/block/centered_tinted_glass_pane_shape_7.json")));

        Path recipeRoot = GENERATED_ROOT.resolve("data/ultimateglass/recipe");
        JsonObject vanillaRecipe = readJson(recipeRoot.resolve("tinted_glass_pane.json"));
        assertEquals("minecraft:crafting_shaped", vanillaRecipe.get("type").getAsString());
        assertEquals(16, vanillaRecipe.getAsJsonObject("result").get("count").getAsInt());
        assertEquals(
                "ultimateglass:tinted_glass_pane",
                vanillaRecipe.getAsJsonObject("result").get("id").getAsString()
        );

        JsonObject temperedRecipe = readJson(recipeRoot.resolve("ultimate_tinted_glass_pane.json"));
        assertEquals("minecraft:smelting", temperedRecipe.get("type").getAsString());
        assertEquals(
                "ultimateglass:tinted_glass_pane",
                temperedRecipe.get("ingredient").getAsString()
        );
        assertTrue(Files.exists(recipeRoot.resolve("ultimate_tinted_glass_pane_from_blasting.json")));
        assertFalse(Files.exists(recipeRoot.resolve(
                "tinted_glass_pane_from_ultimate_tinted_glass_pane.json")));
    }

    @Test
    void framedModelsAddOnePixelFaceBandAndMarkDynamicWoodQuads() throws IOException {
        JsonArray fixed = readJson(MODEL_ROOT.resolve(
                "edge_pane_shape_0_framed_base.json"
        )).getAsJsonArray("elements");
        JsonArray dynamic = readJson(MODEL_ROOT.resolve(
                "edge_pane_shape_0_dynamic_framed_base.json"
        )).getAsJsonArray("elements");

        assertTrue(hasOnePixelBroadWoodBand(fixed, false));
        assertTrue(hasOnePixelBroadWoodBand(dynamic, true));
        assertTrue(Files.exists(MODEL_ROOT.resolve("edge_oak_framed_glass_pane_shape_0.json")));
        assertTrue(Files.exists(MODEL_ROOT.resolve("edge_modded_framed_glass_pane_shape_0.json")));
        JsonObject itemBase = readJson(MODEL_ROOT.resolve("edge_pane_item_framed_base.json"));
        assertFalse(itemBase.toString().contains("tintindex"));
        JsonObject oakItem = readJson(GENERATED_ROOT.resolve(
                "assets/ultimateglass/models/item/oak_framed_ultimate_glass_pane.json"
        ));
        assertEquals(
                "ultimateglass:block/edge_pane_item_framed_base",
                oakItem.get("parent").getAsString()
        );

        JsonObject framedRecipe = readJson(GENERATED_ROOT.resolve(
                "data/ultimateglass/recipe/wood_framed_pane.json"
        ));
        assertEquals("ultimateglass:wood_framed_pane", framedRecipe.get("type").getAsString());
    }

    @Test
    void framedSeamSamplesKeepTheirRealPixelDimensions() throws IOException {
        assertSeamSampleDimensions("edge_pane_shape_0_framed_base.json");
        assertSeamSampleDimensions("edge_pane_shape_0_dynamic_framed_base.json");
    }

    @Test
    void everyFramedTwoPixelBoundarySectionHasASeamReplacement() throws IOException {
        assertEveryBoundarySectionHasFill("edge_pane_shape_0_framed_base.json");
        assertEveryBoundarySectionHasFill("edge_pane_shape_0_dynamic_framed_base.json");
    }

    @Test
    void dynamicItemRendererUsesNormalizedAtlasCoordinates() throws IOException {
        String source = Files.readString(Path.of(
                "src/client/java/com/github/tionard/ultimateglass/client/render/"
                        + "DynamicFramePaneItemRenderer.java"
        ));
        assertTrue(source.contains(".setUv(sprite.getU(u), sprite.getV(v))"));
        assertFalse(source.contains("getU(u * 16"));
        assertFalse(source.contains("getV(v * 16"));
    }

    @Test
    void ordinaryPanesUseFurnaceAndBlastFurnaceTempering() throws IOException {
        Path recipeRoot = GENERATED_ROOT.resolve("data/ultimateglass/recipe");
        JsonObject smelting = readJson(recipeRoot.resolve("ultimate_glass_pane.json"));
        JsonObject blasting = readJson(recipeRoot.resolve("ultimate_glass_pane_from_blasting.json"));
        assertEquals("minecraft:smelting", smelting.get("type").getAsString());
        assertEquals("minecraft:glass_pane", smelting.get("ingredient").getAsString());
        assertEquals("minecraft:blasting", blasting.get("type").getAsString());
        assertFalse(Files.exists(recipeRoot.resolve(
                "glass_pane_from_ultimate_glass_pane.json"
        )));
    }

    private static boolean hasOnePixelBroadWoodBand(JsonArray elements, boolean dynamic) {
        for (JsonElement value : elements) {
            JsonObject element = value.getAsJsonObject();
            int[] from = coordinates(element.getAsJsonArray("from"));
            int[] to = coordinates(element.getAsJsonArray("to"));
            boolean onePixelBand = (from[0] == 0 && to[0] == 1)
                    || (from[0] == 15 && to[0] == 16)
                    || (from[1] == 0 && to[1] == 1)
                    || (from[1] == 15 && to[1] == 16);
            if (!onePixelBand) {
                continue;
            }
            for (String broadFace : List.of("north", "south")) {
                if (!element.getAsJsonObject("faces").has(broadFace)) {
                    continue;
                }
                JsonObject face = element.getAsJsonObject("faces").getAsJsonObject(broadFace);
                if ("#edge".equals(face.get("texture").getAsString())
                        && face.has("tintindex")
                        && face.get("tintindex").getAsInt() == (dynamic ? 12 : 13)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static JsonObject readJson(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static void assertCenteredVariant(
            JsonObject variants,
            String axis,
            boolean connectFirst,
            boolean connectSecond,
            int shapeMask
    ) {
        String key = "axis=" + axis
                + ",connect_first=" + connectFirst
                + ",connect_second=" + connectSecond;
        assertEquals(
                "ultimateglass:block/centered_glass_pane_shape_" + shapeMask,
                variants.getAsJsonObject(key).get("model").getAsString()
        );
    }

    private static void assertNoOverlappingNonSeamFaces(String modelName) throws IOException {
        JsonArray elements = readJson(MODEL_ROOT.resolve(modelName)).getAsJsonArray("elements");
        List<ModelFace> faces = new ArrayList<>();
        for (JsonElement elementValue : elements) {
            JsonObject element = elementValue.getAsJsonObject();
            int[] from = coordinates(element.getAsJsonArray("from"));
            int[] to = coordinates(element.getAsJsonArray("to"));
            for (var entry : element.getAsJsonObject("faces").entrySet()) {
                JsonObject face = entry.getValue().getAsJsonObject();
                if (face.has("tintindex")) {
                    continue;
                }
                ModelFace candidate = ModelFace.of(entry.getKey(), from, to);
                for (ModelFace existing : faces) {
                    assertFalse(
                            candidate.overlaps(existing),
                            () -> modelName + " contains overlapping " + candidate + " and " + existing
                    );
                }
                faces.add(candidate);
            }
        }
    }

    private static int[] coordinates(JsonArray values) {
        return new int[] {
                values.get(0).getAsInt(),
                values.get(1).getAsInt(),
                values.get(2).getAsInt()
        };
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
                    seamless++;
                } else {
                    normal++;
                }
            }
            return new PaneElementCounts(normal, seamless);
        }
    }

    private static void assertSeamSampleDimensions(String modelName) throws IOException {
        JsonArray elements = readJson(MODEL_ROOT.resolve(modelName)).getAsJsonArray("elements");
        for (JsonElement elementValue : elements) {
            JsonObject element = elementValue.getAsJsonObject();
            int[] from = coordinates(element.getAsJsonArray("from"));
            int[] to = coordinates(element.getAsJsonArray("to"));
            for (var entry : element.getAsJsonObject("faces").entrySet()) {
                JsonObject face = entry.getValue().getAsJsonObject();
                if (!face.has("tintindex")
                        || (face.get("tintindex").getAsInt() != 11
                        && face.get("tintindex").getAsInt() != 15)) {
                    continue;
                }

                int[] uvAxes = switch (entry.getKey()) {
                    case "west", "east" -> new int[] {2, 1};
                    case "down", "up" -> new int[] {0, 2};
                    case "north", "south" -> new int[] {0, 1};
                    default -> throw new IllegalArgumentException(entry.getKey());
                };
                JsonArray uv = face.getAsJsonArray("uv");
                float u0 = uv.get(0).getAsFloat();
                float v0 = uv.get(1).getAsFloat();
                float u1 = uv.get(2).getAsFloat();
                float v1 = uv.get(3).getAsFloat();
                assertEquals(to[uvAxes[0]] - from[uvAxes[0]], u1 - u0, 0.0001F);
                assertEquals(to[uvAxes[1]] - from[uvAxes[1]], v1 - v0, 0.0001F);
                assertEquals(16.0F, u0 + u1, 0.0001F);
                assertEquals(16.0F, v0 + v1, 0.0001F);
            }
        }
    }

    private static void assertEveryBoundarySectionHasFill(String modelName) throws IOException {
        JsonArray elements = readJson(MODEL_ROOT.resolve(modelName)).getAsJsonArray("elements");
        Set<String> normalBoundarySections = new HashSet<>();
        Set<String> seamBoundarySections = new HashSet<>();
        for (JsonElement elementValue : elements) {
            JsonObject element = elementValue.getAsJsonObject();
            JsonObject faces = element.getAsJsonObject("faces");
            if (!faces.has("north")) {
                continue;
            }
            int[] from = coordinates(element.getAsJsonArray("from"));
            int[] to = coordinates(element.getAsJsonArray("to"));
            if (to[0] > 2 && from[0] < 14 && to[1] > 2 && from[1] < 14) {
                continue;
            }

            JsonObject face = faces.getAsJsonObject("north");
            String section = element.getAsJsonArray("from") + ":"
                    + element.getAsJsonArray("to");
            int marker = face.has("tintindex") ? face.get("tintindex").getAsInt() : -1;
            if (marker == 11 || marker == 15) {
                seamBoundarySections.add(section);
            } else if ("#pane".equals(face.get("texture").getAsString())
                    || "#edge".equals(face.get("texture").getAsString())) {
                normalBoundarySections.add(section);
            }
        }

        assertEquals(24, normalBoundarySections.size());
        assertEquals(normalBoundarySections, seamBoundarySections);
    }

    private record PaneElementCounts(int normal, int seamless) {
    }

    private record ModelFace(
            String direction,
            int axis,
            int plane,
            int firstMin,
            int firstMax,
            int secondMin,
            int secondMax
    ) {
        private static ModelFace of(String direction, int[] from, int[] to) {
            int axis = switch (direction) {
                case "west", "east" -> 0;
                case "down", "up" -> 1;
                case "north", "south" -> 2;
                default -> throw new IllegalArgumentException(direction);
            };
            boolean positive = direction.equals("east")
                    || direction.equals("up")
                    || direction.equals("south");
            int firstAxis = (axis + 1) % 3;
            int secondAxis = (axis + 2) % 3;
            return new ModelFace(
                    direction,
                    axis,
                    positive ? to[axis] : from[axis],
                    from[firstAxis],
                    to[firstAxis],
                    from[secondAxis],
                    to[secondAxis]
            );
        }

        private boolean overlaps(ModelFace other) {
            return direction.equals(other.direction)
                    && axis == other.axis
                    && plane == other.plane
                    && Math.max(firstMin, other.firstMin) < Math.min(firstMax, other.firstMax)
                    && Math.max(secondMin, other.secondMin) < Math.min(secondMax, other.secondMax);
        }
    }
}
