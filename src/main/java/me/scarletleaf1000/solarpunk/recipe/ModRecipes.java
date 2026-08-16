package me.scarletleaf1000.solarpunk.recipe;

import me.scarletleaf1000.solarpunk.Solarpunk;
import me.scarletleaf1000.solarpunk.recipe.custom.AlloySmelterRecipe;
import me.scarletleaf1000.solarpunk.recipe.custom.AlloySmelterRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Solarpunk.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Solarpunk.MOD_ID);

    public static final Supplier<RecipeSerializer<AlloySmelterRecipe>> ALLOY_SMELTER_SERIALIZER =
            SERIALIZERS.register("alloy_smelting", AlloySmelterRecipeSerializer::new);
    public static final Supplier<RecipeType<AlloySmelterRecipe>> ALLOY_SMELTER_TYPE =
            TYPES.register("alloy_smelting", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return "alloy_smelting";
                }
            });

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}
