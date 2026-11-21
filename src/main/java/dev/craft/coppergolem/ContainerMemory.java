package dev.craft.coppergolem;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.core.BlockPos;

/**
 * The golem's memory of visited containers (spec 35 §2.3): at most 9 are remembered, 10 failures in a row send it
 * wandering, after which it forgets everything; a success forgets everything at once.
 */
public final class ContainerMemory {
    public static final int CAPACITY = 9;
    public static final int MAX_FAILURES = 10;

    private final Deque<BlockPos> visited = new ArrayDeque<>();
    private int failures;

    public boolean isVisited(BlockPos pos) {
        return visited.contains(pos);
    }

    public int size() {
        return visited.size();
    }

    public boolean isEmpty() {
        return visited.isEmpty();
    }

    /** Remembers a container that did not help; the oldest one is dropped beyond 9. */
    public void fail(BlockPos pos) {
        remember(pos);
        failures++;
    }

    /** Remembers a container without counting a failure (it was used, but more is left to do). */
    public void remember(BlockPos pos) {
        visited.remove(pos);
        visited.addLast(pos.immutable());
        while (visited.size() > CAPACITY) {
            visited.removeFirst();
        }
    }

    public void succeed() {
        forget();
    }

    public boolean isExhausted() {
        return failures >= MAX_FAILURES;
    }

    public void forget() {
        visited.clear();
        failures = 0;
    }
}
