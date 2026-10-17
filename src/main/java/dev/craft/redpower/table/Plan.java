package dev.craft.redpower.table;

import java.util.Arrays;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * A written project table plan (RedPower {@code ItemPlan} NBT): the crafting result and one item per used grid slot.
 * {@link ItemStack} has no value equality, so the record compares its stacks itself (data components need it).
 */
public record Plan(ItemStack result, List<Requirement> requires) {
    public static final Codec<Plan> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("result").forGetter(Plan::result),
            Requirement.CODEC.listOf().fieldOf("requires").forGetter(Plan::requires)
    ).apply(instance, Plan::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Plan> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, Plan::result,
            Requirement.STREAM_CODEC.apply(ByteBufCodecs.list()), Plan::requires,
            Plan::new);

    public Plan {
        result = result.copy();
        requires = List.copyOf(requires);
    }

    /** {@code getShadowItems}: the required item of each grid slot, empty where the plan needs nothing. */
    public ItemStack[] shadowItems() {
        ItemStack[] items = new ItemStack[ProjectTableBlockEntity.GRID_SIZE];
        Arrays.fill(items, ItemStack.EMPTY);
        for (Requirement requirement : requires) {
            if (requirement.slot() >= 0 && requirement.slot() < items.length) {
                items[requirement.slot()] = requirement.item();
            }
        }
        return items;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Plan plan && ItemStack.matches(result, plan.result) && requires.equals(plan.requires);
    }

    @Override
    public int hashCode() {
        return 31 * ItemStack.hashItemAndComponents(result) + requires.hashCode();
    }

    /** One entry of {@code requires}: the item (count 1) and its grid slot. */
    public record Requirement(int slot, ItemStack item) {
        public static final Codec<Requirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("slot").forGetter(Requirement::slot),
                ItemStack.CODEC.fieldOf("item").forGetter(Requirement::item)
        ).apply(instance, Requirement::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Requirement> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Requirement::slot,
                ItemStack.STREAM_CODEC, Requirement::item,
                Requirement::new);

        public Requirement {
            item = item.copyWithCount(1);
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Requirement requirement && slot == requirement.slot
                    && ItemStack.matches(item, requirement.item);
        }

        @Override
        public int hashCode() {
            return 31 * slot + ItemStack.hashItemAndComponents(item);
        }
    }
}
