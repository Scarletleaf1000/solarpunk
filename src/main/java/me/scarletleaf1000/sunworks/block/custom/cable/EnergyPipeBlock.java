package me.scarletleaf1000.sunworks.block.custom.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * The plain, block-entity-free energy pipe. The vast majority of a network should be made up
 * of this block, since it has zero tick overhead.
 */
public class EnergyPipeBlock extends AbstractPipeBlock {
    private final Supplier<EnergyPipeExtractorBlock> extractorVariant;

    public EnergyPipeBlock(Properties properties, CableTier tier, Supplier<EnergyPipeExtractorBlock> extractorVariant) {
        super(properties, tier);
        this.extractorVariant = extractorVariant;
    }

    @Override
    protected boolean isExtractor() {
        return false;
    }

    @Override
    protected @Nullable BlockState toggleExtracting(Level level, BlockPos pos, BlockState state) {
        if (!hasEnergyConnectableNeighbor(level, pos)) {
            return null;
        }

        EnergyPipeExtractorBlock extractor = extractorVariant.get();
        BlockState newState = extractor.defaultBlockState();
        return extractor.recomputeAllConnections(level, pos, newState);
    }
}
