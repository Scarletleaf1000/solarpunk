package me.scarletleaf1000.sunworks.block.entity.custom.cable;

import me.scarletleaf1000.sunworks.block.custom.cable.AbstractPipeBlock;
import me.scarletleaf1000.sunworks.block.custom.cable.PipeConnection;
import me.scarletleaf1000.sunworks.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Every energy pipe segment carries one of these. There is no separate "extractor" pipe and no
 * per-side toggle - every {@link PipeConnection#MACHINE} face automatically acts as a source,
 * a sink, or both, purely based on the connected block's own {@link IEnergyStorage#canExtract()}
 * / {@link IEnergyStorage#canReceive()} flags. Each tick walks the pipe network it is part of
 * via a bounded flood fill and moves energy from every reachable source to every reachable
 * sink, with each individual machine connection capped by the tier of the specific pipe
 * segment touching it (not just the network-wide weakest tier).
 *
 * <p>The block entity also exposes its own {@link IEnergyStorage} capability (registered in
 * {@code ModBusEvents}) so that other mods' cables/machines can push or pull energy through a
 * pipe face directly, without needing to understand our internal network logic.
 */
public class EnergyPipeBlockEntity extends BlockEntity {
    private static final int MAX_NETWORK_SEARCH = 4096;

    /**
     * A single pipe-to-machine junction. {@code tierCap} is the max transfer rate of the pipe
     * segment at {@code pipePos} (i.e. the segment directly touching the machine), which bounds
     * how much energy can flow through this specific junction regardless of the rest of the
     * network's tier.
     */
    private record Endpoint(BlockPos pipePos, BlockPos machinePos, Direction side, int tierCap) {
    }

    private static final int RECOMPUTE_INTERVAL_TICKS = 20;

    /**
     * Bumped whenever any pipe's connection state actually changes (a pipe or machine placed or
     * removed), see {@code AbstractPipeBlock#updateShape}. Lets every segment cheaply notice its
     * cached topology is stale without needing per-block dirty-flag propagation.
     */
    private static long globalTopologyVersion = 0;

    public static void bumpTopologyVersion() {
        globalTopologyVersion++;
    }

    private Set<BlockPos> poweredPositions = Set.of();

    private long cachedVersion = -1;
    private int ticksUntilRecompute = 0;
    private BlockPos cachedLeader;
    private Set<BlockPos> cachedNetwork = Set.of();
    private List<Endpoint> cachedEndpoints = List.of();
    private Map<BlockPos, Map<BlockPos, Integer>> cachedBottlenecks = Map.of();

    public EnergyPipeBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ENERGY_PIPE_BE.get(), pos, blockState);
    }

    /**
     * The full network flood-fill and widest-path bottleneck search are both fairly expensive,
     * so they are only ever (re)computed by whichever segment first notices the cache is stale -
     * either because the topology actually changed ({@link #globalTopologyVersion}) or because
     * {@link #RECOMPUTE_INTERVAL_TICKS} ticks have passed as a periodic safety net - and the
     * result is then pushed directly onto every other segment's block entity in the same
     * network, so the rest of them never redo the search themselves that cycle.
     */
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        if (!(state.getBlock() instanceof AbstractPipeBlock)) return;

        if (cachedLeader == null || cachedVersion != globalTopologyVersion || --ticksUntilRecompute <= 0) {
            recomputeAndPropagate(level, pos);
        }

        if (!pos.equals(cachedLeader)) {
            return;
        }

        int transferred = autoTransfer(level, cachedEndpoints, cachedBottlenecks);
        updatePoweredPositions(level, transferred > 0 ? cachedNetwork : Set.of());
    }

    private void recomputeAndPropagate(Level level, BlockPos pos) {
        Set<BlockPos> network = new HashSet<>();
        List<Endpoint> endpoints = new ArrayList<>();
        collectNetwork(level, pos, network, endpoints, null);

        BlockPos leader = pos;
        for (BlockPos member : network) {
            if (member.asLong() < leader.asLong()) {
                leader = member;
            }
        }

        Set<BlockPos> junctions = new HashSet<>();
        for (Endpoint endpoint : endpoints) {
            junctions.add(endpoint.pipePos());
        }

        Map<BlockPos, Map<BlockPos, Integer>> bottlenecks = new HashMap<>();
        for (BlockPos junction : junctions) {
            int ownTier = level.getBlockState(junction).getBlock() instanceof AbstractPipeBlock pipeBlock
                    ? pipeBlock.getTier().getMaxTransfer() : 0;
            bottlenecks.put(junction, computeBottleneckFrom(level, junction, ownTier));
        }

        long version = globalTopologyVersion;
        for (BlockPos member : network) {
            if (level.getBlockEntity(member) instanceof EnergyPipeBlockEntity pipeEntity) {
                pipeEntity.cachedLeader = leader;
                pipeEntity.cachedNetwork = network;
                pipeEntity.cachedEndpoints = endpoints;
                pipeEntity.cachedBottlenecks = bottlenecks;
                pipeEntity.cachedVersion = version;
                pipeEntity.ticksUntilRecompute = RECOMPUTE_INTERVAL_TICKS;
            }
        }
    }

    /**
     * Widest-path (maximum bottleneck) search from {@code startPipePos} through the pipe graph -
     * for every other pipe segment reachable, finds the route that maximizes the minimum tier
     * along the way, so a single weak segment anywhere on the actual path correctly throttles
     * that specific route, rather than only the two ends touching machines.
     */
    private Map<BlockPos, Integer> computeBottleneckFrom(Level level, BlockPos startPipePos, int startTierCap) {
        Map<BlockPos, Integer> bestBottleneck = new HashMap<>();
        bestBottleneck.put(startPipePos, startTierCap);

        PriorityQueue<BlockPos> queue = new PriorityQueue<>(Comparator.comparingInt((BlockPos p) -> bestBottleneck.get(p)).reversed());
        queue.add(startPipePos);

        Set<BlockPos> settled = new HashSet<>();

        while (!queue.isEmpty() && settled.size() <= MAX_NETWORK_SEARCH) {
            BlockPos current = queue.poll();
            if (!settled.add(current)) continue;

            int currentBottleneck = bestBottleneck.get(current);
            BlockState currentState = level.getBlockState(current);
            if (!(currentState.getBlock() instanceof AbstractPipeBlock)) continue;

            for (Direction direction : Direction.values()) {
                PipeConnection connection = currentState.getValue(AbstractPipeBlock.PROPERTY_BY_DIRECTION.get(direction));
                if (connection != PipeConnection.PIPE) continue;

                BlockPos neighborPos = current.relative(direction);
                if (settled.contains(neighborPos)) continue;

                BlockState neighborState = level.getBlockState(neighborPos);
                if (!(neighborState.getBlock() instanceof AbstractPipeBlock neighborPipe)) continue;

                int candidate = Math.min(currentBottleneck, neighborPipe.getTier().getMaxTransfer());
                Integer existing = bestBottleneck.get(neighborPos);
                if (existing == null || candidate > existing) {
                    bestBottleneck.put(neighborPos, candidate);
                    queue.add(neighborPos);
                }
            }
        }

        return bestBottleneck;
    }

    /**
     * Called from this pipe segment's own {@link IEnergyStorage} capability when an external
     * source (another mod's cable/machine) pushes energy into the face facing {@code fromSide}.
     * Routes the energy on into the rest of the network's real sinks, excluding the pusher
     * itself, capped by this segment's own tier.
     */
    public int receiveFromExternal(Direction fromSide, int maxReceive, boolean simulate) {
        if (level == null || maxReceive <= 0 || !(getBlockState().getBlock() instanceof AbstractPipeBlock pipeBlock)) {
            return 0;
        }

        int budget = Math.min(maxReceive, pipeBlock.getTier().getMaxTransfer());
        BlockPos externalPos = worldPosition.relative(fromSide);

        Set<BlockPos> network = new HashSet<>();
        List<Endpoint> endpoints = new ArrayList<>();
        collectNetwork(level, worldPosition, network, endpoints, externalPos);
        Map<BlockPos, Integer> bottleneck = computeBottleneckFrom(level, worldPosition, budget);

        return pushToSinks(level, endpoints, budget, bottleneck, simulate);
    }

    /**
     * Called from this pipe segment's own {@link IEnergyStorage} capability when an external
     * consumer (another mod's cable/machine) pulls energy from the face facing {@code fromSide}.
     * Pulls from the rest of the network's real sources, excluding the puller itself, capped by
     * this segment's own tier.
     */
    public int extractForExternal(Direction fromSide, int maxExtract, boolean simulate) {
        if (level == null || maxExtract <= 0 || !(getBlockState().getBlock() instanceof AbstractPipeBlock pipeBlock)) {
            return 0;
        }

        int budget = Math.min(maxExtract, pipeBlock.getTier().getMaxTransfer());
        BlockPos externalPos = worldPosition.relative(fromSide);

        Set<BlockPos> network = new HashSet<>();
        List<Endpoint> endpoints = new ArrayList<>();
        collectNetwork(level, worldPosition, network, endpoints, externalPos);
        Map<BlockPos, Integer> bottleneck = computeBottleneckFrom(level, worldPosition, budget);

        return pullFromSources(level, endpoints, budget, bottleneck, simulate);
    }

    public int getMaxTransfer() {
        return getBlockState().getBlock() instanceof AbstractPipeBlock pipeBlock ? pipeBlock.getTier().getMaxTransfer() : 0;
    }

    /**
     * The capability exposed to the outside world on every face - lets other mods' cables and
     * machines auto-detect and interoperate with our pipes exactly like any other energy block,
     * pushing/pulling through {@link #receiveFromExternal} and {@link #extractForExternal}
     * rather than needing to understand our internal network. {@code side} is null when a
     * caller requests the capability without specifying a face (e.g. some inventory scanners);
     * we simply refuse those, since routing requires knowing which junction to exclude.
     */
    public IEnergyStorage getEnergyStorage(@Nullable Direction side) {
        if (side == null) {
            return null;
        }
        return new IEnergyStorage() {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                return receiveFromExternal(side, maxReceive, simulate);
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                return extractForExternal(side, maxExtract, simulate);
            }

            @Override
            public int getEnergyStored() {
                return 0;
            }

            @Override
            public int getMaxEnergyStored() {
                return getMaxTransfer();
            }

            @Override
            public boolean canExtract() {
                return true;
            }

            @Override
            public boolean canReceive() {
                return true;
            }
        };
    }

    private void updatePoweredPositions(Level level, Set<BlockPos> newPowered) {
        if (newPowered.equals(poweredPositions)) return;

        for (BlockPos previouslyPowered : poweredPositions) {
            if (!newPowered.contains(previouslyPowered)) {
                setPowered(level, previouslyPowered, false);
            }
        }
        for (BlockPos nowPowered : newPowered) {
            setPowered(level, nowPowered, true);
        }

        poweredPositions = newPowered;
    }

    private void setPowered(Level level, BlockPos pos, boolean powered) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof AbstractPipeBlock) || state.getValue(AbstractPipeBlock.POWERED) == powered) {
            return;
        }
        level.setBlock(pos, state.setValue(AbstractPipeBlock.POWERED, powered), Block.UPDATE_CLIENTS);
    }

    /**
     * Floods outward through pipes of any tier, collecting every {@link PipeConnection#MACHINE}
     * junction reachable from {@code start} - the network can freely mix tiers, since each
     * junction is capped individually by its own touching segment's tier rather than a single
     * network-wide bottleneck. {@code excludePos}, if given, is skipped entirely (used to keep
     * an externally-calling neighbor out of its own request's routing).
     */
    private void collectNetwork(Level level, BlockPos start, Set<BlockPos> visitedPipes,
                                 List<Endpoint> endpoints, @Nullable BlockPos excludePos) {
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        visitedPipes.add(start);

        while (!queue.isEmpty() && visitedPipes.size() <= MAX_NETWORK_SEARCH) {
            BlockPos current = queue.poll();
            BlockState currentState = level.getBlockState(current);
            if (!(currentState.getBlock() instanceof AbstractPipeBlock pipeBlock)) {
                continue;
            }
            int tierCap = pipeBlock.getTier().getMaxTransfer();

            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = current.relative(direction);
                if (neighborPos.equals(excludePos)) {
                    continue;
                }

                PipeConnection connection = currentState.getValue(AbstractPipeBlock.PROPERTY_BY_DIRECTION.get(direction));
                if (connection == PipeConnection.PIPE) {
                    if (visitedPipes.add(neighborPos)) {
                        queue.add(neighborPos);
                    }
                } else if (connection == PipeConnection.MACHINE) {
                    endpoints.add(new Endpoint(current, neighborPos, direction, tierCap));
                }
            }
        }
    }

    /**
     * Classifies every endpoint as a source and/or sink from its own capability's
     * {@code canExtract()}/{@code canReceive()} flags, then hands off to {@link #route} to
     * actually move energy. A junction that is both (e.g. a battery) never feeds itself.
     */
    private int autoTransfer(Level level, List<Endpoint> endpoints, Map<BlockPos, Map<BlockPos, Integer>> bottlenecks) {
        Map<Endpoint, IEnergyStorage> storages = new HashMap<>();
        List<Endpoint> sources = new ArrayList<>();
        List<Endpoint> sinks = new ArrayList<>();

        for (Endpoint endpoint : endpoints) {
            IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, endpoint.machinePos(), endpoint.side().getOpposite());
            if (storage == null) continue;
            storages.put(endpoint, storage);

            if (storage.canExtract() && storage.getEnergyStored() > 0) sources.add(endpoint);
            if (storage.canReceive()) sinks.add(endpoint);
        }

        return route(storages, sources, sinks, bottlenecks);
    }

    private int pushToSinks(Level level, List<Endpoint> endpoints, int budget, Map<BlockPos, Integer> bottleneck, boolean simulate) {
        if (budget <= 0) return 0;

        int totalSent = 0;
        for (Endpoint sinkEndpoint : endpoints) {
            if (budget - totalSent <= 0) break;

            IEnergyStorage sink = level.getCapability(Capabilities.EnergyStorage.BLOCK, sinkEndpoint.machinePos(), sinkEndpoint.side().getOpposite());
            if (sink == null || !sink.canReceive()) continue;

            int pathCap = bottleneck.getOrDefault(sinkEndpoint.pipePos(), 0);
            int attempt = Math.min(budget - totalSent, pathCap);
            if (attempt <= 0) continue;

            totalSent += sink.receiveEnergy(attempt, simulate);
        }

        return totalSent;
    }

    private int pullFromSources(Level level, List<Endpoint> endpoints, int budget, Map<BlockPos, Integer> bottleneck, boolean simulate) {
        if (budget <= 0) return 0;

        int totalPulled = 0;
        for (Endpoint sourceEndpoint : endpoints) {
            if (budget - totalPulled <= 0) break;

            IEnergyStorage source = level.getCapability(Capabilities.EnergyStorage.BLOCK, sourceEndpoint.machinePos(), sourceEndpoint.side().getOpposite());
            if (source == null || !source.canExtract()) continue;

            int pathCap = bottleneck.getOrDefault(sourceEndpoint.pipePos(), 0);
            int attempt = Math.min(budget - totalPulled, pathCap);
            if (attempt <= 0) continue;

            totalPulled += source.extractEnergy(attempt, simulate);
        }

        return totalPulled;
    }

    /**
     * Moves energy from sources to sinks. Each source/sink pair is capped by three independent
     * budgets: the source's own remaining per-tick allowance, the sink's own remaining per-tick
     * allowance, and {@code pathCap} - the widest-path bottleneck tier between that specific
     * source and sink (see {@link #computeBottleneckFrom}), so a weak segment anywhere along the
     * actual route between them throttles that route specifically.
     */
    private int route(Map<Endpoint, IEnergyStorage> storages, List<Endpoint> sources, List<Endpoint> sinks,
                       Map<BlockPos, Map<BlockPos, Integer>> bottlenecks) {
        if (sources.isEmpty() || sinks.isEmpty()) return 0;

        Map<Endpoint, Integer> sourceRemaining = new HashMap<>();
        for (Endpoint source : sources) {
            sourceRemaining.put(source, source.tierCap());
        }

        int totalSent = 0;
        for (Endpoint sinkEndpoint : sinks) {
            int sinkRemaining = sinkEndpoint.tierCap();
            IEnergyStorage sink = storages.get(sinkEndpoint);

            for (Endpoint sourceEndpoint : sources) {
                if (sinkRemaining <= 0) break;
                if (sourceEndpoint.equals(sinkEndpoint)) continue;

                int available = sourceRemaining.getOrDefault(sourceEndpoint, 0);
                if (available <= 0) continue;

                Map<BlockPos, Integer> fromSource = bottlenecks.get(sourceEndpoint.pipePos());
                int pathCap = fromSource == null ? 0 : fromSource.getOrDefault(sinkEndpoint.pipePos(), 0);
                if (pathCap <= 0) continue;

                IEnergyStorage source = storages.get(sourceEndpoint);
                int attempt = Math.min(Math.min(available, sinkRemaining), pathCap);

                int simulatedExtract = source.extractEnergy(attempt, true);
                if (simulatedExtract <= 0) continue;

                int accepted = sink.receiveEnergy(simulatedExtract, true);
                if (accepted <= 0) continue;

                int actuallyExtracted = source.extractEnergy(accepted, false);
                int actuallyReceived = sink.receiveEnergy(actuallyExtracted, false);

                sourceRemaining.put(sourceEndpoint, available - actuallyReceived);
                sinkRemaining -= actuallyReceived;
                totalSent += actuallyReceived;
            }
        }

        return totalSent;
    }
}
