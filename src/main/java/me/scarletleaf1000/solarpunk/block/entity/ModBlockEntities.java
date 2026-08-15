package me.scarletleaf1000.solarpunk.block.entity;

import me.scarletleaf1000.solarpunk.Solarpunk;
import me.scarletleaf1000.solarpunk.block.ModBlocks;
import me.scarletleaf1000.solarpunk.block.entity.custom.SolarAlloySmelterBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Solarpunk.MOD_ID);

    public static final Supplier<BlockEntityType<SolarAlloySmelterBlockEntity>> SOLAR_ALLOY_SMELTER_BE =
            BLOCK_ENTITIES.register("solar_alloy_smelter_be", () -> BlockEntityType.Builder.of(
                    SolarAlloySmelterBlockEntity::new, ModBlocks.SOLAR_ALLOY_SMELTER.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
