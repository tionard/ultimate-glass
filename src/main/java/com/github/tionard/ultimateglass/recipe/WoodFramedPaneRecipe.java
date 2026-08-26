package com.github.tionard.ultimateglass.recipe;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.github.tionard.ultimateglass.glass.GlassVariant;
import com.github.tionard.ultimateglass.registry.UltimateGlassFamilyItems;

/** Frames one supported ordinary/Tempered pane or full block with any tagged plank. */
public final class WoodFramedPaneRecipe extends CustomRecipe {
    @Override
    public boolean matches(CraftingInput input, Level level) {
        return ingredients(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        Ingredients ingredients = ingredients(input);
        if (ingredients == null) {
            return ItemStack.EMPTY;
        }
        return UltimateGlassFamilyItems.framedStack(ingredients.variant(), ingredients.plank());
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return UltimateGlassRecipes.WOOD_FRAMED_PANE;
    }

    private static Ingredients ingredients(CraftingInput input) {
        if (input.ingredientCount() != 2) {
            return null;
        }

        Block plank = null;
        GlassVariant variant = null;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(ItemTags.PLANKS) && plank == null) {
                Block candidate = Block.byItem(stack.getItem());
                if (candidate == Blocks.AIR) {
                    return null;
                }
                plank = candidate;
                continue;
            }
            GlassVariant candidate = UltimateGlassFamilyItems.unframedVariant(stack);
            if (candidate == null || variant != null) {
                return null;
            }
            variant = candidate;
        }
        return plank == null || variant == null ? null : new Ingredients(plank, variant);
    }

    private record Ingredients(Block plank, GlassVariant variant) {
    }
}
