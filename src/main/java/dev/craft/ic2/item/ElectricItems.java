package dev.craft.ic2.item;

import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.Ic2Items;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

/** Charge handling of electric items, 1:1 with IC2 {@code ElectricItem}. */
public final class ElectricItems {
    /** Max damage IC2 electric items use for their charge display. */
    public static final int DISPLAY_MAX_DAMAGE = 27;

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD
    };

    private ElectricItems() {
    }

    public static boolean isElectric(ItemStack stack) {
        return stack.getItem() instanceof ElectricItem;
    }

    /**
     * What a machine battery slot can use ({@code TileEntityElecMachine.provideEnergy}): dischargeable electric items,
     * redstone and single-use batteries.
     */
    public static boolean isMachineEnergySource(ItemStack stack) {
        return stack.getItem() instanceof ElectricItem item && item.canProvideEnergy()
                || stack.is(Items.REDSTONE) || stack.is(Ic2Items.SU_BATTERY.get());
    }

    public static int getCharge(ItemStack stack) {
        return stack.getOrDefault(Ic2DataComponents.CHARGE.get(), 0);
    }

    private static void setCharge(ItemStack stack, ElectricItem item, int charge) {
        if (charge > 0) {
            stack.set(Ic2DataComponents.CHARGE.get(), charge);
        } else {
            stack.remove(Ic2DataComponents.CHARGE.get());
        }
        item.onChargeChanged(stack, charge);
    }

    /**
     * Charges {@code stack}.
     *
     * @return EU actually (or, when simulating, possibly) transferred
     */
    public static int charge(ItemStack stack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
        if (!(stack.getItem() instanceof ElectricItem item) || amount < 0 || stack.getCount() > 1
                || item.getTier() > tier) {
            return 0;
        }
        if (amount > item.getTransferLimit() && !ignoreTransferLimit) {
            amount = item.getTransferLimit();
        }
        int charge = getCharge(stack);
        if (amount > item.getMaxCharge() - charge) {
            amount = item.getMaxCharge() - charge;
        }
        if (!simulate) {
            setCharge(stack, item, charge + amount);
        }
        return amount;
    }

    /**
     * Discharges {@code stack}.
     *
     * @return EU actually (or, when simulating, possibly) transferred
     */
    public static int discharge(ItemStack stack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
        if (!(stack.getItem() instanceof ElectricItem item) || amount < 0 || stack.getCount() > 1
                || item.getTier() > tier) {
            return 0;
        }
        if (amount > item.getTransferLimit() && !ignoreTransferLimit) {
            amount = item.getTransferLimit();
        }
        int charge = getCharge(stack);
        if (amount > charge) {
            amount = charge;
        }
        if (!simulate) {
            setCharge(stack, item, charge - amount);
        }
        return amount;
    }

    public static boolean canUse(ItemStack stack, int amount) {
        return isElectric(stack) && getCharge(stack) >= amount;
    }

    /**
     * Uses {@code amount} EU if fully available, recharging from armor before and after. Without a player (IC2
     * passes {@code null}, e.g. for the miner) nothing is recharged.
     */
    public static boolean use(ItemStack stack, int amount, @Nullable Player player) {
        if (player != null && player.level().isClientSide() || !isElectric(stack)) {
            return false;
        }
        chargeFromArmor(stack, player);
        if (discharge(stack, amount, Integer.MAX_VALUE, true, true) == amount) {
            discharge(stack, amount, Integer.MAX_VALUE, true, false);
            chargeFromArmor(stack, player);
            return true;
        }
        return false;
    }

    /** Tops up {@code stack} from worn armor that provides energy and has at least the item's tier. */
    public static void chargeFromArmor(ItemStack stack, @Nullable Player player) {
        if (player == null || player.level().isClientSide() || !(stack.getItem() instanceof ElectricItem item)) {
            return;
        }
        boolean changed = false;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armor = player.getItemBySlot(slot);
            if (armor.getItem() instanceof ElectricItem armorItem && armorItem.canProvideEnergy()
                    && armorItem.getTier() >= item.getTier()) {
                int transfer = charge(stack, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true);
                transfer = discharge(armor, transfer, Integer.MAX_VALUE, true, false);
                if (transfer > 0) {
                    charge(stack, transfer, Integer.MAX_VALUE, true, false);
                    changed = true;
                }
            }
        }
        if (changed) {
            player.containerMenu.broadcastChanges();
        }
    }

    /** Charge bar width as IC2 shows it through the item damage. */
    public static int barWidth(ElectricItem item, ItemStack stack) {
        return Math.round(13.0F - item.getChargeDamage(stack) * 13.0F / DISPLAY_MAX_DAMAGE);
    }

    public static int barColor(ElectricItem item, ItemStack stack) {
        float fraction = 1.0F - (float) item.getChargeDamage(stack) / DISPLAY_MAX_DAMAGE;
        return Mth.hsvToRgb(Math.max(0.0F, fraction) / 3.0F, 1.0F, 1.0F);
    }

    /** A fully charged copy of {@code stack}, e.g. for creative tabs. */
    public static ItemStack charged(ItemStack stack) {
        ItemStack copy = stack.copyWithCount(1);
        charge(copy, Integer.MAX_VALUE, Integer.MAX_VALUE, true, false);
        return copy;
    }
}
