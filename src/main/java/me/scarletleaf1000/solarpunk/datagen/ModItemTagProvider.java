package me.scarletleaf1000.solarpunk.datagen;

import me.scarletleaf1000.solarpunk.Solarpunk;
import me.scarletleaf1000.solarpunk.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider>
            pLookupProvider, CompletableFuture<TagLookup<Block>> pBlockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, pBlockTags, Solarpunk.MOD_ID, existingFileHelper);
    }

    public static final TagKey<Item> INGOTS_CINDERITE = commonTag("ingots/cinderite");
    public static final TagKey<Item> INGOTS_SILVER = commonTag("ingots/silver");
    public static final TagKey<Item> NUGGETS_CINDERITE = commonTag("nuggets/cinderite");
    public static final TagKey<Item> NUGGETS_SILVER = commonTag("nuggets/silver");
    public static final TagKey<Item> RAW_MATERIALS_CINDERITE = commonTag("raw_materials/cinderite");
    public static final TagKey<Item> RAW_MATERIALS_SILVER = commonTag("raw_materials/silver");

    private static TagKey<Item> commonTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(Tags.Items.INGOTS)
                .addTag(INGOTS_CINDERITE)
                .addTag(INGOTS_SILVER);
        this.tag(INGOTS_CINDERITE)
                .add(ModItems.CINDERITE_INGOT.get());
        this.tag(INGOTS_SILVER)
                .add(ModItems.SILVER_INGOT.get());

        this.tag(Tags.Items.NUGGETS)
                .addTag(NUGGETS_CINDERITE)
                .addTag(NUGGETS_SILVER);
        this.tag(NUGGETS_CINDERITE)
                .add(ModItems.CINDERITE_NUGGET.get());
        this.tag(NUGGETS_SILVER)
                .add(ModItems.SILVER_NUGGET.get());

        this.tag(Tags.Items.RAW_MATERIALS)
                .addTag(RAW_MATERIALS_CINDERITE)
                .addTag(RAW_MATERIALS_SILVER);
        this.tag(RAW_MATERIALS_CINDERITE)
                .add(ModItems.RAW_CINDERITE.get());
        this.tag(RAW_MATERIALS_SILVER)
                .add(ModItems.RAW_SILVER.get());
    }
}
