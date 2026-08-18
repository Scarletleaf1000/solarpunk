package me.scarletleaf1000.sunworks.block.entity.custom.generator;

import me.scarletleaf1000.sunworks.block.entity.ModBlockEntities;
import me.scarletleaf1000.sunworks.block.entity.energy.ModEnergyStorage;
import me.scarletleaf1000.sunworks.block.entity.energy.ModEnergyUtil;
import me.scarletleaf1000.sunworks.block.entity.io.ConfigurableMachine;
import me.scarletleaf1000.sunworks.block.entity.io.IOType;
import me.scarletleaf1000.sunworks.block.entity.io.RelativeSide;
import me.scarletleaf1000.sunworks.block.entity.io.SideConfiguration;
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

import java.util.Set;

public class SolarPanelBlockEntity extends BlockEntity implements MenuProvider, ConfigurableMachine {
    private static final Set<IOType> SUPPORTED_TYPES = Set.of(IOType.ENERGY_OUTPUT);

    private final SideConfiguration sideConfiguration = new SideConfiguration();

    public SolarPanelBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.SOLAR_PANEL_BE.get(), pos, blockState);
        sideConfiguration.set(RelativeSide.DOWN, IOType.ENERGY_OUTPUT);
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

    private final IEnergyStorage EXTRACT_ONLY_STORAGE = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return ENERGY_STORAGE.extractEnergy(maxExtract, simulate);
        }

        @Override
        public int getEnergyStored() {
            return ENERGY_STORAGE.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return ENERGY_STORAGE.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return ENERGY_STORAGE.canExtract();
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    };

    public IEnergyStorage getEnergyStorage(@Nullable Direction direction) {
        if (direction == null) {
            return this.EXTRACT_ONLY_STORAGE;
        }

        RelativeSide side = RelativeSide.fromAbsolute(getFacing(), direction);
        return sideConfiguration.get(side) == IOType.ENERGY_OUTPUT ? this.EXTRACT_ONLY_STORAGE : null;
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
    public boolean isSideConfigurable(RelativeSide side) {
        return side == RelativeSide.DOWN;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        generatePower(getSunlightPower(level, pos));

        pushEnergyToNeighborsBelow();
    }

    private void pushEnergyToNeighborsBelow() {
        if (sideConfiguration.get(RelativeSide.DOWN) != IOType.ENERGY_OUTPUT) {
            return;
        }

        if (ModEnergyUtil.doesBlockHaveEnergyStorage(this.worldPosition.below(), Direction.UP, this.level)) {
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
        if (level.isRainingAt(pos)) minLight += 2;

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

        CompoundTag sideConfigTag = new CompoundTag();
        sideConfiguration.save(sideConfigTag);
        tag.put("side_config", sideConfigTag);

        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ENERGY_STORAGE.setEnergy(tag.getInt("solar_panel.energy"));

        if (tag.contains("side_config")) {
            sideConfiguration.load(tag.getCompound("side_config"));
        }
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
