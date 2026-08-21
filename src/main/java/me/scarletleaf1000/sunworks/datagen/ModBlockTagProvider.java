package me.scarletleaf1000.sunworks.datagen;

import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.block.ModBlocks;
import me.scarletleaf1000.sunworks.block.custom.cable.CableTier;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Sunworks.MOD_ID, existingFileHelper);
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
                .add(ModBlocks.SMALL_HELIOLITE_BUD.get())
                .add(ModBlocks.CINDERITE_BLOCK.get())
                .add(ModBlocks.SILVER_BLOCK.get())
                .add(ModBlocks.RAW_CINDERITE_BLOCK.get())
                .add(ModBlocks.RAW_SILVER_BLOCK.get())
        ;

        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.CINDERITE_ORE.get())
                .add(ModBlocks.DEEPSLATE_CINDERITE_ORE.get())
                .add(ModBlocks.SILVER_ORE.get())
                .add(ModBlocks.DEEPSLATE_SILVER_ORE.get())
                .add(ModBlocks.CINDERITE_BLOCK.get())
                .add(ModBlocks.SILVER_BLOCK.get())
                .add(ModBlocks.RAW_CINDERITE_BLOCK.get())
                .add(ModBlocks.RAW_SILVER_BLOCK.get())
                .add(ModBlocks.ELECTRUM_BLOCK.get())
                .add(ModBlocks.SOLAR_ALLOY_SMELTER.get())
                .add(ModBlocks.ALLOY_SMELTER.get())
        ;

        this.tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.CINDERITE_ORE.get())
                .add(ModBlocks.DEEPSLATE_CINDERITE_ORE.get())
                .add(ModBlocks.SILVER_ORE.get())
                .add(ModBlocks.DEEPSLATE_SILVER_ORE.get())
                .add(ModBlocks.ELECTRUM_BLOCK.get())
        ;

        for (CableTier tier : CableTier.values()) {
            this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .add(ModBlocks.ENERGY_PIPES.get(tier).get());
        }

        this.tag(Tags.Blocks.ORES)
                .addTag(ORES_CINDERITE)
                .addTag(ORES_SILVER);
        this.tag(ORES_CINDERITE)
                .add(ModBlocks.CINDERITE_ORE.get())
                .add(ModBlocks.DEEPSLATE_CINDERITE_ORE.get());
        this.tag(ORES_SILVER)
                .add(ModBlocks.SILVER_ORE.get())
                .add(ModBlocks.DEEPSLATE_SILVER_ORE.get());
        this.tag(Tags.Blocks.ORES_IN_GROUND_STONE)
                .add(ModBlocks.CINDERITE_ORE.get())
                .add(ModBlocks.SILVER_ORE.get());
        this.tag(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE)
                .add(ModBlocks.DEEPSLATE_CINDERITE_ORE.get())
                .add(ModBlocks.DEEPSLATE_SILVER_ORE.get());

        this.tag(Tags.Blocks.STORAGE_BLOCKS)
                .addTag(STORAGE_BLOCKS_CINDERITE)
                .addTag(STORAGE_BLOCKS_SILVER)
                .addTag(STORAGE_BLOCKS_RAW_CINDERITE)
                .addTag(STORAGE_BLOCKS_RAW_SILVER)
                .addTag(STORAGE_BLOCKS_ELECTRUM);
        this.tag(STORAGE_BLOCKS_CINDERITE)
                .add(ModBlocks.CINDERITE_BLOCK.get());
        this.tag(STORAGE_BLOCKS_SILVER)
                .add(ModBlocks.SILVER_BLOCK.get());
        this.tag(STORAGE_BLOCKS_RAW_CINDERITE)
                .add(ModBlocks.RAW_CINDERITE_BLOCK.get());
        this.tag(STORAGE_BLOCKS_RAW_SILVER)
                .add(ModBlocks.RAW_SILVER_BLOCK.get());
        this.tag(STORAGE_BLOCKS_ELECTRUM)
                .add(ModBlocks.ELECTRUM_BLOCK.get());
    }

    public static final TagKey<Block> ORES_CINDERITE = commonTag("ores/cinderite");
    public static final TagKey<Block> ORES_SILVER = commonTag("ores/silver");
    public static final TagKey<Block> STORAGE_BLOCKS_CINDERITE = commonTag("storage_blocks/cinderite");
    public static final TagKey<Block> STORAGE_BLOCKS_SILVER = commonTag("storage_blocks/silver");
    public static final TagKey<Block> STORAGE_BLOCKS_RAW_CINDERITE = commonTag("storage_blocks/raw_cinderite");
    public static final TagKey<Block> STORAGE_BLOCKS_RAW_SILVER = commonTag("storage_blocks/raw_silver");
    public static final TagKey<Block> STORAGE_BLOCKS_ELECTRUM = commonTag("storage_blocks/electrum");

    private static TagKey<Block> commonTag(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", path));
    }
}
