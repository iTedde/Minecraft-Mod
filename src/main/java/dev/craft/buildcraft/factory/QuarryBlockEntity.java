package dev.craft.buildcraft.factory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import dev.craft.buildcraft.BcBlockEntityTypes;
import dev.craft.buildcraft.BcBlocks;
import dev.craft.buildcraft.area.AreaProvider;
import dev.craft.buildcraft.area.Box;
import dev.craft.buildcraft.engine.EngineBlockEntity;
import dev.craft.buildcraft.power.PowerProvider;
import dev.craft.buildcraft.power.PowerReceptor;
import dev.craft.buildcraft.util.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

/**
 * Quarry, 1:1 with BuildCraft {@code TileQuarry}: takes over a landmark box (or the default 11×5×11 box behind it),
 * keeps its chunks loaded, has the robot build the frame and then digs the inside layer by layer with the drill
 * head. MJ: 25 MJ per frame order, 60 MJ per block plus the head movement.
 */
public class QuarryBlockEntity extends BlockEntity implements PowerReceptor {
    public static final int MAX_ENERGY = 15000;
    /** {@code forgeChunkLoading.cfg}: {@code maximumChunksPerTicket} of the pack. */
    public static final int MAX_CHUNK_LIST_DEPTH = 25;
    /** BuildCraft {@code itemLifespan} (pack config). */
    private static final int ITEM_LIFESPAN = 1200;

    private final PowerProvider provider = new PowerProvider();
    private final Box box = new Box();
    private boolean inProcess;
    private int targetX;
    private int targetY;
    private int targetZ;
    private double headPosX;
    private double headPosY;
    private double headPosZ;
    private boolean builderDone;
    @Nullable
    private BuilderRobot robot;
    @Nullable
    private FrameBlueprint blueprint;
    private boolean armCreated;
    private boolean isDigging;
    private boolean loadDefaultBoundaries;
    private boolean movingHorizontally;
    private boolean movingVertically;
    private double headTrajectory;
    private boolean isAlive;
    private boolean initialized;
    @Nullable
    private Player placedBy;
    private final Deque<BlockPos> visitList = new ArrayDeque<>();
    private int lastRobotSize = -1;

    // client view
    private int clientRobotSize;
    @Nullable
    private BlockPos clientRobotTarget;
    private boolean clientHasRobot;
    /** Client robot animation (BuildCraft {@code EntityRobot.move} and the energy laser's power average). */
    final RobotView robotView = new RobotView();

    public QuarryBlockEntity(BlockPos pos, BlockState state) {
        super(BcBlockEntityTypes.QUARRY.get(), pos, state);
        provider.configure(25, 100, 25, MAX_ENERGY);
    }

    @Override
    public PowerProvider getPowerProvider() {
        return provider;
    }

    @Override
    public void doWork() {
    }

    public void setPlacedBy(Player player) {
        this.placedBy = player;
    }

    // --- ticking ---

    public void serverTick() {
        if (!initialized) {
            initialized = true;
            initialize();
        }
        provider.update(this, level);
        if (robot != null) {
            robot.tick(level);
            if (robot != null && robot.size() != lastRobotSize) {
                lastRobotSize = robot.size();
                sendNetworkUpdate();
            }
        }
        if (!isAlive) {
            return;
        }
        if (inProcess) {
            float energyToUse = 2.0F + provider.getEnergyStored() / 500.0F;
            float energy = provider.useEnergy(energyToUse, energyToUse, true);
            if (energy > 0.0F) {
                moveHead(0.1 + energy / 200.0F);
            }
            sendNetworkUpdate();
        }
        if (!inProcess && isDigging) {
            createUtilsIfNeeded();
            if (blueprint != null) {
                builderDone = blueprint.isDone();
                if (!builderDone) {
                    buildFrame();
                    setChanged();
                    return;
                }
                if (robot != null && robot.done()) {
                    robot = null;
                    lastRobotSize = -1;
                    sendNetworkUpdate();
                }
            }
            if (robot == null) {
                dig();
            }
        }
        setChanged();
    }

    private void initialize() {
        if (!box.isInitialized()) {
            setBoundaries(loadDefaultBoundaries);
        } else {
            // BuildCraft's chunk loading callback re-forces the chunks of a loaded quarry.
            forceChunkLoading();
        }
        createUtilsIfNeeded();
        sendNetworkUpdate();
    }

    private void createUtilsIfNeeded() {
        if (blueprint == null) {
            if (!box.isInitialized()) {
                setBoundaries(loadDefaultBoundaries);
            }
            initializeBlueprint();
        }
        if (builderDone) {
            if (!armCreated) {
                armCreated = true;
                sendNetworkUpdate();
            }
            if (findTarget(false)) {
                isDigging = true;
                if (headPosX < box.xMin || headPosX > box.xMax || headPosZ < box.zMin || headPosZ > box.zMax) {
                    setHead(box.xMin + 1, worldPosition.getY() + 2, box.zMin + 1);
                }
            }
        } else {
            isDigging = true;
        }
    }

    private void initializeBlueprint() {
        blueprint = new FrameBlueprint(box.xMin, worldPosition.getY(), box.zMin, box.sizeX(), box.sizeY(),
                box.sizeZ());
    }

    /** BuildCraft {@code reinitalize} (wrench): checks and rebuilds the frame. */
    public void reinitialize() {
        builderDone = false;
        initializeBlueprint();
        isDigging = true;
        sendNetworkUpdate();
    }

    private void buildFrame() {
        provider.configure(25, 100, 25, MAX_ENERGY);
        if (provider.useEnergy(25.0F, 25.0F, true) == 25.0F) {
            if (robot == null) {
                robot = new BuilderRobot();
                sendNetworkUpdate();
            }
            if (robot.readyToBuild()) {
                robot.schedule(blueprint.getNextBlock(level));
            }
        }
    }

    private void dig() {
        provider.configure(30, 500, 50, MAX_ENERGY);
        if (provider.useEnergy(60.0F, 60.0F, true) == 60.0F) {
            if (!findTarget(true)) {
                if (armCreated) {
                    setTarget(box.xMin + 1, worldPosition.getY() + 2, box.zMin + 1);
                }
                isDigging = false;
            }
            inProcess = true;
            movingHorizontally = true;
            movingVertically = true;
            headTrajectory = Math.atan2(targetZ - headPosZ, targetX - headPosX);
        }
    }

    private boolean findTarget(boolean doSet) {
        if (visitList.isEmpty()) {
            createColumnVisitList();
        }
        if (!doSet) {
            return !visitList.isEmpty();
        }
        if (visitList.isEmpty()) {
            return false;
        }
        BlockPos target;
        boolean foundTarget;
        do {
            if (visitList.isEmpty()) {
                createColumnVisitList();
                if (visitList.isEmpty()) {
                    return false;
                }
            }
            target = visitList.removeFirst();
            boolean alternativeTarget = false;
            for (int y = target.getY() + 1; y < worldPosition.getY() + 3; y++) {
                BlockPos above = new BlockPos(target.getX(), y, target.getZ());
                if (BlockUtil.canChangeBlock(level, above) && !BlockUtil.isSoftBlock(level, above)) {
                    createColumnVisitList();
                    alternativeTarget = true;
                    break;
                }
            }
            foundTarget = !alternativeTarget;
        } while (!foundTarget);
        setTarget(target.getX(), target.getY() + 1, target.getZ());
        return true;
    }

    /**
     * BuildCraft {@code createColumnVisitList}: serpentine order from {@code y + 3} down to the bottom of the world;
     * a column stops at the first block that cannot be changed. At most {@code 2·sizeX·sizeZ} entries at a time.
     */
    private void createColumnVisitList() {
        visitList.clear();
        int innerX = box.sizeX() - 2;
        int innerZ = box.sizeZ() - 2;
        if (innerX <= 0 || innerZ <= 0) {
            return;
        }
        boolean[][] blockedColumns = new boolean[innerX][innerZ];
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int searchY = worldPosition.getY() + 3; searchY >= level.getMinBuildHeight(); searchY--) {
            int yParity = Math.floorMod(searchY, 2);
            int startX = yParity == 0 ? 0 : innerX - 1;
            int endX = yParity == 0 ? innerX : -1;
            int incX = yParity == 0 ? 1 : -1;
            for (int searchX = startX; searchX != endX; searchX += incX) {
                boolean forward = searchX % 2 == yParity;
                int startZ = forward ? 0 : innerZ - 1;
                int endZ = forward ? innerZ : -1;
                int incZ = forward ? 1 : -1;
                for (int searchZ = startZ; searchZ != endZ; searchZ += incZ) {
                    if (!blockedColumns[searchX][searchZ]) {
                        pos.set(box.xMin + searchX + 1, searchY, box.zMin + searchZ + 1);
                        if (!BlockUtil.canChangeBlock(level, pos)) {
                            blockedColumns[searchX][searchZ] = true;
                        } else if (!BlockUtil.isSoftBlock(level, pos)) {
                            visitList.add(pos.immutable());
                        }
                        if (visitList.size() > box.sizeZ() * box.sizeX() * 2) {
                            return;
                        }
                    }
                }
            }
        }
    }

    private void moveHead(double instantSpeed) {
        if (movingHorizontally) {
            if (Math.abs(targetX - headPosX) < instantSpeed * 2.0 && Math.abs(targetZ - headPosZ) < instantSpeed * 2.0) {
                headPosX = targetX;
                headPosZ = targetZ;
                movingHorizontally = false;
                if (!movingVertically) {
                    positionReached();
                    headPosY = targetY;
                }
            } else {
                headPosX += Math.cos(headTrajectory) * instantSpeed;
                headPosZ += Math.sin(headTrajectory) * instantSpeed;
            }
        }
        if (movingVertically) {
            if (Math.abs(targetY - headPosY) < instantSpeed * 2.0) {
                headPosY = targetY;
                movingVertically = false;
                if (!movingHorizontally) {
                    positionReached();
                    headPosX = targetX;
                    headPosZ = targetZ;
                }
            } else if (targetY > headPosY) {
                headPosY += instantSpeed;
            } else {
                headPosY -= instantSpeed;
            }
        }
    }

    private void positionReached() {
        inProcess = false;
        BlockPos pos = new BlockPos(targetX, targetY - 1, targetZ);
        if (isQuarriableBlock(pos)) {
            BlockState state = level.getBlockState(pos);
            List<ItemStack> stacks = Block.getDrops(state, (ServerLevel) level, pos, level.getBlockEntity(pos));
            for (ItemStack stack : stacks) {
                if (!stack.isEmpty()) {
                    mineStack(stack);
                }
            }
            level.levelEvent(2001, pos, Block.getId(state));
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
        AABB area = new AABB(headPosX - 2.0, headPosY - 2.0, headPosZ - 2.0, headPosX + 3.0, headPosY + 3.0,
                headPosZ + 3.0);
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, area)) {
            if (entity.isAlive()) {
                ItemStack mineable = entity.getItem().copy();
                if (mineable.getCount() > 0) {
                    entity.discard();
                    mineStack(mineable);
                }
            }
        }
    }

    private boolean isQuarriableBlock(BlockPos pos) {
        return BlockUtil.canChangeBlock(level, pos) && !BlockUtil.isSoftBlock(level, pos);
    }

    /** Random adjacent inventory first (pipes come later), then the rest flies out above the quarry. */
    private void mineStack(ItemStack stack) {
        ItemStack rest = addToRandomInventory(stack);
        if (!rest.isEmpty()) {
            float f = level.random.nextFloat() * 0.8F + 0.1F;
            float f1 = level.random.nextFloat() * 0.8F + 0.1F;
            float f2 = level.random.nextFloat() * 0.8F + 0.1F;
            ItemEntity entity = new ItemEntity(level, worldPosition.getX() + f, worldPosition.getY() + f1 + 0.5F,
                    worldPosition.getZ() + f2, rest);
            entity.lifespan = ITEM_LIFESPAN;
            entity.setPickUpDelay(10);
            float f3 = 0.05F;
            entity.setDeltaMovement((float) level.random.nextGaussian() * f3,
                    (float) level.random.nextGaussian() * f3 + 1.0F, (float) level.random.nextGaussian() * f3);
            level.addFreshEntity(entity);
        }
    }

    /** BuildCraft {@code Utils.addToRandomInventory}: one random neighbour (no engine) that takes something. */
    private ItemStack addToRandomInventory(ItemStack stack) {
        List<IItemHandler> possible = new ArrayList<>();
        for (Direction side : Direction.values()) {
            BlockPos adjacent = worldPosition.relative(side);
            if (level.getBlockEntity(adjacent) instanceof EngineBlockEntity) {
                continue;
            }
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, adjacent, side.getOpposite());
            if (handler != null
                    && ItemHandlerHelper.insertItemStacked(handler, stack.copy(), true).getCount() < stack.getCount()) {
                possible.add(handler);
            }
        }
        if (possible.isEmpty()) {
            return stack;
        }
        return ItemHandlerHelper.insertItemStacked(possible.get(level.random.nextInt(possible.size())), stack, false);
    }

    private void setHead(double x, double y, double z) {
        headPosX = x;
        headPosY = y;
        headPosZ = z;
    }

    private void setTarget(int x, int y, int z) {
        targetX = x;
        targetY = y;
        targetZ = z;
    }

    // --- area and chunk loading ---

    private void setBoundaries(boolean useDefault) {
        AreaProvider area = null;
        if (!useDefault) {
            area = AreaProvider.findNearby(level, worldPosition);
        }
        int x = worldPosition.getX();
        int y = worldPosition.getY();
        int z = worldPosition.getZ();
        if (area == null) {
            area = AreaProvider.of(x, y, z, x + 10, y + 4, z + 10);
            useDefault = true;
        }
        int xSize = area.xMax() - area.xMin() + 1;
        int zSize = area.zMax() - area.zMin() + 1;
        if (xSize < 3 || zSize < 3 || xSize * zSize >> 8 >= MAX_CHUNK_LIST_DEPTH) {
            if (placedBy != null) {
                placedBy.sendSystemMessage(Component.translatable("message.craft.quarry.size", xSize, zSize,
                        MAX_CHUNK_LIST_DEPTH));
            }
            area = AreaProvider.of(x, y, z, x + 10, y + 4, z + 10);
            useDefault = true;
        }
        xSize = area.xMax() - area.xMin() + 1;
        int ySize = area.yMax() - area.yMin() + 1;
        zSize = area.zMax() - area.zMin() + 1;
        box.initialize(area);
        if (ySize < 5) {
            ySize = 5;
            box.yMax = box.yMin + ySize - 1;
        }
        if (useDefault) {
            int xMin;
            int zMin;
            switch (getBlockState().getValue(QuarryBlock.FACING).getOpposite()) {
                case EAST -> {
                    xMin = x + 1;
                    zMin = z - 4 - 1;
                }
                case WEST -> {
                    xMin = x - 9 - 2;
                    zMin = z - 4 - 1;
                }
                case SOUTH -> {
                    xMin = x - 4 - 1;
                    zMin = z + 1;
                }
                default -> {
                    xMin = x - 4 - 1;
                    zMin = z - 9 - 2;
                }
            }
            box.initialize(xMin, y, zMin, xMin + xSize - 1, y + ySize - 1, zMin + zSize - 1);
        }
        area.removeFromWorld();
        forceChunkLoading();
        if (placedBy != null) {
            placedBy.sendSystemMessage(Component.translatable("message.craft.quarry.chunks", x, y, z,
                    forcedChunks().size()));
        }
    }

    /** The quarry's chunk and every chunk of the box. */
    private Set<ChunkPos> forcedChunks() {
        Set<ChunkPos> chunks = new LinkedHashSet<>();
        chunks.add(new ChunkPos(worldPosition));
        for (int chunkX = box.xMin >> 4; chunkX <= box.xMax >> 4; chunkX++) {
            for (int chunkZ = box.zMin >> 4; chunkZ <= box.zMax >> 4; chunkZ++) {
                chunks.add(new ChunkPos(chunkX, chunkZ));
            }
        }
        return chunks;
    }

    private void forceChunkLoading() {
        isAlive = true;
        for (ChunkPos chunk : forcedChunks()) {
            QuarryChunkLoading.CONTROLLER.forceChunk((ServerLevel) level, worldPosition, chunk.x, chunk.z, true,
                    true);
        }
        sendNetworkUpdate();
    }

    /** Called when the quarry block is removed: lets the frame decay and releases the chunks. */
    public void onBreak() {
        if (box.isInitialized() && box.xMax != Integer.MAX_VALUE) {
            for (int x = box.xMin; x <= box.xMax; x++) {
                markFrameForDecay(x, box.yMin, box.zMin);
                markFrameForDecay(x, box.yMax, box.zMin);
                markFrameForDecay(x, box.yMin, box.zMax);
                markFrameForDecay(x, box.yMax, box.zMax);
            }
            for (int z = box.zMin + 1; z <= box.zMax - 1; z++) {
                markFrameForDecay(box.xMin, box.yMin, z);
                markFrameForDecay(box.xMax, box.yMin, z);
                markFrameForDecay(box.xMin, box.yMax, z);
                markFrameForDecay(box.xMax, box.yMax, z);
            }
            for (int y = box.yMin + 1; y <= box.yMax - 1; y++) {
                markFrameForDecay(box.xMin, y, box.zMin);
                markFrameForDecay(box.xMax, y, box.zMin);
                markFrameForDecay(box.xMin, y, box.zMax);
                markFrameForDecay(box.xMax, y, box.zMax);
            }
        }
        Set<ChunkPos> chunks = box.isInitialized() && box.xMax != Integer.MAX_VALUE ? forcedChunks()
                : Set.of(new ChunkPos(worldPosition));
        for (ChunkPos chunk : chunks) {
            QuarryChunkLoading.CONTROLLER.forceChunk((ServerLevel) level, worldPosition, chunk.x, chunk.z, false,
                    true);
        }
        robot = null;
    }

    private void markFrameForDecay(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = level.getBlockState(pos);
        if (state.is(BcBlocks.FRAME.get())) {
            level.setBlock(pos, state.setValue(FrameBlock.DECAYING, true), 2);
        }
    }

    // --- persistence (builderDone, inProcess, isDigging and the robot are not saved, as in the original) ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("power", provider.save());
        tag.putInt("targetX", targetX);
        tag.putInt("targetY", targetY);
        tag.putInt("targetZ", targetZ);
        tag.putDouble("headPosX", headPosX);
        tag.putDouble("headPosY", headPosY);
        tag.putDouble("headPosZ", headPosZ);
        if (box.isInitialized()) {
            tag.put("box", box.save());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("power")) {
            provider.load(tag.getCompound("power"));
        }
        if (tag.contains("box")) {
            box.load(tag.getCompound("box"));
            loadDefaultBoundaries = false;
        } else {
            loadDefaultBoundaries = true;
        }
        targetX = tag.getInt("targetX");
        targetY = tag.getInt("targetY");
        targetZ = tag.getInt("targetZ");
        headPosX = tag.getDouble("headPosX");
        headPosY = tag.getDouble("headPosY");
        headPosZ = tag.getDouble("headPosZ");
    }

    // --- sync ---

    private void sendNetworkUpdate() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    private CompoundTag writeSync() {
        CompoundTag tag = new CompoundTag();
        if (box.isInitialized()) {
            tag.put("box", box.save());
        }
        tag.putBoolean("alive", isAlive);
        tag.putBoolean("builderDone", builderDone);
        tag.putBoolean("arm", armCreated);
        tag.putDouble("headX", headPosX);
        tag.putDouble("headY", headPosY);
        tag.putDouble("headZ", headPosZ);
        if (robot != null) {
            tag.putInt("robot", robot.size());
            BlockPos target = robot.currentTarget();
            if (target != null) {
                tag.put("robotTarget", NbtUtils.writeBlockPos(target));
            }
        }
        return tag;
    }

    private void readSync(CompoundTag tag) {
        if (tag.contains("box")) {
            box.load(tag.getCompound("box"));
        } else {
            box.reset();
        }
        isAlive = tag.getBoolean("alive");
        builderDone = tag.getBoolean("builderDone");
        armCreated = tag.getBoolean("arm");
        headPosX = tag.getDouble("headX");
        headPosY = tag.getDouble("headY");
        headPosZ = tag.getDouble("headZ");
        clientHasRobot = tag.contains("robot");
        clientRobotSize = tag.getInt("robot");
        clientRobotTarget = NbtUtils.readBlockPos(tag, "robotTarget").orElse(null);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return writeSync();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        readSync(tag);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        readSync(packet.getTag());
    }

    // --- client view ---

    public void clientTick() {
        if (clientHasRobot && isAlive && box.isInitialized()) {
            robotView.tick(level, box, clientRobotSize);
        } else {
            robotView.reset();
        }
    }

    public Box getBox() {
        return box;
    }

    public boolean isAliveForRender() {
        return isAlive;
    }

    /** Yellow/black stripe lasers around the box while the frame is being built. */
    public boolean showsStripes() {
        return isAlive && box.isInitialized() && !builderDone;
    }

    public boolean hasArm() {
        return isAlive && box.isInitialized() && armCreated;
    }

    /** Head position as the original arm entity sees it (rounded down to 1/32 block). */
    public double armHead(int axis) {
        double value = axis == 0 ? headPosX : axis == 1 ? headPosY : headPosZ;
        return (int) (value * 32.0) / 32.0;
    }

    public boolean hasRobot() {
        return clientHasRobot && robotView.isInitialized();
    }

    @Nullable
    public BlockPos getRobotTarget() {
        return clientRobotTarget;
    }

    public RobotView getRobotView() {
        return robotView;
    }

    /** Client-side flight of the construction robot (purely visual). */
    public static final class RobotView {
        private static final int POWER_AVERAGING = 100;
        private boolean initialized;
        private double x;
        private double y;
        private double z;
        private double prevX;
        private double prevY;
        private double prevZ;
        private int destX;
        private int destY;
        private int destZ;
        private double motionX;
        private double motionY;
        private double motionZ;
        private final float[] power = new float[POWER_AVERAGING];
        private int powerIndex;
        private float powerSum;
        private int displayStage;

        void reset() {
            initialized = false;
        }

        boolean isInitialized() {
            return initialized;
        }

        void tick(Level level, Box box, int targets) {
            if (!initialized) {
                initialized = true;
                destX = (int) box.centerX();
                destY = (int) box.centerY();
                destZ = (int) box.centerZ();
                x = prevX = destX;
                y = prevY = destY;
                z = prevZ = destZ;
                motionX = motionY = motionZ = 0.0;
                Arrays.fill(power, 0.0F);
                powerSum = 0.0F;
                powerIndex = 0;
            }
            prevX = x;
            prevY = y;
            prevZ = z;
            x += motionX;
            y += motionY;
            z += motionZ;
            double dx = destX - x;
            double dy = destY - y;
            double dz = destZ - z;
            if (Math.sqrt(dx * dx + dy * dy + dz * dz) <= 0.2) {
                Box boundary = box.shifted(1);
                List<BlockPos> candidates = new ArrayList<>();
                for (BlockPos pos : BlockPos.betweenClosed(destX - 1, destY - 1, destZ - 1, destX + 1, destY + 1,
                        destZ + 1)) {
                    if (BlockUtil.isSoftBlock(level, pos) && boundary.contains(pos.getX(), pos.getY(), pos.getZ())) {
                        candidates.add(pos.immutable());
                    }
                }
                if (!candidates.isEmpty()) {
                    BlockPos dest = candidates.get(level.random.nextInt(candidates.size()));
                    destX = dest.getX();
                    destY = dest.getY();
                    destZ = dest.getZ();
                    float factor = getPowerAverage() / 2.0F + 1.0F;
                    motionX = (destX - x) / 75.0 * factor;
                    motionY = (destY - y) / 75.0 * factor;
                    motionZ = (destZ - z) / 75.0 * factor;
                }
            }
            float received = (float) targets / BuilderRobot.MAX_TARGETS * 4.0F;
            powerSum -= power[powerIndex];
            powerSum += received;
            power[powerIndex] = received;
            powerIndex = (powerIndex + 1) % POWER_AVERAGING;
        }

        public float getPowerAverage() {
            return powerSum / POWER_AVERAGING;
        }

        public double x(float partialTick) {
            return Mth.lerp(partialTick, prevX, x);
        }

        public double y(float partialTick) {
            return Mth.lerp(partialTick, prevY, y);
        }

        public double z(float partialTick) {
            return Mth.lerp(partialTick, prevZ, z);
        }

        /** {@code RenderEnergyLaser}: animation stage 0..399, advanced once per drawn frame. */
        public int nextDisplayStage() {
            int stage = displayStage;
            displayStage = (displayStage + 1) % 400;
            return stage;
        }
    }
}
