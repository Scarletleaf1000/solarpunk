package me.scarletleaf1000.sunworks.screen;

import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.screen.custom.AlloySmelterMenu;
import me.scarletleaf1000.sunworks.screen.custom.SolarAlloySmelterMenu;
import me.scarletleaf1000.sunworks.screen.custom.SolarPanelMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Sunworks.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<AlloySmelterMenu>> ALLOY_SMELTER_MENU =
            registerMenuType("alloy_smelter_menu", AlloySmelterMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<SolarAlloySmelterMenu>> SOLAR_ALLOY_SMELTER_MENU =
            registerMenuType("solar_alloy_smelter_menu", SolarAlloySmelterMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<SolarPanelMenu>> SOLAR_PANEL_MENU =
            registerMenuType("solar_panel_menu", SolarPanelMenu::new);


    private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(String name,
                                                                                                               IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
