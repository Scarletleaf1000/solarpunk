package me.scarletleaf1000.solarpunk.block.entity.custom;

import me.scarletleaf1000.solarpunk.block.entity.ModBlockEntities;
import me.scarletleaf1000.solarpunk.recipe.ModRecipes;
import me.scarletleaf1000.solarpunk.recipe.custom.AlloySmelterRecipe;
import me.scarletleaf1000.solarpunk.recipe.custom.AlloySmelterRecipeInput;
import me.scarletleaf1000.solarpunk.screen.custom.SolarAlloySmelterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static me.scarletleaf1000.solarpunk.block.custom.SolarAlloySmelterBlock.LIT;

public class SolarAlloySmelterBlockEntity extends BlockEntity implements MenuProvider {
    public final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
           setChanged();
           if(!level.isClientSide) {
               level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
           }
        }
    };

    private static final int INPUT_SLOT_1 = 0;
    private static final int INPUT_SLOT_2 = 1;
    private static final int INPUT_SLOT_3 = 2;
    private static final int OUTPUT_SLOT = 3;

    private final ContainerData data;
    private int progress = 0;
    private int maxProgress = 600;
    private int DEFAULT_MAX_PROGRESS = 600;
    private static final int TIME_MULTIPLIER = 3;

    public  SolarAlloySmelterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.SOLAR_ALLOY_SMELTER_BE.get(), pos, blockState);
        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> SolarAlloySmelterBlockEntity.this.progress;
                    case 1 -> SolarAlloySmelterBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }
            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0: SolarAlloySmelterBlockEntity.this.progress = value;
                    case 1: SolarAlloySmelterBlockEntity.this.maxProgress = value;
                }
            }
            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.menu.solarpunk.solar_alloy_smelter");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new SolarAlloySmelterMenu(i, inventory, this, this.data);
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(itemHandler.getSlots());
        for(int i = 0; i < itemHandler.getSlots(); i++) {
            inv.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
           if (hasRecipe() && isOutputSlotReceivable()) {
               increaseProgress();
               level.setBlock(pos, state.setValue(LIT, true), 3);
               setChanged(level, pos, state);

               if (hasCraftingFinished()) {
                   craftItem();
                   resetProgress();
               }
           } else {
               resetProgress();
               level.setBlock(pos, state.setValue(LIT, false), 3);
           }
    }

    private void resetProgress() {
        this.progress = 0;
        this.maxProgress = DEFAULT_MAX_PROGRESS;
    }

    private void craftItem() {
        Optional<AlloySmelterRecipe> alloyRecipe = getCurrentAlloyRecipe();
        if (alloyRecipe.isPresent()) {
            for (SizedIngredient ingredient : alloyRecipe.get().inputs()) {
                int remaining = ingredient.count();
                for (int slot = INPUT_SLOT_1; slot <= INPUT_SLOT_3 && remaining > 0; slot++) {
                    ItemStack stack = itemHandler.getStackInSlot(slot);
                    if (!stack.isEmpty() && ingredient.test(stack)) {
                        remaining -= itemHandler.extractItem(slot, remaining, false).getCount();
                    }
                }
            }
            insertIntoOutput(alloyRecipe.get().output());
            return;
        }

        int blastingSlot = getBlastingInputSlot();
        if (blastingSlot != -1) {
            Optional<RecipeHolder<BlastingRecipe>> blastingRecipe = getBlastingRecipe(blastingSlot);
            if (blastingRecipe.isEmpty()) {
                return;
            }
            ItemStack output = blastingRecipe.get().value().getResultItem(this.level.registryAccess());
            itemHandler.extractItem(blastingSlot, 1, false);
            insertIntoOutput(output);
        }
    }

    private void insertIntoOutput(ItemStack result) {
        itemHandler.insertItem(OUTPUT_SLOT, result.copy(), false);
    }

    private boolean hasCraftingFinished() {
        return this.progress >= this.maxProgress;
    }

    private void increaseProgress() {
        progress++;
    }

    private boolean isOutputSlotReceivable() {
        return this.itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() ||
                this.itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() < this.itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
    }

    private boolean hasRecipe() {
        Optional<AlloySmelterRecipe> alloyRecipe = getCurrentAlloyRecipe();
        if (alloyRecipe.isPresent()) {
            ItemStack output = alloyRecipe.get().output();
            if (!canInsertItemIntoOutputSlot(output) || !canInsertAmountIntoOutputSlot(output.getCount())) {
                return false;
            }
            this.maxProgress = alloyRecipe.get().time() * TIME_MULTIPLIER;
            return true;
        }

        int blastingSlot = getBlastingInputSlot();
        if (blastingSlot != -1) {
            Optional<RecipeHolder<BlastingRecipe>> blastingRecipe = getBlastingRecipe(blastingSlot);
            if (blastingRecipe.isEmpty()) {
                return false;
            }
            ItemStack output = blastingRecipe.get().value().getResultItem(this.level.registryAccess());
            if (output.isEmpty() || !canInsertItemIntoOutputSlot(output)
                    || !canInsertAmountIntoOutputSlot(output.getCount())) {
                return false;
            }
            this.maxProgress = blastingRecipe.get().value().getCookingTime() * TIME_MULTIPLIER;
            return true;
        }

        return false;
    }

    private Optional<AlloySmelterRecipe> getCurrentAlloyRecipe() {
        if (this.level == null) {
            return Optional.empty();
        }
        AlloySmelterRecipeInput input = new AlloySmelterRecipeInput(List.of(
                itemHandler.getStackInSlot(INPUT_SLOT_1),
                itemHandler.getStackInSlot(INPUT_SLOT_2),
                itemHandler.getStackInSlot(INPUT_SLOT_3)));
        return this.level.getRecipeManager()
                .getRecipeFor(ModRecipes.ALLOY_SMELTER_TYPE.get(), input, this.level)
                .map(RecipeHolder::value);
    }

    private int getBlastingInputSlot() {
        for (int slot = INPUT_SLOT_1; slot <= INPUT_SLOT_3; slot++) {
            if (!itemHandler.getStackInSlot(slot).isEmpty() && getBlastingRecipe(slot).isPresent()) {
                return slot;
            }
        }
        return -1;
    }

    private Optional<RecipeHolder<BlastingRecipe>> getBlastingRecipe(int slot) {
        if (this.level == null) {
            return Optional.empty();
        }
        return this.level.getRecipeManager().getRecipeFor(RecipeType.BLASTING,
                new SingleRecipeInput(itemHandler.getStackInSlot(slot)), this.level);
    }

    private boolean canInsertItemIntoOutputSlot(ItemStack output) {
        return itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() ||
                itemHandler.getStackInSlot(OUTPUT_SLOT).getItem() == output.getItem();
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        int maxCount = itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() ? 64 : itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
        int currentCount = itemHandler.getStackInSlot(OUTPUT_SLOT).getCount();

        return maxCount >= currentCount + count;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putInt("solar_alloy_smelter.progress", progress);
        tag.putInt("solar_alloy_smelter.max_progress", maxProgress);

        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        progress = tag.getInt("solar_alloy_smelter.progress");
        maxProgress = tag.getInt("solar_alloy_smelter.max_progress");

        super.loadAdditional(tag, registries);
    }
}
