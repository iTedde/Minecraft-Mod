package dev.craft.ic2.crop;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.Ic2Items;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

/**
 * Seed bag (IC2 {@code ItemCropSeed}): crop id, stats and scan level in the data component {@code craft:crop_seed}
 * (IC2 NBT {@code id}, {@code growth}, {@code gain}, {@code resistance}, {@code scan}). Named after the crop from
 * scan level 1; stats show from scan level 4. Using it on an empty crop stick plants it.
 */
public class CropSeedItem extends Item {
    public CropSeedItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public record Seed(int id, int growth, int gain, int resistance, int scan) {
        public static final Codec<Seed> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("id").forGetter(Seed::id),
                Codec.INT.fieldOf("growth").forGetter(Seed::growth),
                Codec.INT.fieldOf("gain").forGetter(Seed::gain),
                Codec.INT.fieldOf("resistance").forGetter(Seed::resistance),
                Codec.INT.fieldOf("scan").forGetter(Seed::scan)).apply(instance, Seed::new));
        public static final StreamCodec<ByteBuf, Seed> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Seed::id, ByteBufCodecs.VAR_INT, Seed::growth, ByteBufCodecs.VAR_INT,
                Seed::gain, ByteBufCodecs.VAR_INT, Seed::resistance, ByteBufCodecs.VAR_INT, Seed::scan, Seed::new);
    }

    public static ItemStack create(int id, int growth, int gain, int resistance, int scan) {
        ItemStack stack = new ItemStack(Ic2Items.CROP_SEED_BAG.get());
        stack.set(Ic2DataComponents.CROP_SEED.get(), new Seed(id, growth, gain, resistance, scan));
        return stack;
    }

    /** IC2 reads −1 for every value of a bag without NBT. */
    public static Seed getSeed(ItemStack stack) {
        return stack.getOrDefault(Ic2DataComponents.CROP_SEED.get(), new Seed(-1, -1, -1, -1, -1));
    }

    /** IC2 {@code incrementScannedOfStack}. */
    public static void incrementScan(ItemStack stack) {
        Seed seed = stack.get(Ic2DataComponents.CROP_SEED.get());
        if (seed != null) {
            stack.set(Ic2DataComponents.CROP_SEED.get(),
                    new Seed(seed.id(), seed.growth(), seed.gain(), seed.resistance(), seed.scan() + 1));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        Seed seed = getSeed(stack);
        if (seed.scan() == 0) {
            return Component.translatable("item.craft.crop_seed_bag.unknown");
        }
        if (seed.scan() < 0) {
            return Component.translatable("item.craft.crop_seed_bag.invalid");
        }
        return Component.translatable("item.craft.crop_seed_bag.named", Crops.get(seed.id()).displayName());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Seed seed = getSeed(stack);
        if (seed.scan() >= 4) {
            tooltip.add(Component.literal("Gr").withStyle(ChatFormatting.DARK_GREEN)
                    .append(Component.literal(" " + seed.growth()).withStyle(ChatFormatting.GRAY)));
            tooltip.add(Component.literal("Ga").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(" " + seed.gain()).withStyle(ChatFormatting.GRAY)));
            tooltip.add(Component.literal("Re").withStyle(ChatFormatting.DARK_AQUA)
                    .append(Component.literal(" " + seed.resistance()).withStyle(ChatFormatting.GRAY)));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof CropBlockEntity crop)) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Seed seed = getSeed(context.getItemInHand());
        if (crop.tryPlantIn(seed.id(), 1, seed.growth(), seed.gain(), seed.resistance(), seed.scan())) {
            if (context.getPlayer() != null) {
                context.getPlayer().setItemInHand(context.getHand(), ItemStack.EMPTY);
            } else {
                context.getItemInHand().setCount(0);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }
}
