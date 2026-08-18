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
 * Shared geometry/connection logic for every energy pipe, regardless of tier. Each of the 6
 * sides independently tracks whether it is a normal (output) or extracting (input) connection,
 * toggleable per-side by the player - see {@link #useWithoutItem}.
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

    /**
     * @return true if the block at {@code pos} is any energy pipe, regardless of tier - pipes of
     * every tier visually and functionally connect to one another, with the network's effective
     * transfer rate bottlenecked by the weakest tier along the path.
     */
    public static boolean isPipe(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof AbstractPipeBlock;
    }

    /**
     * @return the "default" connection to a neighbor, ignoring any previously player-toggled
     * extract state - always {@link PipeConnection#PIPE} for a fresh/newly-connectable side.
     * Extract sides are only ever set by the player via {@link #useWithoutItem}, and preserved
     * across recomputation by {@link #updateShape}.
     */
    protected PipeConnection computeConnection(LevelReader level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);

        if (isPipe(level, neighborPos)) {
            return PipeConnection.PIPE;
        }

        if (level instanceof Level realLevel
                && ModEnergyUtil.doesBlockHaveEnergyStorage(neighborPos, direction.getOpposite(), realLevel)) {
            return PipeConnection.PIPE;
        }

        return PipeConnection.NONE;
    }

    protected BlockState recomputeAllConnections(LevelReader level, BlockPos pos, BlockState state) {
        for (Direction direction : Direction.values()) {
            state = state.setValue(PROPERTY_BY_DIRECTION.get(direction), computeConnection(level, pos, direction));
        }
        return state;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return recomputeAllConnections(context.getLevel(), context.getClickedPos(), defaultBlockState());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                      LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!(level instanceof LevelReader levelReader)) {
            return state;
        }

        EnumProperty<PipeConnection> property = PROPERTY_BY_DIRECTION.get(direction);
        PipeConnection newConnection = computeConnection(levelReader, pos, direction);
        if (newConnection == PipeConnection.PIPE && state.getValue(property) == PipeConnection.EXTRACT) {
            newConnection = PipeConnection.EXTRACT;
        }

        return state.setValue(property, newConnection);
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

    /**
     * Shift-right-clicking a specific face toggles that side alone between a normal (output)
     * connection and an extracting (input) one - only valid on a side directly touching a
     * (non-pipe) energy storage; pipe-to-pipe junctions have nothing to extract from.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Direction direction = hitResult.getDirection();
        EnumProperty<PipeConnection> property = PROPERTY_BY_DIRECTION.get(direction);
        PipeConnection current = state.getValue(property);

        if (current == PipeConnection.NONE || isPipe(level, pos.relative(direction))) {
            return InteractionResult.FAIL;
        }

        PipeConnection toggled = current == PipeConnection.EXTRACT ? PipeConnection.PIPE : PipeConnection.EXTRACT;
        level.setBlock(pos, state.setValue(property, toggled), Block.UPDATE_ALL);
        return InteractionResult.CONSUME;
    }
}
