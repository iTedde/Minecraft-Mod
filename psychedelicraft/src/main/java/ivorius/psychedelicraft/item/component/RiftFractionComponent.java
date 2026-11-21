package ivorius.psychedelicraft.item.component;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.RiftJarItem;

public record RiftFractionComponent(float amount) implements TooltipProvider {
    public static final RiftFractionComponent DEFAULT = new RiftFractionComponent(0F);
    public static final Codec<RiftFractionComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("amount").forGetter(RiftFractionComponent::amount)
    ).apply(instance, RiftFractionComponent::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, RiftFractionComponent> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, RiftFractionComponent::amount,
            RiftFractionComponent::new
    );

    public static float getRiftFraction(ItemStack stack) {
        RiftFractionComponent amount = stack.get(PSComponents.RIFT_FRACTION);
        return amount == null ? 0 : amount.amount();
    }

    public static ItemStack set(ItemStack stack, float riftFraction) {
        if (riftFraction > 0 && stack.getItem() instanceof RiftJarItem) {
            stack.set(PSComponents.RIFT_FRACTION, new RiftFractionComponent(riftFraction));
        }
        return stack;
    }

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> tooltip, TooltipFlag type) {
        tooltip.accept(Component.translatable("item.psychedelicraft.rift_jar." + getUnlocalizedFractionName(amount)).withStyle(ChatFormatting.GRAY));
    }

    private static String getUnlocalizedFractionName(float fraction) {
        if (fraction <= 0) {
            return "empty";
        }
        if (fraction < 0.4F) {
            return "slightly_filled";
        }
        if (fraction < 0.6F) {
            return "half_filled";
        }
        if (fraction < 0.8F) {
            return "filled";
        }

        return "over_filled";
    }
}
