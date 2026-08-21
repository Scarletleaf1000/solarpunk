package me.scarletleaf1000.sunworks.block.entity;

import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.block.ModBlocks;
import me.scarletleaf1000.sunworks.block.custom.generator.SolarPanelBlock;
import me.scarletleaf1000.sunworks.block.entity.custom.cable.EnergyPipeBlockEntity;
import me.scarletleaf1000.sunworks.block.entity.custom.generator.SolarPanelBlockEntity;
import me.scarletleaf1000.sunworks.block.entity.custom.processor.AlloySmelterBlockEntity;
import me.scarletleaf1000.sunworks.block.entity.custom.processor.SolarAlloySmelterBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Sunworks.MOD_ID);

    public static final Supplier<BlockEntityType<AlloySmelterBlockEntity>> ALLOY_SMELTER_BE =
            BLOCK_ENTITIES.register("alloy_smelter_be", () -> BlockEntityType.Builder.of(
                    AlloySmelterBlockEntity::new, ModBlocks.ALLOY_SMELTER.get()).build(null));
    public static final Supplier<BlockEntityType<SolarAlloySmelterBlockEntity>> SOLAR_ALLOY_SMELTER_BE =
            BLOCK_ENTITIES.register("solar_alloy_smelter_be", () -> BlockEntityType.Builder.of(
                    SolarAlloySmelterBlockEntity::new, ModBlocks.SOLAR_ALLOY_SMELTER.get()).build(null));

    public static final Supplier<BlockEntityType<SolarPanelBlockEntity>> SOLAR_PANEL_BE =
            BLOCK_ENTITIES.register("solar_panel_be", () -> BlockEntityType.Builder.of(
                    SolarPanelBlockEntity::new, ModBlocks.SOLAR_PANEL.get()).build(null));

    public static final Supplier<BlockEntityType<EnergyPipeBlockEntity>> ENERGY_PIPE_BE =
            BLOCK_ENTITIES.register("energy_pipe_be", () -> BlockEntityType.Builder.of(
                    EnergyPipeBlockEntity::new,
                    ModBlocks.ENERGY_PIPES.values().stream()
                            .map(deferredBlock -> (Block) deferredBlock.get())
                            .toArray(Block[]::new)).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
