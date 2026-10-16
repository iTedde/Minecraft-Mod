package dev.craft.buildcraft.transport;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

import dev.craft.buildcraft.BcBlockEntityTypes;
import dev.craft.buildcraft.factory.PumpBlockEntity;
import dev.craft.buildcraft.factory.RefineryBlockEntity;
import dev.craft.buildcraft.power.SafeTimeTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * BuildCraft {@code PipeTransportLiquids} with {@code PipeLiquidsWood}/{@code PipeLiquidsEmerald} (extraction),
 * {@code PipeLiquidsSandstone} (only filled by pipes) and {@code PipeLiquidsVoid} (destroys everything).
 * Seven sections (six sides + center) of 250 mB; fluid that flowed in only becomes available after
 * {@code travelDelay} ticks.
 */
public class FluidPipeBlockEntity extends PipeBlockEntity {
    public static final int CAPACITY = 250;
    public static final int CENTER = 6;
    static final int INPUT_TTL = 60;
    static final int OUTPUT_TTL = 80;
    static final int OUTPUT_COOLDOWN = 30;
    /** {@code BuildCraftCore.updateFactor} / {@code longUpdateFactor} (pack: 10 / 40). */
    private static final int UPDATE_FACTOR = 10;
    private static final int LONG_UPDATE_FACTOR = 40;
    /** The section's {@code incomming} ring is created before the gold values are set, so it always has 12 slots. */
    private static final int INCOMING_SLOTS = 12;

    private final int flowRate;
    private final int travelDelay;
    private final Section[] sections = new Section[7];
    private final TransferState[] transferState = new TransferState[6];
    private final int[] inputPerTick = new int[6];
    private final int[] inputTTL = new int[6];
    private final int[] outputTTL = new int[6];
    private final int[] outputCooldown = new int[6];
    private final IFluidHandler[] handlers = new IFluidHandler[7];
    private final SafeTimeTracker tracker = new SafeTimeTracker();
    private int clientSyncCounter;
    /** Smoothed amounts shown by the renderer; on the client set by {@link PipeFluidPayload}. */
    private FluidStack[] renderCache = emptyCache();
    /** {@code PipeLiquidsWood.liquidToExtract}: extraction budget in mB, 1 MJ = 1000 mB. Not saved (network field). */
    private int liquidToExtract;

    public FluidPipeBlockEntity(BlockPos pos, BlockState state) {
        super(BcBlockEntityTypes.FLUID_PIPE.get(), pos, state);
        // PipeLiquidsGold / PipeLiquidsEmerald: 40 mB, 4 ticks; all others 10 mB, 12 ticks.
        PipeMaterial material = getPipeType().getMaterial();
        boolean fast = material == PipeMaterial.GOLD || material == PipeMaterial.EMERALD;
        flowRate = fast ? 40 : 10;
        travelDelay = fast ? 4 : 12;
        for (int i = 0; i < 7; i++) {
            sections[i] = new Section();
            handlers[i] = new Handler(i);
        }
        for (int i = 0; i < 6; i++) {
            transferState[i] = TransferState.NONE;
            outputTTL[i] = OUTPUT_TTL;
        }
    }

    private static FluidStack[] emptyCache() {
        FluidStack[] cache = new FluidStack[7];
        Arrays.fill(cache, FluidStack.EMPTY);
        return cache;
    }

    private PipeMaterial material() {
        return getPipeType().getMaterial();
    }

    public int getFlowRate() {
        return flowRate;
    }

    /** Content of section {@code index} (0–5 = side, 6 = center), for tests. */
    public FluidStack getFluid(int index) {
        return sections[index].fluid;
    }

    public TransferState getTransferState(Direction side) {
        return transferState[side.get3DDataValue()];
    }

    /** Displayed content of each section (0–5 = side, 6 = center). */
    public FluidStack[] getRenderCache() {
        return renderCache;
    }

    /** Capability: fills the section of {@code side} ({@code null} = center), never drains. */
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return handlers[side == null ? CENTER : side.get3DDataValue()];
    }

    /** {@code isPipeConnected}: a tank with at least one tank on this side, the pump or the refinery. */
    @Override
    protected boolean acceptsNonPipe(BlockEntity other, Direction side) {
        if (other instanceof PumpBlockEntity || other instanceof RefineryBlockEntity) {
            return true;
        }
        IFluidHandler handler = PipeConnections.fluidHandler(level, worldPosition, side);
        return handler != null && handler.getTanks() > 0;
    }

    // --- external fill ---

    /**
     * {@code fill(tankIndex, …)}: the sandstone pipe only takes fluid from pipes, the void pipe takes everything
     * and keeps nothing. A side that received something becomes an input for {@value #INPUT_TTL} ticks.
     */
    public int fill(int index, FluidStack resource, boolean doFill) {
        int filled;
        if (material() == PipeMaterial.SANDSTONE) {
            filled = index != CENTER && level != null && level.getBlockEntity(
                    worldPosition.relative(Direction.from3DDataValue(index))) instanceof PipeBlockEntity
                    ? sections[index].fill(resource, doFill) : 0;
        } else if (material() == PipeMaterial.VOID) {
            filled = resource.getAmount();
        } else {
            filled = sections[index].fill(resource, doFill);
        }
        if (filled > 0 && doFill && index != CENTER) {
            transferState[index] = TransferState.INPUT;
            inputTTL[index] = INPUT_TTL;
        }
        return filled;
    }

    // --- tick ---

    @Override
    protected void tickTransport() {
        if (level.isClientSide()) {
            return;
        }
        moveLiquids();
        if (tracker.markTimeIfDelay(level, UPDATE_FACTOR)) {
            boolean init = false;
            if (++clientSyncCounter > LONG_UPDATE_FACTOR) {
                clientSyncCounter = 0;
                init = true;
            }
            if (computeLiquidUpdate(init) && level instanceof ServerLevel serverLevel) {
                PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(worldPosition),
                        new PipeFluidPayload(worldPosition, List.of(renderCache)));
            }
        }
        if (material().isWood()) {
            extract();
        }
    }

    private void moveLiquids() {
        int slot = (int) (level.getGameTime() % travelDelay);
        int outputCount = computeCurrentConnectionStatesAndTickFlows(slot);
        moveFromPipe(outputCount);
        moveFromCenter(outputCount);
        moveToCenter();
    }

    private int computeCurrentConnectionStatesAndTickFlows(int slot) {
        int outputCount = 0;
        for (Section section : sections) {
            section.time = slot;
            section.incoming[slot] = 0;
        }
        for (Direction side : Direction.values()) {
            int i = side.get3DDataValue();
            if (transferState[i] == TransferState.INPUT) {
                if (--inputTTL[i] <= 0) {
                    transferState[i] = TransferState.NONE;
                }
            } else if (!outputOpen(side)) {
                transferState[i] = TransferState.NONE;
            } else if (outputCooldown[i] > 0) {
                outputCooldown[i]--;
            } else if (outputTTL[i] <= 0) {
                transferState[i] = TransferState.NONE;
                outputCooldown[i] = OUTPUT_COOLDOWN;
                outputTTL[i] = OUTPUT_TTL;
            } else if (canReceiveLiquid(side)) {
                transferState[i] = TransferState.OUTPUT;
                outputCount++;
            }
        }
        return outputCount;
    }

    /** {@code PipeLogicIron}: only the output side; {@code PipeLogicWood} (liquids): never the source side. */
    private boolean outputOpen(Direction side) {
        BlockState state = getBlockState();
        if (state.hasProperty(GenericPipeBlock.OUTPUT)) {
            return state.getValue(GenericPipeBlock.OUTPUT) == side.get3DDataValue();
        }
        if (state.hasProperty(GenericPipeBlock.SOURCE)) {
            return state.getValue(GenericPipeBlock.SOURCE) != side.get3DDataValue();
        }
        return true;
    }

    /** {@code canReceiveLiquid}: connected and a pipe or a tank. */
    private boolean canReceiveLiquid(Direction side) {
        if (!PipeConnections.canConnect(level, worldPosition, side)) {
            return false;
        }
        return level.getBlockEntity(worldPosition.relative(side)) instanceof PipeBlockEntity
                || PipeConnections.fluidHandler(level, worldPosition, side) != null;
    }

    private void moveFromPipe(int outputCount) {
        if (outputCount <= 0) {
            return;
        }
        for (Direction side : Direction.values()) {
            int i = side.get3DDataValue();
            if (transferState[i] != TransferState.OUTPUT) {
                continue;
            }
            IFluidHandler target = PipeConnections.fluidHandler(level, worldPosition, side);
            if (target == null) {
                continue;
            }
            Section section = sections[i];
            int toPush = section.drain(flowRate, false);
            if (toPush > 0) {
                int filled = target.fill(section.fluid.copyWithAmount(toPush), IFluidHandler.FluidAction.EXECUTE);
                section.drain(filled, true);
                if (filled <= 0) {
                    outputTTL[i]--;
                }
            }
        }
    }

    private void moveFromCenter(int outputCount) {
        Section center = sections[CENTER];
        int totalAvailable = center.getAvailable();
        if (totalAvailable < 1 || center.fluid.isEmpty()) {
            return;
        }
        FluidStack testStack = center.fluid.copyWithAmount(flowRate);
        for (Direction side : Direction.values()) {
            int i = side.get3DDataValue();
            if (transferState[i] != TransferState.OUTPUT) {
                continue;
            }
            int available = sections[i].fill(testStack, false);
            int amountToPush = (int) ((double) available / flowRate / outputCount
                    * Math.min(flowRate, totalAvailable));
            if (amountToPush < 1) {
                amountToPush++;
            }
            FluidStack pushed = center.fluid;
            int toPush = center.drain(amountToPush, false);
            if (toPush >= 0) {
                int filled = sections[i].fill(pushed.copyWithAmount(toPush), true);
                center.drain(filled, true);
            }
        }
    }

    private void moveToCenter() {
        Section center = sections[CENTER];
        int transferInCount = 0;
        int inCenter = center.drain(flowRate, false);
        int spaceAvailable = CAPACITY;
        if (inCenter >= 0) {
            spaceAvailable -= inCenter;
        }
        for (Direction side : Direction.values()) {
            int i = side.get3DDataValue();
            inputPerTick[i] = 0;
            if (transferState[i] != TransferState.OUTPUT) {
                int test = sections[i].drain(flowRate, false);
                if (test >= 0 && (inCenter < 0
                        || FluidStack.isSameFluidSameComponents(center.fluid, sections[i].fluid))) {
                    // A section whose content is not yet available still counts (drain returns an empty stack).
                    inputPerTick[i] = test;
                    transferInCount++;
                }
            }
        }
        for (Direction side : Direction.values()) {
            int i = side.get3DDataValue();
            if (transferState[i] == TransferState.OUTPUT || inputPerTick[i] <= 0) {
                continue;
            }
            int amountToDrain = (int) ((double) inputPerTick[i] / flowRate / transferInCount
                    * Math.min(flowRate, spaceAvailable));
            if (amountToDrain < 1) {
                amountToDrain++;
            }
            FluidStack pushed = sections[i].fluid;
            int toPush = sections[i].drain(amountToDrain, false);
            if (toPush >= 0) {
                int filled = center.fill(pushed.copyWithAmount(toPush), true);
                sections[i].drain(filled, true);
            }
        }
    }

    /**
     * {@code PipeLiquidsWood.updateEntity}: with budget, up to {@code flowRate} from the source tank into the section
     * of the source side (via {@link #fill}, so the side becomes an input).
     */
    private void extract() {
        Direction source = getSource();
        if (liquidToExtract <= 0 || source == null) {
            return;
        }
        IFluidHandler tank = PipeConnections.fluidHandler(level, worldPosition, source);
        if (tank == null) {
            return;
        }
        FluidStack extracted = tank.drain(Math.min(liquidToExtract, flowRate), IFluidHandler.FluidAction.SIMULATE);
        int inserted = 0;
        if (!extracted.isEmpty()) {
            inserted = fill(source.get3DDataValue(), extracted, true);
            tank.drain(extracted.copyWithAmount(inserted), IFluidHandler.FluidAction.EXECUTE);
        }
        liquidToExtract -= inserted;
    }

    /** {@code PipeLiquidsWood.doWork}: a tank on the source side and at most 1000 mB budget → 1 MJ for 1000 mB. */
    @Override
    public void doWork() {
        Direction source = getSource();
        if (provider == null || provider.getEnergyStored() <= 0.0F || source == null
                || PipeConnections.fluidHandler(level, worldPosition, source) == null) {
            return;
        }
        if (liquidToExtract <= 1000) {
            liquidToExtract += (int) (provider.useEnergy(1.0F, 1.0F, true) * 1000.0F);
        }
    }

    @Override
    protected void onNeighborBlockChange() {
        super.onNeighborBlockChange();
        for (Direction side : Direction.values()) {
            if (!PipeConnections.canConnect(level, worldPosition, side)) {
                int i = side.get3DDataValue();
                sections[i].reset();
                transferState[i] = TransferState.NONE;
                renderCache[i] = FluidStack.EMPTY;
            }
        }
    }

    /** Liquids are lost when the pipe is broken. */
    @Override
    public void dropContents() {
    }

    // --- client sync ---

    /**
     * {@code computeLiquidUpdate}: moves the display amounts towards the real ones ({@code (old·4 + new)/5}, the real
     * amount at once when that would show nothing or on a full update); true if something changed.
     */
    private boolean computeLiquidUpdate(boolean init) {
        boolean changed = false;
        for (int i = 0; i < 7; i++) {
            FluidStack current = sections[i].fluid;
            FluidStack prev = renderCache[i];
            if (prev.isEmpty() && current.isEmpty()) {
                continue;
            }
            if (prev.isEmpty() || current.isEmpty()) {
                renderCache[i] = current.copy();
                changed = true;
                continue;
            }
            int displayQty = (prev.getAmount() * 4 + current.getAmount()) / 5;
            if (displayQty == 0 && current.getAmount() > 0 || init) {
                displayQty = current.getAmount();
            }
            displayQty = Math.min(CAPACITY, displayQty);
            if (!FluidStack.isSameFluidSameComponents(prev, current) || prev.getAmount() != displayQty || init) {
                renderCache[i] = current.copyWithAmount(displayQty);
                changed = true;
            }
        }
        return changed || init;
    }

    void handleFluidPacket(PipeFluidPayload payload) {
        renderCache = payload.fluids().toArray(FluidStack[]::new);
    }

    // --- saving ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        for (int i = 0; i < 7; i++) {
            CompoundTag sectionTag = new CompoundTag();
            sectionTag.putInt("capacity", CAPACITY);
            if (!sections[i].fluid.isEmpty()) {
                sectionTag.put("fluid", sections[i].fluid.save(registries));
            }
            tag.put("tank" + i, sectionTag);
            if (i < 6) {
                tag.putShort("transferState" + i, (short) transferState[i].ordinal());
            }
        }
    }

    /** The {@code incoming} ring is not saved (original bug: {@code writeToNBT} reads instead of writing). */
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for (int i = 0; i < 7; i++) {
            CompoundTag sectionTag = tag.getCompound("tank" + i);
            sections[i].fluid = sectionTag.contains("fluid")
                    ? FluidStack.parseOptional(registries, sectionTag.getCompound("fluid")) : FluidStack.EMPTY;
            Arrays.fill(sections[i].incoming, (short) 0);
            if (i < 6) {
                transferState[i] = TransferState.values()[tag.getShort("transferState" + i)];
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < 7; i++) {
            if (!renderCache[i].isEmpty()) {
                tag.put("render" + i, renderCache[i].save(registries));
            }
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        FluidStack[] cache = emptyCache();
        for (int i = 0; i < 7; i++) {
            if (tag.contains("render" + i)) {
                cache[i] = FluidStack.parseOptional(registries, tag.getCompound("render" + i));
            }
        }
        renderCache = cache;
    }

    public enum TransferState {
        NONE, INPUT, OUTPUT
    }

    /** {@code PipeSection}: a 250 mB tank that takes at most {@code flowRate} per tick. */
    private final class Section {
        private FluidStack fluid = FluidStack.EMPTY;
        private final short[] incoming = new short[INCOMING_SLOTS];
        private int time;

        /** Forge {@code LiquidTank.fill}, limited to what may still flow in during this tick. */
        int fill(FluidStack resource, boolean doFill) {
            if (resource.isEmpty()) {
                return 0;
            }
            int maxToFill = Math.min(resource.getAmount(), flowRate - incoming[time]);
            if (maxToFill <= 0) {
                return 0;
            }
            int filled;
            if (fluid.isEmpty()) {
                filled = Math.min(maxToFill, CAPACITY);
                if (doFill) {
                    fluid = resource.copyWithAmount(filled);
                }
            } else if (!FluidStack.isSameFluidSameComponents(fluid, resource)) {
                return 0;
            } else {
                filled = Math.min(maxToFill, CAPACITY - fluid.getAmount());
                if (doFill) {
                    fluid.grow(filled);
                }
            }
            if (doFill) {
                incoming[time] += (short) filled;
            }
            return filled;
        }

        /**
         * Forge {@code LiquidTank.drain} limited to {@code min(flowRate, available)}; returns the drained amount, which
         * may be 0 while the section holds fluid, or −1 for the original's {@code null} (empty section).
         */
        int drain(int maxDrain, boolean doDrain) {
            int maxToDrain = Math.min(maxDrain, Math.min(flowRate, getAvailable()));
            if (maxToDrain < 0 || fluid.isEmpty()) {
                return -1;
            }
            int used = Math.min(maxToDrain, fluid.getAmount());
            if (doDrain) {
                fluid.shrink(used);
                if (fluid.isEmpty()) {
                    fluid = FluidStack.EMPTY;
                }
            }
            return used;
        }

        int getAvailable() {
            int all = fluid.getAmount();
            for (short in : incoming) {
                all -= in;
            }
            return all;
        }

        void reset() {
            fluid = FluidStack.EMPTY;
            Arrays.fill(incoming, (short) 0);
        }
    }

    /** {@code ITankContainer} of the pipe for one side: all seven sections, fill only. */
    private final class Handler implements IFluidHandler {
        private final int index;

        Handler(int index) {
            this.index = index;
        }

        @Override
        public int getTanks() {
            return 7;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return sections[tank].fluid;
        }

        @Override
        public int getTankCapacity(int tank) {
            return CAPACITY;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return FluidPipeBlockEntity.this.fill(index, resource, action.execute());
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
