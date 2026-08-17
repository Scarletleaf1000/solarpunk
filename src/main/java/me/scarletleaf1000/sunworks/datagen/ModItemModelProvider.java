package me.scarletleaf1000.sunworks.datagen;

import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.block.ModBlocks;
import me.scarletleaf1000.sunworks.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Sunworks.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.HELIOLITE_SHARD.get());
        basicItem(ModItems.CINDERITE_INGOT.get());
        basicItem(ModItems.RAW_CINDERITE.get());
        basicItem(ModItems.SILVER_INGOT.get());
        basicItem(ModItems.RAW_SILVER.get());
        basicItem(ModItems.CINDERITE_NUGGET.get());
        basicItem(ModItems.SILVER_NUGGET.get());
        basicItem(ModItems.ELECTRUM_INGOT.get());
        basicItem(ModItems.ELECTRUM_NUGGET.get());

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
