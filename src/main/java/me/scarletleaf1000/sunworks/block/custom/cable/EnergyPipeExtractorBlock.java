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

import java.util.function.Supplier;

/**
 * The extracting variant of an energy pipe. Only pipes explicitly toggled into this state
 * carry a block entity/tick - this keeps the vast majority of a pipe network cheap.
 */
public class EnergyPipeExtractorBlock extends AbstractPipeBlock implements EntityBlock {
    private final Supplier<EnergyPipeBlock> plainVariant;

    public EnergyPipeExtractorBlock(Properties properties, CableTier tier, Supplier<EnergyPipeBlock> plainVariant) {
        super(properties, tier);
        this.plainVariant = plainVariant;
    }

    @Override
    protected boolean isExtractor() {
        return true;
    }

    @Override
    protected @Nullable BlockState toggleExtracting(Level level, BlockPos pos, BlockState state) {
        EnergyPipeBlock plain = plainVariant.get();
        BlockState newState = plain.defaultBlockState();
        return plain.recomputeAllConnections(level, pos, newState);
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
