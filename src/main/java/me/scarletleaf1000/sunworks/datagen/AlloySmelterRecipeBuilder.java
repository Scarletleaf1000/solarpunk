package me.scarletleaf1000.sunworks.datagen;

import me.scarletleaf1000.sunworks.recipe.custom.AlloySmelterRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AlloySmelterRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final ItemStack result;
    private final int time;
    private final List<SizedIngredient> ingredients = new ArrayList<>();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;

    private AlloySmelterRecipeBuilder(RecipeCategory category, ItemStack result, int time) {
        this.category = category;
        this.result = result;
        this.time = time;
    }

    public static AlloySmelterRecipeBuilder alloySmelting(RecipeCategory category, ItemLike result, int time) {
        return new AlloySmelterRecipeBuilder(category, new ItemStack(result), time);
    }

    public static AlloySmelterRecipeBuilder alloySmelting(RecipeCategory category, ItemStack result, int time) {
        return new AlloySmelterRecipeBuilder(category, result, time);
    }

    public AlloySmelterRecipeBuilder requires(TagKey<Item> tag, int count) {
        return requires(new SizedIngredient(Ingredient.of(tag), count));
    }

    public AlloySmelterRecipeBuilder requires(TagKey<Item> tag) {
        return requires(tag, 1);
    }

    public AlloySmelterRecipeBuilder requires(ItemLike item, int count) {
        return requires(new SizedIngredient(Ingredient.of(item), count));
    }

    public AlloySmelterRecipeBuilder requires(ItemLike item) {
        return requires(item, 1);
    }

    public AlloySmelterRecipeBuilder requires(SizedIngredient ingredient) {
        if (this.ingredients.size() >= 3) {
            throw new IllegalArgumentException("Alloy smelter recipes support at most 3 ingredients");
        }
        this.ingredients.add(ingredient);
        return this;
    }

    @Override
    public AlloySmelterRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public AlloySmelterRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public Item getResult() {
        return this.result.getItem();
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        if (this.ingredients.isEmpty()) {
            throw new IllegalStateException("No ingredients for alloy smelter recipe " + id);
        }
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }

        Advancement.Builder advancementBuilder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancementBuilder::addCriterion);

        AlloySmelterRecipe recipe = new AlloySmelterRecipe(this.ingredients, this.result, this.time);
        recipeOutput.accept(id, recipe, advancementBuilder.build(id.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }
}
