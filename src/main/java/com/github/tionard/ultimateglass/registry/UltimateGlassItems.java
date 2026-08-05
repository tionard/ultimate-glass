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

public final class UltimateGlassItems {
    private static final ResourceKey<Item> GLAZIERS_TOOL_KEY = key("glaziers_tool");

    public static final Item GLAZIERS_TOOL = register(
            GLAZIERS_TOOL_KEY,
            GlaziersToolItem::new,
            new Item.Properties().stacksTo(1)
    );

    private UltimateGlassItems() {
    }

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(output -> output.accept(GLAZIERS_TOOL));
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
