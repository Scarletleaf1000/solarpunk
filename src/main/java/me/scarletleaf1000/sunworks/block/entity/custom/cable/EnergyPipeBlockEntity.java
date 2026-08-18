package me.scarletleaf1000.sunworks.block.entity.custom.cable;

import me.scarletleaf1000.sunworks.block.custom.cable.AbstractPipeBlock;
import me.scarletleaf1000.sunworks.block.custom.cable.CableTier;
import me.scarletleaf1000.sunworks.block.custom.cable.PipeConnection;
import me.scarletleaf1000.sunworks.block.entity.ModBlockEntities;
import me.scarletleaf1000.sunworks.block.entity.energy.ModEnergyUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The only block entity in an energy pipe network. Every tick it pulls energy from whatever
 * it is set to extract from, walks the (block-entity-free) pipe network it is part of via a
 * simple bounded flood fill, and splits the pulled energy evenly across every reachable output.
 */
public class EnergyPipeBlockEntity extends BlockEntity {
    private static final int MAX_NETWORK_SEARCH = 4096;

    private record Endpoint(BlockPos pos, Direction side) {
    }

    private Set<BlockPos> poweredPositions = Set.of();

    public EnergyPipeBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ENERGY_PIPE_BE.get(), pos, blockState);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        if (!(state.getBlock() instanceof AbstractPipeBlock pipeBlock)) return;

        CableTier tier = pipeBlock.getTier();
        Set<BlockPos> network = null;
        boolean transferred = false;

        transferAttempt:
        {
            List<Endpoint> sourceEndpoints = collectExtractSides(state, pos);
            if (sourceEndpoints.isEmpty()) break transferAttempt;

            List<IEnergyStorage> sources = new ArrayList<>();
            for (Endpoint endpoint : sourceEndpoints) {
                IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, endpoint.pos(), endpoint.side().getOpposite());
                if (storage != null && storage.canExtract()) {
                    sources.add(storage);
                }
            }
            if (sources.isEmpty()) break transferAttempt;

            Set<BlockPos> sourcePositions = new HashSet<>();
            for (Endpoint endpoint : sourceEndpoints) {
                sourcePositions.add(endpoint.pos());
            }

            network = new HashSet<>();
            Set<Endpoint> sinkEndpoints = findNetworkSinks(level, pos, tier, sourcePositions, network);
            if (sinkEndpoints.isEmpty()) break transferAttempt;

            List<IEnergyStorage> sinks = new ArrayList<>();
            for (Endpoint endpoint : sinkEndpoints) {
                IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, endpoint.pos(), endpoint.side().getOpposite());
                if (storage != null && storage.canReceive()) {
                    sinks.add(storage);
                }
            }
            if (sinks.isEmpty()) break transferAttempt;

            transferred = distribute(sources, sinks, tier.getMaxTransfer()) > 0;
        }

        Set<BlockPos> newPowered = transferred ? network : Set.of();
        updatePoweredPositions(level, newPowered);
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

    private List<Endpoint> collectExtractSides(BlockState state, BlockPos pos) {
        List<Endpoint> endpoints = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (state.getValue(AbstractPipeBlock.PROPERTY_BY_DIRECTION.get(direction)) == PipeConnection.EXTRACT) {
                endpoints.add(new Endpoint(pos.relative(direction), direction));
            }
        }
        return endpoints;
    }

    private Set<Endpoint> findNetworkSinks(Level level, BlockPos start, CableTier tier, Set<BlockPos> excludedPositions, Set<BlockPos> visitedPipes) {
        Set<Endpoint> sinks = new LinkedHashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();

        queue.add(start);
        visitedPipes.add(start);

        while (!queue.isEmpty() && visitedPipes.size() <= MAX_NETWORK_SEARCH) {
            BlockPos current = queue.poll();
            BlockState currentState = level.getBlockState(current);

            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = current.relative(direction);

                if (AbstractPipeBlock.isPipeOfTier(level, neighborPos, tier)) {
                    if (visitedPipes.add(neighborPos)) {
                        queue.add(neighborPos);
                    }
                    continue;
                }

                if (excludedPositions.contains(neighborPos)) {
                    continue;
                }

                // EXTRACT connections are input-only pull points - they must never double as
                // an output sink, even from a different extractor elsewhere in the network.
                if (currentState.getValue(AbstractPipeBlock.PROPERTY_BY_DIRECTION.get(direction)) != PipeConnection.PIPE) {
                    continue;
                }

                if (ModEnergyUtil.doesBlockHaveEnergyStorage(neighborPos, direction.getOpposite(), level)) {
                    sinks.add(new Endpoint(neighborPos, direction));
                }
            }
        }

        return sinks;
    }

    private int distribute(List<IEnergyStorage> sources, List<IEnergyStorage> sinks, int maxTransfer) {
        int share = Math.max(1, maxTransfer / sinks.size());
        int remainingBudget = maxTransfer;

        for (IEnergyStorage sink : sinks) {
            if (remainingBudget <= 0) break;

            int attempt = Math.min(share, remainingBudget);
            int availableFromSources = extractFromSources(sources, attempt, true);
            if (availableFromSources <= 0) continue;

            int accepted = sink.receiveEnergy(availableFromSources, true);
            if (accepted <= 0) continue;

            int actuallyExtracted = extractFromSources(sources, accepted, false);
            int actuallyReceived = sink.receiveEnergy(actuallyExtracted, false);

            remainingBudget -= actuallyReceived;
        }

        return maxTransfer - remainingBudget;
    }

    private int extractFromSources(List<IEnergyStorage> sources, int amount, boolean simulate) {
        int extracted = 0;
        for (IEnergyStorage source : sources) {
            if (extracted >= amount) break;
            extracted += source.extractEnergy(amount - extracted, simulate);
        }
        return extracted;
    }
}
