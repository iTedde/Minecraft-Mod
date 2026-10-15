package dev.craft.ic2.item.tool;

import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.item.ElectricItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Nano saber (IC2 {@code ItemNanoSaber}). IC2's switched-on saber is a separate item id; here it is the
 * {@link Ic2DataComponents#ACTIVE} flag. Its sounds do not exist in the pack, so it is silent as there.
 */
public class NanoSaberItem extends ElectricToolItem {
    public static final int OPERATION_COST = 10;
    public static final int ACTIVE_DAMAGE = 20;
    public static final int INACTIVE_DAMAGE = 4;
    /** EU per drain unit. */
    public static final int DRAIN_FACTOR = 8;

    private static final ItemAttributeModifiers ACTIVE_MODIFIERS = damageModifiers(ACTIVE_DAMAGE);
    private static final ItemAttributeModifiers INACTIVE_MODIFIERS = damageModifiers(INACTIVE_DAMAGE);

    public NanoSaberItem(Properties properties) {
        // The 4.0 mining speed of IC2's override is never used: Forge asks the metadata variant of the base tool.
        super(properties, 40000, 128, 2, OPERATION_COST, 6.0F, null);
    }

    /** IC2 damage values include the player's base damage of 1. */
    private static ItemAttributeModifiers damageModifiers(int damage) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, damage - 1,
                        AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    public static boolean isActive(ItemStack stack) {
        return stack.getOrDefault(Ic2DataComponents.ACTIVE.get(), false);
    }

    private static void setActive(ItemStack stack, boolean active) {
        if (active) {
            stack.set(Ic2DataComponents.ACTIVE.get(), true);
        } else {
            stack.remove(Ic2DataComponents.ACTIVE.get());
        }
    }

    /** Uses {@code units · 8} EU; switches the saber off when that fails. */
    public static void drain(ItemStack stack, int units, @Nullable Player player) {
        if (!ElectricItems.use(stack, units * DRAIN_FACTOR, player)) {
            setActive(stack, false);
        }
    }

    @Override
    protected boolean isMineable(BlockState state) {
        return state.is(Blocks.COBWEB);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return isActive(stack) ? ACTIVE_MODIFIERS : INACTIVE_MODIFIERS;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!isActive(stack) || attacker.level().isClientSide()) {
            return true;
        }
        // IC2: only when the attacker is no player or PvP is off.
        if (!(attacker instanceof Player) || attacker.getServer() == null || !attacker.getServer().isPvpAllowed()) {
            Player player = attacker instanceof Player p ? p : null;
            // Draining nano/quantum armor of player targets follows with the armor (phase 6.3).
            drain(stack, 5, player);
        }
        return true;
    }

    @Override
    protected void onBlockStartBreak(ItemStack stack, Level level, BlockPos pos, BlockState state, Player player) {
        if (isActive(stack)) {
            drain(stack, 10, player);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            if (isActive(stack)) {
                setActive(stack, false);
            } else if (ElectricItems.canUse(stack, 16)) {
                setActive(stack, true);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    /** Idle drain: every 16 ticks 64 units in the hotbar, otherwise every 64 ticks 16 units. */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player) || !isActive(stack)) {
            return;
        }
        long time = level.getGameTime();
        if (time % 16 != 0) {
            return;
        }
        boolean hotbar = slot < 9 && player.getInventory().items.get(slot) == stack;
        if (hotbar) {
            drain(stack, 64, player);
        } else if (time % 64 == 0) {
            drain(stack, 16, player);
        }
    }
}
