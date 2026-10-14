package dev.craft.core.multiblock;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Formation state of one multiblock, owned by the controller block entity. The structure is re-validated every
 * {@code interval} ticks and immediately after {@link #markDirty()}; it is never persisted and is re-checked after
 * loading.
 */
public final class MultiblockController {
    private final MultiblockPattern pattern;
    private final int interval;
    private final Supplier<Direction> facing;
    private final Listener listener;
    private MultiblockPattern.Match match;
    private boolean dirty = true;

    /**
     * @param facing  facing of the controller, or {@code null} to accept any horizontal facing
     * @param listener notified when the structure forms or breaks
     */
    public MultiblockController(MultiblockPattern pattern, int interval, Supplier<Direction> facing,
                                Listener listener) {
        this.pattern = pattern;
        this.interval = interval;
        this.facing = facing;
        this.listener = listener;
    }

    /** Call every server tick from the controller. */
    public void tick(Level level, BlockPos controllerPos) {
        if (dirty || Math.floorMod(level.getGameTime() + controllerPos.asLong(), interval) == 0) {
            dirty = false;
            validate(level, controllerPos);
        }
    }

    public void markDirty() {
        dirty = true;
    }

    public boolean isFormed() {
        return match != null;
    }

    public Optional<MultiblockPattern.Match> getMatch() {
        return Optional.ofNullable(match);
    }

    /** Forgets the structure, e.g. when the controller is removed. */
    public void invalidate() {
        if (match != null) {
            MultiblockPattern.Match old = match;
            match = null;
            listener.onBroken(old);
        }
        dirty = true;
    }

    private void validate(Level level, BlockPos controllerPos) {
        Optional<MultiblockPattern.Match> result = facing == null
                ? pattern.matchAnyFacing(level, controllerPos)
                : pattern.match(level, controllerPos, facing.get());
        MultiblockPattern.Match old = match;
        match = result.orElse(null);
        if (old != null && !old.equals(match)) {
            listener.onBroken(old);
        }
        if (match != null && !match.equals(old)) {
            listener.onFormed(match);
        }
    }

    public interface Listener {
        void onFormed(MultiblockPattern.Match match);

        void onBroken(MultiblockPattern.Match match);
    }
}
