package com.github.tionard.ultimateglass.registry;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

import com.github.tionard.ultimateglass.UltimateGlass;
import com.github.tionard.ultimateglass.item.GlaziersToolItem;
import com.github.tionard.ultimateglass.item.GlaziersToolTier;
import com.github.tionard.ultimateglass.registry.UltimateGlassBlocks.PaneFamily;

public final class UltimateGlassItems {
    private static final Map<Block, Item> PANE_ITEMS_BY_BLOCK = new LinkedHashMap<>();
    private static final Map<PaneFamily, Item> PANE_ITEMS_BY_FAMILY = new LinkedHashMap<>();

    private static final ResourceKey<Item> COPPER_TOOL_KEY = key("copper_glaziers_tool");
    private static final ResourceKey<Item> IRON_TOOL_KEY = key("iron_glaziers_tool");
    private static final ResourceKey<Item> DIAMOND_TOOL_KEY = key("diamond_glaziers_tool");
    private static final ResourceKey<Item> LEGACY_TOOL_KEY = key("glaziers_tool");

    public static final Item COPPER_GLAZIERS_TOOL = registerTool(COPPER_TOOL_KEY, GlaziersToolTier.COPPER);
    public static final Item IRON_GLAZIERS_TOOL = registerTool(IRON_TOOL_KEY, GlaziersToolTier.IRON);
    public static final Item DIAMOND_GLAZIERS_TOOL = registerTool(DIAMOND_TOOL_KEY, GlaziersToolTier.DIAMOND);

    /** Kept registered so 0.1.3 worlds do not lose existing tools. Hidden from recipes and Creative tabs. */
    public static final Item GLAZIERS_TOOL = registerTool(LEGACY_TOOL_KEY, GlaziersToolTier.DIAMOND);

    static {
        UltimateGlassBlocks.paneFamilies().forEach(UltimateGlassItems::registerPaneItem);
    }

    private UltimateGlassItems() {
    }

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS)
                .register(output -> paneItems().forEach(output::accept));

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(output -> {
                    output.accept(COPPER_GLAZIERS_TOOL);
                    output.accept(IRON_GLAZIERS_TOOL);
                    output.accept(DIAMOND_GLAZIERS_TOOL);
                });
    }

    public static Item paneItemFor(Block block) {
        return PANE_ITEMS_BY_BLOCK.get(block);
    }

    public static Item paneItemFor(PaneFamily family) {
        return PANE_ITEMS_BY_FAMILY.get(family);
    }

    public static Collection<Item> paneItems() {
        return PANE_ITEMS_BY_FAMILY.values();
    }

    private static void registerPaneItem(PaneFamily family) {
        String vanillaName = BuiltInRegistries.BLOCK.getKey(family.vanillaPane()).getPath();
        ResourceKey<Item> key = key("ultimate_" + vanillaName);
        BlockItem item = new BlockItem(family.edgePane(), new Item.Properties().setId(key));
        Registry.register(BuiltInRegistries.ITEM, key, item);

        item.registerBlocks(Item.BY_BLOCK, item);
        Item.BY_BLOCK.put(family.centeredPane(), item);
        PANE_ITEMS_BY_BLOCK.put(family.edgePane(), item);
        PANE_ITEMS_BY_BLOCK.put(family.centeredPane(), item);
        PANE_ITEMS_BY_FAMILY.put(family, item);
    }

    private static Item registerTool(ResourceKey<Item> key, GlaziersToolTier tier) {
        return register(
                key,
                properties -> new GlaziersToolItem(properties, tier),
                new Item.Properties().stacksTo(1)
        );
    }

    private static ResourceKey<Item> key(String name) {
        return ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(UltimateGlass.MOD_ID, name)
        );
    }

    private static Item register(
            ResourceKey<Item> key,
            Function<Item.Properties, Item> factory,
            Item.Properties properties
    ) {
        Item item = factory.apply(properties.setId(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }
}
