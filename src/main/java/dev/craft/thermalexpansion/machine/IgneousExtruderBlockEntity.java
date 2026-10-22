package dev.craft.thermalexpansion.machine;

import java.util.List;

import dev.craft.thermalexpansion.TeBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

/**
 * {@code TileRockGen}: mixes lava and water without power – cobblestone (40 ticks, nothing used), stone (80 ticks,
 * 2 mB lava + 1000 mB water) or obsidian (120 ticks, 1000 mB each; pack config {@code factory.rockGen.*}). An original
 * quirk: it only starts with at least 1000 mB in both tanks, whatever the selection.
 */
public class IgneousExtruderBlockEntity extends SelectionMachineBlockEntity {
    static final List<ItemStack> PRODUCTS = List.of(new ItemStack(Items.COBBLESTONE), new ItemStack(Items.STONE),
            new ItemStack(Items.OBSIDIAN));
    private static final int[] PROCESS_LAVA = {0, 2, 1000};
    private static final int[] PROCESS_WATER = {0, 1000, 1000};
    private static final int[] PROCESS_TIME = {40, 80, 120};
    public static final int START_LIQUID = 1000;

    private final FluidTank lavaTank = tank(Fluids.LAVA, this::setChanged);
    private final FluidTank waterTank = tank(Fluids.WATER, this::setChanged);

    public IgneousExtruderBlockEntity(BlockPos pos, BlockState state) {
        super(TeBlockEntityTypes.IGNEOUS_EXTRUDER.get(), TeMachineType.IGNEOUS_EXTRUDER, pos, state);
    }

    /** Lava in mB used for product {@code selection}. */
    public static int processLava(int selection) {
        return PROCESS_LAVA[selection];
    }

    /** Water in mB used for product {@code selection}. */
    public static int processWater(int selection) {
        return PROCESS_WATER[selection];
    }

    /** Ticks for product {@code selection}. */
    public static int processTime(int selection) {
        return PROCESS_TIME[selection];
    }

    @Override
    protected boolean canStart() {
        if (lavaTank.getFluidAmount() < START_LIQUID || waterTank.getFluidAmount() < START_LIQUID) {
            return false;
        }
        ItemStack output = inventory.get(0);
        if (output.isEmpty()) {
            return true;
        }
        ItemStack product = product(curSelection);
        return output.is(product.getItem()) && output.getCount() + product.getCount() <= product.getMaxStackSize();
    }

    @Override
    protected boolean canFinish() {
        if (processCur < processEnd) {
            return false;
        }
        ItemStack output = inventory.get(0);
        return output.isEmpty()
                || output.is(product(prevSelection).getItem()) && output.getCount() < output.getMaxStackSize();
    }

    @Override
    protected void processStart() {
        processEnd = PROCESS_TIME[curSelection];
        lavaTank.drain(PROCESS_LAVA[curSelection], IFluidHandler.FluidAction.EXECUTE);
        waterTank.drain(PROCESS_WATER[curSelection], IFluidHandler.FluidAction.EXECUTE);
        prevSelection = curSelection;
    }

    /** {@code TileRockGen.updateEntity}: one tick of progress per tick. */
    @Override
    public void serverTick() {
        boolean curActive = active;
        if (active) {
            if (processCur < processEnd) {
                processCur++;
            }
            if (canFinish()) {
                processFinish();
                transferProducts();
                processCur = 0.0F;
                if (redstoneControlOrDisable() && canStart()) {
                    processStart();
                } else {
                    markInactive();
                }
            }
        } else if (redstoneControlOrDisable()) {
            if (level.getGameTime() % 40L == 0L && !inventory.get(0).isEmpty()) {
                transferProducts();
            }
            if (canStart()) {
                processStart();
                processCur++;
                active = true;
            }
        }
        updateIfChanged(curActive);
        setChanged();
    }

    @Nullable
    @Override
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return side == null || getSide(side) == GROUP_INPUT ? new TeInputTanks(true, lavaTank, waterTank) : null;
    }

    /** 0 water (gauge on the right), 1 lava (gauge on the left). */
    @Override
    public FluidStack getDisplayFluid(int index) {
        return switch (index) {
            case 0 -> waterTank.getFluid();
            case 1 -> lavaTank.getFluid();
            default -> FluidStack.EMPTY;
        };
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        lavaTank.setFluid(new FluidStack(Fluids.LAVA, Math.min(tag.getInt("qty.lava"), CAPACITY)));
        waterTank.setFluid(new FluidStack(Fluids.WATER, Math.min(tag.getInt("qty.water"), CAPACITY)));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("qty.lava", lavaTank.getFluidAmount());
        tag.putInt("qty.water", waterTank.getFluidAmount());
    }
}
