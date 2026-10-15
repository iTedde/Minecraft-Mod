package dev.craft.buildcraft.factory;

import dev.craft.buildcraft.BcBlockEntityTypes;
import dev.craft.buildcraft.power.SafeTimeTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

/**
 * BuildCraft {@code TileTank}: 16 000 mB; stacked tanks form one store that is filled from the bottom up and drained
 * from the bottom. Every tick a tank pours its whole content into the tank below.
 */
public class TankBlockEntity extends BlockEntity {
    public static final int CAPACITY = 16000;

    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private final IFluidHandler fluidHandler = new Handler();
    private final SafeTimeTracker tracker = new SafeTimeTracker();
    private boolean hasUpdate;

    public TankBlockEntity(BlockPos pos, BlockState state) {
        super(BcBlockEntityTypes.TANK.get(), pos, state);
    }

    public FluidTank getTank() {
        return tank;
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    public void serverTick() {
        if (hasUpdate && tracker.markTimeIfDelay(level, 2)) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            hasUpdate = false;
        }
        if (!tank.isEmpty()) {
            moveLiquidBelow();
        }
    }

    private void moveLiquidBelow() {
        TankBlockEntity below = getTankBelow(this);
        if (below != null) {
            int used = below.tank.fill(tank.getFluid(), IFluidHandler.FluidAction.EXECUTE);
            if (used > 0) {
                hasUpdate = true;
                below.hasUpdate = true;
                tank.drain(used, IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    @Nullable
    private static TankBlockEntity getTankBelow(TankBlockEntity tile) {
        return tile.level.getBlockEntity(tile.worldPosition.below()) instanceof TankBlockEntity below ? below : null;
    }

    @Nullable
    private static TankBlockEntity getTankAbove(TankBlockEntity tile) {
        return tile.level.getBlockEntity(tile.worldPosition.above()) instanceof TankBlockEntity above ? above : null;
    }

    public TankBlockEntity getBottomTank() {
        TankBlockEntity lastTank = this;
        TankBlockEntity below;
        while ((below = getTankBelow(lastTank)) != null) {
            lastTank = below;
        }
        return lastTank;
    }

    /** {@code fill}: from the bottom tank upwards; refused if the bottom tank holds another liquid. */
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty()) {
            return 0;
        }
        FluidStack rest = resource.copy();
        int totalUsed = 0;
        TankBlockEntity tankToFill = getBottomTank();
        FluidStack liquid = tankToFill.tank.getFluid();
        if (!liquid.isEmpty() && !FluidStack.isSameFluidSameComponents(liquid, resource)) {
            return 0;
        }
        while (tankToFill != null && rest.getAmount() > 0) {
            int used = tankToFill.tank.fill(rest, action);
            rest.shrink(used);
            if (used > 0) {
                tankToFill.hasUpdate = true;
            }
            totalUsed += used;
            tankToFill = getTankAbove(tankToFill);
        }
        return totalUsed;
    }

    /** {@code drain}: always from the bottom tank. */
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        TankBlockEntity bottom = getBottomTank();
        bottom.hasUpdate = true;
        return bottom.tank.drain(maxDrain, action);
    }

    /** {@code getTanks}: the bottom tank plus every tank above with the same liquid, up to another liquid. */
    public FluidStack getCompositeFluid() {
        TankBlockEntity tile = getBottomTank();
        if (tile.tank.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidStack composite = tile.tank.getFluid().copy();
        for (TankBlockEntity above = getTankAbove(tile); above != null; above = getTankAbove(above)) {
            FluidStack liquid = above.tank.getFluid();
            if (!liquid.isEmpty()) {
                if (!FluidStack.isSameFluidSameComponents(composite, liquid)) {
                    break;
                }
                composite.grow(liquid.getAmount());
            }
        }
        return composite;
    }

    /** Capacity of {@code getTanks}: every tank up to (not including) one with another liquid. */
    public int getCompositeCapacity() {
        TankBlockEntity tile = getBottomTank();
        int capacity = tank.getCapacity();
        if (tile.tank.isEmpty()) {
            return capacity;
        }
        FluidStack bottom = tile.tank.getFluid();
        for (TankBlockEntity above = getTankAbove(tile); above != null; above = getTankAbove(above)) {
            FluidStack liquid = above.tank.getFluid();
            if (!liquid.isEmpty() && !FluidStack.isSameFluidSameComponents(bottom, liquid)) {
                break;
            }
            capacity += above.tank.getCapacity();
        }
        return capacity;
    }

    // --- persistence and sync ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!tank.isEmpty()) {
            tag.put("tank", tank.getFluid().save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tank.setFluid(FluidStack.parseOptional(registries, tag.getCompound("tank")));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
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
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        tank.setFluid(FluidStack.parseOptional(registries, packet.getTag().getCompound("tank")));
    }

    private final class Handler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int index) {
            return getCompositeFluid();
        }

        @Override
        public int getTankCapacity(int index) {
            return getCompositeCapacity();
        }

        @Override
        public boolean isFluidValid(int index, FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return TankBlockEntity.this.fill(resource, action);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            FluidStack bottom = getBottomTank().tank.getFluid();
            if (resource.isEmpty() || !FluidStack.isSameFluidSameComponents(resource, bottom)) {
                return FluidStack.EMPTY;
            }
            return TankBlockEntity.this.drain(resource.getAmount(), action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return TankBlockEntity.this.drain(maxDrain, action);
        }
    }
}
