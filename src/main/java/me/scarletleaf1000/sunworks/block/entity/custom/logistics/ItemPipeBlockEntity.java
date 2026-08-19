package me.scarletleaf1000.sunworks.block.entity.custom.logistics;

import me.scarletleaf1000.sunworks.block.custom.cable.AbstractPipeBlock;
import me.scarletleaf1000.sunworks.block.custom.cable.PipeConnection;
import me.scarletleaf1000.sunworks.block.custom.logistics.ItemPipeBlock;
import me.scarletleaf1000.sunworks.block.entity.ModBlockEntities;
import me.scarletleaf1000.sunworks.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Every item pipe segment carries one of these. Items only enter the network through extractor
 * attachments - per-face state stored here (not in the blockstate), attached by right-clicking a
 * {@link PipeConnection#MACHINE} face with an extractor item. Each tick, every extractor face
 * pulls up to {@link #EXTRACTOR_MAX_STACK} items of one type out of its adjacent inventory and
 * the network routes them to every other reachable inventory, split equally across all valid
 * outputs (any uneven remainder goes to the outputs on the lowest {@link Direction} ordinals).
 * A package is never routed back out the endpoint it entered from.
 *
 * <p>Extraction from every extractor on a segment stops entirely while that segment is powered
 * by redstone.
 *
 * <p>The block entity also exposes its own {@link IItemHandler} capability (registered in
 * {@code ModBusEvents}) so other mods' pipes/machines can push items into the network directly;
 * it is insert-only - pulling items out of a pipe face is not possible.
 */
public class ItemPipeBlockEntity extends BlockEntity {
    private static final int MAX_NETWORK_SEARCH = 4096;
    private static final int RECOMPUTE_INTERVAL_TICKS = 20;

    /**
     * The maximum stack size a single extractor pulls per extraction operation. Future extractor
     * tiers would raise this.
     */
    public static final int EXTRACTOR_MAX_STACK = 4;

    /**
     * A single pipe-to-inventory junction. {@code side} is the direction from the pipe at
     * {@code pipePos} toward the inventory at {@code machinePos}.
     */
    private record Endpoint(BlockPos pipePos, BlockPos machinePos, Direction side) {
    }

    /**
     * A routing destination: an inventory handler plus the direction of its pipe face, the
     * latter only used to order equal-split remainders (lowest {@link Direction} ordinal first).
     */
    private record OutputTarget(Direction side, IItemHandler handler) {
    }

    /**
     * Bumped whenever any item pipe's connection state actually changes (a pipe or inventory
     * placed or removed), see {@code AbstractPipeBlock#updateShape}. Lets every segment cheaply
     * notice its cached endpoint list is stale without per-block dirty-flag propagation.
     */
    private static long globalTopologyVersion = 0;

    public static void bumpTopologyVersion() {
        globalTopologyVersion++;
    }

    private final Set<Direction> extractors = EnumSet.noneOf(Direction.class);

    private long cachedVersion = -1;
    private int ticksUntilRecompute = 0;
    private List<Endpoint> cachedEndpoints = List.of();

    public ItemPipeBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ITEM_PIPE_BE.get(), pos, blockState);
    }

    public boolean hasExtractor(Direction face) {
        return extractors.contains(face);
    }

    public Set<Direction> getExtractorFaces() {
        return Collections.unmodifiableSet(extractors);
    }

    public void addExtractor(Direction face) {
        if (extractors.add(face)) {
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }

    private void removeExtractor(Direction face, boolean dropItem) {
        if (!extractors.remove(face)) return;
        setChanged();
        if (level != null) {
            if (dropItem) {
                Block.popResource(level, worldPosition, new ItemStack(ModItems.EXTRACTOR.get()));
            }
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    /**
     * The network flood fill is only ever (re)computed by whichever segment first notices the
     * cache is stale - either because the topology actually changed ({@link #globalTopologyVersion})
     * or because {@link #RECOMPUTE_INTERVAL_TICKS} ticks have passed as a periodic safety net -
     * and the resulting endpoint list is pushed directly onto every other segment in the same
     * network, so the rest never redo the search themselves that cycle. Extraction itself runs
     * on every segment that has extractor faces; there is no leader election, since each
     * extractor is an independent item source.
     */
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        if (cachedVersion != globalTopologyVersion || --ticksUntilRecompute <= 0) {
            recomputeAndPropagate(level, pos);
        }

        if (extractors.isEmpty()) return;
        if (level.hasNeighborSignal(pos)) return;

        for (Direction face : List.copyOf(extractors)) {
            if (state.getValue(AbstractPipeBlock.PROPERTY_BY_DIRECTION.get(face)) != PipeConnection.MACHINE) {
                // The inventory this extractor was attached to is gone - pop the extractor off.
                removeExtractor(face, true);
                continue;
            }
            extractAndDistribute(level, pos, face);
        }
    }

    private void recomputeAndPropagate(Level level, BlockPos pos) {
        Set<BlockPos> network = new HashSet<>();
        List<Endpoint> endpoints = new ArrayList<>();
        collectNetwork(level, pos, network, endpoints);

        long version = globalTopologyVersion;
        for (BlockPos member : network) {
            if (level.getBlockEntity(member) instanceof ItemPipeBlockEntity pipeEntity) {
                pipeEntity.cachedEndpoints = endpoints;
                pipeEntity.cachedVersion = version;
                pipeEntity.ticksUntilRecompute = RECOMPUTE_INTERVAL_TICKS;
            }
        }
    }

    /**
     * Floods outward through connected item pipes, collecting every {@link PipeConnection#MACHINE}
     * junction reachable from {@code start}.
     */
    private void collectNetwork(Level level, BlockPos start, Set<BlockPos> visitedPipes, List<Endpoint> endpoints) {
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        visitedPipes.add(start);

        while (!queue.isEmpty() && visitedPipes.size() <= MAX_NETWORK_SEARCH) {
            BlockPos current = queue.poll();
            BlockState currentState = level.getBlockState(current);
            if (!(currentState.getBlock() instanceof ItemPipeBlock)) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = current.relative(direction);
                PipeConnection connection = currentState.getValue(AbstractPipeBlock.PROPERTY_BY_DIRECTION.get(direction));
                if (connection == PipeConnection.PIPE) {
                    if (visitedPipes.add(neighborPos)) {
                        queue.add(neighborPos);
                    }
                } else if (connection == PipeConnection.MACHINE) {
                    endpoints.add(new Endpoint(current, neighborPos, direction));
                }
            }
        }
    }

    /**
     * Pulls up to {@link #EXTRACTOR_MAX_STACK} items of one type from the inventory on
     * {@code face} and routes them to every other reachable inventory in the network. The
     * insertion is simulated first so only what the outputs can actually accept is ever
     * extracted; anything that still fails the real insert is pushed back into the source.
     */
    private void extractAndDistribute(Level level, BlockPos pos, Direction face) {
        BlockPos sourcePos = pos.relative(face);
        IItemHandler source = level.getCapability(Capabilities.ItemHandler.BLOCK, sourcePos, face.getOpposite());
        if (source == null) return;

        int extractSlot = -1;
        ItemStack simulatedExtract = ItemStack.EMPTY;
        for (int slot = 0; slot < source.getSlots(); slot++) {
            ItemStack candidate = source.extractItem(slot, EXTRACTOR_MAX_STACK, true);
            if (!candidate.isEmpty()) {
                extractSlot = slot;
                simulatedExtract = candidate;
                break;
            }
        }
        if (extractSlot < 0) return;

        List<OutputTarget> targets = collectOutputs(level, cachedEndpoints, pos, face);
        if (targets.isEmpty()) return;

        int accepted = simulatedExtract.getCount() - distribute(targets, simulatedExtract, true).getCount();
        if (accepted <= 0) return;

        ItemStack extracted = source.extractItem(extractSlot, accepted, false);
        ItemStack leftover = distribute(targets, extracted, false);
        if (!leftover.isEmpty()) {
            ItemHandlerHelper.insertItem(source, leftover, false);
        }
    }

    /**
     * Every reachable inventory in {@code endpoints} except the junction a package entered from,
     * sorted by face {@link Direction} ordinal so the equal-split remainder deterministically
     * lands on the lowest ordinals.
     */
    private List<OutputTarget> collectOutputs(Level level, List<Endpoint> endpoints,
                                              BlockPos excludePipePos, Direction excludeSide) {
        List<OutputTarget> targets = new ArrayList<>();
        for (Endpoint endpoint : endpoints) {
            if (endpoint.pipePos().equals(excludePipePos) && endpoint.side() == excludeSide) {
                continue;
            }
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, endpoint.machinePos(), endpoint.side().getOpposite());
            if (handler != null && handler.getSlots() > 0) {
                targets.add(new OutputTarget(endpoint.side(), handler));
            }
        }
        targets.sort(Comparator.comparingInt(target -> target.side().ordinal()));
        return targets;
    }

    /**
     * Splits {@code stack} equally across all targets (any uneven remainder going one item each
     * to the lowest-ordinal targets first) and inserts each share; whatever a target refuses of
     * its share is redistributed to any remaining taker in the same order. Never mutates the
     * passed stack.
     *
     * @return whatever could not be inserted anywhere.
     */
    private ItemStack distribute(List<OutputTarget> targets, ItemStack stack, boolean simulate) {
        int share = stack.getCount() / targets.size();
        int extra = stack.getCount() % targets.size();

        ItemStack remaining = stack.copy();
        for (int i = 0; i < targets.size() && !remaining.isEmpty(); i++) {
            int allotment = share + (i < extra ? 1 : 0);
            if (allotment <= 0) continue;

            ItemStack offered = remaining.copyWithCount(Math.min(allotment, remaining.getCount()));
            ItemStack refused = ItemHandlerHelper.insertItem(targets.get(i).handler(), offered, simulate);
            remaining.shrink(offered.getCount() - refused.getCount());
        }

        for (OutputTarget target : targets) {
            if (remaining.isEmpty()) break;
            remaining = ItemHandlerHelper.insertItem(target.handler(), remaining, simulate);
        }

        return remaining;
    }

    /**
     * Called from this pipe segment's own {@link IItemHandler} capability when an external
     * source (another mod's pipe/machine) pushes items into the face facing {@code fromSide}.
     * Routes the items on into the rest of the network's inventories with the same equal-split
     * rules as extractor routing, excluding the pusher's own junction.
     */
    private ItemStack insertIntoNetwork(Direction fromSide, ItemStack stack, boolean simulate) {
        if (level == null || stack.isEmpty() || !(getBlockState().getBlock() instanceof ItemPipeBlock)) {
            return stack;
        }

        Set<BlockPos> network = new HashSet<>();
        List<Endpoint> endpoints = new ArrayList<>();
        collectNetwork(level, worldPosition, network, endpoints);

        List<OutputTarget> targets = collectOutputs(level, endpoints, worldPosition, fromSide);
        if (targets.isEmpty()) {
            return stack;
        }

        return distribute(targets, stack, simulate);
    }

    /**
     * The insert-only capability exposed to the outside world on every face. {@code side} is
     * null when a caller requests the capability without specifying a face (e.g. some inventory
     * scanners); we simply refuse those, since routing requires knowing which junction to exclude.
     */
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return null;
        }
        return new IItemHandler() {
            @Override
            public int getSlots() {
                return 1;
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                return ItemStack.EMPTY;
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return insertIntoNetwork(side, stack, simulate);
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return ItemStack.EMPTY;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 64;
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return true;
            }
        };
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        int mask = 0;
        for (Direction face : extractors) {
            mask |= 1 << face.ordinal();
        }
        tag.putByte("Extractors", (byte) mask);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        int mask = tag.getByte("Extractors");
        extractors.clear();
        for (Direction face : Direction.values()) {
            if ((mask & (1 << face.ordinal())) != 0) {
                extractors.add(face);
            }
        }
    }
}
