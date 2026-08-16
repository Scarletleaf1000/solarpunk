package me.scarletleaf1000.solarpunk.recipe.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class AlloySmelterRecipeSerializer implements RecipeSerializer<AlloySmelterRecipe> {
    public static final MapCodec<AlloySmelterRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SizedIngredient.FLAT_CODEC.listOf(1, 3).fieldOf("ingredients").forGetter(AlloySmelterRecipe::inputs),
            ItemStack.CODEC.fieldOf("result").forGetter(AlloySmelterRecipe::output),
            Codec.INT.fieldOf("time").forGetter(AlloySmelterRecipe::time)
    ).apply(inst, AlloySmelterRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AlloySmelterRecipe> STREAM_CODEC = StreamCodec.composite(
            SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list(3)), AlloySmelterRecipe::inputs,
            ItemStack.STREAM_CODEC, AlloySmelterRecipe::output,
            ByteBufCodecs.VAR_INT, AlloySmelterRecipe::time,
            AlloySmelterRecipe::new);

    @Override
    public MapCodec<AlloySmelterRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AlloySmelterRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
