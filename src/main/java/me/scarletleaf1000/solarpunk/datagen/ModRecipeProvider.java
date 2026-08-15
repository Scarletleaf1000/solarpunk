package me.scarletleaf1000.solarpunk.datagen;

import me.scarletleaf1000.solarpunk.block.ModBlocks;
import me.scarletleaf1000.solarpunk.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(pOutput, pRegistries);
    }

    @Override
    protected void buildRecipes(RecipeOutput pRecipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.HELIOLITE_BLOCK.get())
                .pattern("HH")
                .pattern("HH")
                .define('H', ModItems.HELIOLITE_SHARD.get())
                .unlockedBy("has_heliolite_shard", has(ModItems.HELIOLITE_SHARD.get()))
                .save(pRecipeOutput);

        nineBlockStorageRecipes(pRecipeOutput, RecipeCategory.MISC, ModItems.CINDERITE_NUGGET.get(),
                RecipeCategory.MISC, ModItems.CINDERITE_INGOT.get());
        nineBlockStorageRecipes(pRecipeOutput, RecipeCategory.MISC, ModItems.SILVER_NUGGET.get(),
                RecipeCategory.MISC, ModItems.SILVER_INGOT.get());

        nineBlockStorageRecipes(pRecipeOutput, RecipeCategory.MISC, ModItems.CINDERITE_INGOT.get(),
                RecipeCategory.BUILDING_BLOCKS, ModBlocks.CINDERITE_BLOCK.get());
        nineBlockStorageRecipes(pRecipeOutput, RecipeCategory.MISC, ModItems.SILVER_INGOT.get(),
                RecipeCategory.BUILDING_BLOCKS, ModBlocks.SILVER_BLOCK.get());
        nineBlockStorageRecipes(pRecipeOutput, RecipeCategory.MISC, ModItems.RAW_CINDERITE.get(),
                RecipeCategory.BUILDING_BLOCKS, ModBlocks.RAW_CINDERITE_BLOCK.get());
        nineBlockStorageRecipes(pRecipeOutput, RecipeCategory.MISC, ModItems.RAW_SILVER.get(),
                RecipeCategory.BUILDING_BLOCKS, ModBlocks.RAW_SILVER_BLOCK.get());

        List<ItemLike> cinderiteSmeltables = List.of(
                ModBlocks.CINDERITE_ORE.get(),
                ModBlocks.DEEPSLATE_CINDERITE_ORE.get(),
                ModItems.RAW_CINDERITE.get());
        oreSmeltingRecipes(pRecipeOutput, cinderiteSmeltables, ModItems.CINDERITE_INGOT.get());
        oreBlastingRecipes(pRecipeOutput, cinderiteSmeltables, ModItems.CINDERITE_INGOT.get());

        List<ItemLike> silverSmeltables = List.of(
                ModBlocks.SILVER_ORE.get(),
                ModBlocks.DEEPSLATE_SILVER_ORE.get(),
                ModItems.RAW_SILVER.get());
        oreSmeltingRecipes(pRecipeOutput, silverSmeltables, ModItems.SILVER_INGOT.get());
        oreBlastingRecipes(pRecipeOutput, silverSmeltables, ModItems.SILVER_INGOT.get());
    }

    protected static void oreSmeltingRecipes(RecipeOutput output, List<ItemLike> inputs, ItemLike result) {
        oreSmelting(output, inputs, RecipeCategory.MISC, result, 0.7f, 200, getItemName(result));
    }

    protected static void oreBlastingRecipes(RecipeOutput output, List<ItemLike> inputs, ItemLike result) {
        oreBlasting(output, inputs, RecipeCategory.MISC, result, 0.7f, 100, getItemName(result));
    }
}
