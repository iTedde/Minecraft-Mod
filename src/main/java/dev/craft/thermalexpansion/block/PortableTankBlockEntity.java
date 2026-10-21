package dev.craft.thermalexpansion.block;

import dev.craft.thermalexpansion.TeBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

/**
 * {@code TilePortableTank}: 8000 mB of any liquid, reachable only from above and below. In output mode (wrench) it
 * pushes its liquid down – 500 mB per tick into a portable tank, else 100 mB into any tank. A fill that does not fit
 * overflows into a portable tank above; placed on a portable tank it starts in output mode. Its light value stays 0
 * (an original bug: {@code getLiquidID() >= 0} is always true).
 */
public class PortableTankBlockEntity extends BlockEntity {
    public static final int MAX_LIQUID = 8000;
    /** {@code RATE}, into other tanks. */
    private static final int RATE = 100;
    /** Into a portable tank below. */
    private static final int STACK_RATE = 500;
    /** Display lists the client changes by; {@code UPDATE_FACTOR}. */
    private static final int UPDATE_FACTOR = 4;

    private final FluidTank tank = new FluidTank(MAX_LIQUID) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    /** {@code sideConfig[0]}/{@code [1]}: a portable tank below/above. */
    private boolean tankBelow;
    private boolean tankAbove;
    private boolean mode;
    private int lastDisplayList;

    public PortableTankBlockEntity(BlockPos pos, BlockState state) {
        super(TeBlockEntityTypes.PORTABLE_TANK.get(), pos, state);
    }

    public FluidTank getTank() {
        return tank;
    }

    public boolean getMode() {
        return mode;
    }

    /** {@code changeMode}. */
    public void changeMode() {
        mode = !mode;
        sendUpdate();
    }

    /** {@code placeBlock}: placed on a portable tank, the tank starts in output mode. */
    public void placeBlock() {
        onNeighborBlockChange();
        if (tankBelow) {
            mode = true;
        }
        sendUpdate();
    }

    /** {@code onNeighborBlockChange}. */
    public void onNeighborBlockChange() {
        tankBelow = level.getBlockEntity(worldPosition.below()) instanceof PortableTankBlockEntity;
        tankAbove = level.getBlockEntity(worldPosition.above()) instanceof PortableTankBlockEntity;
        setChanged();
    }

    public void serverTick() {
        if (level.getGameTime() % 2L == 0L) {
            updateRender();
        }
        if (mode) {
            transferLiquid();
        }
    }

    /** {@code updateRender}: tell the client when the level moved by 4 of 99 steps, or the tank became empty. */
    private void updateRender() {
        if (tank.isEmpty()) {
            if (lastDisplayList != 0) {
                lastDisplayList = 0;
                sendUpdate();
            }
            return;
        }
        int curDisplayList = (int) (tank.getFluidAmount() / (float) MAX_LIQUID * 99.0F);
        if (curDisplayList == 0) {
            return;
        }
        if (curDisplayList <= lastDisplayList - UPDATE_FACTOR || curDisplayList >= lastDisplayList + UPDATE_FACTOR) {
            lastDisplayList = curDisplayList;
            sendUpdate();
        }
    }

    private void transferLiquid() {
        if (tank.isEmpty()) {
            return;
        }
        BlockPos below = worldPosition.below();
        if (tankBelow) {
            if (level.getBlockEntity(below) instanceof PortableTankBlockEntity other
                    && (other.tank.isEmpty() || other.tank.getFluidAmount() != MAX_LIQUID)) {
                int quantity = other.fill(Direction.UP, tank.getFluid().copyWithAmount(
                        Math.min(tank.getFluidAmount(), STACK_RATE)), IFluidHandler.FluidAction.EXECUTE);
                if (quantity > 0) {
                    tank.drain(quantity, IFluidHandler.FluidAction.EXECUTE);
                }
            }
            return;
        }
        IFluidHandler target = level.getCapability(Capabilities.FluidHandler.BLOCK, below, Direction.UP);
        if (target != null) {
            int quantity = target.fill(tank.getFluid().copyWithAmount(Math.min(tank.getFluidAmount(), RATE)),
                    IFluidHandler.FluidAction.EXECUTE);
            if (quantity > 0) {
                tank.drain(quantity, IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    /** {@code fill}; {@code from == null} is the unknown side (buckets). */
    public int fill(@Nullable Direction from, FluidStack resource, IFluidHandler.FluidAction action) {
        if (from == Direction.DOWN && mode && !tankBelow || from != null && from.getAxis().isHorizontal()) {
            return 0;
        }
        if (!tank.isEmpty() && !FluidStack.isSameFluidSameComponents(tank.getFluid(), resource)) {
            return 0;
        }
        int amount = tank.fill(resource, action);
        if (from != Direction.UP && tankAbove && amount != resource.getAmount()
                && level.getBlockEntity(worldPosition.above()) instanceof PortableTankBlockEntity above) {
            return amount + above.fill(Direction.DOWN, resource.copyWithAmount(resource.getAmount() - amount), action);
        }
        return amount;
    }

    public FluidStack drain(@Nullable Direction from, int maxDrain, IFluidHandler.FluidAction action) {
        if (from == Direction.DOWN && mode && !tankBelow || from != null && from.getAxis().isHorizontal()) {
            return FluidStack.EMPTY;
        }
        return tank.drain(maxDrain, action);
    }

    /** {@code getTanks}: no tank on the four sides. */
    @Nullable
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        if (side != null && side.getAxis().isHorizontal()) {
            return null;
        }
        return new IFluidHandler() {
            @Override
            public int getTanks() {
                return 1;
            }

            @Override
            public FluidStack getFluidInTank(int index) {
                return tank.getFluid();
            }

            @Override
            public int getTankCapacity(int index) {
                return MAX_LIQUID;
            }

            @Override
            public boolean isFluidValid(int index, FluidStack stack) {
                return true;
            }

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                return PortableTankBlockEntity.this.fill(side, resource, action);
            }

            @Override
            public FluidStack drain(FluidStack resource, FluidAction action) {
                if (!FluidStack.isSameFluidSameComponents(tank.getFluid(), resource)) {
                    return FluidStack.EMPTY;
                }
                return PortableTankBlockEntity.this.drain(side, resource.getAmount(), action);
            }

            @Override
            public FluidStack drain(int maxDrain, FluidAction action) {
                return PortableTankBlockEntity.this.drain(side, maxDrain, action);
            }
        };
    }

    private void sendUpdate() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        mode = tag.getBoolean("process.mode");
        tankBelow = tag.getBoolean("side.down");
        tankAbove = tag.getBoolean("side.up");
        tank.setFluid(FluidStack.parseOptional(registries, tag.getCompound("tank")));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("process.mode", mode);
        tag.putBoolean("side.down", tankBelow);
        tag.putBoolean("side.up", tankAbove);
        if (!tank.isEmpty()) {
            tag.put("tank", tank.getFluid().save(registries));
        }
    }

    /** The description packet: mode and liquid. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putBoolean("process.mode", mode);
        if (!tank.isEmpty()) {
            tag.put("tank", tank.getFluid().save(registries));
        }
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        mode = tag.getBoolean("process.mode");
        tank.setFluid(FluidStack.parseOptional(registries, tag.getCompound("tank")));
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        handleUpdateTag(packet.getTag(), registries);
    }
}
