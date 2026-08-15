package me.scarletleaf1000.solarpunk.datagen;

import me.scarletleaf1000.solarpunk.Solarpunk;
import me.scarletleaf1000.solarpunk.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Solarpunk.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.HELIOLITE_SHARD.get());
    }
}
