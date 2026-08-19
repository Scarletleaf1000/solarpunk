package me.scarletleaf1000.sunworks.block.custom.cable;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Shared geometry/connection logic for every pipe type (energy, item, fluid). Each of the 6
 * sides automatically connects to either another pipe of the same type ({@link PipeConnection#PIPE})
 * or any other block exposing the capability this pipe type transports ({@link PipeConnection#MACHINE}).
 * There is no manual per-side toggle - what flows across a {@code MACHINE} face is decided by
 * the concrete pipe type's block entity.
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

    protected AbstractPipeBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultConnectionState());
    }

    private BlockState defaultConnectionState() {
        BlockState state = stateDefinition.any();
        for (EnumProperty<PipeConnection> property : PROPERTY_BY_DIRECTION.values()) {
            state = state.setValue(property, PipeConnection.NONE);
        }
        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    /**
     * @return true if the block at {@code neighborPos} is a pipe segment this pipe can form a
     * {@link PipeConnection#PIPE} connection with - i.e. same transport type; different pipe
     * types (energy vs item vs fluid) never connect to each other.
     */
    protected abstract boolean canConnectToPipe(LevelReader level, BlockPos neighborPos);

    /**
     * @return true if the block at {@code neighborPos} exposes the capability this pipe type
     * transports on the face looking back at the pipe, making it a {@link PipeConnection#MACHINE}
     * connection. {@code face} is the direction from the pipe toward the neighbor.
     */
    protected abstract boolean canConnectToMachine(LevelReader level, BlockPos neighborPos, Direction face);

    /**
     * Called whenever one of this segment's connection states actually changes (a pipe or
     * machine placed/removed next to it), so the concrete pipe type can invalidate its cached
     * network topology.
     */
    protected void onTopologyChanged() {
    }

    /**
     * @return the connection to a neighbor, purely automatic and recomputed on every
     * neighbor/state change - {@link PipeConnection#PIPE} for another pipe segment of the same
     * type, {@link PipeConnection#MACHINE} for any other block exposing this pipe type's
     * capability, or {@link PipeConnection#NONE} otherwise.
     */
    protected PipeConnection computeConnection(LevelReader level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);

        if (canConnectToPipe(level, neighborPos)) {
            return PipeConnection.PIPE;
        }

        if (canConnectToMachine(level, neighborPos, direction)) {
            return PipeConnection.MACHINE;
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
        if (state.getValue(property) != newConnection) {
            // A pipe or machine was placed/removed next to us - every pipe's cached network
            // topology may now be stale.
            onTopologyChanged();
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
}
