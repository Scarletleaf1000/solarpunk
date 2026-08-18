package me.scarletleaf1000.sunworks.block.entity.io;

import net.minecraft.network.chat.Component;

/**
 * The kind of resource a given side of a {@link ConfigurableMachine} is configured to
 * input/output. Ordinal order matches the layout of the io_icons.png sprite sheet
 * (7 tiles, 8x8 each, left to right): none, energy in, energy out, item in, item out,
 * fluid in, fluid out.
 */
public enum IOType {
    NONE,
    ENERGY_INPUT,
    ENERGY_OUTPUT,
    ITEM_INPUT,
    ITEM_OUTPUT,
    FLUID_INPUT,
    FLUID_OUTPUT;

    public int getIconIndex() {
        return ordinal();
    }

    public Component getDisplayName() {
        return Component.translatable("gui.sunworks.io_type." + name().toLowerCase());
    }
}
