package com.github.tionard.ultimateglass.registry;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

import com.github.tionard.ultimateglass.UltimateGlass;
import com.github.tionard.ultimateglass.item.GlaziersToolItem;
import com.github.tionard.ultimateglass.item.GlaziersToolTier;

public final class UltimateGlassItems {
    private static final ResourceKey<Item> COPPER_TOOL_KEY = key("copper_glaziers_tool");
    private static final ResourceKey<Item> IRON_TOOL_KEY = key("iron_glaziers_tool");
    private static final ResourceKey<Item> DIAMOND_TOOL_KEY = key("diamond_glaziers_tool");
    private static final ResourceKey<Item> LEGACY_TOOL_KEY = key("glaziers_tool");

    public static final Item COPPER_GLAZIERS_TOOL = registerTool(COPPER_TOOL_KEY, GlaziersToolTier.COPPER);
    public static final Item IRON_GLAZIERS_TOOL = registerTool(IRON_TOOL_KEY, GlaziersToolTier.IRON);
    public static final Item DIAMOND_GLAZIERS_TOOL = registerTool(DIAMOND_TOOL_KEY, GlaziersToolTier.DIAMOND);

    /** Kept registered so 0.1.3 worlds do not lose existing tools. Hidden from recipes and Creative tabs. */
    public static final Item GLAZIERS_TOOL = registerTool(LEGACY_TOOL_KEY, GlaziersToolTier.DIAMOND);

    private UltimateGlassItems() {
    }

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(output -> {
                    output.accept(COPPER_GLAZIERS_TOOL);
                    output.accept(IRON_GLAZIERS_TOOL);
                    output.accept(DIAMOND_GLAZIERS_TOOL);
                });
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
