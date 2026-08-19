package me.scarletleaf1000.sunworks.item.custom;

import net.minecraft.network.chat.Component;

/**
 * Attaches to a single face of an item pipe by right-clicking (see
 * {@code ItemPipeBlock#useItemOn}), where it pulls items out of the adjacent inventory into the
 * pipe network. This is the only current tier, pulling up to
 * {@code ItemPipeBlockEntity#EXTRACTOR_MAX_STACK} items per operation.
 */
public class ExtractorItem extends DescriptiveItem {
    public ExtractorItem(Properties properties) {
        super(properties, Component.translatable("tooltip.sunworks.extractor.description"));
    }
}
