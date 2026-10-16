package dev.craft.buildcraft.factory;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import dev.craft.buildcraft.BcBlockEntityTypes;
import dev.craft.buildcraft.power.PowerProvider;
import dev.craft.buildcraft.power.PowerReceptor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

/**
 * BuildCraft {@code TilePump}: lowers its tube onto the liquid below, pumps source blocks (10 MJ per 1000 mB) from the
 * top layer of the connected liquid and hands its tank to every adjacent fluid handler.
 */
public class PumpBlockEntity extends BlockEntity implements PowerReceptor {
    public static final int MAX_LIQUID = 1000;
    /** {@code dx² + dz² > 4096} is out of reach (64 blocks). */
    private static final int MAX_DISTANCE_SQ = 4096;
    private static final int NO_AIM = Integer.MIN_VALUE;
    /** {@code BlockIndex.compareTo}: x, then z, then y. */
    private static final Comparator<BlockPos> BLOCK_INDEX_ORDER = Comparator.<BlockPos>comparingInt(BlockPos::getX)
            .thenComparingInt(BlockPos::getZ).thenComparingInt(BlockPos::getY);

    private final PowerProvider provider = new PowerProvider();
    private final FluidTank tank = new FluidTank(MAX_LIQUID) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private final IFluidHandler fluidHandler = new Handler();
    /** Layers by height; each list is a {@code LinkedList}: sources are pushed to the front. */
    private final TreeMap<Integer, Deque<BlockPos>> blocksToPump = new TreeMap<>();
    private boolean initialized;
    private boolean hasTube;
    private double tubeY = Double.NaN;
    private int aimY = NO_AIM;

    public PumpBlockEntity(BlockPos pos, BlockState state) {
        super(BcBlockEntityTypes.PUMP.get(), pos, state);
        provider.configure(1, 10, 10, 100);
    }

    @Override
    public PowerProvider getPowerProvider() {
        return provider;
    }

    @Override
    public void doWork() {
    }

    /** {@code TileMachine.powerRequest}: only while the next block is pumpable ({@code isActive}). */
    @Override
    public int powerRequest() {
        return isPumpableLiquid(getNextIndexToPump(false)) ? provider.missingEnergyRequest() : 0;
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    public FluidTank getTank() {
        return tank;
    }

    private void initialize() {
        hasTube = true;
        if (Double.isNaN(tubeY)) {
            tubeY = worldPosition.getY();
        }
        if (aimY == NO_AIM) {
            aimY = worldPosition.getY();
        }
        sendNetworkUpdate();
    }

    public void serverTick() {
        if (!initialized) {
            initialized = true;
            initialize();
        }
        provider.update(this, level);
        if (!hasTube) {
            return;
        }
        if (tubeY - aimY > 0.01) {
            tubeY -= 0.01;
            setChanged();
            sendNetworkUpdate();
            return;
        }
        if (tank.isEmpty()) {
            BlockPos index = getNextIndexToPump(false);
            if (isPumpableLiquid(index)) {
                FluidStack liquidToPump = new FluidStack(liquidAt(index), FluidType.BUCKET_VOLUME);
                if (tank.fill(liquidToPump, IFluidHandler.FluidAction.SIMULATE) == liquidToPump.getAmount()
                        && provider.useEnergy(10.0F, 10.0F, true) == 10.0F) {
                    index = getNextIndexToPump(true);
                    // consumeWaterSources = true in the pack: water sources are removed as well.
                    level.setBlock(index, Blocks.AIR.defaultBlockState(), 3);
                    tank.fill(liquidToPump, IFluidHandler.FluidAction.EXECUTE);
                    sendNetworkUpdate();
                }
            } else if (level.getGameTime() % 100L == 0L) {
                initializePumpFromPosition(new BlockPos(worldPosition.getX(), aimY, worldPosition.getZ()));
                if (getNextIndexToPump(false) == null) {
                    for (int y = worldPosition.getY() - 1; y > level.getMinBuildHeight(); y--) {
                        BlockPos pos = new BlockPos(worldPosition.getX(), y, worldPosition.getZ());
                        if (isLiquid(pos)) {
                            aimY = y;
                            setChanged();
                            return;
                        }
                        if (!level.getBlockState(pos).isAir()) {
                            return;
                        }
                    }
                }
            }
        }
        FluidStack liquid = tank.getFluid();
        if (!liquid.isEmpty()) {
            for (Direction side : Direction.values()) {
                IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK,
                        worldPosition.relative(side), side.getOpposite());
                if (handler != null) {
                    int moved = handler.fill(liquid.copy(), IFluidHandler.FluidAction.EXECUTE);
                    tank.drain(moved, IFluidHandler.FluidAction.EXECUTE);
                    if (tank.isEmpty()) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * {@code getNextIndexToPump}: checks the last entry of the top layer, but removes the first one. The original
     * starts at height 0 and so skips layers at y ≤ 0; with negative world heights any layer counts.
     */
    @Nullable
    private BlockPos getNextIndexToPump(boolean remove) {
        Deque<BlockPos> topLayer = null;
        int topLayerHeight = 0;
        boolean found = false;
        for (Map.Entry<Integer, Deque<BlockPos>> layer : blocksToPump.entrySet()) {
            if ((!found || layer.getKey() > topLayerHeight) && !layer.getValue().isEmpty()) {
                topLayerHeight = layer.getKey();
                topLayer = layer.getValue();
                found = true;
            }
        }
        if (topLayer == null) {
            return null;
        }
        if (remove) {
            BlockPos index = topLayer.pop();
            if (topLayer.isEmpty()) {
                blocksToPump.remove(topLayerHeight);
            }
            return index;
        }
        return topLayer.getLast();
    }

    private void initializePumpFromPosition(BlockPos start) {
        Set<BlockPos> markedBlocks = new HashSet<>();
        Set<BlockPos> lastFound = new TreeSet<>(BLOCK_INDEX_ORDER);
        Deque<BlockPos> pumpList = blocksToPump.computeIfAbsent(start.getY(), y -> new ArrayDeque<>());
        if (!isLiquid(start)) {
            return;
        }
        Fluid liquid = liquidAt(start);
        addToPumpIfLiquid(start, markedBlocks, lastFound, pumpList, liquid);
        while (!lastFound.isEmpty()) {
            Set<BlockPos> visitIteration = new TreeSet<>(BLOCK_INDEX_ORDER);
            visitIteration.addAll(lastFound);
            lastFound.clear();
            for (BlockPos index : visitIteration) {
                addToPumpIfLiquid(index.east(), markedBlocks, lastFound, pumpList, liquid);
                addToPumpIfLiquid(index.west(), markedBlocks, lastFound, pumpList, liquid);
                addToPumpIfLiquid(index.south(), markedBlocks, lastFound, pumpList, liquid);
                addToPumpIfLiquid(index.north(), markedBlocks, lastFound, pumpList, liquid);
                pumpList = blocksToPump.computeIfAbsent(index.getY() + 1, y -> new ArrayDeque<>());
                addToPumpIfLiquid(index.above(), markedBlocks, lastFound, pumpList, liquid);
            }
        }
    }

    /**
     * {@code addToPumpIfLiquid}. As in the original, the horizontal neighbours go into the list last selected – after
     * the first block that is the layer above.
     */
    private void addToPumpIfLiquid(BlockPos index, Set<BlockPos> markedBlocks, Set<BlockPos> lastFound,
                                   Deque<BlockPos> pumpList, Fluid liquid) {
        if (!isLiquid(index) || !liquidAt(index).isSame(liquid) || !markedBlocks.add(index)) {
            return;
        }
        int dx = index.getX() - worldPosition.getX();
        int dz = index.getZ() - worldPosition.getZ();
        if (dx * dx + dz * dz > MAX_DISTANCE_SQ) {
            return;
        }
        if (isPumpableLiquid(index)) {
            pumpList.push(index);
        }
        lastFound.add(index);
    }

    private boolean isPumpableLiquid(@Nullable BlockPos index) {
        return isLiquid(index) && level.getFluidState(index).isSource();
    }

    /** {@code Utils.liquidFromBlockId}: liquid blocks only (no waterlogged blocks, which 1.4.7 did not have). */
    private boolean isLiquid(@Nullable BlockPos index) {
        if (index == null || !level.isLoaded(index)) {
            return false;
        }
        BlockState state = level.getBlockState(index);
        return state.getBlock() instanceof LiquidBlock && !state.getFluidState().isEmpty();
    }

    /** The still liquid of a liquid block. */
    private Fluid liquidAt(BlockPos index) {
        FluidState state = level.getFluidState(index);
        return state.isSource() ? state.getType()
                : state.getType() instanceof FlowingFluid flowing
                ? flowing.getSource() : state.getType();
    }

    /** {@code destroy}: the tube vanishes together with the pump. */
    public void onBreak() {
        hasTube = false;
        tubeY = Double.NaN;
        aimY = NO_AIM;
        blocksToPump.clear();
    }

    // --- persistence and sync ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("power", provider.save());
        if (!tank.isEmpty()) {
            tag.put("tank", tank.getFluid().save(registries));
        }
        tag.putInt("aimY", aimY);
        tag.putFloat("tubeY", hasTube ? (float) tubeY : worldPosition.getY());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("power")) {
            provider.load(tag.getCompound("power"));
        }
        tank.setFluid(FluidStack.parseOptional(registries, tag.getCompound("tank")));
        if (tag.contains("aimY")) {
            aimY = tag.getInt("aimY");
            tubeY = tag.getFloat("tubeY");
        }
    }

    private void sendNetworkUpdate() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        if (!tank.isEmpty()) {
            tag.put("tank", tank.getFluid().save(registries));
        }
        tag.putBoolean("tube", hasTube);
        tag.putDouble("tubeY", tubeY);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        readSync(tag, registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        readSync(packet.getTag(), registries);
    }

    private void readSync(CompoundTag tag, HolderLookup.Provider registries) {
        tank.setFluid(FluidStack.parseOptional(registries, tag.getCompound("tank")));
        hasTube = tag.getBoolean("tube");
        tubeY = tag.getDouble("tubeY");
    }

    // --- client view ---

    public boolean hasTube() {
        return hasTube && !Double.isNaN(tubeY);
    }

    public double getTubeY() {
        return tubeY;
    }

    private final class Handler implements IFluidHandler {
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
            return tank.getCapacity();
        }

        @Override
        public boolean isFluidValid(int index, FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return tank.drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return tank.drain(maxDrain, action);
        }
    }
}
