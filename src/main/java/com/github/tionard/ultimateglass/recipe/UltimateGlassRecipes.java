package com.github.tionard.ultimateglass.recipe;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import com.github.tionard.ultimateglass.UltimateGlass;
import com.github.tionard.ultimateglass.item.GlaziersToolTier;

public final class UltimateGlassRecipes {
    public static final RecipeSerializer<WoodFramedPaneRecipe> WOOD_FRAMED_PANE = registerUnit(
            "wood_framed_pane",
            new WoodFramedPaneRecipe()
    );
    public static final RecipeSerializer<TemperedToVanillaRecipe> TEMPERED_TO_VANILLA = registerUnit(
            "tempered_to_vanilla",
            new TemperedToVanillaRecipe()
    );
    public static final RecipeSerializer<TemperedPaneFromBlocksRecipe>
            TEMPERED_PANE_FROM_BLOCKS = registerUnit(
                    "tempered_glass_pane_from_blocks",
                    new TemperedPaneFromBlocksRecipe()
            );
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
        GlaziersToolRecipe recipe = new GlaziersToolRecipe(tier);
        RecipeSerializer<GlaziersToolRecipe> serializer = new RecipeSerializer<>(
                MapCodec.unit(recipe),
                StreamCodec.unit(recipe)
        );
        return Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(UltimateGlass.MOD_ID, name),
                serializer
        );
    }

    private static <T extends CustomRecipe> RecipeSerializer<T> registerUnit(String name, T recipe) {
        RecipeSerializer<T> serializer = new RecipeSerializer<>(
                MapCodec.unit(recipe),
                StreamCodec.unit(recipe)
        );
        return Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(UltimateGlass.MOD_ID, name),
                serializer
        );
    }
}
