package me.scarletleaf1000.sunworks.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * A {@link BlockItem} that shows a fixed, pre-translated description when the player holds
 * shift, and a "hold shift for details" prompt otherwise.
 */
public class DescriptiveBlockItem extends BlockItem {
    private final List<Component> description;

    public DescriptiveBlockItem(Block block, Properties properties, Component... description) {
        super(block, properties);
        this.description = List.of(description);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        if (Screen.hasShiftDown()) {
            tooltip.addAll(description);
        } else {
            tooltip.add(Component.translatable("tooltip.sunworks.hold_shift").withStyle(ChatFormatting.GRAY));
        }
    }
}
