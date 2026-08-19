package me.scarletleaf1000.sunworks.network;

import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.block.entity.io.ConfigurableMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Sunworks.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class SideConfigNetworking {
    private static final double MAX_REACH_SQR = 8.0 * 8.0;

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Sunworks.MOD_ID).versioned("1");
        registrar.playToServer(SideConfigCyclePayload.TYPE, SideConfigCyclePayload.STREAM_CODEC, SideConfigNetworking::handle);
        registrar.playToServer(EjectTogglePayload.TYPE, EjectTogglePayload.STREAM_CODEC, SideConfigNetworking::handleEjectToggle);
    }

    private static void handle(SideConfigCyclePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            Level level = player.level();
            BlockPos pos = payload.pos();

            if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > MAX_REACH_SQR) {
                return;
            }

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof ConfigurableMachine machine) || !machine.isSideConfigurable(payload.side())) {
                return;
            }

            machine.getSideConfiguration().cycle(payload.side(), machine.getSupportedTypes());
            blockEntity.setChanged();
            level.sendBlockUpdated(pos, blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
            level.invalidateCapabilities(pos);
        });
    }

    private static void handleEjectToggle(EjectTogglePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            Level level = player.level();
            BlockPos pos = payload.pos();

            if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > MAX_REACH_SQR) {
                return;
            }

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof ConfigurableMachine machine) || !machine.supportsEject()) {
                return;
            }

            machine.setEjectEnabled(!machine.isEjectEnabled());
        });
    }
}
