package dev.craft.gregtech.machine;

import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.recipe.GtRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/** {@code GT_MetaTileEntity_Centrifuge} (spec 13 §9): 32 EU/p, lava tank, lava items in the holo slot. */
public class CentrifugeBlockEntity extends GtTankMachineBlockEntity {
    public static final int SLOT_LAVA = SLOT_TANK;

    public CentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(GtMachine.INDUSTRIAL_CENTRIFUGE, pos, state);
    }

    @Override
    protected GtRecipe.Kind recipeKind() {
        return GtRecipe.Kind.CENTRIFUGE;
    }

    @Override
    protected Fluid tankFluid() {
        return Fluids.LAVA;
    }

    @Override
    protected Item holoItem() {
        return GtItems.LAVA.get();
    }

    @Override
    protected String storedLiquidKey() {
        return "mStoredLava";
    }
}
