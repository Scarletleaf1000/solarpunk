package me.scarletleaf1000.solarpunk.datagen;


import me.scarletleaf1000.solarpunk.Solarpunk;
import me.scarletleaf1000.solarpunk.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Solarpunk.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        blockWithItem(ModBlocks.HELIOLITE_BLOCK);
        blockWithItem(ModBlocks.BUDDING_HELIOLITE);
    }

    private void blockWithItem(DeferredBlock<Block> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
    }
}
