package me.scarletleaf1000.solarpunk.event;

import me.scarletleaf1000.solarpunk.Solarpunk;
import me.scarletleaf1000.solarpunk.block.entity.ModBlockEntities;
import me.scarletleaf1000.solarpunk.block.entity.custom.SolarAlloySmelterBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = Solarpunk.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModBusEvents {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent e) {
        e.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.SOLAR_ALLOY_SMELTER_BE.get(), SolarAlloySmelterBlockEntity::getItemHandler);
    }
}
