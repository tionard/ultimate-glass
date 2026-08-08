package com.github.tionard.ultimateglass.registry;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.github.tionard.ultimateglass.UltimateGlass;
import com.github.tionard.ultimateglass.block.CenteredPaneBlock;
import com.github.tionard.ultimateglass.block.EdgePaneBlock;

public final class UltimateGlassBlocks {
    private static final Map<Block, PaneFamily> FAMILIES_BY_VANILLA = new LinkedHashMap<>();
    private static final Map<Block, PaneFamily> FAMILIES_BY_BLOCK = new LinkedHashMap<>();

    public static final EdgePaneBlock EDGE_GLASS_PANE = register("edge_glass_pane", Blocks.GLASS_PANE);
    public static final EdgePaneBlock EDGE_WHITE_STAINED_GLASS_PANE = registerColored("white");
    public static final EdgePaneBlock EDGE_ORANGE_STAINED_GLASS_PANE = registerColored("orange");
    public static final EdgePaneBlock EDGE_MAGENTA_STAINED_GLASS_PANE = registerColored("magenta");
    public static final EdgePaneBlock EDGE_LIGHT_BLUE_STAINED_GLASS_PANE = registerColored("light_blue");
    public static final EdgePaneBlock EDGE_YELLOW_STAINED_GLASS_PANE = registerColored("yellow");
    public static final EdgePaneBlock EDGE_LIME_STAINED_GLASS_PANE = registerColored("lime");
    public static final EdgePaneBlock EDGE_PINK_STAINED_GLASS_PANE = registerColored("pink");
    public static final EdgePaneBlock EDGE_GRAY_STAINED_GLASS_PANE = registerColored("gray");
    public static final EdgePaneBlock EDGE_LIGHT_GRAY_STAINED_GLASS_PANE = registerColored("light_gray");
    public static final EdgePaneBlock EDGE_CYAN_STAINED_GLASS_PANE = registerColored("cyan");
    public static final EdgePaneBlock EDGE_PURPLE_STAINED_GLASS_PANE = registerColored("purple");
    public static final EdgePaneBlock EDGE_BLUE_STAINED_GLASS_PANE = registerColored("blue");
    public static final EdgePaneBlock EDGE_BROWN_STAINED_GLASS_PANE = registerColored("brown");
    public static final EdgePaneBlock EDGE_GREEN_STAINED_GLASS_PANE = registerColored("green");
    public static final EdgePaneBlock EDGE_RED_STAINED_GLASS_PANE = registerColored("red");
    public static final EdgePaneBlock EDGE_BLACK_STAINED_GLASS_PANE = registerColored("black");

    private UltimateGlassBlocks() {
    }

    public static void initialize() {
        // Trigger static registration.
    }

    public static EdgePaneBlock edgeFor(Block vanillaPane) {
        PaneFamily family = FAMILIES_BY_VANILLA.get(vanillaPane);
        return family == null ? null : family.edgePane();
    }

    public static CenteredPaneBlock centeredFor(Block vanillaPane) {
        PaneFamily family = FAMILIES_BY_VANILLA.get(vanillaPane);
        return family == null ? null : family.centeredPane();
    }

    public static PaneFamily familyFor(Block block) {
        return FAMILIES_BY_BLOCK.get(block);
    }

    public static Block vanillaFor(Block customPane) {
        PaneFamily family = FAMILIES_BY_BLOCK.get(customPane);
        return family == null || customPane == family.vanillaPane()
                ? null
                : family.vanillaPane();
    }

    public static Collection<EdgePaneBlock> edgePanes() {
        return FAMILIES_BY_VANILLA.values().stream()
                .map(PaneFamily::edgePane)
                .toList();
    }

    public static Collection<CenteredPaneBlock> centeredPanes() {
        return FAMILIES_BY_VANILLA.values().stream()
                .map(PaneFamily::centeredPane)
                .toList();
    }

    public static Collection<PaneFamily> paneFamilies() {
        return FAMILIES_BY_VANILLA.values();
    }

    private static EdgePaneBlock registerColored(String color) {
        return register(
                "edge_" + color + "_stained_glass_pane",
                vanillaBlock(color + "_stained_glass_pane")
        );
    }

    private static Block vanillaBlock(String path) {
        Identifier id = Identifier.fromNamespaceAndPath("minecraft", path);
        Block block = BuiltInRegistries.BLOCK.getValue(id);
        if (block == null || block == Blocks.AIR) {
            throw new IllegalStateException("Missing vanilla block " + id);
        }
        return block;
    }

    private static EdgePaneBlock register(String name, Block vanillaPane) {
        String centeredName = "centered_" + name.substring("edge_".length());
        ResourceKey<Block> centeredKey = blockKey(centeredName);
        CenteredPaneBlock centeredPane = new CenteredPaneBlock(
                vanillaPane,
                BlockBehaviour.Properties.ofFullCopy(vanillaPane).setId(centeredKey)
        );
        Registry.register(BuiltInRegistries.BLOCK, centeredKey, centeredPane);

        ResourceKey<Block> edgeKey = blockKey(name);
        EdgePaneBlock edgePane = new EdgePaneBlock(
                vanillaPane,
                BlockBehaviour.Properties.ofFullCopy(vanillaPane).setId(edgeKey)
        );
        Registry.register(BuiltInRegistries.BLOCK, edgeKey, edgePane);

        PaneFamily family = new PaneFamily(vanillaPane, centeredPane, edgePane);
        FAMILIES_BY_VANILLA.put(vanillaPane, family);
        FAMILIES_BY_BLOCK.put(vanillaPane, family);
        FAMILIES_BY_BLOCK.put(centeredPane, family);
        FAMILIES_BY_BLOCK.put(edgePane, family);
        return edgePane;
    }

    private static ResourceKey<Block> blockKey(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(UltimateGlass.MOD_ID, name);
        return ResourceKey.create(Registries.BLOCK, id);
    }

    public record PaneFamily(
            Block vanillaPane,
            CenteredPaneBlock centeredPane,
            EdgePaneBlock edgePane
    ) {
    }
}
