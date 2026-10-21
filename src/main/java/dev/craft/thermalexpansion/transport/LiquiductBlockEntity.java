package dev.craft.thermalexpansion.transport;

import dev.craft.thermalexpansion.TeBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

/**
 * {@code TileConduitLiquid}: nodes are liquiducts next to tanks. Output nodes (default) fill their tanks and accept
 * liquid from outside; input nodes take nothing from outside but pull from their tanks while they get a redstone
 * signal. Everything may be drained. The client shows the network liquid in six levels; light as the liquid (lava 15).
 */
public class LiquiductBlockEntity extends ConduitBlockEntity {
    /** {@code updateDelay}. */
    private static final int UPDATE_DELAY = 4;

    @Nullable
    LiquidGrid grid;
    private FluidStack pendingLiquid = FluidStack.EMPTY;
    private Fluid renderFluid = Fluids.EMPTY;
    private int renderLevel;

    public LiquiductBlockEntity(BlockPos pos, BlockState state) {
        super(TeBlockEntityTypes.LIQUIDUCT.get(), pos, state);
    }

    @Nullable
    public LiquidGrid getGrid() {
        return grid;
    }

    public Fluid getRenderFluid() {
        return renderFluid;
    }

    public int getRenderLevel() {
        return renderLevel;
    }

    FluidStack getPendingLiquid() {
        return pendingLiquid;
    }

    FluidStack takePendingLiquid() {
        FluidStack liquid = pendingLiquid;
        pendingLiquid = FluidStack.EMPTY;
        return liquid;
    }

    void setPendingLiquid(FluidStack liquid) {
        pendingLiquid = liquid;
    }

    /** A tank on {@code side} (not a liquiduct): a fluid handler with at least one tank. */
    @Nullable
    private IFluidHandler tank(Direction side) {
        BlockEntity neighbour = neighbour(side);
        if (neighbour == null || neighbour instanceof LiquiductBlockEntity) {
            return null;
        }
        IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, neighbour.getBlockPos(),
                side.getOpposite());
        return handler != null && handler.getTanks() > 0 ? handler : null;
    }

    /** {@code checkIfNode}: liquiducts of this network, tanks, nothing. */
    boolean refreshSides() {
        boolean changed = false;
        for (Direction side : Direction.values()) {
            byte value;
            if (neighbour(side) instanceof LiquiductBlockEntity other && !other.isRemoved()) {
                value = other.grid != null && other.grid == grid ? CONDUIT : NONE;
            } else {
                value = tank(side) != null ? OTHER : NONE;
            }
            if (sides[side.get3DDataValue()] != value) {
                changed |= value == OTHER || sides[side.get3DDataValue()] == OTHER;
                sides[side.get3DDataValue()] = value;
            }
        }
        return changed;
    }

    @Override
    public void serverTick() {
        if (grid == null) {
            LiquidGrid.build(this);
        }
        if (grid == null) {
            return;
        }
        if (isNode()) {
            grid.tick(this);
        }
        if (level.getGameTime() % UPDATE_DELAY == 0L) {
            grid.updateRender();
            if (grid.getRenderLevel() != renderLevel || grid.getRenderLiquid() != renderFluid) {
                renderLevel = grid.getRenderLevel();
                renderFluid = grid.getRenderLiquid();
                updateLight();
                sync();
            }
        }
    }

    private void updateLight() {
        BlockState state = getBlockState();
        int light = LiquidGrid.lightLevel(renderFluid);
        if (state.getValue(ConduitBlock.LIGHT) != light) {
            level.setBlock(worldPosition, state.setValue(ConduitBlock.LIGHT, light), Block.UPDATE_ALL);
        }
    }

    @Override
    public void onNeighborBlockChange() {
        boolean nodesChanged = refreshSides();
        if (nodesChanged && grid != null) {
            grid.dissolve();
        }
        syncSides();
    }

    @Override
    protected void leaveGrid() {
        if (grid != null) {
            grid.dissolve();
        }
    }

    /** {@code wrenchAction}: joins the liquiduct on that side if both liquids match (or one is empty). */
    @Override
    public boolean wrenchAction(Direction side) {
        if (grid == null || sides[side.get3DDataValue()] != NONE
                || !(neighbour(side) instanceof LiquiductBlockEntity other) || other.grid == null) {
            return false;
        }
        FluidStack mine = grid.getLiquid();
        FluidStack theirs = other.grid.getLiquid();
        if (!mine.isEmpty() && !theirs.isEmpty() && !mine.getFluid().isSame(theirs.getFluid())) {
            return false;
        }
        LiquidGrid otherGrid = other.grid;
        grid.dissolve();
        otherGrid.dissolve();
        return true;
    }

    @Override
    public void changeMode() {
        if (!isNode() || grid == null) {
            return;
        }
        mode = (byte) ((mode + 1) % 2);
        grid.changeMode(this);
        sync();
    }

    /** {@code transferLiquid(side, available)}: fill the tank on that side. */
    int transferLiquid(Direction side, int available) {
        if (grid == null || grid.getLiquid().isEmpty() || sides[side.get3DDataValue()] != OTHER) {
            return 0;
        }
        IFluidHandler target = tank(side);
        if (target == null) {
            return 0;
        }
        int amountSent = target.fill(grid.getLiquid().copyWithAmount(available), IFluidHandler.FluidAction.SIMULATE);
        if (amountSent <= 0) {
            return 0;
        }
        return target.fill(grid.drain(amountSent, IFluidHandler.FluidAction.EXECUTE),
                IFluidHandler.FluidAction.EXECUTE);
    }

    /** {@code extractLiquid}: only with a redstone signal, at most the base throughput per side. */
    void extractLiquid(Direction side) {
        if (grid == null || !level.hasNeighborSignal(worldPosition) || sides[side.get3DDataValue()] != OTHER) {
            return;
        }
        IFluidHandler source = tank(side);
        if (source == null) {
            return;
        }
        int maxInput = Math.min(grid.getCapacity() - grid.getLiquidAmount(), grid.getBaseThroughput());
        FluidStack returned = source.drain(maxInput, IFluidHandler.FluidAction.SIMULATE);
        if (returned.isEmpty()) {
            return;
        }
        if (grid.getLiquid().isEmpty() || FluidStack.isSameFluidSameComponents(grid.getLiquid(), returned)) {
            grid.fill(source.drain(maxInput, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
        }
    }

    /** {@code getTanks}: the network tank on tank sides; filled unless input node, drained always. */
    @Nullable
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        if (side != null && sides[side.get3DDataValue()] != OTHER) {
            return null;
        }
        return new IFluidHandler() {
            @Override
            public int getTanks() {
                return grid != null ? 1 : 0;
            }

            @Override
            public FluidStack getFluidInTank(int index) {
                return grid != null ? grid.getLiquid() : FluidStack.EMPTY;
            }

            @Override
            public int getTankCapacity(int index) {
                return grid != null ? grid.getCapacity() : 0;
            }

            @Override
            public boolean isFluidValid(int index, FluidStack stack) {
                return true;
            }

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                return grid != null && mode != MODE_INPUT ? grid.fill(resource, action) : 0;
            }

            @Override
            public FluidStack drain(FluidStack resource, FluidAction action) {
                if (grid == null || !FluidStack.isSameFluidSameComponents(grid.getLiquid(), resource)) {
                    return FluidStack.EMPTY;
                }
                return grid.drain(resource.getAmount(), action);
            }

            @Override
            public FluidStack drain(int maxDrain, FluidAction action) {
                return grid != null ? grid.drain(maxDrain, action) : FluidStack.EMPTY;
            }
        };
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        pendingLiquid = FluidStack.parseOptional(registries, tag.getCompound("liquid"));
    }

    /** {@code lastWriteStorage}: this conduit's part of the network liquid. */
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        FluidStack liquid = grid != null ? grid.share(this) : pendingLiquid;
        if (!liquid.isEmpty()) {
            tag.put("liquid", liquid.save(registries));
        }
    }

    @Override
    protected void loadClientData(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadClientData(tag, registries);
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("render.liquid"));
        renderFluid = id != null ? BuiltInRegistries.FLUID.get(id) : Fluids.EMPTY;
        renderLevel = tag.getByte("render.level");
    }

    @Override
    protected void saveClientData(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveClientData(tag, registries);
        tag.putString("render.liquid", BuiltInRegistries.FLUID.getKey(renderFluid).toString());
        tag.putByte("render.level", (byte) renderLevel);
    }
}
