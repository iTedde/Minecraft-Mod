package dev.craft.buildcraft.transport;

import java.util.Arrays;
import java.util.Set;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

/**
 * Settings and counters of the distribution transport pipe (spec 31 §2–3): a weight 0–100 per side and the items sent
 * per side. Each item goes to the candidate with the smallest {@code sent / weight}, so the split is exact; a side that
 * was no candidate last time is lifted to the others on its return instead of catching up.
 */
public final class PipeDistribution {
    public static final int MAX_WEIGHT = 100;

    private final int[] weights = new int[6];
    private final double[] sent = new double[6];
    /** Bit per side: candidates of the last allocation. */
    private int lastActive;

    public int getWeight(Direction side) {
        return weights[side.get3DDataValue()];
    }

    /** Changing a weight restarts the counting. */
    public void setWeight(Direction side, int weight) {
        weights[side.get3DDataValue()] = Math.clamp(weight, 0, MAX_WEIGHT);
        Arrays.fill(sent, 0.0);
        lastActive = 0;
    }

    /** All weights 0: the pipe behaves like a plain pipe. */
    public boolean isConfigured() {
        for (int weight : weights) {
            if (weight > 0) {
                return true;
            }
        }
        return false;
    }

    /** Splits {@code count} items over the candidates (all with weight > 0); returns the count per side index. */
    public int[] allocate(Set<Direction> candidates, int count) {
        int active = 0;
        for (Direction side : candidates) {
            active |= 1 << side.get3DDataValue();
        }
        double floor = Double.POSITIVE_INFINITY;
        for (int i = 0; i < 6; i++) {
            if ((active & lastActive & 1 << i) != 0) {
                floor = Math.min(floor, sent[i] / weights[i]);
            }
        }
        if (floor != Double.POSITIVE_INFINITY) {
            for (int i = 0; i < 6; i++) {
                if ((active & 1 << i) != 0 && (lastActive & 1 << i) == 0) {
                    sent[i] = Math.max(sent[i], floor * weights[i]);
                }
            }
        }
        int[] result = new int[6];
        for (int n = 0; n < count; n++) {
            int best = -1;
            for (int i = 0; i < 6; i++) {
                if ((active & 1 << i) != 0 && (best < 0 || sent[i] / weights[i] < sent[best] / weights[best])) {
                    best = i;
                }
            }
            sent[best]++;
            result[best]++;
        }
        // keep the counters small: only the differences matter
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < 6; i++) {
            if ((active & 1 << i) != 0) {
                min = Math.min(min, sent[i] / weights[i]);
            }
        }
        for (int i = 0; i < 6 && min != Double.POSITIVE_INFINITY; i++) {
            if (weights[i] > 0) {
                sent[i] = Math.max(0.0, sent[i] - min * weights[i]);
            }
        }
        lastActive = active;
        return result;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putIntArray("weights", weights);
        long[] counters = new long[6];
        for (int i = 0; i < 6; i++) {
            counters[i] = Double.doubleToLongBits(sent[i]);
        }
        tag.putLongArray("sent", counters);
        tag.putInt("lastActive", lastActive);
        return tag;
    }

    public void load(CompoundTag tag) {
        int[] savedWeights = tag.getIntArray("weights");
        long[] counters = tag.getLongArray("sent");
        for (int i = 0; i < 6; i++) {
            weights[i] = i < savedWeights.length ? Math.clamp(savedWeights[i], 0, MAX_WEIGHT) : 0;
            sent[i] = i < counters.length ? Double.longBitsToDouble(counters[i]) : 0.0;
        }
        lastActive = tag.getInt("lastActive");
    }
}
