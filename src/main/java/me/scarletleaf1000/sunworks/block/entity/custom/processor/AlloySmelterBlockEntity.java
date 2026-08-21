package me.scarletleaf1000.sunworks.block.entity.custom.processor;

import me.scarletleaf1000.sunworks.block.custom.processor.AlloySmelterBlock;
import me.scarletleaf1000.sunworks.block.entity.ModBlockEntities;
import me.scarletleaf1000.sunworks.block.entity.energy.ModEnergyStorage;
import me.scarletleaf1000.sunworks.block.entity.io.ConfigurableMachine;
import me.scarletleaf1000.sunworks.block.entity.io.IOType;
import me.scarletleaf1000.sunworks.block.entity.io.RelativeSide;
import me.scarletleaf1000.sunworks.block.entity.io.RestrictedItemHandler;
import me.scarletleaf1000.sunworks.block.entity.io.SideConfiguration;
import me.scarletleaf1000.sunworks.recipe.ModRecipes;
import me.scarletleaf1000.sunworks.recipe.custom.AlloySmelterRecipe;
import me.scarletleaf1000.sunworks.recipe.custom.AlloySmelterRecipeInput;
import me.scarletleaf1000.sunworks.screen.custom.AlloySmelterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static me.scarletleaf1000.sunworks.block.custom.processor.AlloySmelterBlock.LIT;

public class AlloySmelterBlockEntity extends BlockEntity implements MenuProvider, ConfigurableMachine {
    private static final Set<IOType> SUPPORTED_TYPES = Set.of(IOType.ENERGY_INPUT, IOType.ITEM_INPUT, IOType.ITEM_OUTPUT);

    private final SideConfiguration sideConfiguration = new SideConfiguration();

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
    private int maxProgress = 300;
    private int DEFAULT_MAX_PROGRESS = 300;
    private static final int TIME_MULTIPLIER = 1;

    private static final int ENERGY_CRAFT_AMOUNT = 60;

    public  AlloySmelterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ALLOY_SMELTER_BE.get(), pos, blockState);
        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> AlloySmelterBlockEntity.this.progress;
                    case 1 -> AlloySmelterBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }
            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0: AlloySmelterBlockEntity.this.progress = value;
                    case 1: AlloySmelterBlockEntity.this.maxProgress = value;
                }
            }
            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    private final ModEnergyStorage ENERGY_STORAGE = createEnergyStorage();
    private final int MAX_TRANSFER = 1000;
    private final int MAX_STORAGE = 16000;
    private ModEnergyStorage createEnergyStorage() {
        return new ModEnergyStorage(MAX_STORAGE, MAX_TRANSFER) {
            @Override
            public void onEnergyChanged() {
                setChanged();
                getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        };
    }

    public IEnergyStorage getEnergyStorage(@Nullable Direction direction) {
        if (direction == null) {
            return this.ENERGY_STORAGE;
        }

        RelativeSide side = RelativeSide.fromAbsolute(getFacing(), direction);
        return sideConfiguration.get(side) == IOType.ENERGY_INPUT ? this.ENERGY_STORAGE : null;
    }

    public IItemHandler getItemHandler(@Nullable Direction direction) {
        if (direction == null) {
            return this.itemHandler;
        }

        RelativeSide side = RelativeSide.fromAbsolute(getFacing(), direction);
        return switch (sideConfiguration.get(side)) {
            case ITEM_INPUT -> new RestrictedItemHandler(itemHandler, INPUT_SLOT_1, OUTPUT_SLOT, true, false);
            case ITEM_OUTPUT -> new RestrictedItemHandler(itemHandler, OUTPUT_SLOT, OUTPUT_SLOT + 1, false, true);
            default -> null;
        };
    }

    @Override
    public SideConfiguration getSideConfiguration() {
        return sideConfiguration;
    }

    @Override
    public Set<IOType> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public Direction getFacing() {
        return getBlockState().getValue(AlloySmelterBlock.FACING);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.menu.sunworks.alloy_smelter");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new AlloySmelterMenu(i, inventory, this, this.data);
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(itemHandler.getSlots());
        for(int i = 0; i < itemHandler.getSlots(); i++) {
            inv.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (hasRecipe() && isOutputSlotReceivable() && hasPower(ENERGY_CRAFT_AMOUNT)) {
            increaseProgress();
            consumePower(ENERGY_CRAFT_AMOUNT);
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

    private void consumePower(int power) {
        this.ENERGY_STORAGE.extractEnergy(power, false);
    }

    private boolean hasPower(int power) {
        return this.ENERGY_STORAGE.getEnergyStored() >= power;
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

        List<Integer> blastingSlots = getBlastingInputSlots();
        if (!blastingSlots.isEmpty()) {
            ItemStack output = getBlastingRecipe(blastingSlots.get(0)).get().value()
                    .getResultItem(this.level.registryAccess()).copy();
            output.setCount(output.getCount() * blastingSlots.size());
            for (int slot : blastingSlots) {
                itemHandler.extractItem(slot, 1, false);
            }
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

        List<Integer> blastingSlots = getBlastingInputSlots();
        if (!blastingSlots.isEmpty()) {
            BlastingRecipe blastingRecipe = getBlastingRecipe(blastingSlots.get(0)).get().value();
            ItemStack output = blastingRecipe.getResultItem(this.level.registryAccess());
            if (!canInsertItemIntoOutputSlot(output)
                    || !canInsertAmountIntoOutputSlot(output.getCount() * blastingSlots.size())) {
                return false;
            }
            this.maxProgress = blastingRecipe.getCookingTime() * TIME_MULTIPLIER;
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

    private List<Integer> getBlastingInputSlots() {
        List<Integer> slots = new ArrayList<>();
        ItemStack expectedOutput = ItemStack.EMPTY;
        for (int slot = INPUT_SLOT_1; slot <= INPUT_SLOT_3; slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            Optional<RecipeHolder<BlastingRecipe>> recipe = getBlastingRecipe(slot);
            if (recipe.isEmpty()) {
                return List.of();
            }
            ItemStack output = recipe.get().value().getResultItem(this.level.registryAccess());
            if (output.isEmpty()) {
                return List.of();
            }
            if (expectedOutput.isEmpty()) {
                expectedOutput = output;
            } else if (!ItemStack.isSameItemSameComponents(expectedOutput, output)) {
                return List.of();
            }
            slots.add(slot);
        }
        return slots;
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

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        return saveWithoutMetadata(pRegistries);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider pRegistries) {
        super.onDataPacket(net, pkt, pRegistries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putInt("alloy_smelter.progress", progress);
        tag.putInt("alloy_smelter.max_progress", maxProgress);
        tag.putInt("solar_panel.energy", ENERGY_STORAGE.getEnergyStored());

        CompoundTag sideConfigTag = new CompoundTag();
        sideConfiguration.save(sideConfigTag);
        tag.put("side_config", sideConfigTag);

        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        progress = tag.getInt("alloy_smelter.progress");
        maxProgress = tag.getInt("alloy_smelter.max_progress");
        ENERGY_STORAGE.setEnergy(tag.getInt("solar_panel.energy"));

        if (tag.contains("side_config")) {
            sideConfiguration.load(tag.getCompound("side_config"));
        }

        super.loadAdditional(tag, registries);
    }
}
