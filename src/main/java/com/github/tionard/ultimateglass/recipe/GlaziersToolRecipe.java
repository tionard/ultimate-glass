package com.github.tionard.ultimateglass.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import com.github.tionard.ultimateglass.config.UltimateGlassServerConfig;
import com.github.tionard.ultimateglass.item.GlaziersToolTier;
import com.github.tionard.ultimateglass.registry.UltimateGlassItems;

public final class GlaziersToolRecipe extends CustomRecipe {
    private final GlaziersToolTier tier;

    public GlaziersToolRecipe(CraftingBookCategory category, GlaziersToolTier tier) {
        super(category);
        this.tier = tier;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (!UltimateGlassServerConfig.isCraftingEnabled(tier)
                || input.width() < 2
                || input.height() < 3
                || input.ingredientCount() != 4) {
            return false;
        }

        for (int offsetX = 0; offsetX <= input.width() - 2; offsetX++) {
            for (int offsetY = 0; offsetY <= input.height() - 3; offsetY++) {
                if (matchesAt(input, offsetX, offsetY, false)
                        || matchesAt(input, offsetX, offsetY, true)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return new ItemStack(resultItem());
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return UltimateGlassRecipes.serializer(tier);
    }

    private boolean matchesAt(CraftingInput input, int offsetX, int offsetY, boolean mirrored) {
        int materialX = offsetX + (mirrored ? 0 : 1);
        int stringX = offsetX + (mirrored ? 1 : 0);
        int stickX = materialX;

        for (int y = 0; y < input.height(); y++) {
            for (int x = 0; x < input.width(); x++) {
                ItemStack stack = input.getItem(x, y);
                boolean expectedMaterial = x == materialX && y == offsetY;
                boolean expectedString = x == stringX && y == offsetY + 1;
                boolean expectedStick = x == stickX && (y == offsetY + 1 || y == offsetY + 2);

                if (expectedMaterial) {
                    if (!stack.is(materialItem())) {
                        return false;
                    }
                } else if (expectedString) {
                    if (!stack.is(Items.STRING)) {
                        return false;
                    }
                } else if (expectedStick) {
                    if (!stack.is(Items.STICK)) {
                        return false;
                    }
                } else if (!stack.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private Item materialItem() {
        return switch (tier) {
            case COPPER -> Items.COPPER_INGOT;
            case IRON -> Items.IRON_INGOT;
            case DIAMOND -> Items.DIAMOND;
        };
    }

    private Item resultItem() {
        return switch (tier) {
            case COPPER -> UltimateGlassItems.COPPER_GLAZIERS_TOOL;
            case IRON -> UltimateGlassItems.IRON_GLAZIERS_TOOL;
            case DIAMOND -> UltimateGlassItems.DIAMOND_GLAZIERS_TOOL;
        };
    }
}
