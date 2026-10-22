package dev.craft.thermalexpansion.transport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.craft.Craft;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * {@code TeleportPacket}: {@link Configure} ({@code TILE_INFO}: frequency, mode, access), {@link Entry}
 * ({@code ENTRY}: save or delete a frequency name) and {@link Names} ({@code NAME_LIST}: the names of the open
 * tesseract's category, kept here for the GUI).
 */
public final class TesseractPayloads {
    private static Map<String, String> clientNames = Map.of();
    private static int clientNamesVersion;

    private TesseractPayloads() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(Configure.TYPE, Configure.STREAM_CODEC, TesseractPayloads::handleConfigure)
                .playToServer(Entry.TYPE, Entry.STREAM_CODEC, TesseractPayloads::handleEntry)
                .playToClient(Names.TYPE, Names.STREAM_CODEC, TesseractPayloads::handleNames);
    }

    /** Client: frequency text → name of the open tesseract's category, in list order, without empty names. */
    public static Map<String, String> clientNames() {
        return clientNames;
    }

    /** Client: changes whenever a new name list arrives. */
    public static int clientNamesVersion() {
        return clientNamesVersion;
    }

    public static void configure(BlockPos pos, int frequency, byte mode, byte access) {
        PacketDistributor.sendToServer(new Configure(pos, frequency, mode, access));
    }

    public static void entry(BlockPos pos, int frequency, String name, boolean remove) {
        PacketDistributor.sendToServer(new Entry(pos, frequency, name, remove));
    }

    /** {@code getNameListPacket}. */
    public static void sendNames(ServerPlayer player, TesseractBlockEntity tesseract) {
        Map<String, String> names = TesseractNames.get(player.server).get(tesseract.getNameCategory());
        PacketDistributor.sendToPlayer(player, new Names(new ArrayList<>(names.keySet()),
                new ArrayList<>(names.values())));
    }

    /** The tesseract of the player's open menu at {@code pos}, or {@code null}. */
    private static TesseractBlockEntity openTesseract(IPayloadContext context, BlockPos pos) {
        if (context.player().containerMenu instanceof TesseractMenu menu && menu.getPos().equals(pos)
                && menu.getTesseract() != null && menu.stillValid(context.player())) {
            return menu.getTesseract();
        }
        return null;
    }

    private static void handleConfigure(Configure payload, IPayloadContext context) {
        TesseractBlockEntity tesseract = openTesseract(context, payload.pos());
        if (tesseract == null || payload.frequency() < -1 || payload.frequency() > TesseractBlockEntity.MAX_FREQUENCY
                || payload.mode() < 0 || payload.mode() > 2 || payload.access() < 0 || payload.access() > 2) {
            return;
        }
        // TabTeleportConfig: only the owner changes the access.
        byte access = tesseract.isOwner(context.player()) ? payload.access() : tesseract.getAccess();
        tesseract.setTileInfo(payload.frequency(), payload.mode(), access);
        sendNames((ServerPlayer) context.player(), tesseract);
    }

    private static void handleEntry(Entry payload, IPayloadContext context) {
        TesseractBlockEntity tesseract = openTesseract(context, payload.pos());
        if (tesseract == null || payload.frequency() < 0 || payload.frequency() > TesseractBlockEntity.MAX_FREQUENCY
                || payload.name().length() > TesseractBlockEntity.MAX_NAME_LENGTH) {
            return;
        }
        ServerPlayer player = (ServerPlayer) context.player();
        TesseractNames.get(player.server).set(tesseract.getNameCategory(), payload.frequency(), payload.name(),
                payload.remove());
        sendNames(player, tesseract);
    }

    private static void handleNames(Names payload, IPayloadContext context) {
        Map<String, String> names = new LinkedHashMap<>();
        for (int i = 0; i < payload.frequencies().size() && i < payload.names().size(); i++) {
            if (!payload.names().get(i).isEmpty()) {
                names.put(payload.frequencies().get(i), payload.names().get(i));
            }
        }
        clientNames = Collections.unmodifiableMap(names);
        clientNamesVersion++;
    }

    public record Configure(BlockPos pos, int frequency, byte mode, byte access) implements CustomPacketPayload {
        public static final Type<Configure> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "tesseract_configure"));
        public static final StreamCodec<ByteBuf, Configure> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, Configure::pos,
                ByteBufCodecs.VAR_INT, Configure::frequency,
                ByteBufCodecs.BYTE, Configure::mode,
                ByteBufCodecs.BYTE, Configure::access,
                Configure::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Entry(BlockPos pos, int frequency, String name, boolean remove) implements CustomPacketPayload {
        public static final Type<Entry> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "tesseract_entry"));
        public static final StreamCodec<ByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, Entry::pos,
                ByteBufCodecs.VAR_INT, Entry::frequency,
                ByteBufCodecs.stringUtf8(TesseractBlockEntity.MAX_NAME_LENGTH * 4), Entry::name,
                ByteBufCodecs.BOOL, Entry::remove,
                Entry::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Names(List<String> frequencies, List<String> names) implements CustomPacketPayload {
        public static final Type<Names> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "tesseract_names"));
        public static final StreamCodec<ByteBuf, Names> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), Names::frequencies,
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), Names::names,
                Names::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
