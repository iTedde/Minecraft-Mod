package ivorius.psychedelicraft.util;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;

public interface NbtSerialisable {

    default CompoundTag toNbt(Provider lookup) {
        CompoundTag tagCompound = new CompoundTag();
        toNbt(tagCompound, lookup);
        return tagCompound;
    }

    void toNbt(CompoundTag compound, Provider lookup);

    void fromNbt(CompoundTag compound, Provider lookup);

    static <T> void put(CompoundTag nbt, String key, Codec<T> codec, T value) {
        nbt.put(key, codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow());
    }

    static <T> void putNullable(CompoundTag nbt, String key, Codec<T> codec, @Nullable T value) {
        if (value != null) {
            put(nbt, key, codec, value);
        }
    }

    static <T> Optional<T> get(CompoundTag nbt, String key, Codec<T> codec) {
        return nbt.contains(key) ? codec.decode(NbtOps.INSTANCE, nbt.get(key)).result().map(Pair::getFirst) : Optional.empty();
    }

    static <T extends NbtSerialisable> ListTag fromList(List<T> list, Provider lookup) {
        ListTag nbt = new ListTag();
        list.forEach(t -> nbt.add(t.toNbt(lookup)));
        return nbt;
    }

    static <T extends NbtSerialisable> List<T> toList(List<T> list, ListTag nbt, Provider lookup, Supplier<T> supplier) {
        list.clear();
        nbt.forEach(element -> {
            T t = supplier.get();
            t.fromNbt((CompoundTag)element, lookup);
            list.add(t);
        });
        return list;
    }
}
