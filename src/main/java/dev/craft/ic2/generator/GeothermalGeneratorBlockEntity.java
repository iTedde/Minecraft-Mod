package dev.craft.ic2.generator;

import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.menu.GeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * Geothermal generator, 1:1 with IC2 {@code TileEntityGeoGenerator}: 20 EU/t from lava, 1 mB per tick, 24000 mB
 * tank (the {@code fuel} field), 32 EU buffer.
 */
public class GeothermalGeneratorBlockEntity extends BaseGeneratorBlockEntity {
    public static final int PRODUCTION = 20;
    public static final int MAX_STORAGE = Math.max(PRODUCTION, 32);
    public static final int MAX_LAVA = 24000;
    public static final int LAVA_PER_CONTAINER = 1000;

    private final IFluidHandler fluidHandler = new LavaTank();

    public GeothermalGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.GEOTHERMAL_GENERATOR), pos, state, 2, PRODUCTION, MAX_STORAGE);
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    public int gaugeFuelScaled(int scale) {
        return fuel <= 0 ? 0 : fuel * scale / MAX_LAVA;
    }

    @Override
    public boolean gainFuel() {
        ItemStack stack = inventory.get(1);
        if (stack.isEmpty() || MAX_LAVA - fuel < LAVA_PER_CONTAINER) {
            return false;
        }
        if (stack.is(Items.LAVA_BUCKET)) {
            fuel += LAVA_PER_CONTAINER;
            inventory.set(1, stack.transmuteCopy(Items.BUCKET, stack.getCount()));
            return true;
        }
        if (isLavaCell(stack)) {
            fuel += LAVA_PER_CONTAINER;
            stack.shrink(1);
            inventory.set(1, stack.isEmpty() ? ItemStack.EMPTY : stack);
            return true;
        }
        if (FuelValues.containsLava(stack)) {
            fuel += LAVA_PER_CONTAINER;
            inventory.set(1, consumeOne(stack));
            return true;
        }
        return false;
    }

    /** Always tries to refuel (only succeeds with at least 1000 mB free space). */
    @Override
    public boolean needsFuel() {
        return fuel <= MAX_LAVA;
    }

    /**
     * Lava delivered by an adjacent IC2 pump: takes up to the free tank space but only credits half of it.
     *
     * @return lava not taken
     */
    public int distributeLava(int amount) {
        int need = Math.min(MAX_LAVA - fuel, amount);
        amount -= need;
        fuel += need / 2;
        return amount;
    }

    @Override
    public SoundEvent getOperationSound() {
        return Ic2Sounds.GEOTHERMAL_LOOP.get();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new GeneratorMenu(id, playerInventory, this);
    }

    /** IC2 {@code ITankContainer}: accepts lava from all sides, never gives any back. */
    private final class LavaTank implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return fuel > 0 ? new FluidStack(Fluids.LAVA, fuel) : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return MAX_LAVA;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return stack.getFluid().isSame(Fluids.LAVA);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!isFluidValid(0, resource)) {
                return 0;
            }
            int toAdd = Math.min(resource.getAmount(), MAX_LAVA - fuel);
            if (action.execute()) {
                fuel += toAdd;
                setChanged();
            }
            return toAdd;
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
