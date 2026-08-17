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

    public static final DeferredItem<Item> CINDERITE_INGOT = ITEMS.registerItem("cinderite_ingot",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> RAW_CINDERITE = ITEMS.registerItem("raw_cinderite",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> SILVER_INGOT = ITEMS.registerItem("silver_ingot",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> RAW_SILVER = ITEMS.registerItem("raw_silver",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> CINDERITE_NUGGET = ITEMS.registerItem("cinderite_nugget",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> SILVER_NUGGET = ITEMS.registerItem("silver_nugget",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> ELECTRUM_INGOT = ITEMS.registerItem("electrum_ingot",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> ELECTRUM_NUGGET = ITEMS.registerItem("electrum_nugget",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> SILICON = ITEMS.registerItem("silicon",
            Item::new, new Item.Properties());

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
