package com.github.tionard.ultimateglass.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import com.github.tionard.ultimateglass.glass.GlassForm;
import com.github.tionard.ultimateglass.glass.GlassVariant;
import com.github.tionard.ultimateglass.glass.SmartGlassKind;
import com.github.tionard.ultimateglass.pane.PaneMaterial;
import com.github.tionard.ultimateglass.registry.UltimateGlassFamilyItems;
import com.github.tionard.ultimateglass.registry.UltimateGlassSmartItems;

/** Vanilla-shaped conversion of six matching Tempered blocks into sixteen matching panes. */
public final class TemperedPaneFromBlocksRecipe extends CustomRecipe {
    private static final int RESULT_COUNT = 16;

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !result(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return result(input);
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return UltimateGlassRecipes.TEMPERED_PANE_FROM_BLOCKS;
    }

    private static ItemStack result(CraftingInput input) {
        if (input.width() < 3 || input.height() < 2 || input.ingredientCount() != 6) {
            return ItemStack.EMPTY;
        }

        for (int offsetY = 0; offsetY <= input.height() - 2; offsetY++) {
            PaneMaterial material = matchingMaterial(input, offsetY);
            if (material != null) {
                ItemStack result = UltimateGlassSmartItems.stack(
                        SmartGlassKind.TEMPERED_PANE,
                        material,
                        null
                );
                result.setCount(RESULT_COUNT);
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    private static PaneMaterial matchingMaterial(CraftingInput input, int offsetY) {
        PaneMaterial material = null;
        for (int y = 0; y < input.height(); y++) {
            for (int x = 0; x < input.width(); x++) {
                ItemStack stack = input.getItem(x, y);
                boolean expected = x < 3 && y >= offsetY && y < offsetY + 2;
                if (!expected) {
                    if (!stack.isEmpty()) {
                        return null;
                    }
                    continue;
                }

                GlassVariant variant = UltimateGlassFamilyItems.unframedVariant(stack);
                if (variant == null
                        || variant.form() != GlassForm.BLOCK
                        || !variant.tempered()
                        || variant.isFramed()) {
                    return null;
                }
                if (material == null) {
                    material = variant.material();
                } else if (material != variant.material()) {
                    return null;
                }
            }
        }
        return material;
    }
}
