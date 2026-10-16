package dev.craft.gregtech.machine;

import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.recipe.GtRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/** {@code GT_MetaTileEntity_Electrolyzer} (spec 13 §4): 128 EU/p, water tank, water items in the holo slot. */
public class ElectrolyzerBlockEntity extends GtTankMachineBlockEntity {
    public static final int SLOT_WATER = SLOT_TANK;

    public ElectrolyzerBlockEntity(BlockPos pos, BlockState state) {
        super(GtMachine.INDUSTRIAL_ELECTROLYZER, pos, state);
    }

    @Override
    protected GtRecipe.Kind recipeKind() {
        return GtRecipe.Kind.ELECTROLYZER;
    }

    @Override
    protected Fluid tankFluid() {
        return Fluids.WATER;
    }

    @Override
    protected Item holoItem() {
        return GtItems.WATER.get();
    }

    @Override
    protected String storedLiquidKey() {
        return "mStoredWater";
    }
}
