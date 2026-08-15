package com.github.tionard.ultimateglass.recipe;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.github.tionard.ultimateglass.pane.PaneMaterial;
import com.github.tionard.ultimateglass.registry.UltimateGlassItems;

/** Frames one tempered pane with any item in Minecraft's planks tag. */
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
        return UltimateGlassItems.framedStack(ingredients.material(), ingredients.plank());
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
        PaneMaterial material = null;
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
            PaneMaterial candidate = UltimateGlassItems.unframedMaterial(stack.getItem());
            if (candidate == null || material != null) {
                return null;
            }
            material = candidate;
        }
        return plank == null || material == null ? null : new Ingredients(plank, material);
    }

    private record Ingredients(Block plank, PaneMaterial material) {
    }
}
