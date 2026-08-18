package me.scarletleaf1000.sunworks.block.custom.cable;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds and caches the VoxelShape for a pipe given the {@link PipeConnection} state of all 6 directions.
 * Shapes are cached by a packed base-3 key so every pipe block/tier reuses the same cache
 * (the shapes only depend on the connection layout, not on the texture/tier).
 */
public final class PipeShapes {
    private static final VoxelShape CORE = Block.box(5, 5, 5, 11, 11, 11);

    private static final Map<Integer, VoxelShape> CACHE = new HashMap<>();

    private PipeShapes() {
    }

    public static VoxelShape getShape(Map<Direction, PipeConnection> connections) {
        int key = 0;
        for (Direction direction : Direction.values()) {
            key = key * 3 + connections.getOrDefault(direction, PipeConnection.NONE).ordinal();
        }

        int finalKey = key;
        return CACHE.computeIfAbsent(finalKey, k -> buildShape(connections));
    }

    private static VoxelShape buildShape(Map<Direction, PipeConnection> connections) {
        VoxelShape shape = CORE;
        for (Direction direction : Direction.values()) {
            PipeConnection connection = connections.getOrDefault(direction, PipeConnection.NONE);
            VoxelShape piece = switch (connection) {
                case NONE -> null;
                case PIPE -> armShape(direction);
                case MACHINE -> Shapes.join(armShape(direction), panelShape(direction), BooleanOp.OR);
            };
            if (piece != null) {
                shape = Shapes.join(shape, piece, BooleanOp.OR);
            }
        }
        return shape;
    }

    private static VoxelShape armShape(Direction direction) {
        return switch (direction) {
            case DOWN -> Block.box(5, 0, 5, 11, 5, 11);
            case UP -> Block.box(5, 11, 5, 11, 16, 11);
            case NORTH -> Block.box(5, 5, 0, 11, 11, 5);
            case SOUTH -> Block.box(5, 5, 11, 11, 11, 16);
            case WEST -> Block.box(0, 5, 5, 5, 11, 11);
            case EAST -> Block.box(11, 5, 5, 16, 11, 11);
        };
    }

    private static VoxelShape panelShape(Direction direction) {
        return switch (direction) {
            case DOWN -> Block.box(4, 0, 4, 12, 1, 12);
            case UP -> Block.box(4, 15, 4, 12, 16, 12);
            case NORTH -> Block.box(4, 4, 0, 12, 12, 1);
            case SOUTH -> Block.box(4, 4, 15, 12, 12, 16);
            case WEST -> Block.box(0, 4, 4, 1, 12, 12);
            case EAST -> Block.box(15, 4, 4, 16, 12, 12);
        };
    }
}
