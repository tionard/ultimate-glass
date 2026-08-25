package com.github.tionard.ultimateglass.registry;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

final class CreativeTabTest {
    @Test
    void ultimateGlassItemsAreCollectedInACustomTab() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/github/tionard/ultimateglass/registry/UltimateGlassItems.java"
        ));

        assertTrue(source.contains("FabricCreativeModeTab.builder()"));
        assertTrue(source.contains("creativeTab.ultimateglass"));
        assertTrue(source.contains("output.accept(GLAZIERS_SCRIBER)"));
        assertTrue(source.contains("paneFamiliesForCreative().forEach"));
    }
}
