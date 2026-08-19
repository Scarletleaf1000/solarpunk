package me.scarletleaf1000.sunworks.block.custom.cable;

import me.scarletleaf1000.sunworks.block.entity.ModBlockEntities;
import me.scarletleaf1000.sunworks.block.entity.custom.cable.EnergyPipeBlockEntity;
import me.scarletleaf1000.sunworks.block.entity.energy.ModEnergyUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

/**
 * An energy pipe segment. Every segment carries a {@link EnergyPipeBlockEntity} that
 * automatically routes energy through any connected machine face based on that machine's own
 * capability - there is no manual per-side input/output toggle.
 */
public class EnergyPipeBlock extends AbstractPipeBlock implements EntityBlock {
    /**
     * Purely cosmetic state, set by {@code EnergyPipeBlockEntity} each tick: true while this
     * segment is actively part of a network that is transferring energy right now, so the
     * model provider can swap in a lit texture.
     */
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    private final CableTier tier;

    public EnergyPipeBlock(Properties properties, CableTier tier) {
        super(properties);
        this.tier = tier;
        registerDefaultState(defaultBlockState().setValue(POWERED, false));
    }

    public CableTier getTier() {
        return tier;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    /**
     * Pipes of every tier visually and functionally connect to one another, with the network's
     * effective transfer rate bottlenecked by the weakest tier along the path.
     */
    @Override
    protected boolean canConnectToPipe(LevelReader level, BlockPos neighborPos) {
        return level.getBlockState(neighborPos).getBlock() instanceof EnergyPipeBlock;
    }

    @Override
    protected boolean canConnectToMachine(LevelReader level, BlockPos neighborPos, Direction face) {
        return level instanceof Level realLevel
                && ModEnergyUtil.doesBlockHaveEnergyStorage(neighborPos, face.getOpposite(), realLevel);
    }

    @Override
    protected void onTopologyChanged() {
        EnergyPipeBlockEntity.bumpTopologyVersion();
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
