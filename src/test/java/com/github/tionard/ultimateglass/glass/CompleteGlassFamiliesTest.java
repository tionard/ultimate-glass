package com.github.tionard.ultimateglass.glass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

final class CompleteGlassFamiliesTest {
    private static final Path GENERATED = Path.of("build/generated/ultimateGlassPaneResources");
    private static final List<String> MATERIALS = List.of(
            "glass",
            "white_stained_glass", "orange_stained_glass", "magenta_stained_glass",
            "light_blue_stained_glass", "yellow_stained_glass", "lime_stained_glass",
            "pink_stained_glass", "gray_stained_glass", "light_gray_stained_glass",
            "cyan_stained_glass", "purple_stained_glass", "blue_stained_glass",
            "brown_stained_glass", "green_stained_glass", "red_stained_glass",
            "black_stained_glass", "tinted_glass"
    );
    private static final List<String> PANES = MATERIALS.stream()
            .map(name -> name.equals("glass") ? "glass_pane" : name + "_pane")
            .toList();
    private static final List<String> WOODS = List.of(
            "oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "pale_oak",
            "crimson", "warped", "mangrove", "bamboo", "cherry"
    );

    @Test
    void everyMaterialHasOneToOneFurnaceAndBlastFurnaceTempering() throws IOException {
        Path recipes = GENERATED.resolve("data/ultimateglass/recipe");
        for (String material : MATERIALS) {
            JsonObject smelting = readJson(recipes.resolve("ultimate_" + material + ".json"));
            JsonObject blasting = readJson(recipes.resolve(
                    "ultimate_" + material + "_from_blasting.json"
            ));
            assertEquals("minecraft:" + material, smelting.get("ingredient").getAsString());
            assertEquals(
                    "ultimateglass:tempered_glass",
                    smelting.getAsJsonObject("result").get("id").getAsString()
            );
            assertEquals(
                    "minecraft:" + material,
                    smelting.getAsJsonObject("result")
                            .getAsJsonObject("components")
                            .get("ultimateglass:glass_material")
                            .getAsString()
            );
            assertFalse(smelting.getAsJsonObject("result").has("count"));
            assertEquals("minecraft:blasting", blasting.get("type").getAsString());
        }
    }

    @Test
    void ordinaryAndTemperedFamiliesExistForEveryBuiltInWood() {
        Path blockstates = GENERATED.resolve("assets/ultimateglass/blockstates");
        for (int material = 0; material < MATERIALS.size(); material++) {
            for (String wood : WOODS) {
                assertTrue(Files.exists(blockstates.resolve(
                        wood + "_framed_" + PANES.get(material) + ".json"
                )));
                assertTrue(Files.exists(blockstates.resolve(
                        wood + "_framed_" + MATERIALS.get(material) + ".json"
                )));
                assertTrue(Files.exists(blockstates.resolve(
                        wood + "_framed_ultimate_" + MATERIALS.get(material) + ".json"
                )));
            }
            assertTrue(Files.exists(blockstates.resolve(
                    "modded_framed_" + PANES.get(material) + ".json"
            )));
            assertTrue(Files.exists(blockstates.resolve(
                    "modded_framed_" + MATERIALS.get(material) + ".json"
            )));
            assertTrue(Files.exists(blockstates.resolve(
                    "modded_framed_ultimate_" + MATERIALS.get(material) + ".json"
            )));
        }
    }

    @Test
    void paneTemperingProducesTheSharedComponentBackedItem() throws IOException {
        JsonObject recipe = readJson(GENERATED.resolve(
                "data/ultimateglass/recipe/ultimate_blue_stained_glass_pane.json"
        ));
        JsonObject result = recipe.getAsJsonObject("result");

        assertEquals("ultimateglass:tempered_glass_pane", result.get("id").getAsString());
        assertEquals(
                "minecraft:blue_stained_glass",
                result.getAsJsonObject("components")
                        .get("ultimateglass:glass_material")
                        .getAsString()
        );
    }

    @Test
    void fullBlockFrameModelCarriesNormalDynamicAndSeamReplacementMarkers()
            throws IOException {
        JsonArray fixed = readJson(GENERATED.resolve(
                "assets/ultimateglass/models/block/framed_glass_block_base.json"
        )).getAsJsonArray("elements");
        JsonArray dynamic = readJson(GENERATED.resolve(
                "assets/ultimateglass/models/block/dynamic_framed_glass_block_base.json"
        )).getAsJsonArray("elements");

        assertEquals(102, fixed.size());
        assertEquals(48, markerCount(fixed, 21));
        assertEquals(48, markerCount(fixed, 23));
        assertEquals(48, markerCount(dynamic, 22));
        assertEquals(48, markerCount(dynamic, 23));
    }

    @Test
    void sharedFrameRenderingAndSettingsCoverFullBlocks() throws IOException {
        String renderer = Files.readString(Path.of(
                "src/client/java/com/github/tionard/ultimateglass/client/render/SeamlessPaneModels.java"
        ));
        String reverseRecipe = Files.readString(Path.of(
                "src/main/java/com/github/tionard/ultimateglass/recipe/TemperedToVanillaRecipe.java"
        ));
        String framedRecipe = Files.readString(Path.of(
                "src/main/java/com/github/tionard/ultimateglass/recipe/WoodFramedPaneRecipe.java"
        ));
        String properties = Files.readString(Path.of("gradle.properties"));

        assertTrue(renderer.contains("matchingFramedGlassNeighbour"));
        assertTrue(renderer.contains("FRAMED_BLOCK_SEAM_FILL_TINT_INDEX"));
        assertTrue(reverseRecipe.contains("vanillaStackForTempered"));
        assertTrue(framedRecipe.contains("unframedVariant"));
        assertTrue(properties.contains("mod_version=0.2.2c"));
    }

    @Test
    void transitionalBlocksReturnSmartStacksInsteadOfLegacyItems() throws IOException {
        String smartItems = Files.readString(Path.of(
                "src/main/java/com/github/tionard/ultimateglass/registry/UltimateGlassSmartItems.java"
        ));
        String framedBlock = Files.readString(Path.of(
                "src/main/java/com/github/tionard/ultimateglass/block/FramedGlassBlock.java"
        ));
        String framedPane = Files.readString(Path.of(
                "src/main/java/com/github/tionard/ultimateglass/block/FramedVanillaPaneBlock.java"
        ));

        assertTrue(smartItems.contains("UltimateGlassBlocks.paneFamilies().forEach"));
        assertTrue(smartItems.contains("UltimateGlassFamilies.variants().forEach"));
        assertTrue(smartItems.contains("Item.BY_BLOCK.put(block, item)"));
        assertTrue(framedBlock.contains("UltimateGlassSmartItems.stackForBlock(this)"));
        assertTrue(framedPane.contains("UltimateGlassSmartItems.stackForBlock(this)"));
    }

    private static long markerCount(JsonArray elements, int marker) {
        long count = 0;
        for (JsonElement value : elements) {
            JsonObject faces = value.getAsJsonObject().getAsJsonObject("faces");
            for (String face : faces.keySet()) {
                JsonObject data = faces.getAsJsonObject(face);
                if (data.has("tintindex") && data.get("tintindex").getAsInt() == marker) {
                    count++;
                }
            }
        }
        return count;
    }

    private static JsonObject readJson(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
