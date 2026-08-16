package me.scarletleaf1000.solarpunk.compat;

import me.scarletleaf1000.solarpunk.Solarpunk;
import me.scarletleaf1000.solarpunk.recipe.ModRecipes;
import me.scarletleaf1000.solarpunk.recipe.custom.AlloySmelterRecipe;
import me.scarletleaf1000.solarpunk.screen.custom.SolarAlloySmelterScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

@JeiPlugin
public class JEISolarpunkPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Solarpunk.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new AlloySmeltingRecipeCategory(
                registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<AlloySmelterRecipe> alloySmelterRecipes = recipeManager
                .getAllRecipesFor(ModRecipes.ALLOY_SMELTER_TYPE.get()).stream().map(RecipeHolder::value).toList();
        registration.addRecipes(AlloySmeltingRecipeCategory.ALLOY_SMELTING_RECIPE_TYPE, alloySmelterRecipes);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {

        registration.addRecipeClickArea(SolarAlloySmelterScreen.class, 51, 32, 24, 33, AlloySmeltingRecipeCategory.ALLOY_SMELTING_RECIPE_TYPE);
    }
}
