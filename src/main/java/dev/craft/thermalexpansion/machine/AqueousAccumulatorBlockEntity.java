package dev.craft.thermalexpansion.machine;

import dev.craft.thermalexpansion.TeBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

/**
 * {@code TileWaterGen}: every 40 ticks 1000 mB water with at least two horizontally adjacent water blocks, else 1 mB
 * (pack config {@code factory.waterGen.rate = 25}); not in a Nether biome. Pushes 25 mB per tick into the next
 * adjacent tank on a side of group 1 (reihum); its own tank can be neither filled nor drained.
 */
public class AqueousAccumulatorBlockEntity extends TeMachineBlockEntity {
    public static final int CAPACITY = 4000;
    /** {@code genRate} = config rate · 40. */
    private static final int GEN_RATE = 1000;
    private static final int TRANSFER_RATE = GEN_RATE / 40;
    private static final int GROUP_OUTPUT = 1;

    private final FluidTank tank = new FluidTank(CAPACITY, stack -> stack.getFluid().isSame(Fluids.WATER)) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private int numAdjacentSources;
    private int outputTrackerLiquid;

    public AqueousAccumulatorBlockEntity(BlockPos pos, BlockState state) {
        super(TeBlockEntityTypes.AQUEOUS_ACCUMULATOR.get(), TeMachineType.AQUEOUS_ACCUMULATOR, pos, state);
    }

    public int getNumAdjacentSources() {
        return numAdjacentSources;
    }

    /** {@code updateAdjacentSources}: water in the four horizontal neighbours. */
    private void updateAdjacentSources() {
        numAdjacentSources = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getFluidState(worldPosition.relative(direction)).is(FluidTags.WATER)) {
                numAdjacentSources++;
            }
        }
    }

    /** {@code transferLiquid}: the first adjacent tank on an output side gets 25 mB, even if it takes nothing. */
    private void transferLiquid() {
        if (tank.getFluidAmount() < TRANSFER_RATE) {
            return;
        }
        for (int i = outputTrackerLiquid + 1; i <= outputTrackerLiquid + 6; i++) {
            int side = i % 6;
            if (sides[side] != GROUP_OUTPUT) {
                continue;
            }
            Direction direction = Direction.from3DDataValue(side);
            IFluidHandler target = level.getCapability(Capabilities.FluidHandler.BLOCK,
                    worldPosition.relative(direction), direction.getOpposite());
            if (target != null) {
                int filled = target.fill(new FluidStack(Fluids.WATER, TRANSFER_RATE),
                        IFluidHandler.FluidAction.EXECUTE);
                tank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                outputTrackerLiquid = side;
                break;
            }
        }
    }

    @Override
    protected boolean canStart() {
        return !level.getBiome(worldPosition).is(BiomeTags.IS_NETHER) && tank.getFluidAmount() <= CAPACITY;
    }

    @Override
    protected boolean canFinish() {
        return false;
    }

    @Override
    protected void processStart() {
    }

    @Override
    protected void processFinish() {
    }

    @Override
    protected void transferProducts() {
    }

    /** {@code TileWaterGen.updateEntity}; in the ticks in between, the sources are counted again. */
    @Override
    public void serverTick() {
        boolean curActive = active;
        transferLiquid();
        if (level.getGameTime() % 40L == 0L && active) {
            if (level.getGameTime() % 1200L == 0L) {
                updateAdjacentSources();
            }
            int waterToAdd = Math.min(numAdjacentSources >= 2 ? GEN_RATE : 1, CAPACITY - tank.getFluidAmount());
            tank.fill(new FluidStack(Fluids.WATER, waterToAdd), IFluidHandler.FluidAction.EXECUTE);
            processCur = (processCur + waterToAdd) % 1000.0F;
            if (!redstoneControlOrDisable()) {
                markInactive();
            }
        } else if (redstoneControlOrDisable() && canStart()) {
            updateAdjacentSources();
            active = true;
        }
        updateIfChanged(curActive);
        setChanged();
    }

    /** Output sides show the tank; nothing goes in or out through them. */
    @Nullable
    @Override
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return side != null && getSide(side) == GROUP_OUTPUT ? new TeInputTanks(false, tank) : null;
    }

    @Override
    public FluidStack getDisplayFluid(int index) {
        return index == 0 ? tank.getFluid() : FluidStack.EMPTY;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new TeMachineMenu(id, playerInventory, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        outputTrackerLiquid = tag.getInt("output.liquid");
        tank.setFluid(new FluidStack(Fluids.WATER, Math.min(tag.getInt("qty.water"), CAPACITY)));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("output.liquid", outputTrackerLiquid);
        tag.putInt("qty.water", tank.getFluidAmount());
    }
}
