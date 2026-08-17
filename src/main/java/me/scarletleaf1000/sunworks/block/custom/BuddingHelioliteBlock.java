package me.scarletleaf1000.sunworks.block.custom;

import me.scarletleaf1000.sunworks.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystBlock;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class BuddingHelioliteBlock extends AmethystBlock {
    public static final int GROWTH_CHANCE = 5;
    private static final Direction[] DIRECTIONS = Direction.values();

    public BuddingHelioliteBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(GROWTH_CHANCE) == 0) {
            Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
            BlockPos budPos = pos.relative(direction);
            BlockState budState = level.getBlockState(budPos);
            Block block = null;
            if (canClusterGrowAtState(budState)) {
                block = ModBlocks.SMALL_HELIOLITE_BUD.get();
            } else if (budState.is(ModBlocks.SMALL_HELIOLITE_BUD.get()) && budState.getValue(AmethystClusterBlock.FACING) == direction) {
                block = ModBlocks.MEDIUM_HELIOLITE_BUD.get();
            } else if (budState.is(ModBlocks.MEDIUM_HELIOLITE_BUD.get()) && budState.getValue(AmethystClusterBlock.FACING) == direction) {
                block = ModBlocks.LARGE_HELIOLITE_BUD.get();
            } else if (budState.is(ModBlocks.LARGE_HELIOLITE_BUD.get()) && budState.getValue(AmethystClusterBlock.FACING) == direction) {
                block = ModBlocks.HELIOLITE_CLUSTER.get();
            }

            if (block != null) {
                BlockState newState = block.defaultBlockState()
                        .setValue(AmethystClusterBlock.FACING, direction)
                        .setValue(AmethystClusterBlock.WATERLOGGED, budState.getFluidState().getType() == Fluids.WATER);
                level.setBlockAndUpdate(budPos, newState);
            }
        }
    }

    public static boolean canClusterGrowAtState(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) && state.getFluidState().getAmount() == 8;
    }
}
