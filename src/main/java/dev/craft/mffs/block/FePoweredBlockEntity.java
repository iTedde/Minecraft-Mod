package dev.craft.mffs.block;

import dev.craft.mffs.energy.PowerLinkItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * {@code TileEntityFEPoweredMachine} (spec 48 §6.2): a machine whose force energy comes from the {@link PowerLinkItem}
 * in its power link slot.
 */
public abstract class FePoweredBlockEntity extends MffsMachineBlockEntity {
    protected FePoweredBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int inventorySize) {
        super(type, pos, state, inventorySize);
    }

    public abstract int getPowerLinkSlot();

    public ItemStack getPowerLinkStack() {
        return getStack(getPowerLinkSlot());
    }

    public boolean hasPowerSource() {
        return getPowerLinkStack().getItem() instanceof PowerLinkItem;
    }

    private PowerLinkItem link() {
        return (PowerLinkItem) getPowerLinkStack().getItem();
    }

    public int getPercentageCapacity() {
        return hasPowerSource() ? link().getPercentageCapacity(getPowerLinkStack(), this) : 0;
    }

    public int getAvailablePower() {
        return hasPowerSource() ? link().getAvailablePower(getPowerLinkStack(), this) : 0;
    }

    public int getPowerSourceId() {
        return hasPowerSource() ? link().getPowerSourceId(getPowerLinkStack(), this) : 0;
    }

    public int getMaximumPower() {
        return hasPowerSource() ? link().getMaximumPower(getPowerLinkStack(), this) : 0;
    }

    public boolean consumePower(int amount, boolean simulate) {
        return hasPowerSource() && link().consumePower(getPowerLinkStack(), amount, simulate, this);
    }

    public boolean emitPower(int amount, boolean simulate) {
        return hasPowerSource() && link().insertPower(getPowerLinkStack(), amount, simulate, this);
    }

    public boolean isPowerSourceItem() {
        return hasPowerSource() && link().isPowerSourceItem();
    }
}
