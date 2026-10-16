package dev.craft.powerplants.block;

import java.util.List;
import java.util.Set;

import dev.craft.core.multiblock.MultiblockController;
import dev.craft.core.multiblock.MultiblockPattern;
import dev.craft.ic2.machine.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Controller of a power plant multiblock (spec 14): checks its structure when a part changes and at least every
 * {@link #CHECK_INTERVAL} ticks, leases all casing parts and only works while the structure is complete. State
 * (inventory, tanks, temperature …) is kept while the structure is incomplete.
 */
public abstract class PlantControllerBlockEntity extends MachineBlockEntity {
    public static final int CHECK_INTERVAL = 100;
    public static final int LEASE_DURATION = CHECK_INTERVAL + 20;

    private final MultiblockController structure;
    private boolean formed;

    protected PlantControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slots) {
        super(type, pos, state, slots);
        this.structure = new MultiblockController(getPattern(), CHECK_INTERVAL, this::getFacing,
                new MultiblockController.Listener() {
                    @Override
                    public void onFormed(MultiblockPattern.Match match) {
                    }

                    @Override
                    public void onBroken(MultiblockPattern.Match match) {
                    }

                    @Override
                    public void onChecked(@Nullable MultiblockPattern.Match match) {
                        boolean wasFormed = formed;
                        formed = match != null && claimParts(match);
                        if (formed) {
                            onStructureChecked(match);
                        }
                        if (wasFormed != formed) {
                            onFormedChanged(formed);
                        }
                    }
                });
    }

    /** Layout for a controller facing north (the controller faces out of the structure). */
    protected abstract MultiblockPattern getPattern();

    /** Called every server tick while the structure is complete. */
    protected abstract void tickFormed();

    /** Called every server tick while the structure is incomplete. */
    protected void tickUnformed() {
        setActive(false);
    }

    /** Called after each successful structure check. */
    protected void onStructureChecked(MultiblockPattern.Match match) {
    }

    protected void onFormedChanged(boolean formed) {
    }

    public boolean isFormed() {
        return formed;
    }

    public void markStructureDirty() {
        structure.markDirty();
    }

    /** Leases every casing part of the match; fails if one is held by another controller. */
    private boolean claimParts(MultiblockPattern.Match match) {
        long now = level.getGameTime();
        Set<BlockPos> owners = Set.of(worldPosition);
        boolean all = true;
        for (List<BlockPos> positions : match.positions().values()) {
            for (BlockPos pos : positions) {
                if (level.getBlockEntity(pos) instanceof PlantPartBlockEntity part
                        && !part.getLease().claim(worldPosition, owners, now, LEASE_DURATION)) {
                    all = false;
                }
            }
        }
        return all;
    }

    @Override
    public void serverTick() {
        structure.tick(level, worldPosition);
        if (formed) {
            tickFormed();
        } else {
            tickUnformed();
        }
    }

    /** Item access through a casing part (and the controller itself); {@code null} for none. */
    @Nullable
    public IItemHandler getPartItemHandler(@Nullable Direction side) {
        return null;
    }

    /** Fluid access through a casing part (and the controller itself); {@code null} for none. */
    @Nullable
    public IFluidHandler getPartFluidHandler(@Nullable Direction side) {
        return null;
    }

    /** Whether right-clicking the controller opens a menu; otherwise {@link #onUse(Player)} is called. */
    public boolean hasMenu() {
        return true;
    }

    /** Right-click without a menu (server side). */
    public void onUse(Player player) {
    }

    @Override
    public float getWrenchDropRate() {
        return 1.0F;
    }
}
