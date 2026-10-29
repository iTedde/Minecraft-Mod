package dev.craft.coppergolem;

import dev.craft.ironchest.CopperChestBlock;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.pathfinder.Path;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * The golem's item transport (spec 35 §2.3): with an empty hand it takes up to 16 of the first item from the nearest
 * copper chest, with an item it walks to the nearest target ({@link CopperGolems#TARGETS}) that is empty or already
 * holds that item. Each container is looked into for 3 seconds.
 */
public class CopperGolemTransportGoal extends Goal {
    /** ±32 blocks around the golem's block in every direction. */
    public static final int RANGE = 32;
    public static final int INSPECT_TICKS = 60;
    public static final int REST_TICKS = 140;
    public static final int TAKE_COUNT = 16;
    /** A walk that takes longer than this counts as a failure. */
    private static final int MOVE_TIMEOUT = 1200;
    private static final int SEARCH_INTERVAL = 20;

    private enum Phase { MOVE, INSPECT }

    private final CopperGolem golem;
    private BlockPos target;
    private Path path;
    private Phase phase;
    private int timer;
    /** {@code tickCount} until which the golem wanders instead of searching, 0 when not resting. */
    private int restUntil;
    private int nextSearch;

    public CopperGolemTransportGoal(CopperGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (restUntil != 0) {
            // Wandering after too many failures; the stroll goal takes over meanwhile.
            if (golem.tickCount < restUntil) {
                return false;
            }
            restUntil = 0;
            golem.getContainerMemory().forget();
        }
        // Right after spawning or while falling there is no path yet; that is no failed visit.
        if (golem.tickCount < nextSearch || !golem.onGround() && !golem.isInWater()) {
            return false;
        }
        nextSearch = golem.tickCount + SEARCH_INTERVAL;
        ContainerMemory memory = golem.getContainerMemory();
        for (BlockPos candidate : findCandidates((ServerLevel) golem.level(), golem.blockPosition(), holding())) {
            if (memory.isVisited(candidate)) {
                continue;
            }
            boolean near = inReach(candidate);
            Path candidatePath = near ? null : golem.getNavigation().createPath(standPositions(candidate), 0);
            if (near || candidatePath != null && candidatePath.canReach()) {
                target = candidate;
                path = candidatePath;
                return true;
            }
            memory.fail(candidate);
            if (memory.isExhausted()) {
                break;
            }
        }
        // Nothing left to try: wander for 7 seconds, then start over.
        if (!memory.isEmpty()) {
            rest();
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null;
    }

    @Override
    public void start() {
        phase = Phase.MOVE;
        timer = 0;
        golem.getNavigation().moveTo(path, 1.0);
    }

    @Override
    public void stop() {
        if (phase == Phase.INSPECT && target != null) {
            ContainerLid.set((ServerLevel) golem.level(), target, false);
        }
        golem.setInteracting(false);
        golem.getNavigation().stop();
        target = null;
        path = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        // Running goals also tick on the odd ticks, before canContinueToUse is asked again.
        if (target == null) {
            return;
        }
        timer++;
        golem.getLookControl().setLookAt(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
        if (phase == Phase.MOVE) {
            if (inReach(target)) {
                golem.getNavigation().stop();
                phase = Phase.INSPECT;
                timer = 0;
                golem.setInteracting(true);
                ContainerLid.set((ServerLevel) golem.level(), target, true);
            } else if (golem.getNavigation().isDone() || timer > MOVE_TIMEOUT) {
                finish(false);
            }
        } else if (timer >= INSPECT_TICKS) {
            ContainerLid.set((ServerLevel) golem.level(), target, false);
            golem.setInteracting(false);
            phase = null;
            finish(holding() ? deposit() : take());
        }
    }

    private void finish(boolean success) {
        ContainerMemory memory = golem.getContainerMemory();
        if (!success) {
            memory.fail(target);
            if (memory.isExhausted()) {
                rest();
            }
        }
        nextSearch = 0;
        target = null;
    }

    private void rest() {
        restUntil = golem.tickCount + REST_TICKS;
    }

    private boolean holding() {
        return !golem.getMainHandItem().isEmpty();
    }

    /** Standing next to the container, which is at most 1 above or 2 below the golem's feet. */
    private boolean inReach(BlockPos pos) {
        BlockPos feet = golem.blockPosition();
        int dy = pos.getY() - feet.getY();
        return dy >= -2 && dy <= 1 && Math.abs(pos.getX() - feet.getX()) <= 1
                && Math.abs(pos.getZ() - feet.getZ()) <= 1;
    }

    /** Every place to stand from which {@link #inReach} holds; the path must end on one of them. */
    private static Set<BlockPos> standPositions(BlockPos container) {
        Set<BlockPos> positions = new HashSet<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -1; dy <= 2; dy++) {
                    positions.add(container.offset(dx, dy, dz));
                }
            }
        }
        return positions;
    }

    private IItemHandler handler() {
        return golem.level().getCapability(Capabilities.ItemHandler.BLOCK, target, null);
    }

    /** Up to 16 of the item in the first occupied slot. */
    private boolean take() {
        IItemHandler handler = handler();
        if (handler == null || !(golem.level().getBlockState(target).getBlock() instanceof CopperChestBlock)) {
            return false;
        }
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (!handler.getStackInSlot(slot).isEmpty()) {
                ItemStack taken = handler.extractItem(slot, TAKE_COUNT, false);
                if (taken.isEmpty()) {
                    return false;
                }
                golem.setItemInHand(InteractionHand.MAIN_HAND, taken);
                golem.getContainerMemory().succeed();
                return true;
            }
        }
        return false;
    }

    /**
     * Only into an empty container or one that already holds the item (components ignored); what does not fit stays
     * in the hand and this container is remembered, so the golem goes on to the next one.
     */
    private boolean deposit() {
        IItemHandler handler = handler();
        if (handler == null || !golem.level().getBlockState(target).is(CopperGolems.TARGETS)) {
            return false;
        }
        ItemStack held = golem.getMainHandItem();
        boolean empty = true;
        boolean same = false;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            empty &= stack.isEmpty();
            same |= stack.is(held.getItem());
        }
        if (!empty && !same) {
            return false;
        }
        ItemStack rest = held.copy();
        for (int slot = 0; slot < handler.getSlots() && !rest.isEmpty(); slot++) {
            rest = handler.insertItem(slot, rest, false);
        }
        if (rest.getCount() == held.getCount()) {
            return false;
        }
        golem.setItemInHand(InteractionHand.MAIN_HAND, rest);
        ContainerMemory memory = golem.getContainerMemory();
        memory.succeed();
        if (!rest.isEmpty()) {
            memory.remember(target);
        }
        return true;
    }

    /**
     * Containers in the ±32 cube, nearest first and not yet visited: copper chests to take from, otherwise targets.
     * Only the block entities of the loaded chunks in range are looked at.
     */
    static List<BlockPos> findCandidates(ServerLevel level, BlockPos center, boolean holding) {
        int minX = (center.getX() - RANGE) >> 4;
        int maxX = (center.getX() + RANGE) >> 4;
        int minZ = (center.getZ() - RANGE) >> 4;
        int maxZ = (center.getZ() + RANGE) >> 4;
        Stream.Builder<BlockEntity> found = Stream.builder();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
                if (chunk != null) {
                    chunk.getBlockEntities().values().forEach(found);
                }
            }
        }
        return found.build()
                .filter(blockEntity -> isCandidate(blockEntity, center, holding))
                .map(BlockEntity::getBlockPos)
                .sorted(Comparator.comparingDouble(pos -> pos.distSqr(center)))
                .toList();
    }

    private static boolean isCandidate(BlockEntity blockEntity, BlockPos center, boolean holding) {
        BlockPos pos = blockEntity.getBlockPos();
        if (Math.abs(pos.getX() - center.getX()) > RANGE || Math.abs(pos.getY() - center.getY()) > RANGE
                || Math.abs(pos.getZ() - center.getZ()) > RANGE || blockEntity.isRemoved()) {
            return false;
        }
        return holding ? blockEntity.getBlockState().is(CopperGolems.TARGETS)
                : blockEntity.getBlockState().getBlock() instanceof CopperChestBlock;
    }
}
