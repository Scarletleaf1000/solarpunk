package me.scarletleaf1000.solarpunk.screen.custom;

import me.scarletleaf1000.solarpunk.block.ModBlocks;
import me.scarletleaf1000.solarpunk.block.entity.custom.SolarAlloySmelterBlockEntity;
import me.scarletleaf1000.solarpunk.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

public class SolarAlloySmelterMenu extends AbstractContainerMenu {
    public final SolarAlloySmelterBlockEntity blockEntity;
    public final Level level;

    public SolarAlloySmelterMenu(int containerId, Inventory inv, FriendlyByteBuf data) {
       this(containerId, inv, inv.player.level().getBlockEntity(data.readBlockPos()));
    }

    public SolarAlloySmelterMenu(int containerId, Inventory inv, @Nullable BlockEntity blockEntity) {
        super(ModMenuTypes.SOLAR_ALLOY_SMELTER_MENU.get(), containerId);
        this.blockEntity = (SolarAlloySmelterBlockEntity) blockEntity;
        this.level = blockEntity.getLevel();

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.addSlot(new SlotItemHandler(this.blockEntity.inventory, 0, 56, 24));
        this.addSlot(new SlotItemHandler(this.blockEntity.inventory, 1, 79, 17));
        this.addSlot(new SlotItemHandler(this.blockEntity.inventory, 2, 102, 24));
        this.addSlot(new SlotItemHandler(this.blockEntity.inventory, 3, 79, 58));
    }

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = 3 * 9;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int MACHINE_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    private static final int MACHINE_INPUT_SLOT_COUNT = 3;
    private static final int MACHINE_SLOT_COUNT = 4;

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = slots.get(index);
        if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (index >= VANILLA_FIRST_SLOT_INDEX && index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, MACHINE_FIRST_SLOT_INDEX, MACHINE_FIRST_SLOT_INDEX + MACHINE_INPUT_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= MACHINE_FIRST_SLOT_INDEX && index < MACHINE_FIRST_SLOT_INDEX + MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(player, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.SOLAR_ALLOY_SMELTER.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
