package me.scarletleaf1000.solarpunk.item;

import me.scarletleaf1000.solarpunk.Solarpunk;
import me.scarletleaf1000.solarpunk.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Solarpunk.MOD_ID);

    public static final Supplier<CreativeModeTab> SOLARPUNK_ITEMS_TAB =
            CREATIVE_MODE_TABS.register("solarpunk_item_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.solarpunk.item_tab"))
                    .icon(() -> new ItemStack(ModItems.HELIOLITE_SHARD.get()))
                    //.withSearchBar()
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.HELIOLITE_SHARD);
                        pOutput.accept(ModItems.CINDERITE_INGOT);
                        pOutput.accept(ModItems.CINDERITE_NUGGET);
                        pOutput.accept(ModItems.RAW_CINDERITE);
                        pOutput.accept(ModItems.SILVER_INGOT);
                        pOutput.accept(ModItems.SILVER_NUGGET);
                        pOutput.accept(ModItems.RAW_SILVER);

                    }).build());

    public static final Supplier<CreativeModeTab> SOLARPUNK_BLOCKS_TAB =
            CREATIVE_MODE_TABS.register("solarpunk_block_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.solarpunk.block_tab"))
                    .icon(() -> new ItemStack(ModBlocks.HELIOLITE_BLOCK.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(Solarpunk.MOD_ID, "solarpunk_item_tab"))
                    //.withSearchBar()
                    .displayItems((pParameters, pOutput) -> {
                       pOutput.accept(ModBlocks.HELIOLITE_BLOCK);
                       pOutput.accept(ModBlocks.BUDDING_HELIOLITE);
                       pOutput.accept(ModBlocks.SMALL_HELIOLITE_BUD);
                       pOutput.accept(ModBlocks.MEDIUM_HELIOLITE_BUD);
                       pOutput.accept(ModBlocks.LARGE_HELIOLITE_BUD);
                       pOutput.accept(ModBlocks.HELIOLITE_CLUSTER);

                       pOutput.accept(ModBlocks.CINDERITE_ORE);
                       pOutput.accept(ModBlocks.DEEPSLATE_CINDERITE_ORE);
                       pOutput.accept(ModBlocks.RAW_CINDERITE_BLOCK);
                       pOutput.accept(ModBlocks.CINDERITE_BLOCK);
                       pOutput.accept(ModBlocks.SILVER_ORE);
                       pOutput.accept(ModBlocks.DEEPSLATE_SILVER_ORE);
                       pOutput.accept(ModBlocks.RAW_SILVER_BLOCK);
                       pOutput.accept(ModBlocks.SILVER_BLOCK);
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
