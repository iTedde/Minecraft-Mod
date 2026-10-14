package dev.craft.ic2.machine;

import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.generator.FuelValues;
import dev.craft.ic2.menu.IronFurnaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Iron furnace, 1:1 with IC2 {@code TileEntityIronFurnace}: 160 ticks per item, burns only while it can smelt. */
public class IronFurnaceBlockEntity extends MachineBlockEntity {
    public static final int OPERATION_LENGTH = 160;

    private int fuel;
    private int maxFuel;
    private int progress;

    public IronFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.IRON_FURNACE), pos, state, 3);
    }

    public int getFuel() {
        return fuel;
    }

    public int getMaxFuel() {
        return maxFuel;
    }

    public int getProgress() {
        return progress;
    }

    @Override
    public void serverTick() {
        boolean wasOperating = isBurning();
        boolean needsInvUpdate = false;
        if (fuel <= 0 && canOperate()) {
            ItemStack fuelStack = inventory.get(1);
            fuel = maxFuel = FuelValues.getFuelValueFor(fuelStack);
            if (fuel > 0) {
                if (fuelStack.hasCraftingRemainingItem()) {
                    inventory.set(1, fuelStack.getCraftingRemainingItem());
                } else {
                    fuelStack.shrink(1);
                }
                needsInvUpdate = true;
            }
        }
        if (isBurning() && canOperate()) {
            progress++;
            if (progress >= OPERATION_LENGTH) {
                progress = 0;
                operate();
                needsInvUpdate = true;
            }
        } else {
            progress = 0;
        }
        if (fuel > 0) {
            fuel--;
        }
        if (wasOperating != isBurning()) {
            setActive(isBurning());
            needsInvUpdate = true;
        }
        if (needsInvUpdate) {
            onInventoryChanged();
        }
    }

    private void operate() {
        if (!canOperate()) {
            return;
        }
        ItemStack input = inventory.get(0);
        ItemStack result = ElectricFurnaceBlockEntity.getSmeltingResult(level, input);
        ItemStack output = inventory.get(2);
        if (output.isEmpty()) {
            inventory.set(2, result.copy());
        } else {
            output.grow(result.getCount());
        }
        if (input.hasCraftingRemainingItem()) {
            inventory.set(0, input.getCraftingRemainingItem());
        } else {
            input.shrink(1);
        }
    }

    public boolean isBurning() {
        return fuel > 0;
    }

    private boolean canOperate() {
        ItemStack input = inventory.get(0);
        if (input.isEmpty()) {
            return false;
        }
        ItemStack result = ElectricFurnaceBlockEntity.getSmeltingResult(level, input);
        if (result.isEmpty()) {
            return false;
        }
        ItemStack output = inventory.get(2);
        if (output.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(output, result)
                && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new IronFurnaceMenu(id, playerInventory, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fuel = tag.getInt("fuel");
        maxFuel = tag.getInt("maxFuel");
        progress = tag.getShort("progress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("fuel", fuel);
        tag.putInt("maxFuel", maxFuel);
        tag.putShort("progress", (short) progress);
    }
}
