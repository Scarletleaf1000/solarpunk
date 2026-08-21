package me.scarletleaf1000.sunworks.block;

import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.block.custom.BuddingHelioliteBlock;
import me.scarletleaf1000.sunworks.block.custom.cable.CableTier;
import me.scarletleaf1000.sunworks.block.custom.cable.EnergyPipeBlock;
import me.scarletleaf1000.sunworks.block.custom.processor.AlloySmelterBlock;
import me.scarletleaf1000.sunworks.block.custom.processor.SolarAlloySmelterBlock;
import me.scarletleaf1000.sunworks.block.custom.generator.SolarPanelBlock;
import me.scarletleaf1000.sunworks.item.ModItems;
import me.scarletleaf1000.sunworks.item.custom.DescriptiveBlockItem;
import net.minecraft.network.chat.Component;
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

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Sunworks.MOD_ID);

    public static final DeferredBlock<Block> HELIOLITE_BLOCK = registerBlock("heliolite_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(2f, 2f)
                    .sound(SoundType.BONE_BLOCK)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> BUDDING_HELIOLITE = registerBlock("budding_heliolite",
            () -> new BuddingHelioliteBlock(BlockBehaviour.Properties.of()
                    .strength(4f, 2f)
                    .noLootTable()
                    .randomTicks()
                    .sound(SoundType.BONE_BLOCK)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<AmethystClusterBlock> HELIOLITE_CLUSTER = registerCluster("heliolite_cluster",
            7f, 5.5f, SoundType.BONE_BLOCK, 5);
    public static final DeferredBlock<AmethystClusterBlock> LARGE_HELIOLITE_BUD = registerCluster("large_heliolite_bud",
            5f, 5.5f, SoundType.BONE_BLOCK, 4);
    public static final DeferredBlock<AmethystClusterBlock> MEDIUM_HELIOLITE_BUD = registerCluster("medium_heliolite_bud",
            4f, 5.5f, SoundType.BONE_BLOCK, 2);
    public static final DeferredBlock<AmethystClusterBlock> SMALL_HELIOLITE_BUD = registerCluster("small_heliolite_bud",
            3f, 6f, SoundType.BONE_BLOCK, 1);

    public static final DeferredBlock<Block> CINDERITE_ORE = registerOre("cinderite_ore", false);
    public static final DeferredBlock<Block> DEEPSLATE_CINDERITE_ORE = registerOre("deepslate_cinderite_ore", true);
    public static final DeferredBlock<Block> SILVER_ORE = registerOre("silver_ore", false);
    public static final DeferredBlock<Block> DEEPSLATE_SILVER_ORE = registerOre("deepslate_silver_ore", true);

    public static final DeferredBlock<Block> CINDERITE_BLOCK = registerBlock("cinderite_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(5f, 6f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> SILVER_BLOCK = registerBlock("silver_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(5f, 6f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> ELECTRUM_BLOCK = registerBlock("electrum_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(5f, 6f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RAW_CINDERITE_BLOCK = registerBlock("raw_cinderite_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(5f, 6f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> RAW_SILVER_BLOCK = registerBlock("raw_silver_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(5f, 6f)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> SOLAR_ALLOY_SMELTER = registerBlock("solar_alloy_smelter",
            () -> new SolarAlloySmelterBlock(BlockBehaviour.Properties.of()
                    .strength(5f, 6f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()),
            block -> new DescriptiveBlockItem(block, new Item.Properties(),
                    Component.translatable("tooltip.sunworks.solar_alloy_smelter.description")));
    public static final DeferredBlock<Block> ALLOY_SMELTER = registerBlock("alloy_smelter",
            () -> new AlloySmelterBlock(BlockBehaviour.Properties.of()
                    .strength(5f, 6f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()),
            block -> new DescriptiveBlockItem(block, new Item.Properties(),
                    Component.translatable("tooltip.sunworks.alloy_smelter.description")));
    public static final DeferredBlock<Block> SOLAR_PANEL = registerBlock("solar_panel",
            () -> new SolarPanelBlock(BlockBehaviour.Properties.of()
                    .strength(5f, 6f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
            ),
            block -> new DescriptiveBlockItem(block, new Item.Properties(),
                    Component.translatable("tooltip.sunworks.solar_panel.power_generation"),
                    Component.translatable("tooltip.sunworks.solar_panel.energy_storage")));

    public static final Map<CableTier, DeferredBlock<EnergyPipeBlock>> ENERGY_PIPES = new EnumMap<>(CableTier.class);

    static {
        for (CableTier tier : CableTier.values()) {
            registerPipeTier(tier);
        }
    }


    private static void registerPipeTier(CableTier tier) {
        String name = "energy_pipe_" + tier.getName();

        DeferredBlock<EnergyPipeBlock> pipe = BLOCKS.register(name, () -> new EnergyPipeBlock(
                BlockBehaviour.Properties.of()
                        .strength(1.5f)
                        .sound(SoundType.METAL)
                        .noOcclusion(),
                tier));
        ModItems.ITEMS.register(name, () -> new DescriptiveBlockItem(pipe.get(), new Item.Properties(),
                Component.translatable("tooltip.sunworks.power_transfer_rate", tier.getMaxTransfer())));

        ENERGY_PIPES.put(tier, pipe);
    }

    private static DeferredBlock<Block> registerOre(String name, boolean deepslate) {
        return registerBlock(name, () -> new Block(BlockBehaviour.Properties.of()
                .strength(deepslate ? 4.5f : 3f, 3f)
                .sound(deepslate ? SoundType.DEEPSLATE : SoundType.STONE)
                .requiresCorrectToolForDrops()));
    }

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

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block, Function<T, ? extends BlockItem> itemFactory) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> itemFactory.apply(toReturn.get()));
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
