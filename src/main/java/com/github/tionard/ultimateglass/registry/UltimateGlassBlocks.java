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
    public static final EdgePaneBlock EDGE_WHITE_STAINED_GLASS_PANE = register("edge_white_stained_glass_pane", Blocks.WHITE_STAINED_GLASS_PANE);
    public static final EdgePaneBlock EDGE_ORANGE_STAINED_GLASS_PANE = register("edge_orange_stained_glass_pane", Blocks.ORANGE_STAINED_GLASS_PANE);
    public static final EdgePaneBlock EDGE_MAGENTA_STAINED_GLASS_PANE = register("edge_magenta_stained_glass_pane", Blocks.MAGENTA_STAINED_GLASS_PANE);
    public static final EdgePaneBlock EDGE_LIGHT_BLUE_STAINED_GLASS_PANE = register("edge_light_blue_stained_glass_pane", Blocks.LIGHT_BLUE_STAINED_GLASS_PANE);
    public static final EdgePaneBlock EDGE_YELLOW_STAINED_GLASS_PANE = register("edge_yellow_stained_glass_pane", Blocks.YELLOW_STAINED_GLASS_PANE);
    public static final EdgePaneBlock EDGE_LIME_STAINED_GLASS_PANE = register("edge_lime_stained_glass_pane", Blocks.LIME_STAINED_GLASS_PANE);
    public static final EdgePaneBlock EDGE_PINK_STAINED_GLASS_PANE = register("edge_pink_stained_glass_pane", Blocks.PINK_STAINED_GLASS_PANE);
    public static final EdgePaneBlock EDGE_GRAY_STAINED_GLASS_PANE = register("edge_gray_stained_glass_pane", Blocks.GRAY_STAINED_GLASS_PANE);
    public static final EdgePaneBlock EDGE_LIGHT_GRAY_STAINED_GLASS_PANE = register("edge_light_gray_stained_glass_pane", Blocks.LIGHT_GRAY_STAINED_GLASS_PANE);
    public static final EdgePaneBlock EDGE_CYAN_STAINED_GLASS_PANE = register("edge_cyan_stained_glass_pane", Blocks.CYAN_STAINED_GLASS_PANE);
    public static final EdgePaneBlock EDGE_PURPLE_STAINED_GLASS_PANE = register("edge_purple_stained_glass_pane", Blocks.PURPLE_STAINED_GLASS_PANE);
    public static final EdgePaneBlock EDGE_BLUE_STAINED_GLASS_PANE = register("edge_blue_stained_glass_pane", Blocks.BLUE_STAINED_GLASS_PANE);
    public static final EdgePaneBlock EDGE_BROWN_STAINED_GLASS_PANE = register("edge_brown_stained_glass_pane", Blocks.BROWN_STAINED_GLASS_PANE);
    public static final EdgePaneBlock EDGE_GREEN_STAINED_GLASS_PANE = register("edge_green_stained_glass_pane", Blocks.GREEN_STAINED_GLASS_PANE);
    public static final EdgePaneBlock EDGE_RED_STAINED_GLASS_PANE = register("edge_red_stained_glass_pane", Blocks.RED_STAINED_GLASS_PANE);
    public static final EdgePaneBlock EDGE_BLACK_STAINED_GLASS_PANE = register("edge_black_stained_glass_pane", Blocks.BLACK_STAINED_GLASS_PANE);

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
