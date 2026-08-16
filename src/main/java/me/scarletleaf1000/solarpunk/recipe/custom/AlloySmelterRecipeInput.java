package me.scarletleaf1000.solarpunk.recipe.custom;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

public record AlloySmelterRecipeInput(List<ItemStack> inputs) implements RecipeInput {
    @Override
    public ItemStack getItem(int i) {
        return inputs.get(i);
    }

    @Override
    public int size() {
        return inputs.size();
    }
}
