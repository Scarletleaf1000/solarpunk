package me.scarletleaf1000.solarpunk.datagen;

import me.scarletleaf1000.solarpunk.block.ModBlocks;
import me.scarletleaf1000.solarpunk.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;

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

        oreSmelting(pRecipeOutput, List.of(ModItems.RAW_CINDERITE.get()), RecipeCategory.MISC,
                ModItems.CINDERITE_INGOT.get(), 0.7f, 200, "cinderite_ingot");
        oreBlasting(pRecipeOutput, List.of(ModItems.RAW_CINDERITE.get()), RecipeCategory.MISC,
                ModItems.CINDERITE_INGOT.get(), 0.7f, 100, "cinderite_ingot");
        oreSmelting(pRecipeOutput, List.of(ModItems.RAW_SILVER.get()), RecipeCategory.MISC,
                ModItems.SILVER_INGOT.get(), 0.7f, 200, "silver_ingot");
        oreBlasting(pRecipeOutput, List.of(ModItems.RAW_SILVER.get()), RecipeCategory.MISC,
                ModItems.SILVER_INGOT.get(), 0.7f, 100, "silver_ingot");
    }
}
