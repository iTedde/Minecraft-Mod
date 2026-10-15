package dev.craft.buildcraft.engine;

import dev.craft.buildcraft.menu.EngineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

/**
 * Combustion engine, 1:1 with BuildCraft {@code EngineIron}: 10000 mB fuel and coolant tanks, burns only while
 * powered. Heat rises by the fuel's MJ per tick; above 49000 coolant is used, above 100000 it explodes on the next
 * energy gain. Losing the signal while running causes a 600 tick cooling penalty that only counts down at heat 0.
 */
public class CombustionEngineBlockEntity extends EngineBlockEntity implements MenuProvider {
    public static final int MAX_LIQUID = 10000;
    public static final int MAX_HEAT = 100000;
    public static final int COOLANT_THRESHOLD = 49000;
    private static final float[] PISTON_SPEEDS = {0.04F, 0.05F, 0.06F, 0.07F};

    private final FluidTank fuelTank = new FluidTank(MAX_LIQUID, stack -> CombustionFuels.getFuel(stack) != null);
    private final FluidTank coolantTank =
            new FluidTank(MAX_LIQUID, stack -> CombustionFuels.getCoolant(stack) != null);
    private final IFluidHandler fluidHandler = new Tanks();
    private int burnTime;
    private int heat;
    @Nullable
    private CombustionFuels.Fuel currentFuel;
    private int penaltyCooling;
    private boolean lastPowered;

    public CombustionEngineBlockEntity(BlockPos pos, BlockState state) {
        super(EngineType.COMBUSTION, pos, state, 100000, 500, 1);
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    public FluidTank getFuelTank() {
        return fuelTank;
    }

    public FluidTank getCoolantTank() {
        return coolantTank;
    }

    @Override
    public int explosionRange() {
        return 8;
    }

    @Override
    public int maxEnergyReceived() {
        return 2000;
    }

    @Override
    protected float[] pistonSpeeds() {
        return PISTON_SPEEDS;
    }

    @Override
    public boolean isBurning() {
        return !fuelTank.isEmpty() && penaltyCooling == 0 && isRedstonePowered();
    }

    @Override
    protected void burn() {
        currentOutput = 0.0F;
        FluidStack fuel = fuelTank.getFluid();
        if (currentFuel == null) {
            currentFuel = CombustionFuels.getFuel(fuel);
        }
        if (currentFuel == null) {
            return;
        }
        if (penaltyCooling <= 0 && isRedstonePowered()) {
            lastPowered = true;
            if (burnTime > 0 || !fuel.isEmpty()) {
                if (burnTime > 0) {
                    burnTime--;
                }
                if (burnTime <= 0) {
                    if (fuel.isEmpty()) {
                        currentFuel = null;
                        return;
                    }
                    fuel.shrink(1);
                    if (fuel.isEmpty()) {
                        fuelTank.setFluid(FluidStack.EMPTY);
                    }
                    burnTime = currentFuel.totalBurningTime() / 1000;
                }
                float power = currentFuel.powerPerCycle();
                currentOutput = power;
                addEnergy(power);
                heat = (int) (heat + power);
            }
        } else if (penaltyCooling <= 0 && lastPowered) {
            lastPowered = false;
            penaltyCooling = 600;
        }
    }

    @Override
    protected void update() {
        super.update();
        ItemStack stack = inventory.getStackInSlot(0);
        if (!stack.isEmpty()) {
            FluidStack liquid = stack.is(Items.ICE) && heat > COOLANT_THRESHOLD
                    ? new FluidStack(Fluids.WATER, 1000)
                    : CombustionFuels.getFluidForFilledItem(stack);
            if (!liquid.isEmpty() && fill(liquid, IFluidHandler.FluidAction.SIMULATE) == liquid.getAmount()) {
                fill(liquid, IFluidHandler.FluidAction.EXECUTE);
                inventory.setStackInSlot(0, consumeItem(stack));
            }
        }
        if (heat > COOLANT_THRESHOLD) {
            int extraHeat = heat - COOLANT_THRESHOLD;
            FluidStack coolant = coolantTank.getFluid();
            Float coolingPerUnit = CombustionFuels.getCoolant(coolant);
            if (coolingPerUnit != null) {
                if (coolant.getAmount() * coolingPerUnit > extraHeat) {
                    coolant.shrink(Math.round(extraHeat / coolingPerUnit));
                    heat = COOLANT_THRESHOLD;
                } else {
                    heat = (int) (heat - coolant.getAmount() * coolingPerUnit);
                    coolantTank.setFluid(FluidStack.EMPTY);
                }
            }
        }
        if (heat > 0 && (penaltyCooling > 0 || !isRedstonePowered())) {
            heat -= 10;
        }
        if (heat <= 0) {
            heat = 0;
        }
        if (heat == 0 && penaltyCooling > 0) {
            penaltyCooling--;
        }
    }

    @Override
    protected EnergyStage computeEnergyStage() {
        if (heat <= MAX_HEAT / 4) {
            return EnergyStage.BLUE;
        }
        if (heat <= MAX_HEAT / 2) {
            return EnergyStage.GREEN;
        }
        if (heat <= MAX_HEAT * 3.0F / 4.0F) {
            return EnergyStage.YELLOW;
        }
        if (heat <= MAX_HEAT) {
            return EnergyStage.RED;
        }
        return EnergyStage.EXPLOSION;
    }

    @Override
    protected boolean isEngineActive() {
        return penaltyCooling <= 0;
    }

    @Override
    public int getHeat() {
        return heat;
    }

    public int getPenaltyCooling() {
        return penaltyCooling;
    }

    /** BuildCraft {@code fill}: coolants go into the coolant tank, fuels into the fuel tank, from every side. */
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (CombustionFuels.getCoolant(resource) != null) {
            return coolantTank.fill(resource, action);
        }
        return CombustionFuels.getFuel(resource) != null ? fuelTank.fill(resource, action) : 0;
    }

    @Override
    public void onBlockBreak() {
        // BuildCraft's EngineIron.delete() drops nothing: the slot content and the tanks are lost.
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!fuelTank.isEmpty()) {
            tag.put("fuelTank", fuelTank.getFluid().save(registries));
        }
        if (!coolantTank.isEmpty()) {
            tag.put("coolantTank", coolantTank.getFluid().save(registries));
        }
        tag.putInt("burnTime", burnTime);
        tag.putInt("heat", heat);
        tag.putInt("penaltyCooling", penaltyCooling);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fuelTank.setFluid(FluidStack.parseOptional(registries, tag.getCompound("fuelTank")));
        coolantTank.setFluid(FluidStack.parseOptional(registries, tag.getCompound("coolantTank")));
        burnTime = tag.getInt("burnTime");
        heat = tag.getInt("heat");
        penaltyCooling = tag.getInt("penaltyCooling");
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new EngineMenu(id, playerInventory, this);
    }

    /** Forge {@code ITankContainer}: fill from every side, no draining. */
    private final class Tanks implements IFluidHandler {
        @Override
        public int getTanks() {
            return 2;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return (tank == 0 ? fuelTank : coolantTank).getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return MAX_LIQUID;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return (tank == 0 ? fuelTank : coolantTank).isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            int filled = CombustionEngineBlockEntity.this.fill(resource, action);
            if (filled > 0 && action.execute()) {
                setChanged();
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
}
