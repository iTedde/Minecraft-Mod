package dev.craft.ic2.menu;

import java.util.function.Predicate;

import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.UpgradeModuleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** GUI slot filters of IC2 ({@code SlotCharge}, {@code SlotDischarge}, {@code SlotOutput}, upgrade slots). */
public final class SlotFilters {
    public static final Predicate<ItemStack> ANY = stack -> true;
    public static final Predicate<ItemStack> NONE = stack -> false;
    public static final Predicate<ItemStack> UPGRADE = stack -> stack.getItem() instanceof UpgradeModuleItem;

    private SlotFilters() {
    }

    /** {@code SlotCharge}: electric items up to {@code tier}. */
    public static Predicate<ItemStack> charge(int tier) {
        return stack -> stack.getItem() instanceof ElectricItem item && item.getTier() <= tier;
    }

    /** {@code SlotDischarge}: redstone, single-use batteries and energy-providing electric items up to {@code tier}. */
    public static Predicate<ItemStack> discharge(int tier) {
        return stack -> stack.is(Items.REDSTONE) || stack.is(Ic2Items.SU_BATTERY.get())
                || stack.getItem() instanceof ElectricItem item && item.canProvideEnergy() && item.getTier() <= tier;
    }
}
