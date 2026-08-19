package me.scarletleaf1000.sunworks.block.custom.logistics;

import me.scarletleaf1000.sunworks.block.custom.cable.AbstractPipeBlock;
import me.scarletleaf1000.sunworks.block.custom.cable.PipeConnection;
import me.scarletleaf1000.sunworks.block.entity.ModBlockEntities;
import me.scarletleaf1000.sunworks.block.entity.custom.logistics.ItemPipeBlockEntity;
import me.scarletleaf1000.sunworks.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;

/**
 * An item pipe segment. Unlike energy pipes there is no tier and no {@code POWERED} cosmetic
 * state - items are only ever moved out of inventories by extractor attachments (see
 * {@code ItemPipeBlockEntity}), which are attached per-face by right-clicking with an
 * {@code sunworks:extractor} and stored in the block entity rather than the blockstate.
 */
public class ItemPipeBlock extends AbstractPipeBlock implements EntityBlock {
    public ItemPipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean canConnectToPipe(LevelReader level, BlockPos neighborPos) {
        return level.getBlockState(neighborPos).getBlock() instanceof ItemPipeBlock;
    }

    @Override
    protected boolean canConnectToMachine(LevelReader level, BlockPos neighborPos, Direction face) {
        return level instanceof Level realLevel
                && realLevel.getCapability(Capabilities.ItemHandler.BLOCK, neighborPos, face.getOpposite()) != null;
    }

    @Override
    protected void onTopologyChanged() {
        ItemPipeBlockEntity.bumpTopologyVersion();
    }

    /**
     * Right-clicking with an extractor attaches it to the specific face that was clicked
     * ({@link BlockHitResult#getDirection()}), but only if that face is already a
     * {@link PipeConnection#MACHINE} connection - i.e. the adjacent block actually has an
     * inventory to pull from.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(ModItems.EXTRACTOR.get())) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }

        Direction face = hitResult.getDirection();
        if (state.getValue(PROPERTY_BY_DIRECTION.get(face)) != PipeConnection.MACHINE
                || !(level.getBlockEntity(pos) instanceof ItemPipeBlockEntity pipeEntity)
                || pipeEntity.hasExtractor(face)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide) {
            pipeEntity.addExtractor(face);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.playSound(null, pos, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ItemPipeBlockEntity pipeEntity
                && !pipeEntity.getExtractorFaces().isEmpty()) {
            popResource(level, pos, new ItemStack(ModItems.EXTRACTOR.get(), pipeEntity.getExtractorFaces().size()));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ItemPipeBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide || blockEntityType != ModBlockEntities.ITEM_PIPE_BE.get()) return null;

        return (BlockEntityTicker<T>) (BlockEntityTicker<ItemPipeBlockEntity>)
                (level1, pos, state1, blockEntity) -> blockEntity.tick(level1, pos, state1);
    }
}
