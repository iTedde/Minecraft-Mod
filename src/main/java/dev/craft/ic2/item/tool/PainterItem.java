package dev.craft.ic2.item.tool;

import java.util.List;

import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.block.PaintableBlock;
import dev.craft.ic2.keyboard.Ic2Keyboard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Colored painter (IC2 {@code ItemToolPainter}): paints cables, wool and sheep; 32 strokes, then it becomes the
 * empty painter or refills itself from dye in the inventory.
 */
public class PainterItem extends Item {
    public static final int DURABILITY = 32;

    /** Dye metadata 0 (black) – 15 (white). */
    private final int color;

    public PainterItem(Properties properties, int color) {
        super(properties.durability(DURABILITY));
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    /** Vanilla dye color of the IC2 dye metadata (1.4.7 {@code BlockCloth.getBlockFromDye}). */
    public static DyeColor dyeColor(int color) {
        return DyeColor.byId(15 - color);
    }

    public static TagKey<Item> dyeTag(int color) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "dyes/" + dyeColor(color).getName()));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState state = level.getBlockState(pos);
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (state.getBlock() instanceof PaintableBlock paintable && paintable.colorBlock(level, pos, color)) {
            if (!level.isClientSide()) {
                damagePainter(player, context.getHand());
            }
        } else if (state.is(BlockTags.WOOL) && state.getBlock() != wool(color)) {
            if (!level.isClientSide()) {
                level.setBlock(pos, wool(color).defaultBlockState(), Block.UPDATE_ALL);
                damagePainter(player, context.getHand());
            }
        } else {
            return InteractionResult.PASS;
        }
        level.playSound(player, pos, Ic2Sounds.PAINTER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
                                                  InteractionHand hand) {
        if (!(target instanceof Sheep sheep) || sheep.getColor() == dyeColor(color)) {
            return InteractionResult.PASS;
        }
        if (!player.level().isClientSide()) {
            sheep.setColor(dyeColor(color));
            damagePainter(player, hand);
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide());
    }

    /** Mode key + right click toggles the automatic refill. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && Ic2Keyboard.isModeSwitchKeyDown(player)) {
            boolean autoRefill = !stack.getOrDefault(Ic2DataComponents.AUTO_REFILL.get(), false);
            stack.set(Ic2DataComponents.AUTO_REFILL.get(), autoRefill);
            player.displayClientMessage(Component.translatable(autoRefill
                    ? "message.craft.painter.auto_refill_on" : "message.craft.painter.auto_refill_off"), false);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("color.minecraft." + dyeColor(color).getName()));
    }

    /** IC2 {@code damagePainter}: the 32nd stroke empties the painter unless a dye refills it. */
    private void damagePainter(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() < stack.getMaxDamage() - 1) {
            stack.setDamageValue(stack.getDamageValue() + 1);
        } else {
            ItemStack dye = stack.getOrDefault(Ic2DataComponents.AUTO_REFILL.get(), false)
                    ? findLastDye(player.getInventory()) : ItemStack.EMPTY;
            if (dye.isEmpty()) {
                player.setItemInHand(hand, new ItemStack(Ic2Items.PAINTER.get()));
            } else {
                dye.shrink(1);
                stack.setDamageValue(0);
            }
        }
        player.inventoryMenu.broadcastChanges();
    }

    /** Original quirk: the search keeps going, so the last main inventory slot with a matching dye is used. */
    private ItemStack findLastDye(Inventory inventory) {
        ItemStack found = ItemStack.EMPTY;
        for (ItemStack stack : inventory.items) {
            if (stack.is(dyeTag(color))) {
                found = stack;
            }
        }
        return found;
    }

    private static Block wool(int color) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.withDefaultNamespace(dyeColor(color).getName() + "_wool"));
    }
}
