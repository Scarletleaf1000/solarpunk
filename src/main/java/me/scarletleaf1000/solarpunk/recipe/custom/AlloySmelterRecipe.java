package me.scarletleaf1000.solarpunk.recipe.custom;

import me.scarletleaf1000.solarpunk.recipe.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.ArrayList;
import java.util.List;

public record AlloySmelterRecipe(List<SizedIngredient> inputs, ItemStack output, int time) implements Recipe<AlloySmelterRecipeInput> {

    @Override
    public NonNullList<Ingredient> getIngredients() {
       NonNullList<Ingredient> list = NonNullList.create();
       for (SizedIngredient input : inputs) {
           list.add(input.ingredient());
       }
       return list;
    }

    @Override
    public boolean matches(AlloySmelterRecipeInput input, Level level) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }

        if (stacks.size() != inputs.size()) {
            return false;
        }

        for (SizedIngredient ingredient : inputs) {
            boolean matched = false;
            for (int i = 0; i < stacks.size(); i++) {
                ItemStack stack = stacks.get(i);
                if (ingredient.test(stack) && stack.getCount() >= ingredient.count()) {
                    stacks.remove(i);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(AlloySmelterRecipeInput input, HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ALLOY_SMELTER_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.ALLOY_SMELTER_TYPE.get();
    }
}
