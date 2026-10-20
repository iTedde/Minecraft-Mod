package dev.craft.advancedmachines;

import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.recipe.MachineRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * {@code TileEntityRotaryMacerator} (spec 25 §4): macerator recipes plus the supplement slot 8 that turns iron dust
 * into refined iron dust (coal), copper dust into bronze dust (tin) and coal dust into hydrated coal dust (water cell).
 */
public class RotaryMaceratorBlockEntity extends AdvancedMachineBlockEntity {
    public static final int SLOT_SUPPLEMENT = 8;
    public static final int ITEMS_PER_COAL = 128;
    public static final int ITEMS_PER_TIN = 4;
    public static final int ITEMS_PER_WATER_CELL = 8;

    /** Not saved, as in the original. */
    private int supplementedItemsLeft;
    private int currentResultCount;

    public RotaryMaceratorBlockEntity(BlockPos pos, BlockState state) {
        super(AdvancedMachineType.ROTARY_MACERATOR, pos, state);
    }

    public int getSupplementedItemsLeft() {
        return supplementedItemsLeft;
    }

    @Nullable
    @Override
    public ItemStack getResultFor(ItemStack input, boolean adjustInput) {
        ItemStack result = super.getResultFor(input, adjustInput);
        ItemStack supplement = inventory.get(SLOT_SUPPLEMENT).copy();
        if (!supplement.isEmpty()) {
            if (supplementedItemsLeft > 0) {
                result = getSpecialResultFor(result, supplement, adjustInput);
            } else {
                ItemStack special = getSpecialResultFor(result, supplement, adjustInput);
                if (special != null) {
                    result = special;
                    supplementedItemsLeft = currentResultCount;
                }
            }
        }
        return result;
    }

    @Override
    protected void onFinishedProcessingItem() {
        if (supplementedItemsLeft != 0) {
            if (supplementedItemsLeft == 1) {
                ItemStack supplement = inventory.get(SLOT_SUPPLEMENT);
                supplement.shrink(1);
                inventory.set(SLOT_SUPPLEMENT, supplement.isEmpty() ? ItemStack.EMPTY : supplement);
            }
            supplementedItemsLeft--;
        }
    }

    /** {@code getSpecialResultFor}: the supplement's own macerator result decides the bronze case. */
    @Nullable
    private ItemStack getSpecialResultFor(@Nullable ItemStack result, ItemStack supplement, boolean adjust) {
        if (result == null) {
            return null;
        }
        // The original compares item ids, which survive the lookup shrinking the supplement copy to 0.
        ItemStack supplementOutput = MachineRecipe.getOutputFor(level, MachineRecipe.Kind.MACERATOR,
                supplement.copy(), adjust);
        // Item id check in the original: coal and charcoal (both item "coal" in 1.4.7).
        if (result.is(Ic2Items.IRON_DUST.get()) && (supplement.is(Items.COAL) || supplement.is(Items.CHARCOAL))) {
            currentResultCount = ITEMS_PER_COAL;
            return new ItemStack(AdvancedMachinesItems.REFINED_IRON_DUST.get(), result.getCount());
        }
        if (result.is(Ic2Items.COPPER_DUST.get()) && supplementOutput.is(Ic2Items.TIN_DUST.get())) {
            currentResultCount = ITEMS_PER_TIN;
            return new ItemStack(Ic2Items.BRONZE_DUST.get(), result.getCount());
        }
        if (result.is(Ic2Items.COAL_DUST.get()) && supplement.is(Ic2Items.WATER_CELL.get())) {
            currentResultCount = ITEMS_PER_WATER_CELL;
            return new ItemStack(Ic2Items.HYDRATED_COAL_DUST.get());
        }
        return null;
    }
}
