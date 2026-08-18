package me.scarletleaf1000.sunworks.block.custom.cable;

import com.google.common.collect.ImmutableMap;
import me.scarletleaf1000.sunworks.block.entity.energy.ModEnergyUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Shared geometry/connection logic for every energy pipe, regardless of tier or whether the
 * concrete block is the plain (no block entity) variant or the extracting (block entity) variant.
 */
public abstract class AbstractPipeBlock extends Block {
    public static final EnumProperty<PipeConnection> NORTH = EnumProperty.create("north", PipeConnection.class);
    public static final EnumProperty<PipeConnection> SOUTH = EnumProperty.create("south", PipeConnection.class);
    public static final EnumProperty<PipeConnection> EAST = EnumProperty.create("east", PipeConnection.class);
    public static final EnumProperty<PipeConnection> WEST = EnumProperty.create("west", PipeConnection.class);
    public static final EnumProperty<PipeConnection> UP = EnumProperty.create("up", PipeConnection.class);
    public static final EnumProperty<PipeConnection> DOWN = EnumProperty.create("down", PipeConnection.class);

    public static final Map<Direction, EnumProperty<PipeConnection>> PROPERTY_BY_DIRECTION = ImmutableMap.<Direction, EnumProperty<PipeConnection>>builder()
            .put(Direction.NORTH, NORTH)
            .put(Direction.SOUTH, SOUTH)
            .put(Direction.EAST, EAST)
            .put(Direction.WEST, WEST)
            .put(Direction.UP, UP)
            .put(Direction.DOWN, DOWN)
            .build();

    /**
     * Purely cosmetic state, set by {@code EnergyPipeBlockEntity} each tick: true while this
     * segment is actively part of a network that is transferring energy right now, so the
     * model provider can swap in a lit texture.
     */
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    private final CableTier tier;

    protected AbstractPipeBlock(Properties properties, CableTier tier) {
        super(properties);
        this.tier = tier;
        registerDefaultState(defaultConnectionState());
    }

    public CableTier getTier() {
        return tier;
    }

    /**
     * @return true if this pipe type should render/behave as an extraction pipe on any
     * side connected to a (non-pipe) energy storage.
     */
    protected abstract boolean isExtractor();

    private BlockState defaultConnectionState() {
        BlockState state = stateDefinition.any();
        for (EnumProperty<PipeConnection> property : PROPERTY_BY_DIRECTION.values()) {
            state = state.setValue(property, PipeConnection.NONE);
        }
        return state.setValue(POWERED, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN, POWERED);
    }

    public static boolean isPipeOfTier(BlockGetter level, BlockPos pos, CableTier tier) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof AbstractPipeBlock pipe && pipe.getTier() == tier;
    }

    protected PipeConnection computeConnection(LevelReader level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);

        if (isPipeOfTier(level, neighborPos, tier)) {
            return PipeConnection.PIPE;
        }

        if (level instanceof Level realLevel
                && ModEnergyUtil.doesBlockHaveEnergyStorage(neighborPos, direction.getOpposite(), realLevel)) {
            return isExtractor() ? PipeConnection.EXTRACT : PipeConnection.PIPE;
        }

        return PipeConnection.NONE;
    }

    protected BlockState recomputeAllConnections(LevelReader level, BlockPos pos, BlockState state) {
        for (Direction direction : Direction.values()) {
            state = state.setValue(PROPERTY_BY_DIRECTION.get(direction), computeConnection(level, pos, direction));
        }
        return state;
    }

    public boolean hasEnergyConnectableNeighbor(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if (!isPipeOfTier(level, neighborPos, tier)
                    && ModEnergyUtil.doesBlockHaveEnergyStorage(neighborPos, direction.getOpposite(), level)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return recomputeAllConnections(context.getLevel(), context.getClickedPos(), defaultBlockState());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                      LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (level instanceof LevelReader levelReader) {
            return state.setValue(PROPERTY_BY_DIRECTION.get(direction), computeConnection(levelReader, pos, direction));
        }
        return state;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return PipeShapes.getShape(connectionMap(state));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return PipeShapes.getShape(connectionMap(state));
    }

    private Map<Direction, PipeConnection> connectionMap(BlockState state) {
        Map<Direction, PipeConnection> map = new EnumMap<>(Direction.class);
        for (Map.Entry<Direction, EnumProperty<PipeConnection>> entry : PROPERTY_BY_DIRECTION.entrySet()) {
            map.put(entry.getKey(), state.getValue(entry.getValue()));
        }
        return map;
    }

    protected abstract @Nullable BlockState toggleExtracting(Level level, BlockPos pos, BlockState state);

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockState newState = toggleExtracting(level, pos, state);
        if (newState == null) {
            return InteractionResult.FAIL;
        }

        level.setBlock(pos, newState, Block.UPDATE_ALL);
        return InteractionResult.CONSUME;
    }
}
