package dev.craft.powerplants.turbine;

import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySource;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.energy.OutputMeter;
import dev.craft.core.energy.OutputReadout;
import dev.craft.core.multiblock.MultiblockPattern;
import dev.craft.powerplants.PpBlockEntityTypes;
import dev.craft.powerplants.PpFluids;
import dev.craft.powerplants.block.PlantControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Steam turbine of the coal power plant (spec 14 §4): 3×3×5 of turbine casings around an axis of three rotors, the
 * controller in the middle of one 3×3 end face. Turns up to 64 mB steam per tick into as many EU (1 mB = 1 EU) and
 * emits them from the controller and every casing block in packets of up to 64 EU (MV). The large turbine only
 * changes the values ({@link SteamTurbineTier}).
 */
public class SteamTurbineBlockEntity extends PlantControllerBlockEntity implements EnergySource, OutputReadout {
    // values of the small turbine; see SteamTurbineTier for the large one
    public static final int TANK_CAPACITY = 16000;
    public static final int MAX_STEAM_PER_TICK = 64;
    public static final int MAX_OUTPUT = 64;
    public static final int BUFFER_CAPACITY = 128;

    private final IFluidHandler tank = new SteamTank();
    private final OutputMeter outputMeter = new OutputMeter();
    private int steam;
    private int buffer;
    /** EU produced in the last tick (GUI). */
    private int production;

    public SteamTurbineBlockEntity(BlockPos pos, BlockState state) {
        this(PpBlockEntityTypes.STEAM_TURBINE.get(), pos, state);
    }

    protected SteamTurbineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 0);
    }

    /** Size and values of this turbine; constant per class (it is needed before the constructor finishes). */
    public SteamTurbineTier getTier() {
        return SteamTurbineTier.SMALL;
    }

    public int getSteam() {
        return steam;
    }

    public int getBuffer() {
        return buffer;
    }

    public int getProduction() {
        return production;
    }

    public IFluidHandler getTank() {
        return tank;
    }

    @Override
    protected MultiblockPattern getPattern() {
        return getTier().pattern();
    }

    @Override
    protected void tickFormed() {
        SteamTurbineTier tier = getTier();
        production = Math.min(steam, Math.min(tier.maxSteamPerTick(), tier.bufferCapacity() - buffer));
        steam -= production;
        outputMeter.record(level, production);
        buffer += production;
        int output = Math.min(buffer, tier.maxOutput());
        if (output > 0) {
            buffer += EnergyNets.emit(level, this, output) - output;
        }
        setActive(production > 0);
        setChanged();
    }

    @Override
    protected void tickUnformed() {
        production = 0;
        super.tickUnformed();
    }

    @Override
    protected void onFormedChanged(boolean formed) {
        // the casing conducts only while formed
        EnergyNets.rejoin(level, this);
    }

    @Override
    public IFluidHandler getPartFluidHandler(@Nullable Direction side) {
        return tank;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new SteamTurbineMenu(id, playerInventory, this);
    }

    // --- energy net ---

    @Override
    public void onLoad() {
        super.onLoad();
        EnergyNets.add(level, this);
    }

    @Override
    public void setRemoved() {
        EnergyNets.remove(level, this);
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        EnergyNets.remove(level, this);
        super.onChunkUnloaded();
    }

    @Override
    public boolean emitsEnergyTo(EnergyTile receiver, Direction direction) {
        return true;
    }

    @Override
    public int getMaxEnergyOutput() {
        return getTier().maxOutput();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        steam = tag.getInt("steam");
        buffer = tag.getInt("buffer");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("steam", steam);
        tag.putInt("buffer", buffer);
    }

    /** Steam only, filled from every turbine block, never drained. */
    private final class SteamTank implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int index) {
            return steam > 0 ? new FluidStack(PpFluids.STEAM.get(), steam) : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int index) {
            return getTier().tankCapacity();
        }

        @Override
        public boolean isFluidValid(int index, FluidStack stack) {
            return stack.is(PpFluids.STEAM.get());
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || !resource.is(PpFluids.STEAM.get())) {
                return 0;
            }
            int filled = Math.min(resource.getAmount(), getTier().tankCapacity() - steam);
            if (action.execute() && filled > 0) {
                steam += filled;
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

    @Override
    public OutputReading getOutputReading() {
        return outputMeter.read(level, OutputReading.Unit.EU);
    }
}
