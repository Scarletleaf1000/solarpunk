package me.scarletleaf1000.solarpunk.item;

import me.scarletleaf1000.solarpunk.Solarpunk;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Solarpunk.MOD_ID);

    public static final DeferredItem<Item> HELIOLITE_SHARD = ITEMS.registerItem("heliolite_shard",
            Item::new, new Item.Properties());

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
