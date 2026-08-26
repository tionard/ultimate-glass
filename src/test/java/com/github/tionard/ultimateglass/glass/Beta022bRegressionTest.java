package com.github.tionard.ultimateglass.glass;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

final class Beta022bRegressionTest {
    @Test
    void dynamicDropsApplyMaterialBeforeRestoringTheStoredFrame() throws IOException {
        for (String file : List.of(
                "DynamicFramedEdgePaneBlock.java",
                "DynamicFramedCenteredPaneBlock.java",
                "DynamicFramedVanillaPaneBlock.java",
                "DynamicFramedGlassBlock.java"
        )) {
            String source = Files.readString(Path.of(
                    "src/main/java/com/github/tionard/ultimateglass/block/" + file
            ));
            int drops = source.indexOf("getDrops(");
            int components = source.indexOf("applyComponents(this, stack)", drops);
            int storedFrame = source.indexOf("frame.frameBlockId()", components);
            assertTrue(drops >= 0 && components > drops && storedFrame > components);
        }

        String smartItems = Files.readString(Path.of(
                "src/main/java/com/github/tionard/ultimateglass/registry/UltimateGlassSmartItems.java"
        ));
        assertTrue(smartItems.contains(
                "stack.get(UltimateGlassComponents.FRAME_BLOCK) == null"
        ));
    }

    @Test
    void smartFramedPanesInheritNormalPaneItemTransforms() throws IOException {
        for (String model : List.of(
                "framed_glass_pane.json",
                "framed_tempered_glass_pane.json"
        )) {
            String json = Files.readString(Path.of(
                    "src/main/resources/assets/ultimateglass/models/item/" + model
            ));
            assertTrue(json.contains("ultimateglass:item/ultimate_glass_pane"));
        }
    }

    @Test
    void glassChiselTargetsAndRendersFramedFullBlockEdges() throws IOException {
        String chisel = Files.readString(Path.of(
                "src/main/java/com/github/tionard/ultimateglass/item/GlassChiselItem.java"
        ));
        String networking = Files.readString(Path.of(
                "src/main/java/com/github/tionard/ultimateglass/network/UltimateGlassNetworking.java"
        ));
        String renderer = Files.readString(Path.of(
                "src/client/java/com/github/tionard/ultimateglass/client/render/SeamlessPaneModels.java"
        ));

        assertTrue(chisel.contains("glass.glassVariant().form() == GlassForm.BLOCK"));
        assertTrue(networking.contains("PanePlane.edge(boundary)"));
        assertTrue(renderer.contains("framedGlassBoundaryIsSeamless"));
        assertTrue(renderer.contains("framedGlassBoundaryMask"));
    }

    @Test
    void compositeEditsRemeshOnTheServerAndInstallationRequiresShift() throws IOException {
        String composite = Files.readString(Path.of(
                "src/main/java/com/github/tionard/ultimateglass/block/entity/CompositePaneBlockEntity.java"
        ));
        String item = Files.readString(Path.of(
                "src/main/java/com/github/tionard/ultimateglass/item/TemperedPaneItem.java"
        ));

        assertTrue(composite.contains("markSeamsChanged()"));
        assertTrue(composite.contains("level.sendBlockUpdated"));
        assertTrue(!composite.contains("level != null && level.isClientSide()"));
        assertTrue(item.contains("player == null || !player.isShiftKeyDown()"));
    }
}
