package dev.craft.thermalexpansion.energy;

import dev.craft.thermalexpansion.TeBlockEntityTypes;
import dev.craft.thermalexpansion.machine.TeInputTanks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

/**
 * {@code TileEngineMagmatic}: 50 mB lava from its 4000 mB tank give 900 MJ ({@code core.lava.mj} 20 000 · 90 / 2000,
 * i.e. 18 000 MJ per bucket). Breaks only when its buffer is full.
 */
public class MagmaticEngineBlockEntity extends TeEngineBlockEntity {
    public static final int CAPACITY = 4000;
    private static final int LAVA_PER_PROCESS = 50;
    private static final int ENERGY_PER_PROCESS = 20000 * 90 / 2000;

    private final FluidTank tank = new FluidTank(CAPACITY, stack -> stack.getFluid().isSame(Fluids.LAVA)) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    public MagmaticEngineBlockEntity(BlockPos pos, BlockState state) {
        super(TeBlockEntityTypes.MAGMATIC_ENGINE.get(), TeEngineType.MAGMATIC, 0, pos, state);
    }

    @Override
    protected boolean canProcess() {
        return fuelMJ > 0.0F || tank.getFluidAmount() >= LAVA_PER_PROCESS;
    }

    @Override
    protected void processActive() {
        if (fuelMJ <= 0.0F) {
            fuelMJ += ENERGY_PER_PROCESS;
            tank.drain(LAVA_PER_PROCESS, IFluidHandler.FluidAction.EXECUTE);
        }
        if (fuelMJ > 0.0F) {
            float energyToAdd = getPower();
            addEnergy(energyToAdd);
            fuelMJ -= energyToAdd;
        }
        if (provider.getEnergyStored() >= provider.getMaxEnergyStored()) {
            shutdownEngine();
        }
    }

    @Override
    public float getDurationLevel() {
        return fuelMJ / ENERGY_PER_PROCESS;
    }

    @Override
    public FluidStack getTankFluid() {
        return tank.getFluid();
    }

    @Nullable
    @Override
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return side == getFacing() ? null : new TeInputTanks(true, tank);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tank.setFluid(new FluidStack(Fluids.LAVA, Math.min(tag.getInt("qty.lava"), CAPACITY)));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("qty.lava", tank.getFluidAmount());
    }
}
