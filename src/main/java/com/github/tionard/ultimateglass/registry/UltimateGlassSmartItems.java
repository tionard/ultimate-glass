package com.github.tionard.ultimateglass.registry;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.github.tionard.ultimateglass.UltimateGlass;
import com.github.tionard.ultimateglass.block.entity.DynamicFrameBlockEntity;
import com.github.tionard.ultimateglass.glass.GlassForm;
import com.github.tionard.ultimateglass.glass.GlassVariant;
import com.github.tionard.ultimateglass.glass.SmartGlassKind;
import com.github.tionard.ultimateglass.item.SmartGlassBlockItem;
import com.github.tionard.ultimateglass.item.SmartTemperedPaneItem;
import com.github.tionard.ultimateglass.pane.PaneFrame;
import com.github.tionard.ultimateglass.pane.PaneMaterial;

/**
 * The component-backed item layer introduced in 0.2.2. Material-specific blocks remain internal,
 * while six stable item IDs carry material and frame identity between recipes, inventories and
 * placement.
 */
public final class UltimateGlassSmartItems {
    private static final Map<SmartGlassKind, Item> ITEMS =
            new EnumMap<>(SmartGlassKind.class);
    private static final Map<Item, SmartGlassKind> KINDS_BY_ITEM = new IdentityHashMap<>();
    private static final Map<Identifier, SmartGlassKind> KINDS_BY_ID = new LinkedHashMap<>();
    private static final Map<Block, Target> TARGETS = new IdentityHashMap<>();

    static {
        for (SmartGlassKind kind : SmartGlassKind.values()) {
            register(kind);
        }
        for (PaneMaterial material : PaneMaterial.values()) {
            for (SmartGlassKind kind : SmartGlassKind.values()) {
                bind(kind, material);
            }
        }
        bindLegacyBlocks();
    }

    private UltimateGlassSmartItems() {
    }

    public static void initialize() {
        // Trigger smart item registration after every legacy item has registered its block mapping.
    }

    public static Item item(SmartGlassKind kind) {
        return ITEMS.get(kind);
    }

    public static SmartGlassKind kind(Item item) {
        return KINDS_BY_ITEM.get(item);
    }

    public static SmartGlassKind kind(Identifier itemId) {
        return KINDS_BY_ID.get(itemId);
    }

    public static PaneMaterial material(ItemStack stack) {
        return PaneMaterial.fromComponentId(stack.get(UltimateGlassComponents.GLASS_MATERIAL));
    }

    public static Block targetBlock(SmartGlassKind kind, PaneMaterial material) {
        if (kind.form() == GlassForm.PANE && kind.tempered()) {
            UltimateGlassBlocks.PaneFamily family = kind.framed()
                    ? UltimateGlassBlocks.dynamicFamily(material)
                    : UltimateGlassBlocks.familyFor(material);
            return family == null ? null : family.edgePane();
        }

        PaneFrame frame = kind.framed() ? PaneFrame.DYNAMIC : PaneFrame.NONE;
        if (kind.form() == GlassForm.BLOCK && kind.tempered() && !kind.framed()) {
            return UltimateGlassFamilies.temperedBlock(material);
        }
        return UltimateGlassFamilies.blockFor(new GlassVariant(
                material, kind.form(), kind.tempered(), frame
        ));
    }

    public static ItemStack stack(
            SmartGlassKind kind,
            PaneMaterial material,
            Block frame
    ) {
        Item item = item(kind);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item);
        stack.set(UltimateGlassComponents.GLASS_MATERIAL, material.componentId());
        if (kind.framed()) {
            Block resolvedFrame = frame == null ? Blocks.OAK_PLANKS : frame;
            stack.set(
                    UltimateGlassComponents.FRAME_BLOCK,
                    BuiltInRegistries.BLOCK.getKey(resolvedFrame)
            );
        }
        return stack;
    }

    public static ItemStack stackForVariant(GlassVariant variant, Block frame) {
        SmartGlassKind kind = kindFor(variant);
        return kind == null ? ItemStack.EMPTY : stack(kind, variant.material(), frame);
    }

    public static GlassVariant unframedVariant(ItemStack stack) {
        SmartGlassKind kind = kind(stack.getItem());
        if (kind == null || kind.framed()) {
            return null;
        }
        return new GlassVariant(
                material(stack), kind.form(), kind.tempered(), PaneFrame.NONE
        );
    }

    public static ItemStack stackForBlock(Block block) {
        Target target = TARGETS.get(block);
        return target == null
                ? ItemStack.EMPTY
                : stack(target.kind(), target.material(), target.frame());
    }

    /** Replaces legacy loot-table item IDs with the smart item bound to this internal block. */
    public static List<ItemStack> modernizeDrops(Block block, List<ItemStack> drops) {
        Target target = TARGETS.get(block);
        if (target == null || drops.isEmpty()) {
            return drops;
        }
        List<ItemStack> modern = new ArrayList<>(drops.size());
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) {
                continue;
            }
            ItemStack replacement = stack(target.kind(), target.material(), target.frame());
            replacement.setCount(drop.getCount());
            modern.add(replacement);
        }
        return modern;
    }

    public static void applyComponents(Block block, ItemStack stack) {
        Target target = TARGETS.get(block);
        if (target != null && !stack.isEmpty()) {
            stack.set(UltimateGlassComponents.GLASS_MATERIAL, target.material().componentId());
            if (target.kind().framed()
                    && stack.get(UltimateGlassComponents.FRAME_BLOCK) == null) {
                stack.set(
                        UltimateGlassComponents.FRAME_BLOCK,
                        BuiltInRegistries.BLOCK.getKey(target.frame())
                );
            }
        }
    }

    /** Keeps the tab useful without enumerating every wood-by-glass cross product. */
    public static List<ItemStack> creativeStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (PaneMaterial material : PaneMaterial.values()) {
            stacks.add(stack(SmartGlassKind.TEMPERED_PANE, material, null));
            stacks.add(stack(SmartGlassKind.TEMPERED_BLOCK, material, null));
        }
        stacks.add(stack(SmartGlassKind.FRAMED_PANE, PaneMaterial.CLEAR, Blocks.OAK_PLANKS));
        stacks.add(stack(
                SmartGlassKind.FRAMED_TEMPERED_PANE,
                PaneMaterial.CLEAR,
                Blocks.OAK_PLANKS
        ));
        stacks.add(stack(SmartGlassKind.FRAMED_BLOCK, PaneMaterial.CLEAR, Blocks.OAK_PLANKS));
        stacks.add(stack(
                SmartGlassKind.FRAMED_TEMPERED_BLOCK,
                PaneMaterial.CLEAR,
                Blocks.OAK_PLANKS
        ));
        return List.copyOf(stacks);
    }

    private static void register(SmartGlassKind kind) {
        ResourceKey<Item> key = ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(UltimateGlass.MOD_ID, kind.itemPath())
        );
        Block defaultBlock = targetBlock(kind, PaneMaterial.CLEAR);
        Item item = kind.form() == GlassForm.PANE && kind.tempered()
                ? new SmartTemperedPaneItem(
                        defaultBlock, kind, new Item.Properties().setId(key)
                )
                : new SmartGlassBlockItem(
                        defaultBlock, kind, new Item.Properties().setId(key)
                );
        Registry.register(BuiltInRegistries.ITEM, key, item);
        ITEMS.put(kind, item);
        KINDS_BY_ITEM.put(item, kind);
        KINDS_BY_ID.put(key.identifier(), kind);
    }

    private static void bind(SmartGlassKind kind, PaneMaterial material) {
        Block target = targetBlock(kind, material);
        if (target == null) {
            throw new IllegalStateException("Missing internal block for " + kind + "/" + material);
        }
        Item item = item(kind);
        Item.BY_BLOCK.put(target, item);
        TARGETS.put(target, new Target(kind, material, Blocks.OAK_PLANKS));

        if (kind.form() == GlassForm.PANE && kind.tempered()) {
            UltimateGlassBlocks.PaneFamily family = kind.framed()
                    ? UltimateGlassBlocks.dynamicFamily(material)
                    : UltimateGlassBlocks.familyFor(material);
            Item.BY_BLOCK.put(family.centeredPane(), item);
            TARGETS.put(family.centeredPane(), new Target(kind, material, Blocks.OAK_PLANKS));
        }
    }

    private static void bindLegacyBlocks() {
        UltimateGlassBlocks.paneFamilies().forEach(family -> {
            PaneFrame paneFrame = family.appearance().frame();
            SmartGlassKind kind = paneFrame.isFramed()
                    ? SmartGlassKind.FRAMED_TEMPERED_PANE
                    : SmartGlassKind.TEMPERED_PANE;
            Block frame = frameBlock(paneFrame);
            bindLegacy(family.edgePane(), kind, family.appearance().material(), frame);
            bindLegacy(family.centeredPane(), kind, family.appearance().material(), frame);
        });

        UltimateGlassFamilies.variants().forEach(entry -> {
            GlassVariant variant = entry.getKey();
            SmartGlassKind kind = kindFor(variant);
            if (kind != null) {
                bindLegacy(entry.getValue(), kind, variant.material(), frameBlock(variant.frame()));
            }
        });
    }

    private static void bindLegacy(
            Block block, SmartGlassKind kind, PaneMaterial material, Block frame
    ) {
        Item item = item(kind);
        Item.BY_BLOCK.put(block, item);
        TARGETS.put(block, new Target(kind, material, frame));
    }

    private static Block frameBlock(PaneFrame frame) {
        if (!frame.isFramed() || frame.isDynamic()) {
            return Blocks.OAK_PLANKS;
        }
        Identifier id = Identifier.withDefaultNamespace(frame.path() + "_planks");
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(Blocks.OAK_PLANKS);
    }

    private static SmartGlassKind kindFor(GlassVariant variant) {
        if (!variant.tempered() && !variant.isFramed()) {
            return null;
        }
        if (variant.form() == GlassForm.PANE) {
            if (!variant.tempered()) {
                return SmartGlassKind.FRAMED_PANE;
            }
            return variant.isFramed()
                    ? SmartGlassKind.FRAMED_TEMPERED_PANE
                    : SmartGlassKind.TEMPERED_PANE;
        }
        if (!variant.tempered()) {
            return SmartGlassKind.FRAMED_BLOCK;
        }
        return variant.isFramed()
                ? SmartGlassKind.FRAMED_TEMPERED_BLOCK
                : SmartGlassKind.TEMPERED_BLOCK;
    }

    private record Target(SmartGlassKind kind, PaneMaterial material, Block frame) {
    }
}
