package dev.craft.core.energy;

import dev.craft.Craft;
import dev.craft.core.energy.OutputReadout.OutputReading;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

/**
 * Look-at overlay: the client asks for the output of the {@link OutputReadout} under the crosshair, the server
 * answers with the current reading. The last answer is kept here for the overlay.
 */
@EventBusSubscriber(modid = Craft.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class OutputPayloads {
    /** Farther than any block reach; only guards against querying arbitrary positions. */
    private static final double MAX_QUERY_DISTANCE_SQR = 32.0 * 32.0;

    @Nullable
    private static volatile Answer lastAnswer;

    private OutputPayloads() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(Query.TYPE, Query.STREAM_CODEC, OutputPayloads::handleQuery)
                .playToClient(Answer.TYPE, Answer.STREAM_CODEC, (answer, context) -> lastAnswer = answer);
    }

    /** Client: the last answer from the server, or {@code null}. */
    @Nullable
    public static Answer lastAnswer() {
        return lastAnswer;
    }

    public static void query(BlockPos pos) {
        PacketDistributor.sendToServer(new Query(pos));
    }

    private static void handleQuery(Query query, IPayloadContext context) {
        Player player = context.player();
        BlockPos pos = query.pos();
        if (player.distanceToSqr(pos.getCenter()) > MAX_QUERY_DISTANCE_SQR || !player.level().isLoaded(pos)) {
            return;
        }
        OutputReading reading = player.level().getBlockEntity(pos) instanceof OutputReadout readout
                ? readout.getOutputReading() : null;
        context.reply(reading == null
                ? new Answer(pos, false, 0.0, (byte) 0)
                : new Answer(pos, true, reading.rate(), (byte) reading.unit().ordinal()));
    }

    public record Query(BlockPos pos) implements CustomPacketPayload {
        public static final Type<Query> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "output_query"));
        public static final StreamCodec<ByteBuf, Query> STREAM_CODEC =
                StreamCodec.composite(BlockPos.STREAM_CODEC, Query::pos, Query::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Answer(BlockPos pos, boolean present, double rate, byte unit) implements CustomPacketPayload {
        public static final Type<Answer> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "output_answer"));
        public static final StreamCodec<ByteBuf, Answer> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, Answer::pos,
                ByteBufCodecs.BOOL, Answer::present,
                ByteBufCodecs.DOUBLE, Answer::rate,
                ByteBufCodecs.BYTE, Answer::unit,
                Answer::new);

        @Nullable
        public OutputReading reading() {
            OutputReading.Unit[] units = OutputReading.Unit.values();
            return present && unit >= 0 && unit < units.length ? new OutputReading(rate, units[unit]) : null;
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
