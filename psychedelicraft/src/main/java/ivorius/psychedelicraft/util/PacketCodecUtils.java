package ivorius.psychedelicraft.util;

import java.util.Optional;
import java.util.function.Function;

import com.mojang.datafixers.util.Function7;

import io.netty.buffer.ByteBuf;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.valueproviders.IntProvider;

public interface PacketCodecUtils {
    StreamCodec<ByteBuf, Optional<Integer>> OPTIONAL_INT = ByteBufCodecs.optional(ByteBufCodecs.INT);
    StreamCodec<ByteBuf, Optional<Long>> OPTIONAL_VAR_LONG = ByteBufCodecs.optional(ByteBufCodecs.VAR_LONG);
    StreamCodec<ByteBuf, Ints> INT_RANGE = StreamCodec.composite(
            OPTIONAL_INT, Ints::min,
            OPTIONAL_INT, Ints::max,
            OPTIONAL_VAR_LONG, Ints::minSq,
            OPTIONAL_VAR_LONG, Ints::maxSq,
            Ints::new
    );
    StreamCodec<ByteBuf, IntProvider> INT_PROVIDER_VALUE_CODEC = ByteBufCodecs.optional(ByteBufCodecs.TAG).map(
            nbt -> nbt.flatMap(i -> IntProvider.CODEC.decode(NbtOps.INSTANCE, i).result().map(pair -> pair.getFirst())).orElseThrow(),
            input -> IntProvider.CODEC.encodeStart(NbtOps.INSTANCE, input).result());

    static <T extends Enum<T>> StreamCodec<RegistryFriendlyByteBuf, T> ofEnum(Class<T> type) {
        return StreamCodec.of(RegistryFriendlyByteBuf::writeEnum, b -> b.readEnum(type));
    }

    static<B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, NonNullList<V>> toDefaultedList() {
        return ByteBufCodecs.collection(NonNullList::createWithCapacity);
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7> StreamCodec<B, C> tuple(
            StreamCodec<? super B, T1> codec1, Function<C, T1> from1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> from2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> from3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> from4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> from5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> from6,
            StreamCodec<? super B, T7> codec7, Function<C, T7> from7,
            Function7<T1, T2, T3, T4, T5, T6, T7, C> to
        ) {
            return new StreamCodec<>() {
                @Override
                public C decode(B b) {
                    return to.apply(codec1.decode(b), codec2.decode(b), codec3.decode(b), codec4.decode(b), codec5.decode(b), codec6.decode(b), codec7.decode(b));
                }

                @Override
                public void encode(B b, C c) {
                    codec1.encode(b, from1.apply(c));
                    codec2.encode(b, from2.apply(c));
                    codec3.encode(b, from3.apply(c));
                    codec4.encode(b, from4.apply(c));
                    codec5.encode(b, from5.apply(c));
                    codec6.encode(b, from6.apply(c));
                    codec7.encode(b, from7.apply(c));
                }
            };
        }
}
