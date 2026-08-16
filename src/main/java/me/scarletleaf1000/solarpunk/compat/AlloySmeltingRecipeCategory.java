package me.scarletleaf1000.solarpunk.compat;

import me.scarletleaf1000.solarpunk.Solarpunk;
import me.scarletleaf1000.solarpunk.block.ModBlocks;
import me.scarletleaf1000.solarpunk.item.ModItems;
import me.scarletleaf1000.solarpunk.recipe.custom.AlloySmelterRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class AlloySmeltingRecipeCategory implements IRecipeCategory {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Solarpunk.MOD_ID, "alloy_smelting");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Solarpunk.MOD_ID, "textures/gui/container/solar_alloy_smelter.png");

    public static final RecipeType<AlloySmelterRecipe> ALLOY_SMELTING_RECIPE_TYPE =
        new RecipeType<>(UID, AlloySmelterRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public AlloySmeltingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 75);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.SOLAR_ALLOY_SMELTER.asItem()));
    }

    @Override
    public RecipeType getRecipeType() {
        return AlloySmeltingRecipeCategory.ALLOY_SMELTING_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("recipe.solarpunk.alloy_smelting");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Object recipe, IFocusGroup focuses) {
        AlloySmelterRecipe alloySmelterRecipe = (AlloySmelterRecipe) recipe;
        int[][] slotPositions = {{56, 24}, {79, 17}, {102, 24}};
        List<SizedIngredient> inputs = alloySmelterRecipe.inputs();
        for (int i = 0; i < inputs.size(); i++) {
            builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, slotPositions[i][0], slotPositions[i][1])
                .addItemStacks(Arrays.asList(inputs.get(i).getItems()));
        }
        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT, 79, 58)
            .addItemStack(alloySmelterRecipe.getResultItem(null));
    }
}
