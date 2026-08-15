package me.scarletleaf1000.solarpunk.block;

import me.scarletleaf1000.solarpunk.Solarpunk;
import me.scarletleaf1000.solarpunk.block.custom.BuddingHelioliteBlock;
import me.scarletleaf1000.solarpunk.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
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
            () -> new BuddingHelioliteBlock(BlockBehaviour.Properties.of()
                    .strength(4f)
                    .explosionResistance(2f)
                    .noLootTable()
                    .randomTicks()
                    .sound(SoundType.AMETHYST)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<AmethystClusterBlock> HELIOLITE_CLUSTER = registerCluster("heliolite_cluster",
            7f, 5.5f, SoundType.AMETHYST_CLUSTER, 5);
    public static final DeferredBlock<AmethystClusterBlock> LARGE_HELIOLITE_BUD = registerCluster("large_heliolite_bud",
            5f, 5.5f, SoundType.LARGE_AMETHYST_BUD, 4);
    public static final DeferredBlock<AmethystClusterBlock> MEDIUM_HELIOLITE_BUD = registerCluster("medium_heliolite_bud",
            4f, 5.5f, SoundType.MEDIUM_AMETHYST_BUD, 2);
    public static final DeferredBlock<AmethystClusterBlock> SMALL_HELIOLITE_BUD = registerCluster("small_heliolite_bud",
            3f, 6f, SoundType.SMALL_AMETHYST_BUD, 1);

    private static DeferredBlock<AmethystClusterBlock> registerCluster(String name, float height, float inset, SoundType sound, int lightLevel) {
        return registerBlock(name, () -> new AmethystClusterBlock(height, inset,
                BlockBehaviour.Properties.of()
                        .forceSolidOn()
                        .noOcclusion()
                        .sound(sound)
                        .strength(1.5f)
                        .lightLevel(state -> lightLevel)
                        .pushReaction(PushReaction.DESTROY)));
    }


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
