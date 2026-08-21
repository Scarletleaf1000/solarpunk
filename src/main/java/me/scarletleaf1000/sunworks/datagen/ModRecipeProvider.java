package me.scarletleaf1000.sunworks.datagen;

import me.scarletleaf1000.sunworks.block.ModBlocks;
import me.scarletleaf1000.sunworks.block.custom.cable.CableTier;
import me.scarletleaf1000.sunworks.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(pOutput, pRegistries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.HELIOLITE_BLOCK.get())
                .pattern("HH")
                .pattern("HH")
                .define('H', ModItems.HELIOLITE_SHARD.get())
                .unlockedBy("has_heliolite_shard", has(ModItems.HELIOLITE_SHARD.get()))
                .save(recipeOutput);

        nineBlockStorageRecipes(recipeOutput, RecipeCategory.MISC, ModItems.CINDERITE_NUGGET.get(),
                RecipeCategory.MISC, ModItems.CINDERITE_INGOT.get());
        nineBlockStorageRecipes(recipeOutput, RecipeCategory.MISC, ModItems.SILVER_NUGGET.get(),
                RecipeCategory.MISC, ModItems.SILVER_INGOT.get());
        nineBlockStorageRecipes(recipeOutput, RecipeCategory.MISC, ModItems.ELECTRUM_NUGGET.get(),
                RecipeCategory.MISC, ModItems.ELECTRUM_INGOT.get());

        nineBlockStorageRecipesRecipesWithCustomUnpacking(recipeOutput, RecipeCategory.MISC, ModItems.CINDERITE_INGOT.get(),
                RecipeCategory.BUILDING_BLOCKS, ModBlocks.CINDERITE_BLOCK.get(), "cinderite_ingot_from_cinderite_block", "cinderite_ingot");
        nineBlockStorageRecipesRecipesWithCustomUnpacking(recipeOutput, RecipeCategory.MISC, ModItems.SILVER_INGOT.get(),
                RecipeCategory.BUILDING_BLOCKS, ModBlocks.SILVER_BLOCK.get(), "silver_ingot_from_silver_block", "silver_ingot");
        nineBlockStorageRecipesRecipesWithCustomUnpacking(recipeOutput, RecipeCategory.MISC, ModItems.ELECTRUM_INGOT.get(),
                RecipeCategory.BUILDING_BLOCKS, ModBlocks.ELECTRUM_BLOCK.get(), "electrum_ingot_from_electrum_block", "electrum_ingot");
        nineBlockStorageRecipes(recipeOutput, RecipeCategory.MISC, ModItems.RAW_CINDERITE.get(),
                RecipeCategory.BUILDING_BLOCKS, ModBlocks.RAW_CINDERITE_BLOCK.get());
        nineBlockStorageRecipes(recipeOutput, RecipeCategory.MISC, ModItems.RAW_SILVER.get(),
                RecipeCategory.BUILDING_BLOCKS, ModBlocks.RAW_SILVER_BLOCK.get());

        AlloySmelterRecipeBuilder.alloySmelting(RecipeCategory.MISC, new ItemStack(ModItems.ELECTRUM_INGOT.get(), 6), 300)
                .requires(Items.GOLD_INGOT)
                .requires(ModItems.SILVER_INGOT.get(), 4)
                .requires(Items.REDSTONE, 2)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .unlockedBy("has_silver_ingot", has(ModItems.SILVER_INGOT.get()))
                .save(recipeOutput);

        AlloySmelterRecipeBuilder.alloySmelting(RecipeCategory.MISC, new ItemStack(ModItems.SILICON.get(), 1), 300)
                .requires(Items.QUARTZ, 2)
                .unlockedBy("has_quartz", has(Items.QUARTZ))
                .save(recipeOutput);

        List<ItemLike> cinderiteSmeltables = List.of(
                ModBlocks.CINDERITE_ORE.get(),
                ModBlocks.DEEPSLATE_CINDERITE_ORE.get(),
                ModItems.RAW_CINDERITE.get());
        oreSmeltingRecipes(recipeOutput, cinderiteSmeltables, ModItems.CINDERITE_INGOT.get());
        oreBlastingRecipes(recipeOutput, cinderiteSmeltables, ModItems.CINDERITE_INGOT.get());

        List<ItemLike> silverSmeltables = List.of(
                ModBlocks.SILVER_ORE.get(),
                ModBlocks.DEEPSLATE_SILVER_ORE.get(),
                ModItems.RAW_SILVER.get());
        oreSmeltingRecipes(recipeOutput, silverSmeltables, ModItems.SILVER_INGOT.get());
        oreBlastingRecipes(recipeOutput, silverSmeltables, ModItems.SILVER_INGOT.get());

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.HEAT_CORE.get())
                .pattern(" H ")
                .pattern("HCH")
                .pattern(" H ")
                .define('H', ModItems.HELIOLITE_SHARD.get())
                .define('C', ModItems.CINDERITE_NUGGET.get())
                .unlockedBy("has_heliolite_shard", has(ModItems.HELIOLITE_SHARD.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SOLAR_PANEL_COMPONENT.get(), 3)
                .pattern("SSS")
                .pattern("#H#")
                .define('S', ModItems.SILVER_INGOT.get())
                .define('#', ModItems.SILICON.get())
                .define('H', ModItems.HEAT_CORE.get())
                .unlockedBy("has_heat_core", has(ModItems.HEAT_CORE.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SOLAR_ALLOY_SMELTER.get())
                .pattern("SPS")
                .pattern("IBI")
                .pattern("ICI")
                .define('S', ModItems.SILVER_INGOT.get())
                .define('P', ModItems.SOLAR_PANEL_COMPONENT.get())
                .define('I', Items.IRON_BLOCK)
                .define('B', Items.BLAST_FURNACE)
                .define('C', ModItems.HEAT_CORE.get())
                .unlockedBy("has_solar_panel_component", has(ModItems.SOLAR_PANEL_COMPONENT.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ENERGY_PIPES.get(CableTier.BASIC).get(), 8)
                .pattern("RER")
                .define('R', Items.REDSTONE)
                .define('E', ModItems.ELECTRUM_INGOT.get())
                .unlockedBy("has_electrum_ingot", has(ModItems.ELECTRUM_INGOT.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SOLAR_PANEL.get())
                .pattern("PPP")
                .pattern("iIi")
                .pattern(" e ")
                .define('P', ModItems.SOLAR_PANEL_COMPONENT.get())
                .define('i', Items.IRON_INGOT)
                .define('I', Items.IRON_BLOCK)
                .define('e', ModItems.ELECTRUM_INGOT.get())
                .unlockedBy("has_solar_panel_component", has(ModItems.SOLAR_PANEL_COMPONENT.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ALLOY_SMELTER.get())
                .pattern("IBI")
                .pattern("IFI")
                .pattern("CCC")
                .define('I', Items.IRON_INGOT)
                .define('B', Items.BLAST_FURNACE)
                .define('F', Items.FURNACE)
                .define('C', Items.COBBLESTONE)
                .unlockedBy("has_blast_furnace", has(Items.BLAST_FURNACE))
                .save(recipeOutput);
    }

    protected static void oreSmeltingRecipes(RecipeOutput output, List<ItemLike> inputs, ItemLike result) {
        oreSmelting(output, inputs, RecipeCategory.MISC, result, 0.7f, 200, getItemName(result));
    }

    protected static void oreBlastingRecipes(RecipeOutput output, List<ItemLike> inputs, ItemLike result) {
        oreBlasting(output, inputs, RecipeCategory.MISC, result, 0.7f, 100, getItemName(result));
    }
}
