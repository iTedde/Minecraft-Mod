package dev.craft.powerplants.block;

import dev.craft.core.multiblock.MultiblockLease;
import dev.craft.powerplants.PpBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Casing part of a power plant multiblock. The controller leases it on every structure check; while leased, item and
 * fluid access on the part is forwarded to the controller.
 */
public class PlantPartBlockEntity extends BlockEntity {
    private final MultiblockLease lease = new MultiblockLease();

    public PlantPartBlockEntity(BlockPos pos, BlockState state) {
        super(PpBlockEntityTypes.PLANT_PART.get(), pos, state);
    }

    public MultiblockLease getLease() {
        return lease;
    }

    /** The controller that currently holds this part, or {@code null}. */
    @Nullable
    public PlantControllerBlockEntity getController() {
        if (level == null) {
            return null;
        }
        BlockPos owner = lease.getOwner(level.getGameTime());
        if (owner != null && level.isLoaded(owner)
                && level.getBlockEntity(owner) instanceof PlantControllerBlockEntity controller
                && controller.isFormed()) {
            return controller;
        }
        return null;
    }

    @Nullable
    public IItemHandler getItemHandler(@Nullable Direction side) {
        PlantControllerBlockEntity controller = getController();
        return controller == null ? null : controller.getPartItemHandler(side);
    }

    @Nullable
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        PlantControllerBlockEntity controller = getController();
        return controller == null ? null : controller.getPartFluidHandler(side);
    }
}
