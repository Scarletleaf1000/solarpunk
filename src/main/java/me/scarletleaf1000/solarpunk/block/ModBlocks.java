package me.scarletleaf1000.solarpunk.block;

import me.scarletleaf1000.solarpunk.Solarpunk;
import me.scarletleaf1000.solarpunk.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Solarpunk.MOD_ID);

    public static final DeferredBlock<Block> HELIOLITE_BLOCK = registerBlock("heliolite_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(2f)
                    .explosionResistance(2f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> BUDDING_HELIOLITE = registerBlock("budding_heliolite",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(2f)
                    .explosionResistance(2f)
                    .noLootTable()));


    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
