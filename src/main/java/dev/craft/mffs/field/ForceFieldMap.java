package dev.craft.mffs.field;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * {@code WorldMap}/{@code ForceFieldBlockStack} (spec 48 §9.1): per level and position a queue of claims (capacitor ID,
 * projector ID, field type); the first claim owns the block, the next one takes over when it goes away. {@code sync}
 * marks a position whose block has been placed. Kept in memory only – projectors rebuild it after loading.
 */
public final class ForceFieldMap {
    private static final Map<Level, Map<BlockPos, Stack>> LEVELS = new WeakHashMap<>();

    private ForceFieldMap() {
    }

    private static Map<BlockPos, Stack> positions(Level level) {
        return LEVELS.computeIfAbsent(level, key -> new HashMap<>());
    }

    public static synchronized Stack getOrCreate(Level level, BlockPos pos) {
        return positions(level).computeIfAbsent(pos.immutable(), Stack::new);
    }

    public static synchronized Stack get(Level level, BlockPos pos) {
        return positions(level).get(pos);
    }

    /** One claim on a position. */
    public record Claim(int capacitorId, int projectorId, ForceFieldType type) {
    }

    /** {@code ForceFieldBlockStack}. */
    public static final class Stack {
        private final BlockPos pos;
        private final Deque<Claim> claims = new ArrayDeque<>();
        private boolean sync;

        private Stack(BlockPos pos) {
            this.pos = pos;
        }

        public BlockPos getPos() {
            return pos;
        }

        public boolean isEmpty() {
            return claims.isEmpty();
        }

        public void add(int capacitorId, int projectorId, ForceFieldType type) {
            claims.add(new Claim(capacitorId, projectorId, type));
        }

        public void removeByProjector(int projectorId) {
            claims.removeIf(claim -> claim.projectorId() == projectorId);
        }

        /** Owner's projector ID, 0 when empty. */
        public int getProjectorId() {
            Claim first = claims.peek();
            return first != null ? first.projectorId() : 0;
        }

        /** Owner's capacitor ID ({@code getGenratorID}), 0 when empty. */
        public int getCapacitorId() {
            Claim first = claims.peek();
            return first != null ? first.capacitorId() : 0;
        }

        /** Owner's field type, null when empty. */
        public ForceFieldType getType() {
            Claim first = claims.peek();
            return first != null ? first.type() : null;
        }

        public boolean isSync() {
            return sync;
        }

        public void setSync(boolean sync) {
            this.sync = sync;
        }
    }
}
