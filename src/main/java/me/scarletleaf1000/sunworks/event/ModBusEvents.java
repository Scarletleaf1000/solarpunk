package me.scarletleaf1000.sunworks.event;

import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.block.entity.ModBlockEntities;
import me.scarletleaf1000.sunworks.block.entity.custom.cable.EnergyPipeBlockEntity;
import me.scarletleaf1000.sunworks.block.entity.custom.generator.SolarPanelBlockEntity;
import me.scarletleaf1000.sunworks.block.entity.custom.processor.AlloySmelterBlockEntity;
import me.scarletleaf1000.sunworks.block.entity.custom.processor.SolarAlloySmelterBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = Sunworks.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModBusEvents {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent e) {
        e.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ALLOY_SMELTER_BE.get(), AlloySmelterBlockEntity::getItemHandler);
        e.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.SOLAR_ALLOY_SMELTER_BE.get(), SolarAlloySmelterBlockEntity::getItemHandler);
        e.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.SOLAR_PANEL_BE.get(), SolarPanelBlockEntity::getEnergyStorage);
        e.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.ENERGY_PIPE_BE.get(), EnergyPipeBlockEntity::getEnergyStorage);
        e.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.ALLOY_SMELTER_BE.get(), AlloySmelterBlockEntity::getEnergyStorage);
    }
}
