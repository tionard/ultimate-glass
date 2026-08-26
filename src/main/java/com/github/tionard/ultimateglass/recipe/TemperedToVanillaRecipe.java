package com.github.tionard.ultimateglass.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import com.github.tionard.ultimateglass.config.UltimateGlassServerConfig;
import com.github.tionard.ultimateglass.registry.UltimateGlassFamilyItems;

/** Optional one-to-one recovery of an unframed Tempered pane into its vanilla-style source. */
public final class TemperedToVanillaRecipe extends CustomRecipe {
    @Override
    public boolean matches(CraftingInput input, Level level) {
        return UltimateGlassServerConfig.temperedToVanillaRecipeEnabled()
                && !result(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return result(input);
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return UltimateGlassRecipes.TEMPERED_TO_VANILLA;
    }

    private static ItemStack result(CraftingInput input) {
        if (input.ingredientCount() != 1) {
            return ItemStack.EMPTY;
        }

        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) {
                continue;
            }
            return UltimateGlassFamilyItems.vanillaStackForTempered(stack.getItem());
        }
        return ItemStack.EMPTY;
    }
}
