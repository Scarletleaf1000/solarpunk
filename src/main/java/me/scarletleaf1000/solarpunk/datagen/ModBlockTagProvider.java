package me.scarletleaf1000.solarpunk.datagen;

import me.scarletleaf1000.solarpunk.Solarpunk;
import me.scarletleaf1000.solarpunk.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Solarpunk.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.HELIOLITE_BLOCK.get())
                .add(ModBlocks.BUDDING_HELIOLITE.get())
                .add(ModBlocks.HELIOLITE_CLUSTER.get())
                .add(ModBlocks.LARGE_HELIOLITE_BUD.get())
                .add(ModBlocks.MEDIUM_HELIOLITE_BUD.get())
                .add(ModBlocks.SMALL_HELIOLITE_BUD.get());

        this.tag(BlockTags.NEEDS_STONE_TOOL)
                .add(ModBlocks.HELIOLITE_BLOCK.get())
                .add(ModBlocks.BUDDING_HELIOLITE.get())
                .add(ModBlocks.HELIOLITE_CLUSTER.get())
                .add(ModBlocks.LARGE_HELIOLITE_BUD.get())
                .add(ModBlocks.MEDIUM_HELIOLITE_BUD.get())
                .add(ModBlocks.SMALL_HELIOLITE_BUD.get());
    }
}
