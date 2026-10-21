package dev.craft.thermalexpansion.energy;

import java.util.Map;

import dev.craft.gregtech.GtItems;
import dev.craft.ic2.Ic2Items;
import dev.craft.powerplants.PpItems;
import dev.craft.thermalexpansion.TeBlockEntityTypes;
import dev.craft.thermalexpansion.machine.TeInputTanks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

/**
 * {@code TileEngineSteam}: solid fuel and water (4000 mB tank). Needs at least 20 mB to start or keep running; every
 * 40 ticks it uses {@code power · 10} mB. Fuel values in MJ ({@code getItemEnergyValue}, spec 26 §6.1).
 */
public class SteamEngineBlockEntity extends TeEngineBlockEntity {
    public static final int CAPACITY = 4000;
    private static final int MIN_WATER = 20;
    /**
     * {@code GameRegistry.getFuelValue}: only the fuel handlers of the pack's mods count (IC2, GregTech; Forestry peat
     * does not exist in Craft), not the vanilla burn times; plus Craft's coke (spec 14).
     */
    private static final Map<Item, Integer> HANDLER_FUELS = Map.ofEntries(
            Map.entry(Ic2Items.RUBBER_SAPLING.get(), 80), Map.entry(Items.SUGAR_CANE, 50),
            Map.entry(Items.CACTUS, 50), Map.entry(Ic2Items.SCRAP.get(), 350), Map.entry(Ic2Items.SCRAP_BOX.get(), 3150),
            Map.entry(Ic2Items.LAVA_CELL.get(), 20000), Map.entry(GtItems.WOOD_PULP.get(), 100),
            Map.entry(GtItems.WOOD_PLATE.get(), 1600), Map.entry(PpItems.COKE.get(), 1600),
            Map.entry(PpItems.COKE_BLOCK.get(), 16000));

    private final FluidTank tank = new FluidTank(CAPACITY, stack -> stack.getFluid().isSame(Fluids.WATER)) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private int currentFuelMJ = 4800;

    public SteamEngineBlockEntity(BlockPos pos, BlockState state) {
        super(TeBlockEntityTypes.STEAM_ENGINE.get(), TeEngineType.STEAM, 1, pos, state);
    }

    /**
     * {@code getItemEnergyValue}: coal 4800, charcoal 3200, wooden blocks 450, sticks and saplings 150, otherwise the
     * mod fuel handlers · 3/2. Signs are items in 1.4.7 (GregTech handler 600), wooden doors too (no fuel).
     */
    public static int getItemEnergyValue(ItemStack fuel) {
        if (fuel.isEmpty()) {
            return 0;
        }
        if (fuel.is(Items.COAL)) {
            return 4800;
        }
        if (fuel.is(Items.CHARCOAL)) {
            return 3200;
        }
        if (fuel.is(ItemTags.SIGNS)) {
            return 600 * 3 / 2;
        }
        if (fuel.getItem() instanceof BlockItem blockItem && !fuel.is(ItemTags.WOODEN_DOORS)) {
            BlockState state = blockItem.getBlock().defaultBlockState();
            // Material.wood in 1.21: the bass instrument and burnable.
            if (state.instrument() == NoteBlockInstrument.BASS && state.ignitedByLava()) {
                return 450;
            }
        }
        if (fuel.is(Items.STICK) || fuel.is(ItemTags.SAPLINGS) && !fuel.is(Ic2Items.RUBBER_SAPLING.get())) {
            return 150;
        }
        return HANDLER_FUELS.getOrDefault(fuel.getItem(), 0) * 3 / 2;
    }

    @Override
    protected boolean canProcess() {
        if (fuelMJ > 0.0F) {
            return true;
        }
        return getItemEnergyValue(inventory.get(0)) > 0 && tank.getFluidAmount() >= MIN_WATER;
    }

    @Override
    protected void processActive() {
        if (fuelMJ <= 0.0F) {
            ItemStack fuel = inventory.get(0);
            fuelMJ += getItemEnergyValue(fuel);
            currentFuelMJ = getItemEnergyValue(fuel);
            // No container comes back (the lava cell is used up).
            fuel.shrink(1);
            inventory.set(0, fuel.isEmpty() ? ItemStack.EMPTY : fuel);
        }
        if (fuelMJ > 0.0F) {
            float energyToAdd = getPower();
            addEnergy(energyToAdd);
            fuelMJ -= energyToAdd;
            if (level.getGameTime() % 40L == 0L) {
                tank.drain((int) (getPower() * 10.0F), IFluidHandler.FluidAction.EXECUTE);
            }
        }
        if (provider.getEnergyStored() >= provider.getMaxEnergyStored() || tank.getFluidAmount() < MIN_WATER) {
            shutdownEngine();
        }
    }

    @Override
    public float getDurationLevel() {
        return currentFuelMJ == 0 ? 0.0F : fuelMJ / currentFuelMJ;
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
        tank.setFluid(new FluidStack(Fluids.WATER, Math.min(tag.getInt("qty.water"), CAPACITY)));
        currentFuelMJ = tag.contains("fuel.current") ? tag.getInt("fuel.current") : 4800;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("qty.water", tank.getFluidAmount());
        tag.putInt("fuel.current", currentFuelMJ);
    }
}
