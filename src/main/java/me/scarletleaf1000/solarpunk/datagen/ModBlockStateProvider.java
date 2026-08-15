package me.scarletleaf1000.solarpunk.datagen;


import me.scarletleaf1000.solarpunk.Solarpunk;
import me.scarletleaf1000.solarpunk.block.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
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

        clusterBlock(ModBlocks.HELIOLITE_CLUSTER);
        clusterBlock(ModBlocks.LARGE_HELIOLITE_BUD);
        clusterBlock(ModBlocks.MEDIUM_HELIOLITE_BUD);
        clusterBlock(ModBlocks.SMALL_HELIOLITE_BUD);
    }

    private void clusterBlock(DeferredBlock<? extends Block> block) {
        ModelFile model = models().cross(block.getId().getPath(), blockTexture(block.get())).renderType("cutout");
        getVariantBuilder(block.get()).forAllStatesExcept(state -> {
            Direction facing = state.getValue(AmethystClusterBlock.FACING);
            int x = facing == Direction.DOWN ? 180 : facing.getAxis().isHorizontal() ? 90 : 0;
            int y = switch (facing) {
                case EAST -> 90;
                case SOUTH -> 180;
                case WEST -> 270;
                default -> 0;
            };
            return ConfiguredModel.builder().modelFile(model).rotationX(x).rotationY(y).build();
        }, AmethystClusterBlock.WATERLOGGED);
    }

    private void blockWithItem(DeferredBlock<Block> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
    }
}
