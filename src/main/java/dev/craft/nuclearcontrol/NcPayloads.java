package dev.craft.nuclearcontrol;

import dev.craft.Craft;
import dev.craft.nuclearcontrol.block.AlarmBlockEntity;
import dev.craft.nuclearcontrol.block.ThermalMonitorBlockEntity;
import dev.craft.nuclearcontrol.menu.NcMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * GUI settings sent to the server (the original's {@code initiateClientTileEntityEvent} and
 * {@code setNewAlarmSound}): heat level and redstone inversion of the thermal monitors, range and sound of the alarms.
 * Only accepted for the block of the player's open Nuclear Control menu.
 */
public final class NcPayloads {
    public static final int HEAT_LEVEL = 0;
    public static final int INVERT = 1;
    public static final int RANGE = 2;
    public static final int SOUND = 3;

    private NcPayloads() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(Setting.TYPE, Setting.STREAM_CODEC, NcPayloads::handle);
    }

    public static void send(BlockPos pos, int kind, int value) {
        PacketDistributor.sendToServer(new Setting(pos, kind, value, ""));
    }

    public static void sendSound(BlockPos pos, String sound) {
        PacketDistributor.sendToServer(new Setting(pos, SOUND, 0, sound));
    }

    private static void handle(Setting payload, IPayloadContext context) {
        if (!(context.player().containerMenu instanceof NcMenu menu) || !menu.getPos().equals(payload.pos())
                || !menu.stillValid(context.player())) {
            return;
        }
        BlockEntity blockEntity = context.player().level().getBlockEntity(payload.pos());
        switch (payload.kind()) {
            case HEAT_LEVEL -> {
                if (blockEntity instanceof ThermalMonitorBlockEntity monitor) {
                    monitor.setHeatLevel(payload.value());
                }
            }
            case INVERT -> {
                if (blockEntity instanceof ThermalMonitorBlockEntity monitor) {
                    monitor.setInvertRedstone(payload.value() != 0);
                }
            }
            case RANGE -> {
                if (blockEntity instanceof AlarmBlockEntity alarm) {
                    alarm.setRange(payload.value());
                }
            }
            case SOUND -> {
                if (blockEntity instanceof AlarmBlockEntity alarm && NcSounds.ALARM_NAMES.contains(payload.text())) {
                    alarm.setSoundName(payload.text());
                }
            }
            default -> {
            }
        }
    }

    public record Setting(BlockPos pos, int kind, int value, String text) implements CustomPacketPayload {
        public static final Type<Setting> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "nuclear_control_setting"));
        public static final StreamCodec<ByteBuf, Setting> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, Setting::pos,
                ByteBufCodecs.VAR_INT, Setting::kind,
                ByteBufCodecs.VAR_INT, Setting::value,
                ByteBufCodecs.stringUtf8(32), Setting::text,
                Setting::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
