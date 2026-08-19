package me.scarletleaf1000.sunworks.network;

import me.scarletleaf1000.sunworks.Sunworks;
import net.minecraft.core.BlockPos;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent client -> server when the player clicks the eject toggle button in the {@link
 * me.scarletleaf1000.sunworks.screen.widget.ConfigurationPanelWidget}, asking the server to
 * flip that machine's {@link me.scarletleaf1000.sunworks.block.entity.io.ConfigurableMachine#isEjectEnabled()}.
 */
public record EjectTogglePayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<EjectTogglePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Sunworks.MOD_ID, "eject_toggle"));

    public static final StreamCodec<ByteBuf, EjectTogglePayload> STREAM_CODEC =
            BlockPos.STREAM_CODEC.map(EjectTogglePayload::new, EjectTogglePayload::pos);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
