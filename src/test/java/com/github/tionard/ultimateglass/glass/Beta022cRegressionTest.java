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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

final class Beta022cRegressionTest {
    private static final Path GENERATED = Path.of("build/generated/ultimateGlassPaneResources");

    @Test
    void framedFullBlockItemsInheritVanillaBlockTransforms() throws IOException {
        for (String model : List.of(
                "framed_glass_block_base.json",
                "dynamic_framed_glass_block_base.json"
        )) {
            JsonObject base = readJson(GENERATED.resolve(
                    "assets/ultimateglass/models/block/" + model
            ));
            assertEquals("minecraft:block/block", base.get("parent").getAsString());
        }
    }

    @Test
    void modPanesShareCompactHeldAndGroundTransforms() throws IOException {
        JsonObject display = readJson(Path.of(
                "src/main/resources/assets/ultimateglass/models/item/glass_pane_display.json"
        )).getAsJsonObject("display");
        assertScale(display, "ground", 0.375F);
        assertScale(display, "thirdperson_righthand", 0.4F);
        assertScale(display, "firstperson_righthand", 0.5F);

        JsonObject normalPane = readJson(GENERATED.resolve(
                "assets/ultimateglass/models/item/ultimate_glass_pane.json"
        ));
        JsonObject framedPane = readJson(GENERATED.resolve(
                "assets/ultimateglass/models/block/edge_pane_item_framed_base.json"
        ));
        assertEquals(
                "ultimateglass:item/glass_pane_display",
                normalPane.get("parent").getAsString()
        );
        assertEquals(
                "ultimateglass:item/glass_pane_display",
                framedPane.get("parent").getAsString()
        );
    }

    @Test
    void dynamicGlassUsesVanillaShaderCompatibleItemSheets() throws IOException {
        String renderer = Files.readString(Path.of(
                "src/client/java/com/github/tionard/ultimateglass/client/render/"
                        + "DynamicFramePaneItemRenderer.java"
        ));
        assertTrue(renderer.contains("Sheets.translucentBlockItemSheet()"));
        assertTrue(renderer.contains("Sheets.cutoutBlockItemSheet()"));
        assertTrue(renderer.contains("RenderTypes.glintTranslucent()"));
        assertFalse(renderer.contains("RenderTypes.itemTranslucent("));
    }

    @Test
    void sixMatchingTemperedBlocksProduceSixteenMatchingPanes() throws IOException {
        JsonObject recipe = readJson(Path.of(
                "src/main/resources/data/ultimateglass/recipe/"
                        + "tempered_glass_pane_from_blocks.json"
        ));
        assertEquals(
                "ultimateglass:tempered_glass_pane_from_blocks",
                recipe.get("type").getAsString()
        );

        String source = Files.readString(Path.of(
                "src/main/java/com/github/tionard/ultimateglass/recipe/"
                        + "TemperedPaneFromBlocksRecipe.java"
        ));
        assertTrue(source.contains("input.ingredientCount() != 6"));
        assertTrue(source.contains("variant.form() != GlassForm.BLOCK"));
        assertTrue(source.contains("material != variant.material()"));
        assertTrue(source.contains("SmartGlassKind.TEMPERED_PANE"));
        assertTrue(source.contains("result.setCount(RESULT_COUNT)"));
        assertTrue(source.contains("RESULT_COUNT = 16"));
    }

    private static void assertScale(JsonObject display, String context, float expected) {
        JsonArray scale = display.getAsJsonObject(context).getAsJsonArray("scale");
        assertEquals(expected, scale.get(0).getAsFloat());
        assertEquals(expected, scale.get(1).getAsFloat());
        assertEquals(expected, scale.get(2).getAsFloat());
    }

    private static JsonObject readJson(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
