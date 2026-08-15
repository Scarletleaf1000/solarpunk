package me.scarletleaf1000.solarpunk.block.entity.custom;

import me.scarletleaf1000.solarpunk.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SolarAlloySmelterBlockEntity extends BlockEntity implements Container {
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);

    public  SolarAlloySmelterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.SOLAR_ALLOY_SMELTER_BE.get(), pos, blockState);
    }

    @Override
    public int getContainerSize() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack item = getItem(i);
            if (!item.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int i) {
        setChanged();
        return inventory.get(i);
    }

    @Override
    public ItemStack removeItem(int i, int amount) {
        setChanged();
        ItemStack item = inventory.get(i);
        item.shrink(amount);
        return inventory.set(i, item);
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        setChanged();
        return ContainerHelper.takeItem(inventory, i);
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        setChanged();
        inventory.set(i, itemStack);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        inventory.clear();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, inventory, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, inventory, registries);
    }
}
