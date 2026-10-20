package dev.craft.core.multiblock;

import java.util.Set;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Membership of a part in a multiblock, held by the owning controller as a lease: the owner renews it on every check
 * and it lapses on its own when the owner stops doing so (structure broken, controller removed or unloaded). Not
 * persisted.
 */
public final class MultiblockLease {
    @Nullable
    private BlockPos owner;
    private long expiry;

    /**
     * Claims or renews the lease for {@code owner} until {@code now + duration}. Succeeds if the lease is free, has
     * lapsed, or is held by one of {@code takeOverFrom} (which should include {@code owner} itself).
     */
    public boolean claim(BlockPos owner, Set<BlockPos> takeOverFrom, long now, int duration) {
        if (!canClaim(takeOverFrom, now)) {
            return false;
        }
        this.owner = owner;
        this.expiry = now + duration;
        return true;
    }

    /** Whether {@link #claim} would succeed, without claiming. */
    public boolean canClaim(Set<BlockPos> takeOverFrom, long now) {
        return !isHeld(now) || takeOverFrom.contains(owner);
    }

    public boolean isHeld(long now) {
        return owner != null && now < expiry;
    }

    /** The current owner, or {@code null} if the lease is free or has lapsed. */
    @Nullable
    public BlockPos getOwner(long now) {
        return isHeld(now) ? owner : null;
    }
}
