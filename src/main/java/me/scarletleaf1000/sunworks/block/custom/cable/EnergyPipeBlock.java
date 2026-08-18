package me.scarletleaf1000.sunworks.block.custom.cable;

import me.scarletleaf1000.sunworks.block.entity.ModBlockEntities;
import me.scarletleaf1000.sunworks.block.entity.custom.cable.EnergyPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * An energy pipe segment. Every segment carries a {@link EnergyPipeBlockEntity} that
 * automatically routes energy through any connected machine face based on that machine's own
 * capability - there is no manual per-side input/output toggle.
 */
public class EnergyPipeBlock extends AbstractPipeBlock implements EntityBlock {
    public EnergyPipeBlock(Properties properties, CableTier tier) {
        super(properties, tier);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyPipeBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide || blockEntityType != ModBlockEntities.ENERGY_PIPE_BE.get()) return null;

        return (BlockEntityTicker<T>) (BlockEntityTicker<EnergyPipeBlockEntity>)
                (level1, pos, state1, blockEntity) -> blockEntity.tick(level1, pos, state1);
    }
}
