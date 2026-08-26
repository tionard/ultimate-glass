package com.github.tionard.ultimateglass.registry;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.github.tionard.ultimateglass.UltimateGlass;
import com.github.tionard.ultimateglass.glass.GlassForm;
import com.github.tionard.ultimateglass.glass.GlassVariant;
import com.github.tionard.ultimateglass.item.DynamicFramedGlassItem;
import com.github.tionard.ultimateglass.item.StaticFramedGlassItem;
import com.github.tionard.ultimateglass.pane.PaneFrame;
import com.github.tionard.ultimateglass.pane.PaneMaterial;

/** Items and recipe-facing lookups for the complete glass families. */
public final class UltimateGlassFamilyItems {
    private static final Map<GlassVariant, Item> ITEMS_BY_VARIANT = new LinkedHashMap<>();
    private static final Map<Block, Item> ITEMS_BY_BLOCK = new LinkedHashMap<>();
    private static final Map<Item, GlassVariant> VARIANTS_BY_ITEM = new LinkedHashMap<>();
    private static final Map<Identifier, GlassVariant> DYNAMIC_VARIANTS = new LinkedHashMap<>();

    static {
        UltimateGlassFamilies.variants().forEach(entry -> register(entry.getKey(), entry.getValue()));
    }

    private UltimateGlassFamilyItems() {
    }

    public static void initialize() {
        // Trigger static registration.
    }

    public static Item itemFor(GlassVariant variant) {
        return ITEMS_BY_VARIANT.get(variant);
    }

    public static Item itemFor(Block block) {
        return ITEMS_BY_BLOCK.get(block);
    }

    public static GlassVariant variantFor(Item item) {
        return VARIANTS_BY_ITEM.get(item);
    }

    public static GlassVariant dynamicVariant(Identifier itemId) {
        return DYNAMIC_VARIANTS.get(itemId);
    }

    /** Resolves every unframed vanilla/tempered pane or full block accepted by framing recipes. */
    public static GlassVariant unframedVariant(ItemStack stack) {
        GlassVariant smart = UltimateGlassSmartItems.unframedVariant(stack);
        if (smart != null) {
            return smart;
        }

        Item item = stack.getItem();
        GlassVariant registered = VARIANTS_BY_ITEM.get(item);
        if (registered != null && !registered.isFramed()) {
            return registered;
        }

        PaneMaterial temperedPane = UltimateGlassItems.unframedMaterial(item);
        if (temperedPane != null) {
            return new GlassVariant(
                    temperedPane, GlassForm.PANE, true, PaneFrame.NONE
            );
        }

        for (PaneMaterial material : PaneMaterial.values()) {
            if (UltimateGlassFamilies.vanillaPane(material).asItem() == item) {
                return new GlassVariant(material, GlassForm.PANE, false, PaneFrame.NONE);
            }
            if (UltimateGlassFamilies.vanillaBlock(material).asItem() == item) {
                return new GlassVariant(material, GlassForm.BLOCK, false, PaneFrame.NONE);
            }
        }
        return null;
    }

    /** Legacy lookup retained for compatibility tests and old fixed item IDs. */
    public static GlassVariant unframedVariant(Item item) {
        return unframedVariant(new ItemStack(item));
    }

    public static ItemStack framedStack(GlassVariant source, Block plank) {
        return UltimateGlassSmartItems.stackForVariant(
                source.withFrame(PaneFrame.DYNAMIC),
                plank
        );
    }

    /** Optional reverse-recipe output for unframed Tempered panes and full blocks. */
    public static ItemStack vanillaStackForTempered(ItemStack stack) {
        GlassVariant variant = unframedVariant(stack);
        if (variant == null || !variant.tempered() || variant.isFramed()) {
            return ItemStack.EMPTY;
        }
        Block source = variant.form() == GlassForm.PANE
                ? UltimateGlassFamilies.vanillaPane(variant.material())
                : UltimateGlassFamilies.vanillaBlock(variant.material());
        return new ItemStack(source.asItem());
    }

    public static ItemStack vanillaStackForTempered(Item item) {
        return vanillaStackForTempered(new ItemStack(item));
    }

    /** Diamond Glazier's Tool recovery applies only to Tempered members, never ordinary frames. */
    public static ItemStack collectedStack(Block block) {
        ItemStack smart = UltimateGlassSmartItems.stackForBlock(block);
        if (!smart.isEmpty()) {
            return smart;
        }
        GlassVariant variant = UltimateGlassFamilies.variantFor(block);
        if (variant == null || !variant.tempered()) {
            return ItemStack.EMPTY;
        }
        Item item = itemFor(block);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    private static void register(GlassVariant variant, Block block) {
        ResourceKey<Item> key = ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(
                        UltimateGlass.MOD_ID,
                        UltimateGlassFamilies.itemPath(variant)
                )
        );
        Item item;
        if (!variant.isFramed()) {
            item = new BlockItem(block, new Item.Properties().setId(key));
        } else {
            Item baseItem = baseItem(variant);
            item = variant.frame().isDynamic()
                    ? new DynamicFramedGlassItem(
                            block, baseItem, new Item.Properties().setId(key)
                    )
                    : new StaticFramedGlassItem(
                            block, variant.frame(), baseItem, new Item.Properties().setId(key)
                    );
        }

        Registry.register(BuiltInRegistries.ITEM, key, item);
        ((BlockItem) item).registerBlocks(Item.BY_BLOCK, item);
        ITEMS_BY_VARIANT.put(variant, item);
        ITEMS_BY_BLOCK.put(block, item);
        VARIANTS_BY_ITEM.put(item, variant);
        if (variant.frame().isDynamic()) {
            DYNAMIC_VARIANTS.put(key.identifier(), variant);
        }
    }

    private static Item baseItem(GlassVariant variant) {
        if (variant.form() == GlassForm.PANE) {
            return UltimateGlassFamilies.vanillaPane(variant.material()).asItem();
        }
        if (!variant.tempered()) {
            return UltimateGlassFamilies.vanillaBlock(variant.material()).asItem();
        }
        return itemFor(new GlassVariant(
                variant.material(), GlassForm.BLOCK, true, PaneFrame.NONE
        ));
    }
}
