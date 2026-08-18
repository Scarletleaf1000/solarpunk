package me.scarletleaf1000.sunworks.block.entity.io;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

/**
 * A view over a slot range of an {@link IItemHandlerModifiable} that only allows insertion,
 * only extraction, or neither - used to expose a machine's inventory to a given side according
 * to its configured {@link IOType}.
 */
public class RestrictedItemHandler implements IItemHandler {
    private final RangedWrapper delegate;
    private final boolean allowInsert;
    private final boolean allowExtract;

    public RestrictedItemHandler(IItemHandlerModifiable base, int minSlot, int maxSlotExclusive,
                                  boolean allowInsert, boolean allowExtract) {
        this.delegate = new RangedWrapper(base, minSlot, maxSlotExclusive);
        this.allowInsert = allowInsert;
        this.allowExtract = allowExtract;
    }

    @Override
    public int getSlots() {
        return delegate.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return delegate.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return allowInsert ? delegate.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return allowExtract ? delegate.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return delegate.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return allowInsert && delegate.isItemValid(slot, stack);
    }
}
