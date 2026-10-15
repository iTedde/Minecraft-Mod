package dev.craft.gregtech.machine;

import dev.craft.gregtech.GtBlockEntityTypes;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.recipe.GtRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** {@code GT_MetaTileEntity_Bender}: one input (slot 2), ingot → plate (spec 13 §2). */
public class BenderBlockEntity extends GtBasicMachineBlockEntity {
    public BenderBlockEntity(BlockPos pos, BlockState state) {
        super(GtBlockEntityTypes.get(GtMachine.PLATE_BENDING_MACHINE), pos, state);
    }

    @Override
    protected void checkRecipe() {
        moveStack(SLOT_INPUT_1, SLOT_INPUT_2);
        moveStack(SLOT_OUTPUT_1, SLOT_OUTPUT_2);
        ItemStack input = inventory.get(SLOT_INPUT_2);
        if (!input.isEmpty()) {
            GtRecipe recipe = GtRecipe.find(level, GtRecipe.Kind.BENDER, input, ItemStack.EMPTY,
                    GtRecipe.Kind.BENDER.isShapeless());
            if (recipe != null && spaceForOutput(recipe.getOutput(0), ItemStack.EMPTY)
                    && recipe.isInputEqual(true, true, input, ItemStack.EMPTY)) {
                inventory.set(SLOT_INPUT_2, input.isEmpty() ? ItemStack.EMPTY : input);
                euPerTick = recipe.getEuPerTick();
                maxProgressTime = recipe.getDuration();
                outputItem1 = recipe.getOutput(0).copy();
                return;
            }
        }
        outputItem1 = ItemStack.EMPTY;
    }
}
