package dev.craft.thermalexpansion.machine;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * {@code TileIceGen} and {@code TileRockGen}: one of three products is chosen in the GUI ({@code curSelection}); the
 * running operation finishes with the product it started with ({@code prevSelection}). Slot 0 is the output, ejected
 * to side group 2; group 1 takes liquids (4000 mB tanks), group 3 is open.
 */
public abstract class SelectionMachineBlockEntity extends TeMachineBlockEntity {
    /** {@code maxLiquid}/{@code MAX_LIQUID}. */
    public static final int CAPACITY = 4000;
    protected static final int GROUP_INPUT = 1;
    private static final int GROUP_OUTPUT = 2;

    protected byte curSelection;
    protected byte prevSelection;
    private int outputTracker;

    protected SelectionMachineBlockEntity(BlockEntityType<?> blockEntityType, TeMachineType type, BlockPos pos,
                                          BlockState state) {
        super(blockEntityType, type, pos, state);
    }

    /** {@code processItems} of the machine type, for the GUI. */
    public static List<ItemStack> products(TeMachineType type) {
        return type == TeMachineType.IGNEOUS_EXTRUDER ? IgneousExtruderBlockEntity.PRODUCTS
                : GlacialPrecipitatorBlockEntity.PRODUCTS;
    }

    protected ItemStack product(int selection) {
        return products(type).get(selection);
    }

    protected static FluidTank tank(Fluid fluid, Runnable onChanged) {
        return new FluidTank(CAPACITY, stack -> stack.getFluid().isSame(fluid)) {
            @Override
            protected void onContentsChanged() {
                onChanged.run();
            }
        };
    }

    /** The mode packet: the new selection applies to the running operation only if none is running. */
    public void setSelection(int selection) {
        curSelection = (byte) selection;
        if (processCur == 0.0F) {
            prevSelection = curSelection;
        }
        setChanged();
    }

    /** Bits 0–1 {@code curSelection}, bits 2–3 {@code prevSelection}. */
    @Override
    public int getModeBits() {
        return curSelection | prevSelection << 2;
    }

    @Override
    public boolean acceptsContainers() {
        return true;
    }

    @Override
    protected void processFinish() {
        addToSlot(0, product(prevSelection).copy());
        prevSelection = curSelection;
    }

    @Override
    protected void transferProducts() {
        for (int i = outputTracker + 1; i <= outputTracker + 6; i++) {
            int side = i % 6;
            if (sides[side] == GROUP_OUTPUT && ejectItem(0, 4, Direction.from3DDataValue(side))) {
                outputTracker = side;
                break;
            }
        }
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new TeMachineMenu(id, playerInventory, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        outputTracker = tag.getInt("output");
        prevSelection = (byte) Math.floorMod(tag.getByte("selection.prev"), 3);
        curSelection = (byte) Math.floorMod(tag.getByte("selection.cur"), 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("output", outputTracker);
        tag.putByte("selection.prev", prevSelection);
        tag.putByte("selection.cur", curSelection);
    }
}
