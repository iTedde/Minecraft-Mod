package dev.craft.ic2.reactor;

import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySource;
import dev.craft.core.energy.EnergyTile;
import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.machine.Wrenchable;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * IC2 {@code TileEntityReactorChamberElectric}: widens the reactor by one column, proxies its inventory and offers
 * the energy the reactor could not deliver once more (without the ×5).
 */
public class ReactorChamberBlockEntity extends BlockEntity implements EnergySource, Wrenchable {
    public ReactorChamberBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.REACTOR_CHAMBER.get(), pos, state);
    }

    /** The adjacent reactor (first in direction order), or {@code null}. */
    @Nullable
    public NuclearReactorBlockEntity getReactor() {
        if (level == null) {
            return null;
        }
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof NuclearReactorBlockEntity reactor) {
                return reactor;
            }
        }
        return null;
    }

    @Nullable
    public IItemHandler getItemHandler(@Nullable Direction side) {
        NuclearReactorBlockEntity reactor = getReactor();
        return reactor == null ? null : reactor.getItemHandler(side);
    }

    // --- energy ---

    @Override
    public boolean emitsEnergyTo(EnergyTile receiver, Direction direction) {
        return true;
    }

    @Override
    public int getMaxEnergyOutput() {
        return 240 * NuclearReactorBlockEntity.ENERGY_GENERATOR_NUCLEAR;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        EnergyNets.add(level, this);
    }

    @Override
    public void setRemoved() {
        EnergyNets.remove(level, this);
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        EnergyNets.remove(level, this);
        super.onChunkUnloaded();
    }

    // --- wrench ---

    @Override
    public Direction getFacing() {
        return Direction.DOWN;
    }

    @Override
    public boolean wrenchCanSetFacing(Player player, Direction side) {
        return false;
    }

    @Override
    public void setFacing(Direction facing) {
    }

    @Override
    public float getWrenchDropRate() {
        return 0.8F;
    }

    @Override
    public ItemStack getWrenchDrop(Player player) {
        return new ItemStack(getBlockState().getBlock());
    }
}
