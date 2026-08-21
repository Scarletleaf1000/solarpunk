package me.scarletleaf1000.sunworks.datagen;

import me.scarletleaf1000.sunworks.block.ModBlocks;
import me.scarletleaf1000.sunworks.block.custom.cable.CableTier;
import me.scarletleaf1000.sunworks.item.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
    protected ModBlockLootTableProvider(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.HELIOLITE_BLOCK.get());

        dropSelf(ModBlocks.CINDERITE_BLOCK.get());
        dropSelf(ModBlocks.SILVER_BLOCK.get());
        dropSelf(ModBlocks.RAW_CINDERITE_BLOCK.get());
        dropSelf(ModBlocks.RAW_SILVER_BLOCK.get());
        dropSelf(ModBlocks.ELECTRUM_BLOCK.get());
        dropSelf(ModBlocks.SOLAR_ALLOY_SMELTER.get());
        dropSelf(ModBlocks.ALLOY_SMELTER.get());
        dropSelf(ModBlocks.SOLAR_PANEL.get());

        for (CableTier tier : CableTier.values()) {
            dropSelf(ModBlocks.ENERGY_PIPES.get(tier).get());
        }

        add(ModBlocks.CINDERITE_ORE.get(), block -> createOreDrop(block, ModItems.RAW_CINDERITE.get()));
        add(ModBlocks.DEEPSLATE_CINDERITE_ORE.get(), block -> createOreDrop(block, ModItems.RAW_CINDERITE.get()));
        add(ModBlocks.SILVER_ORE.get(), block -> createOreDrop(block, ModItems.RAW_SILVER.get()));
        add(ModBlocks.DEEPSLATE_SILVER_ORE.get(), block -> createOreDrop(block, ModItems.RAW_SILVER.get()));

        dropWhenSilkTouch(ModBlocks.SMALL_HELIOLITE_BUD.get());
        dropWhenSilkTouch(ModBlocks.MEDIUM_HELIOLITE_BUD.get());
        dropWhenSilkTouch(ModBlocks.LARGE_HELIOLITE_BUD.get());

        Holder<Enchantment> fortune = this.registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
        this.add(ModBlocks.HELIOLITE_CLUSTER.get(), block -> createSilkTouchDispatchTable(block,
                LootItem.lootTableItem(ModItems.HELIOLITE_SHARD.get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 3.0F)))
                        .apply(ApplyBonusCount.addUniformBonusCount(fortune))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
