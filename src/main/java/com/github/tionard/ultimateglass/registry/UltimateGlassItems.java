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
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.network.chat.Component;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;

import com.github.tionard.ultimateglass.UltimateGlass;
import com.github.tionard.ultimateglass.item.DynamicFramedPaneItem;
import com.github.tionard.ultimateglass.item.GlaziersToolItem;
import com.github.tionard.ultimateglass.item.GlaziersToolTier;
import com.github.tionard.ultimateglass.item.GlassChiselItem;
import com.github.tionard.ultimateglass.item.StaticFramedPaneItem;
import com.github.tionard.ultimateglass.item.TemperedPaneItem;
import com.github.tionard.ultimateglass.pane.PaneFrame;
import com.github.tionard.ultimateglass.pane.PaneMaterial;
import com.github.tionard.ultimateglass.registry.UltimateGlassBlocks.PaneFamily;

public final class UltimateGlassItems {
    private static final Map<Block, Item> PANE_ITEMS_BY_BLOCK = new LinkedHashMap<>();
    private static final Map<PaneFamily, Item> PANE_ITEMS_BY_FAMILY = new LinkedHashMap<>();
    private static final Map<PaneMaterial, Item> UNFRAMED_ITEMS = new LinkedHashMap<>();
    private static final Map<Identifier, PaneMaterial> DYNAMIC_FRAME_MATERIALS =
            new LinkedHashMap<>();

    private static final ResourceKey<Item> COPPER_TOOL_KEY = key("copper_glaziers_tool");
    private static final ResourceKey<Item> IRON_TOOL_KEY = key("iron_glaziers_tool");
    private static final ResourceKey<Item> DIAMOND_TOOL_KEY = key("diamond_glaziers_tool");
    private static final ResourceKey<Item> LEGACY_TOOL_KEY = key("glaziers_tool");
    private static final ResourceKey<Item> GLASS_CHISEL_KEY = key("glass_chisel");
    private static final ResourceKey<CreativeModeTab> CREATIVE_TAB_KEY = ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(),
            Identifier.fromNamespaceAndPath(UltimateGlass.MOD_ID, "creative_tab")
    );

    public static final Item COPPER_GLAZIERS_TOOL = registerTool(COPPER_TOOL_KEY, GlaziersToolTier.COPPER);
    public static final Item IRON_GLAZIERS_TOOL = registerTool(IRON_TOOL_KEY, GlaziersToolTier.IRON);
    public static final Item DIAMOND_GLAZIERS_TOOL = registerTool(DIAMOND_TOOL_KEY, GlaziersToolTier.DIAMOND);
    public static final Item GLASS_CHISEL = register(
            GLASS_CHISEL_KEY,
            GlassChiselItem::new,
            new Item.Properties().stacksTo(1)
    );

    /** Kept registered so 0.1.3 worlds do not lose existing tools. Hidden from recipes and Creative tabs. */
    public static final Item GLAZIERS_TOOL = registerTool(LEGACY_TOOL_KEY, GlaziersToolTier.DIAMOND);

    public static final Item TINTED_GLASS_PANE = registerBlockItem(
            "tinted_glass_pane", UltimateGlassBlocks.TINTED_GLASS_PANE
    );

    static {
        UltimateGlassBlocks.paneFamilies().forEach(UltimateGlassItems::registerPaneItem);
    }

    private UltimateGlassItems() {
    }

    public static void initialize() {
        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                CREATIVE_TAB_KEY,
                FabricCreativeModeTab.builder()
                        .icon(() -> new ItemStack(paneItemFor(PaneMaterial.CLEAR)))
                        .title(Component.translatable("creativeTab.ultimateglass"))
                        .displayItems((parameters, output) -> {
                            output.accept(GLASS_CHISEL);
                            output.accept(COPPER_GLAZIERS_TOOL);
                            output.accept(IRON_GLAZIERS_TOOL);
                            output.accept(DIAMOND_GLAZIERS_TOOL);
                            output.accept(TINTED_GLASS_PANE);
                            paneFamiliesForCreative().forEach(
                                    family -> output.accept(paneItemFor(family))
                            );
                        })
                        .build()
        );

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS)
                .register(output -> {
                    output.accept(TINTED_GLASS_PANE);
                    paneFamiliesForCreative().forEach(family -> output.accept(paneItemFor(family)));
                });

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(output -> {
                    output.accept(COPPER_GLAZIERS_TOOL);
                    output.accept(IRON_GLAZIERS_TOOL);
                    output.accept(DIAMOND_GLAZIERS_TOOL);
                    output.accept(GLASS_CHISEL);
                });
    }

    public static Item paneItemFor(Block block) {
        return PANE_ITEMS_BY_BLOCK.get(block);
    }

    public static Item paneItemFor(PaneFamily family) {
        return PANE_ITEMS_BY_FAMILY.get(family);
    }

    public static Item paneItemFor(PaneMaterial material) {
        return UNFRAMED_ITEMS.get(material);
    }

    public static PaneMaterial unframedMaterial(Item item) {
        return UNFRAMED_ITEMS.entrySet().stream()
                .filter(entry -> entry.getValue() == item)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public static PaneMaterial dynamicFrameMaterial(Identifier itemId) {
        return DYNAMIC_FRAME_MATERIALS.get(itemId);
    }

    public static ItemStack framedStack(PaneMaterial material, Block plank) {
        PaneFrame fixedFrame = PaneFrame.fromPlank(plank.asItem());
        PaneFamily family = fixedFrame == null
                ? UltimateGlassBlocks.dynamicFamily(material)
                : UltimateGlassBlocks.familyFor(new com.github.tionard.ultimateglass.pane.PaneAppearance(
                        material, fixedFrame
                ));
        ItemStack stack = new ItemStack(paneItemFor(family));
        if (fixedFrame == null) {
            stack.set(UltimateGlassComponents.FRAME_BLOCK, BuiltInRegistries.BLOCK.getKey(plank));
        }
        return stack;
    }

    public static Collection<Item> paneItems() {
        return PANE_ITEMS_BY_FAMILY.values();
    }

    private static Collection<PaneFamily> paneFamiliesForCreative() {
        return UltimateGlassBlocks.paneFamilies().stream()
                .filter(family -> !family.appearance().frame().isDynamic())
                .toList();
    }

    private static void registerPaneItem(PaneFamily family) {
        ResourceKey<Item> key = key(family.itemPath());
        Item item = family.appearance().frame().isDynamic()
                ? new DynamicFramedPaneItem(
                        family.edgePane(), family.appearance().material(),
                        new Item.Properties().setId(key)
                )
                : family.appearance().frame() == PaneFrame.NONE
                ? new TemperedPaneItem(family.edgePane(), new Item.Properties().setId(key))
                : new StaticFramedPaneItem(
                        family.edgePane(),
                        family.appearance().frame(),
                        family.appearance().material(),
                        new Item.Properties().setId(key)
                );
        Registry.register(BuiltInRegistries.ITEM, key, item);

        ((BlockItem) item).registerBlocks(Item.BY_BLOCK, item);
        Item.BY_BLOCK.put(family.centeredPane(), item);
        PANE_ITEMS_BY_BLOCK.put(family.edgePane(), item);
        PANE_ITEMS_BY_BLOCK.put(family.centeredPane(), item);
        PANE_ITEMS_BY_FAMILY.put(family, item);
        if (family.appearance().frame().isDynamic()) {
            DYNAMIC_FRAME_MATERIALS.put(key.identifier(), family.appearance().material());
        }
        if (family.appearance().frame() == PaneFrame.NONE) {
            UNFRAMED_ITEMS.put(family.appearance().material(), item);
        }
    }

    private static Item registerBlockItem(String name, Block block) {
        ResourceKey<Item> key = key(name);
        BlockItem item = new BlockItem(block, new Item.Properties().setId(key));
        Registry.register(BuiltInRegistries.ITEM, key, item);
        item.registerBlocks(Item.BY_BLOCK, item);
        return item;
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
