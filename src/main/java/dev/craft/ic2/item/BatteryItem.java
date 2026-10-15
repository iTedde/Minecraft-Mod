package dev.craft.ic2.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * RE battery, energy crystal and lapotron crystal (IC2 {@code ItemBattery}). The RE battery merges IC2's separate
 * empty and charged items: without charge it stacks to 16 and shows no charge bar, as {@code itemBatREDischarged} did.
 */
public class BatteryItem extends Item implements ElectricItem {
    public static final int EMPTY_STACK_SIZE = 16;

    private final int maxCharge;
    private final int transferLimit;
    private final int tier;
    /** RE battery only: separate empty state and right click discharging into the hotbar. */
    private final boolean reBattery;

    public BatteryItem(Properties properties, int maxCharge, int transferLimit, int tier, boolean reBattery) {
        super(properties.stacksTo(reBattery ? EMPTY_STACK_SIZE : 1).setNoRepair());
        this.maxCharge = maxCharge;
        this.transferLimit = transferLimit;
        this.tier = tier;
        this.reBattery = reBattery;
    }

    @Override
    public int getMaxCharge() {
        return maxCharge;
    }

    @Override
    public int getTransferLimit() {
        return transferLimit;
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public boolean canProvideEnergy() {
        return true;
    }

    public boolean isEmptyState(ItemStack stack) {
        return reBattery && ElectricItems.getCharge(stack) <= 0;
    }

    @Override
    public void onChargeChanged(ItemStack stack, int charge) {
        if (reBattery) {
            if (charge > 0) {
                stack.set(DataComponents.MAX_STACK_SIZE, 1);
            } else {
                stack.remove(DataComponents.MAX_STACK_SIZE);
            }
        }
    }

    /** Icon step 0 (empty) … 4 (full), IC2 {@code getIconFromDamage}. */
    public int getIconLevel(ItemStack stack) {
        if (isEmptyState(stack)) {
            return 0;
        }
        int damage = getChargeDamage(stack);
        if (damage <= 1) {
            return 4;
        } else if (damage <= 8) {
            return 3;
        } else if (damage <= 14) {
            return 2;
        }
        return damage <= 20 ? 1 : 0;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !isEmptyState(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return ElectricItems.barWidth(this, stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ElectricItems.barColor(this, stack);
    }

    /** Charged RE battery: discharges into electric non-battery items of the hotbar. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && reBattery && !isEmptyState(stack)) {
            boolean transferred = false;
            for (int i = 0; i < Inventory.getSelectionSize(); i++) {
                ItemStack target = player.getInventory().getItem(i);
                if (target.getItem() instanceof ElectricItem item && !(target.getItem() instanceof BatteryItem)) {
                    int transfer = ElectricItems.discharge(stack, 2 * transferLimit, item.getTier(), true, true);
                    transfer = ElectricItems.charge(target, transfer, tier, true, false);
                    ElectricItems.discharge(stack, transfer, item.getTier(), true, false);
                    if (transfer > 0) {
                        transferred = true;
                    }
                }
            }
            if (transferred) {
                player.containerMenu.broadcastChanges();
            }
        }
        return InteractionResultHolder.pass(stack);
    }
}
