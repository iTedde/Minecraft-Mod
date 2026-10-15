package dev.craft.ic2.item;

import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.Ic2Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

/**
 * IC2 {@code ItemFuelCanFilled}: fuel can filled by the canning machine; burns for {@code min(32767, value · 2)}
 * ticks in any furnace or generator and leaves the empty can behind.
 */
public class FilledFuelCanItem extends Item {
    public FilledFuelCanItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(int value) {
        ItemStack stack = new ItemStack(Ic2Items.FILLED_FUEL_CAN.get());
        stack.set(Ic2DataComponents.FUEL_VALUE.get(), value);
        return stack;
    }

    public static int getValue(ItemStack stack) {
        return stack.getOrDefault(Ic2DataComponents.FUEL_VALUE.get(), 0);
    }

    @Override
    public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
        return Math.min(32767, getValue(stack) * 2);
    }
}
