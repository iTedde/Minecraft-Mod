package dev.craft.nuclearcontrol.block;

import javax.annotation.Nullable;

import dev.craft.ic2.machine.Wrenchable;
import dev.craft.nuclearcontrol.NcBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Information panel extender ({@code TileEntityInfoPanelExtender}, spec 46 §5.1): part of the screen of the panel
 * stored in {@link #getCore()}; no inventory, no GUI. The wrench turns it like the panel.
 */
public class InfoPanelExtenderBlockEntity extends BlockEntity implements Wrenchable {
    @Nullable
    private BlockPos core;

    public InfoPanelExtenderBlockEntity(BlockPos pos, BlockState state) {
        super(NcBlockEntityTypes.INFORMATION_PANEL_EXTENDER.get(), pos, state);
    }

    @Nullable
    public BlockPos getCore() {
        return core;
    }

    public void setCore(@Nullable BlockPos core) {
        this.core = core;
        setChanged();
    }

    @Nullable
    public InfoPanelBlockEntity getCorePanel() {
        return core != null && level != null && level.isLoaded(core)
                && level.getBlockEntity(core) instanceof InfoPanelBlockEntity panel ? panel : null;
    }

    /**
     * {@code registerInfoPanelExtender}: screens and panels next to a new (or turned) extender with the same facing
     * are rebuilt.
     */
    public static void notifyPlaced(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(InfoPanelBlock.FACING)) {
            return;
        }
        Direction facing = state.getValue(InfoPanelBlock.FACING);
        for (Direction side : Direction.values()) {
            if (side.getAxis() == facing.getAxis()) {
                continue;
            }
            BlockEntity neighbour = level.getBlockEntity(pos.relative(side));
            if (neighbour instanceof InfoPanelBlockEntity panel && panel.getFacing() == facing) {
                panel.rebuildScreen();
            } else if (neighbour instanceof InfoPanelExtenderBlockEntity extender && extender.getFacing() == facing) {
                InfoPanelBlockEntity panel = extender.getCorePanel();
                if (panel != null) {
                    panel.rebuildScreen();
                }
            }
        }
    }

    // --- wrench ---

    @Override
    public Direction getFacing() {
        return getBlockState().getValue(InfoPanelBlock.FACING);
    }

    @Override
    public boolean wrenchCanSetFacing(Player player, Direction side) {
        return !player.isShiftKeyDown() && getFacing() != side;
    }

    /** Leaves the old screen, then joins whatever fits the new facing. */
    @Override
    public void setFacing(Direction facing) {
        InfoPanelBlockEntity panel = getCorePanel();
        core = null;
        level.setBlock(worldPosition, getBlockState().setValue(InfoPanelBlock.FACING, facing)
                .setValue(InfoPanelBlock.LIT, false), Block.UPDATE_ALL);
        if (panel != null) {
            panel.rebuildScreen();
        }
        notifyPlaced(level, worldPosition);
    }

    @Override
    public boolean wrenchCanRemove(Player player) {
        return !player.isShiftKeyDown();
    }

    @Override
    public float getWrenchDropRate() {
        return 1.0F;
    }

    @Override
    public ItemStack getWrenchDrop(Player player) {
        return new ItemStack(getBlockState().getBlock());
    }

    // --- persistence ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        core = tag.contains("core") ? BlockPos.of(tag.getLong("core")) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (core != null) {
            tag.putLong("core", core.asLong());
        }
    }
}
