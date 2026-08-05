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
import com.github.tionard.ultimateglass.block.EdgePaneBlock;

public final class UltimateGlassBlocks {
    private static final Map<Block, EdgePaneBlock> VANILLA_TO_EDGE = new LinkedHashMap<>();
    private static final Map<Block, Block> EDGE_TO_VANILLA = new LinkedHashMap<>();

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
        return VANILLA_TO_EDGE.get(vanillaPane);
    }

    public static Block vanillaFor(Block edgePane) {
        return EDGE_TO_VANILLA.get(edgePane);
    }

    public static Collection<EdgePaneBlock> edgePanes() {
        return VANILLA_TO_EDGE.values();
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
        Identifier id = Identifier.fromNamespaceAndPath(UltimateGlass.MOD_ID, name);
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
        EdgePaneBlock block = new EdgePaneBlock(
                vanillaPane,
                BlockBehaviour.Properties.ofFullCopy(vanillaPane).setId(key)
        );

        Registry.register(BuiltInRegistries.BLOCK, key, block);
        VANILLA_TO_EDGE.put(vanillaPane, block);
        EDGE_TO_VANILLA.put(block, vanillaPane);
        return block;
    }
}
