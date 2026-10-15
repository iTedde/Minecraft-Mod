package dev.craft.buildcraft.factory;

import java.util.ArrayDeque;
import java.util.Deque;

import dev.craft.buildcraft.util.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

/**
 * Server side of BuildCraft {@code EntityRobot}: the construction queue of the quarry. The flying robot itself is
 * only drawn by the client ({@link QuarryBlockEntity#clientTick()}).
 */
final class BuilderRobot {
    static final int MAX_TARGETS = 20;

    private final Deque<FrameBlueprint.Slot> targets = new ArrayDeque<>();
    private int wait;

    boolean readyToBuild() {
        return targets.size() < MAX_TARGETS;
    }

    boolean done() {
        return targets.isEmpty();
    }

    int size() {
        return targets.size();
    }

    @Nullable
    BlockPos currentTarget() {
        FrameBlueprint.Slot slot = targets.peekFirst();
        return slot == null ? null : slot.pos();
    }

    void schedule(@Nullable FrameBlueprint.Slot slot) {
        if (slot != null) {
            targets.addLast(slot);
        }
    }

    /**
     * BuildCraft {@code build}: executes the first order once the wait is over. An order on a block that cannot be
     * changed (lava, oil, bedrock) stays at the head of the queue forever, as in the original.
     */
    void tick(Level level) {
        if (!targets.isEmpty()) {
            if (wait == 0) {
                wait = MAX_TARGETS - targets.size() + 2;
            } else {
                wait--;
            }
        }
        FrameBlueprint.Slot slot = targets.peekFirst();
        if (slot != null && wait <= 0 && BlockUtil.canChangeBlock(level, slot.pos())) {
            if (slot.clear()) {
                if (!slot.isValid(level)) {
                    level.setBlock(slot.pos(), Blocks.AIR.defaultBlockState(), 3);
                }
            } else {
                FrameBlueprint.build(level, slot);
            }
            targets.removeFirst();
        }
    }
}
