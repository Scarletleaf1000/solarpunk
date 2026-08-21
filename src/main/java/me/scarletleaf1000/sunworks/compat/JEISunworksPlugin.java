package me.scarletleaf1000.sunworks.compat;

import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.block.ModBlocks;
import me.scarletleaf1000.sunworks.recipe.ModRecipes;
import me.scarletleaf1000.sunworks.recipe.custom.AlloySmelterRecipe;
import me.scarletleaf1000.sunworks.screen.custom.AlloySmelterScreen;
import me.scarletleaf1000.sunworks.screen.custom.SolarAlloySmelterScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

@JeiPlugin
public class JEISunworksPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Sunworks.MOD_ID, "jei_plugin");
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

        registration.addRecipeClickArea(AlloySmelterScreen.class, 65, 45, 8, 16, AlloySmeltingRecipeCategory.ALLOY_SMELTING_RECIPE_TYPE);
        registration.addRecipeClickArea(SolarAlloySmelterScreen.class, 65, 45, 8, 16, AlloySmeltingRecipeCategory.ALLOY_SMELTING_RECIPE_TYPE);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        ItemStack solarSmelterStack = new ItemStack(ModBlocks.SOLAR_ALLOY_SMELTER.asItem());
        ItemStack smelterStack = new ItemStack(ModBlocks.ALLOY_SMELTER.asItem());
        registration.addRecipeCatalyst(solarSmelterStack, RecipeTypes.BLASTING);
        registration.addRecipeCatalyst(solarSmelterStack, AlloySmeltingRecipeCategory.ALLOY_SMELTING_RECIPE_TYPE);
        registration.addRecipeCatalyst(smelterStack, RecipeTypes.BLASTING);
        registration.addRecipeCatalyst(smelterStack, AlloySmeltingRecipeCategory.ALLOY_SMELTING_RECIPE_TYPE);
    }
}
