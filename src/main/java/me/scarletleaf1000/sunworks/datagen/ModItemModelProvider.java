package me.scarletleaf1000.sunworks.datagen;

import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.block.ModBlocks;
import me.scarletleaf1000.sunworks.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemDisplayContext;
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
        basicItem(ModItems.SILICON.get());
        basicItem(ModItems.HEAT_CORE.get());
        basicItem(ModItems.SOLAR_PANEL_COMPONENT.get());
        basicItem(ModItems.EXTRACTOR.get());

        clusterItem(ModBlocks.HELIOLITE_CLUSTER);
        clusterItem(ModBlocks.LARGE_HELIOLITE_BUD);
        clusterItem(ModBlocks.MEDIUM_HELIOLITE_BUD);
        clusterItem(ModBlocks.SMALL_HELIOLITE_BUD);

        withExistingParent(ModBlocks.SOLAR_PANEL.getId().getPath(), modLoc("block/solar_panel"))
                .transforms()
                    .transform(ItemDisplayContext.GUI)
                        .rotation(30, 225, 0)
                        .translation(0, 0, 0)
                        .scale(0.625f, 0.625f, 0.625f)
                        .end()
                    .transform(ItemDisplayContext.GROUND)
                        .rotation(0, 0, 0)
                        .translation(0, 3, 0)
                        .scale(0.25f, 0.25f, 0.25f)
                        .end()
                    .transform(ItemDisplayContext.FIXED)
                        .rotation(0, 0, 0)
                        .translation(0, 0, 0)
                        .scale(0.5f, 0.5f, 0.5f)
                        .end()
                    .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                        .rotation(75, 45, 0)
                        .translation(0, 2.5f, 0)
                        .scale(0.375f, 0.375f, 0.375f)
                        .end()
                    .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                        .rotation(75, 45, 0)
                        .translation(0, 2.5f, 0)
                        .scale(0.375f, 0.375f, 0.375f)
                        .end()
                    .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                        .rotation(0, 45, 0)
                        .translation(0, 0, 0)
                        .scale(0.4f, 0.4f, 0.4f)
                        .end()
                    .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                        .rotation(0, 225, 0)
                        .translation(0, 0, 0)
                        .scale(0.4f, 0.4f, 0.4f)
                        .end()
                    .end();
    }

    private void clusterItem(DeferredBlock<? extends Block> block) {
        String name = block.getId().getPath();
        withExistingParent(name, mcLoc("item/generated"))
                .texture("layer0", modLoc("block/" + name));
    }
}
