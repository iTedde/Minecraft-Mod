package dev.craft.gregtech.machine;

import dev.craft.gregtech.GtBlockEntityTypes;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.recipe.GtRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** {@code GT_MetaTileEntity_Assembler}: two separated inputs, shapeless recipes, one output (spec 13 §6). */
public class AssemblerBlockEntity extends GtBasicMachineBlockEntity {
    public AssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(GtBlockEntityTypes.get(GtMachine.ASSEMBLING_MACHINE), pos, state);
    }

    @Override
    protected boolean hasTwoSeparateInputs() {
        return true;
    }

    @Override
    protected void checkRecipe() {
        moveStack(SLOT_OUTPUT_1, SLOT_OUTPUT_2);
        ItemStack input1 = inventory.get(SLOT_INPUT_1);
        ItemStack input2 = inventory.get(SLOT_INPUT_2);
        if (!input1.isEmpty() || !input2.isEmpty()) {
            GtRecipe recipe = GtRecipe.find(level, GtRecipe.Kind.ASSEMBLER, input1, input2,
                    GtRecipe.Kind.ASSEMBLER.isShapeless());
            if (recipe != null && spaceForOutput(recipe.getOutput(0), ItemStack.EMPTY)
                    && recipe.isInputEqual(true, true, input1, input2)) {
                inventory.set(SLOT_INPUT_1, input1.isEmpty() ? ItemStack.EMPTY : input1);
                inventory.set(SLOT_INPUT_2, input2.isEmpty() ? ItemStack.EMPTY : input2);
                euPerTick = recipe.getEuPerTick();
                maxProgressTime = recipe.getDuration();
                outputItem1 = recipe.getOutput(0).copy();
                return;
            }
        }
        outputItem1 = ItemStack.EMPTY;
    }
}
