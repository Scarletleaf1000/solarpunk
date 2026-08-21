package me.scarletleaf1000.sunworks.item;

import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.block.ModBlocks;
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
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Sunworks.MOD_ID);

    public static final Supplier<CreativeModeTab> SUNWORKS_ITEMS_TAB =
            CREATIVE_MODE_TABS.register("sunworks_item_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.sunworks.item_tab"))
                    .icon(() -> new ItemStack(ModItems.HELIOLITE_SHARD.get()))
                    //.withSearchBar()
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.HELIOLITE_SHARD);
                        output.accept(ModItems.CINDERITE_INGOT);
                        output.accept(ModItems.CINDERITE_NUGGET);
                        output.accept(ModItems.RAW_CINDERITE);
                        output.accept(ModItems.SILVER_INGOT);
                        output.accept(ModItems.SILVER_NUGGET);
                        output.accept(ModItems.RAW_SILVER);
                        output.accept(ModItems.ELECTRUM_INGOT);
                        output.accept(ModItems.ELECTRUM_NUGGET);
                        output.accept(ModItems.SILICON);
                        output.accept(ModItems.HEAT_CORE);
                        output.accept(ModItems.SOLAR_PANEL_COMPONENT);

                    }).build());

    public static final Supplier<CreativeModeTab> SUNWORKS_BLOCKS_TAB =
            CREATIVE_MODE_TABS.register("sunworks_block_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.sunworks.block_tab"))
                    .icon(() -> new ItemStack(ModBlocks.HELIOLITE_BLOCK.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(Sunworks.MOD_ID, "sunworks_item_tab"))
                    //.withSearchBar()
                    .displayItems((parameters, output) -> {
                       output.accept(ModBlocks.HELIOLITE_BLOCK);
                       output.accept(ModBlocks.BUDDING_HELIOLITE);
                       output.accept(ModBlocks.SMALL_HELIOLITE_BUD);
                       output.accept(ModBlocks.MEDIUM_HELIOLITE_BUD);
                       output.accept(ModBlocks.LARGE_HELIOLITE_BUD);
                       output.accept(ModBlocks.HELIOLITE_CLUSTER);

                       output.accept(ModBlocks.CINDERITE_ORE);
                       output.accept(ModBlocks.DEEPSLATE_CINDERITE_ORE);
                       output.accept(ModBlocks.RAW_CINDERITE_BLOCK);
                       output.accept(ModBlocks.CINDERITE_BLOCK);
                       output.accept(ModBlocks.SILVER_ORE);
                       output.accept(ModBlocks.DEEPSLATE_SILVER_ORE);
                       output.accept(ModBlocks.RAW_SILVER_BLOCK);
                       output.accept(ModBlocks.SILVER_BLOCK);
                       output.accept(ModBlocks.ELECTRUM_BLOCK);

                       output.accept(ModBlocks.SOLAR_ALLOY_SMELTER);
                       output.accept(ModBlocks.ALLOY_SMELTER);
                       output.accept(ModBlocks.SOLAR_PANEL);

                       ModBlocks.ENERGY_PIPES.values().forEach(output::accept);
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
