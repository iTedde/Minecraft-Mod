package dev.craft.nuclearcontrol;

import dev.craft.Craft;
import dev.craft.nuclearcontrol.block.AlarmBlockEntity;
import dev.craft.nuclearcontrol.block.InfoPanelBlockEntity;
import dev.craft.nuclearcontrol.block.ThermalMonitorBlockEntity;
import dev.craft.nuclearcontrol.item.TextCardItem;
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
 * GUI settings sent to the server (the original's {@code initiateClientTileEntityEvent}, {@code setNewAlarmSound},
 * {@code setScreenColor} and the card settings): heat level and redstone inversion of the thermal monitors, range and
 * sound of the alarms, display bits, labels, colours, card title and text lines of the information panel. Only
 * accepted for the block of the player's open Nuclear Control menu.
 */
public final class NcPayloads {
    public static final int HEAT_LEVEL = 0;
    public static final int INVERT = 1;
    public static final int RANGE = 2;
    public static final int SOUND = 3;
    public static final int DISPLAY_SETTINGS = 4;
    public static final int SHOW_LABELS = 5;
    /** Value {@code background | text << 4}. */
    public static final int COLORS = 6;
    public static final int TITLE = 7;
    /** Value = line index. */
    public static final int TEXT_LINE = 8;
    public static final int MAX_TEXT = 64;

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

    public static void sendText(BlockPos pos, int kind, int value, String text) {
        PacketDistributor.sendToServer(new Setting(pos, kind, value, text));
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
                if (blockEntity instanceof InfoPanelBlockEntity panel) {
                    handlePanel(panel, payload);
                }
            }
        }
    }

    private static void handlePanel(InfoPanelBlockEntity panel, Setting payload) {
        switch (payload.kind()) {
            case DISPLAY_SETTINGS -> panel.setDisplaySettings(payload.value());
            case SHOW_LABELS -> panel.setShowLabels(payload.value() != 0);
            case COLORS -> {
                if (panel.isColored()) {
                    panel.setColors(payload.value(), payload.value() >> 4);
                }
            }
            case TITLE -> panel.editCard(card -> card.setTitle(payload.text()));
            case TEXT_LINE -> {
                if (payload.value() >= 0 && payload.value() < TextCardItem.LINES
                        && panel.getCard().getItem() instanceof TextCardItem) {
                    panel.editCard(card -> card.setString("line_" + payload.value(), payload.text()));
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
                ByteBufCodecs.stringUtf8(MAX_TEXT), Setting::text,
                Setting::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
