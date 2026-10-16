package dev.craft.buildcraft.transport;

import javax.annotation.Nullable;

import dev.craft.buildcraft.power.PowerProvider;
import dev.craft.buildcraft.power.PowerReceptor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BuildCraft {@code TileGenericPipe} with the pipe logic: connections, neighbour changes (handled one tick later),
 * the MJ provider of wood/emerald/obsidian pipes and the per-material logic (wood source, iron redstone).
 * The transport lives in the subclasses.
 */
public abstract class PipeBlockEntity extends BlockEntity implements PowerReceptor {
    @Nullable
    protected final PowerProvider provider;
    private boolean initialized;
    private boolean blockNeighborChange = true;
    /** {@code PipeLogicIron.lastPower}. */
    private boolean lastPower;

    protected PipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        provider = createPowerProvider(getPipeType());
    }

    @Nullable
    private static PowerProvider createPowerProvider(PipeType type) {
        if (type.getTransport() == PipeType.Transport.FLUIDS) {
            if (!type.getMaterial().isWood()) {
                return null;
            }
            // PipeLiquidsWood (and PipeLiquidsEmerald).
            PowerProvider provider = new PowerProvider();
            provider.configure(1, 100, 1, 250);
            provider.configurePowerPerdition(1, 1);
            return provider;
        }
        if (type.getTransport() != PipeType.Transport.ITEMS) {
            return null;
        }
        if (type.getMaterial().isWood()) {
            // PipeItemsWood (and PipeItemsEmerald): everything not used in the same tick is lost again.
            PowerProvider provider = new PowerProvider();
            provider.configure(1, 64, 1, 64);
            provider.configurePowerPerdition(64, 1);
            return provider;
        }
        if (type.getMaterial() == PipeMaterial.OBSIDIAN) {
            PowerProvider provider = new PowerProvider();
            provider.configure(1, 64, 1, 256);
            provider.configurePowerPerdition(1, 1);
            return provider;
        }
        return null;
    }

    public PipeType getPipeType() {
        return ((GenericPipeBlock) getBlockState().getBlock()).getType();
    }

    @Nullable
    @Override
    public PowerProvider getPowerProvider() {
        return provider;
    }

    @Override
    public void doWork() {
    }

    /** {@code Pipe.isPipeConnected} towards another pipe: same transport and both materials agree (one side). */
    boolean acceptsPipe(PipeBlockEntity other) {
        PipeType type = getPipeType();
        PipeType otherType = other.getPipeType();
        return type.getTransport() == otherType.getTransport()
                && type.getMaterial().acceptsPipe(otherType.getMaterial());
    }

    /** Whether this transport connects to a block entity that is not a pipe. */
    protected abstract boolean acceptsNonPipe(BlockEntity other, Direction side);

    public boolean isConnected(Direction side) {
        return GenericPipeBlock.isConnected(getBlockState(), side);
    }

    public void scheduleNeighborChange() {
        blockNeighborChange = true;
    }

    /** {@code TileGenericPipe.updateEntity}: initialise, neighbour change, power, transport. */
    public void tick() {
        boolean server = !level.isClientSide();
        if (!initialized) {
            initialized = true;
            if (server) {
                initialize();
            }
        }
        if (server && blockNeighborChange) {
            blockNeighborChange = false;
            computeConnections();
            onNeighborBlockChange();
        }
        if (server && provider != null) {
            provider.update(this, level);
        }
        tickTransport();
    }

    protected abstract void tickTransport();

    private void initialize() {
        PipeMaterial material = getPipeType().getMaterial();
        if (material.isWood()) {
            switchSourceIfNeeded();
        } else if (material == PipeMaterial.IRON) {
            lastPower = level.hasNeighborSignal(worldPosition);
        }
    }

    private void computeConnections() {
        BlockState state = getBlockState();
        BlockState updated = state;
        for (Direction side : Direction.values()) {
            updated = updated.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(side),
                    PipeConnections.canConnect(level, worldPosition, side));
        }
        if (updated != state) {
            level.setBlock(worldPosition, updated, Block.UPDATE_ALL);
        }
    }

    protected void onNeighborBlockChange() {
        PipeMaterial material = getPipeType().getMaterial();
        if (material.isWood()) {
            switchSourceIfNeeded();
        } else if (material == PipeMaterial.IRON) {
            boolean power = level.hasNeighborSignal(worldPosition);
            if (power != lastPower) {
                PipeConnections.switchIronOutput(level, worldPosition);
                lastPower = power;
            }
        }
    }

    /** Wood/emerald source side, or {@code null}. */
    @Nullable
    public Direction getSource() {
        BlockState state = getBlockState();
        if (!state.hasProperty(GenericPipeBlock.SOURCE)) {
            return null;
        }
        int source = state.getValue(GenericPipeBlock.SOURCE);
        return source < 6 ? Direction.from3DDataValue(source) : null;
    }

    /** {@code PipeLogicWood.isInput}: an inventory or tank (no pipe) that is connected. */
    private boolean isInput(Direction side) {
        return !(level.getBlockEntity(worldPosition.relative(side)) instanceof PipeBlockEntity)
                && (PipeConnections.itemHandler(level, worldPosition, side) != null
                || PipeConnections.fluidHandler(level, worldPosition, side) != null)
                && PipeConnections.canConnect(level, worldPosition, side);
    }

    private void switchSourceIfNeeded() {
        Direction source = getSource();
        if (source == null || !isInput(source)) {
            switchSource();
        }
    }

    /** {@code PipeLogicWood.switchSource}: from the current side + 1 round the sides, the first input or none. */
    public void switchSource() {
        BlockState state = getBlockState();
        int meta = state.getValue(GenericPipeBlock.SOURCE);
        int newMeta = 6;
        for (int i = meta + 1; i <= meta + 6; i++) {
            Direction side = Direction.from3DDataValue(i % 6);
            if (isInput(side)) {
                newMeta = side.get3DDataValue();
                break;
            }
        }
        if (newMeta != meta) {
            level.setBlock(worldPosition, state.setValue(GenericPipeBlock.SOURCE, newMeta), Block.UPDATE_ALL);
        }
    }

    /** {@code dropContents} on breaking the pipe. */
    public abstract void dropContents();

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (provider != null) {
            tag.put("power", provider.save());
        }
        tag.putBoolean("lastPower", lastPower);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (provider != null && tag.contains("power")) {
            provider.load(tag.getCompound("power"));
        }
        lastPower = tag.getBoolean("lastPower");
    }
}
