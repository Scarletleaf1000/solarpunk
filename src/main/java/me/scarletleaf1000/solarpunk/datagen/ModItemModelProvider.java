package me.scarletleaf1000.solarpunk.datagen;

import me.scarletleaf1000.solarpunk.Solarpunk;
import me.scarletleaf1000.solarpunk.block.ModBlocks;
import me.scarletleaf1000.solarpunk.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Solarpunk.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.HELIOLITE_SHARD.get());

        clusterItem(ModBlocks.HELIOLITE_CLUSTER);
        clusterItem(ModBlocks.LARGE_HELIOLITE_BUD);
        clusterItem(ModBlocks.MEDIUM_HELIOLITE_BUD);
        clusterItem(ModBlocks.SMALL_HELIOLITE_BUD);
    }

    private void clusterItem(DeferredBlock<? extends Block> block) {
        String name = block.getId().getPath();
        withExistingParent(name, mcLoc("item/generated"))
                .texture("layer0", modLoc("block/" + name));
    }
}
