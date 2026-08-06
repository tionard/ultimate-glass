package com.github.tionard.ultimateglass.recipe;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import com.github.tionard.ultimateglass.UltimateGlass;
import com.github.tionard.ultimateglass.item.GlaziersToolTier;

public final class UltimateGlassRecipes {
    public static final RecipeSerializer<GlaziersToolRecipe> COPPER_TOOL = register(
            "copper_glaziers_tool",
            GlaziersToolTier.COPPER
    );
    public static final RecipeSerializer<GlaziersToolRecipe> IRON_TOOL = register(
            "iron_glaziers_tool",
            GlaziersToolTier.IRON
    );
    public static final RecipeSerializer<GlaziersToolRecipe> DIAMOND_TOOL = register(
            "diamond_glaziers_tool",
            GlaziersToolTier.DIAMOND
    );

    private UltimateGlassRecipes() {
    }

    public static void initialize() {
        // Static initialization registers the serializers.
    }

    public static RecipeSerializer<? extends CustomRecipe> serializer(GlaziersToolTier tier) {
        return switch (tier) {
            case COPPER -> COPPER_TOOL;
            case IRON -> IRON_TOOL;
            case DIAMOND -> DIAMOND_TOOL;
        };
    }

    private static RecipeSerializer<GlaziersToolRecipe> register(
            String name,
            GlaziersToolTier tier
    ) {
        RecipeSerializer<GlaziersToolRecipe> serializer = new CustomRecipe.Serializer<>(
                category -> new GlaziersToolRecipe(category, tier)
        );
        return Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(UltimateGlass.MOD_ID, name),
                serializer
        );
    }
}
