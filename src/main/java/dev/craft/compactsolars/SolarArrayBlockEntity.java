package dev.craft.compactsolars;

import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.machine.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Leftover solar array (user, 2026-10-24; spec 11 §4): only IC2 solar panels generate solar power now. Arrays already
 * placed or stored stay in the world without a function; they keep their charge slot so nothing in it is lost and can
 * be taken apart into 8 of the tier below at the crafting table.
 */
public class SolarArrayBlockEntity extends MachineBlockEntity {
    private final SolarArrayType type;

    public SolarArrayBlockEntity(BlockPos pos, BlockState state) {
        super(CsBlockEntityTypes.SOLAR_ARRAY.get(), pos, state, 1);
        this.type = ((SolarArrayBlock) state.getBlock()).getArrayType();
    }

    public SolarArrayType getArrayType() {
        return type;
    }

    @Override
    public void serverTick() {
    }

    // --- no facing, no active state ---

    @Override
    public Ic2Machine getMachine() {
        throw new UnsupportedOperationException("solar arrays are not IC2 machines");
    }

    @Override
    public Direction getFacing() {
        return Direction.NORTH;
    }

    @Override
    public void setFacing(Direction facing) {
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void setActive(boolean active) {
    }

    @Override
    public float getWrenchDropRate() {
        return 1.0F;
    }

    @Override
    protected int getSlotForSide(Direction side) {
        return 0;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new SolarArrayMenu(id, playerInventory, this);
    }
}
