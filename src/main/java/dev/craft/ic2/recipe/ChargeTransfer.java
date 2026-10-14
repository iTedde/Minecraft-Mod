package dev.craft.ic2.recipe;

import dev.craft.ic2.item.ElectricItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

/** IC2 crafting: the summed charge of electric ingredients goes into an electric result. */
final class ChargeTransfer {
    private ChargeTransfer() {
    }

    static ItemStack apply(ItemStack result, CraftingInput input) {
        if (!ElectricItems.isElectric(result)) {
            return result;
        }
        int charge = 0;
        for (ItemStack stack : input.items()) {
            if (ElectricItems.isElectric(stack)) {
                charge += ElectricItems.discharge(stack, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true);
            }
        }
        ElectricItems.charge(result, charge, Integer.MAX_VALUE, true, false);
        return result;
    }
}
