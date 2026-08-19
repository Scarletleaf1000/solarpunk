package me.scarletleaf1000.sunworks.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * An {@link Item} that shows a fixed, pre-translated description when the player holds shift,
 * and a "hold shift for details" prompt otherwise. The plain-item counterpart to
 * {@link DescriptiveBlockItem}.
 */
public class DescriptiveItem extends Item {
    private final List<Component> description;

    public DescriptiveItem(Properties properties, Component... description) {
        super(properties);
        this.description = List.of(description).stream()
                .<Component>map(component -> component.copy().withStyle(ChatFormatting.GRAY))
                .toList();
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
