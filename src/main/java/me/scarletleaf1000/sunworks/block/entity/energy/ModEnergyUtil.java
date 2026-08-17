package me.scarletleaf1000.sunworks.block.entity.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class ModEnergyUtil {
    public static boolean move(BlockPos from, BlockPos to, int amount, Level level) {
        Direction direction = Direction.fromDelta(
                to.getX() - from.getX(), to.getY() - from.getY(), to.getZ() - from.getZ());

        IEnergyStorage fromStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, from, direction);
        IEnergyStorage toStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, to,
                direction != null ? direction.getOpposite() : null);

        if (fromStorage == null || toStorage == null) {
            return false;
        }

        int maxAmountToReceive = toStorage.receiveEnergy(amount, true);

        int extractedEnergy = fromStorage.extractEnergy(maxAmountToReceive, false);
        toStorage.receiveEnergy(extractedEnergy, false);

        return true;
    }

    public static boolean doesBlockHaveEnergyStorage(BlockPos positionToCheck, Direction side, Level level) {
        return level.getBlockEntity(positionToCheck) != null
                && level.getCapability(Capabilities.EnergyStorage.BLOCK, positionToCheck, side) != null;
    }
}
