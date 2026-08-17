package me.scarletleaf1000.sunworks.block.entity.custom.generator;

import me.scarletleaf1000.sunworks.block.entity.ModBlockEntities;
import me.scarletleaf1000.sunworks.block.entity.energy.ModEnergyStorage;
import me.scarletleaf1000.sunworks.block.entity.energy.ModEnergyUtil;
import me.scarletleaf1000.sunworks.screen.custom.SolarPanelMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class SolarPanelBlockEntity extends BlockEntity implements MenuProvider {
    public SolarPanelBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.SOLAR_PANEL_BE.get(), pos, blockState);
    }

    private final ModEnergyStorage ENERGY_STORAGE = createEnergyStorage();
    private final int MAX_TRANSFER = 60;
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
        return this.ENERGY_STORAGE;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        generatePower(getSunlightPower(level, pos));

        pushEnergyToNeighborsBelow();
    }

    private void pushEnergyToNeighborsBelow() {
        if (ModEnergyUtil.doesBlockHaveEnergyStorage(this.worldPosition.below(), this.level)) {
            ModEnergyUtil.move(this.worldPosition, this.worldPosition.below(), MAX_TRANSFER, this.level);
        }
    }

    private void generatePower(int power) {
        this.ENERGY_STORAGE.receiveEnergy(5 * power, false);
    }


    private int getSunlightPower(Level level, BlockPos pos) {
        if (level.isClientSide) return 0;
        int minLight = 14;
        if (level.getDayTime() > 23000 || level.getDayTime() < 13000) {
            minLight = 9;
        }

        if (level.getBrightness(LightLayer.SKY, pos.above()) > minLight) {
            return level.getBrightness(LightLayer.SKY, pos.above()) - minLight;
        }

        return 0;
    }


    @Override
    public Component getDisplayName() {
        return Component.translatable("block.menu.sunworks.solar_panel");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new SolarPanelMenu(i, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("solar_panel.energy", ENERGY_STORAGE.getEnergyStored());

        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ENERGY_STORAGE.setEnergy(tag.getInt("solar_panel.energy"));

    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket kt, HolderLookup.Provider registries) {
        super.onDataPacket(net, kt, registries);
    }
}
