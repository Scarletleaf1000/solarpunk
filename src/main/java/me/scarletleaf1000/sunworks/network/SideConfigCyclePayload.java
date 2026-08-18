package me.scarletleaf1000.sunworks.network;

import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.block.entity.io.RelativeSide;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent client -> server when the player clicks a face button in the {@link
 * me.scarletleaf1000.sunworks.screen.widget.ConfigurationPanelWidget}, asking the server to
 * cycle that side's {@link me.scarletleaf1000.sunworks.block.entity.io.IOType} forward.
 */
public record SideConfigCyclePayload(BlockPos pos, RelativeSide side) implements CustomPacketPayload {
    public static final Type<SideConfigCyclePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Sunworks.MOD_ID, "side_config_cycle"));

    public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, SideConfigCyclePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SideConfigCyclePayload::pos,
            ByteBufCodecs.STRING_UTF8, payload -> payload.side().name(),
            (pos, sideName) -> new SideConfigCyclePayload(pos, RelativeSide.valueOf(sideName))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
