package ivorius.psychedelicraft.recipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.util.NbtSerialisable;

public class ItemMound implements NbtSerialisable {
    private final List<Item> indexes = new ArrayList<>();
    private final Map<Item, Entry> items = new HashMap<>();

    public ItemMound() {

    }

    public ItemMound(ItemMound original) {
        synchronized (original) {
            indexes.addAll(original.indexes);
            original.items.entrySet().forEach(entry -> items.put(entry.getKey(), new Entry(entry.getValue())));
        }
    }

    public ItemMound(CompoundTag compound, Provider lookup) {
        fromNbt(compound, lookup);
    }

    public synchronized void addStack(ItemStack stack) {
        Entry entry = items.computeIfAbsent(stack.getItem(), Entry::new);
        entry.push(stack.getComponentsPatch(), stack.getCount());
        if (!indexes.contains(stack.getItem())) {
            indexes.add(stack.getItem());
        }
    }

    public synchronized ItemStack removeStack(int index, int amount) {
        if (index >= 0 && index < indexes.size()) {
            ItemStack[] stack = { ItemStack.EMPTY };
            items.compute(indexes.get(index), (i, entry) -> entry == null ? null : entry.remove(amount, stack));
            return stack[0];
        }
        return ItemStack.EMPTY;
    }

    public synchronized boolean removeWhere(@Nullable Predicate<ItemStack> predicate, int max) {
        var iter = items.entrySet().iterator();
        while (iter.hasNext()) {
            var entry = iter.next().getValue();
            if (predicate == null || predicate.test(entry.item.getDefaultInstance())) {
                int available = Math.min(max, entry.count);
                max -= available;
                if (entry.removeWhere(null, available) == null) {
                    indexes.remove(entry.item);
                    iter.remove();
                }

                if (max <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized int countMatches(@Nullable Predicate<ItemStack> predicate) {
        return items.values().stream()
                .mapToInt(i -> i.countMatches(predicate))
                .sum();
    }

    public synchronized boolean isEmpty() {
        return items.isEmpty();
    }

    public synchronized int size() {
        return items.size();
    }

    public synchronized ItemStack getStack(int index) {
        ItemStack[] stack = {ItemStack.EMPTY};
        Entry entry = items.get(this.indexes.get(index));
        entry.popSingle(stack, false);
        stack[0].setCount(entry.count);
        return stack[0];
    }

    public synchronized int getCount(Item item) {
        Entry entry = items.get(item);
        return entry == null ? 0 : entry.count;
    }

    public synchronized NonNullList<ItemStack> convertToItemStacks() {
        NonNullList<ItemStack> stacks = NonNullList.createWithCapacity(size());
        items.forEach((item, entry) -> entry.toItemStacks(stacks));
        return stacks;
    }

    public synchronized void clear() {
        items.clear();
        indexes.clear();
    }

    @Override
    public synchronized void toNbt(CompoundTag compound, Provider lookup) {
        items.forEach((item, count) -> {
            compound.put(BuiltInRegistries.ITEM.getKey(item).toString(), count.toNbt());
        });
    }

    @Override
    public synchronized void fromNbt(CompoundTag compound, Provider lookup) {
        clear();
        compound.getAllKeys().forEach(key -> {
            Optional.ofNullable(ResourceLocation.tryParse(key)).map(BuiltInRegistries.ITEM::get)
                .filter(Objects::nonNull)
                .ifPresent(item -> {
                    Entry entry = items.computeIfAbsent(item, Entry::new);
                    if (!indexes.contains(item)) {
                        indexes.add(item);
                    }
                    if (compound.contains(key, Tag.TAG_COMPOUND)) {
                        items.computeIfAbsent(item, Entry::new).fromNbt(compound.getCompound(key));
                    } else if (compound.contains(key, Tag.TAG_INT)) {
                        entry.push(DataComponentPatch.EMPTY, compound.getInt(key));
                    }
                });
        });
    }

    private final class Entry {
        private static final Codec<Map<DataComponentPatch, ComponentStack>> COMPONENTS_CODEC = ComponentStack.CODEC.listOf().xmap(
                list -> list.stream().collect(Collectors.toUnmodifiableMap(p -> p.changes, p -> p, (a, b) -> b)),
                map -> map.entrySet().stream().map(e -> e.getValue()).toList()
        );

        private int count;
        private final Item item;
        private final Map<DataComponentPatch, ComponentStack> components = new HashMap<>();

        Entry(Item item) {
            this.item = item;
        }

        Entry(Entry entry) {
            this.item = entry.item;
            this.count = entry.count;
            this.components.putAll(entry.components);
        }

        public void fromNbt(CompoundTag nbt) {
            count = nbt.getInt("count");
            components.clear();
            NbtSerialisable.get(nbt, "components", COMPONENTS_CODEC).ifPresent(components::putAll);
        }

        public CompoundTag toNbt() {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("count", count);
            NbtSerialisable.put(nbt, "components", COMPONENTS_CODEC, components);
            return nbt;
        }

        public void toItemStacks(NonNullList<ItemStack> stacks) {
            int outputted = 0;
            for (var entry : components.values()) {
                outputted += entry.count;
                ItemStack stack = entry.getStack(item);
                int maxCount = stack.getMaxStackSize();
                int stackCount = entry.count / maxCount;
                for (int i = 0; i < stackCount; i++) {
                    stacks.add(stack.copyWithCount(maxCount));
                }
                int last = entry.count % maxCount;
                if (last > 0) {
                    stacks.add(stack.copyWithCount(maxCount));
                }
            }
            while (outputted++ < count) {
                stacks.add(item.getDefaultInstance());
            }
        }

        public int countMatches(@Nullable Predicate<ItemStack> predicate) {
            if (predicate == null) {
                return count;
            }

            int testCount = 0;
            int matchCount = 0;
            for (var entry : components.values()) {
                testCount += entry.count;
                if (predicate.test(entry.getStack(item))) {
                    matchCount += entry.count;
                }
            }
            if (predicate.test(item.getDefaultInstance())) {
                matchCount += count - testCount;
            }

            return matchCount;
        }

        public Entry removeWhere(@Nullable Predicate<ItemStack> predicate, int count) {
            int matchCount = 0;
            int testCount = 0;
            if (this.count == 0) {
                components.clear();
            } else {
                var iter = components.entrySet().iterator();
                while (iter.hasNext() && count > 0) {
                    var entry = iter.next().getValue();
                    int subtracted = Math.min(count, entry.count);
                    testCount += subtracted;
                    if (predicate != null && !predicate.test(entry.getStack(item))) {
                        continue;
                    }
                    count -= subtracted;
                    matchCount += subtracted;
                    if (subtracted <= entry.count) {
                        iter.remove();
                    } else {
                        entry.count -= subtracted;
                    }
                }
                if (predicate == null || predicate.test(item.getDefaultInstance())) {
                    matchCount += this.count - testCount;
                }
                this.count = Math.max(0, this.count - matchCount);
            }

            return this.count <= 0 ? null : this;
        }

        public void push(DataComponentPatch changes, int count) {
            this.count += count;
            if (!changes.isEmpty()) {
                components.compute(changes, (i, c) -> {
                    if (c == null) {
                        return new ComponentStack(i, count);
                    }
                    c.count += count;
                    return c;
                });
            }
        }

        public Entry remove(int amount, ItemStack[] outStack) {
            amount = Math.min(count, amount);

            if (amount <= 0) {
                outStack[0] = ItemStack.EMPTY;
                return null;
            }
            if (amount == 1) {
                return popSingle(outStack, true);
            }

            var entry = components.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .sorted(Comparator.comparing(i -> -i.count))
                .findFirst().orElse(null);
            if (entry == null) {
                outStack[0] = item.getDefaultInstance();
            } else {
                amount = Math.min(amount, entry.count);
                count -= amount;
                if (count <= 0) {
                    items.remove(item);
                }

                outStack[0] = entry.getStack(item).copy();
                if (amount >= entry.count) {
                    components.remove(entry.changes);
                } else {
                    entry.count -= amount;
                }
            }
            return count <= 0 ? null : this;
        }

        public Entry popSingle(ItemStack[] outStack, boolean remove) {
            if (remove) {
                count = Math.max(0, count);
            }
            var iter = components.entrySet().iterator();
            if (!iter.hasNext()) {
                outStack[0] = item.getDefaultInstance();
            } else {
                var entry = iter.next().getValue();
                if (remove) {
                    if (entry.count <= 1) {
                        iter.remove();
                    } else {
                        entry.count--;
                    }
                }

                outStack[0] = entry.getStack(item).copy();
            }
            return count <= 0 ? null : this;
        }

        private static final class ComponentStack {
            private static final Codec<ComponentStack> CODEC = RecordCodecBuilder.create(i -> i.group(
                    DataComponentPatch.CODEC.fieldOf("changes").forGetter(s -> s.changes),
                    Codec.INT.fieldOf("count").forGetter(s -> s.count)
            ).apply(i, ComponentStack::new));

            public final DataComponentPatch changes;
            public int count;

            private ItemStack cachedStack;

            ComponentStack(DataComponentPatch changes, int count) {
                this.changes = changes;
                this.count = count;
            }

            public ItemStack getStack(Item item) {
                if (cachedStack == null) {
                    cachedStack = item.getDefaultInstance();
                    if (!changes.isEmpty()) {
                        cachedStack.applyComponents(changes);
                    }
                }
                return cachedStack;
            }
        }
    }
}
