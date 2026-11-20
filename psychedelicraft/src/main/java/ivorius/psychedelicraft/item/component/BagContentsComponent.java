package ivorius.psychedelicraft.item.component;

import java.util.function.Consumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.PaperBagItem;

public record BagContentsComponent(ItemStack stack, int count) implements TooltipProvider {
    public static final BagContentsComponent EMPTY = new BagContentsComponent(ItemStack.EMPTY, 0);
    public static final int FULL_COUNT = 16000;
    public static final Codec<BagContentsComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("stack").forGetter(BagContentsComponent::stack),
            Codec.INT.fieldOf("count").forGetter(BagContentsComponent::count)
    ).apply(instance, BagContentsComponent::of));
    public static final StreamCodec<RegistryFriendlyByteBuf, BagContentsComponent> PACKET_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, BagContentsComponent::stack,
            ByteBufCodecs.INT, BagContentsComponent::count,
            BagContentsComponent::of
    );

    public static BagContentsComponent of(ItemStack stack, int count) {
        if (count <= 0 || stack.isEmpty()) {
            return EMPTY;
        }
        return new BagContentsComponent(stack, count);
    }

    public static BagContentsComponent get(ItemStack stack) {
        BagContentsComponent contents = stack.get(PSComponents.BAG_CONTENTS);
        return contents == null ? EMPTY : contents;
    }

    public static BagContentsComponent set(ItemStack stack, Builder builder) {
        return stack.set(PSComponents.BAG_CONTENTS, builder.build());
    }

    public static ItemStack withdraw(ItemStack stack, int count) {
        BagContentsComponent contents = get(stack);
        if (contents.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Builder builder = new Builder(contents);
        ItemStack result = builder.split(count);
        if (!result.isEmpty()) {
            set(stack, builder);
        }
        return result;
    }

    public static int getMaxCountForItem(Item item) {
        if (item == PSItems.BOTTLE || item == PSItems.MOLOTOV_COCKTAIL) {
            return 1;
        }
        return 64 * 1000;
    }

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> tooltip, TooltipFlag type) {
        if (count > 0) {
            tooltip.accept(Component.literal(count() + " x ").append(stack().getHoverName()));
        }
    }

    public boolean isEmpty() {
        return count <= 0 || stack.isEmpty();
    }

    public boolean isFull() {
        return count <= getMaxCountForItem(stack.getItem());
    }

    public static class Builder {
        private ItemStack stack;
        private int count;

        public Builder(BagContentsComponent contents) {
            stack = contents.stack();
            count = contents.count();
        }

        public boolean canAdd(ItemStack stack) {
            if (stack.getItem() instanceof PaperBagItem) {
                return false;
            }

            if (this.stack.isEmpty()) {
                return true;
            }
            //if (stack.getItem() instanceof PaperBagItem) {
            //    return canAdd(getContents(stack).stack());
            //}
            return (this.stack.isEmpty() || ItemStack.isSameItemSameComponents(this.stack, stack)) && count < getMaxCountForItem(stack.getItem());
        }

        public boolean add(ItemStack stack) {
            /*if (stack.getItem() instanceof PaperBagItem) {
                Builder builder = new Builder(getContents(stack));
                this.stack = builder.stack.copy();
                count += builder.split(getMaxCountForItem(builder.stack.getItem()) - count).getCount();
                setContents(stack, builder.build());
                return true;
            }*/
            if (canAdd(stack)) {
                this.stack = stack.copyWithCount(1);
                count += stack.split(getMaxCountForItem(stack.getItem()) - count).getCount();
                return true;
            }
            return false;
        }

        public ItemStack split(int count) {
            ItemStack dispensed = stack.copyWithCount(Math.min(this.count, count));
            this.count -= dispensed.getCount();
            return dispensed;
        }

        public BagContentsComponent build() {
            return new BagContentsComponent(stack, count);
        }
    }
}
