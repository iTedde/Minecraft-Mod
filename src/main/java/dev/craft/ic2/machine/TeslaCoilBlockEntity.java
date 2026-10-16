package dev.craft.ic2.machine;

import dev.craft.core.CraftDamageTypes;
import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.explosion.Ic2Explosion;
import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.item.armor.HazmatArmorItem;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * IC2 {@code TileEntityTesla}: with a redstone signal and at least 400 EU it shocks every living entity without a
 * full hazmat suit within 4 blocks every 32 ticks for {@code energy / 400} damage (spec 04, tesla coil).
 */
public class TeslaCoilBlockEntity extends MachineBlockEntity implements EnergySink {
    public static final int MAX_ENERGY = 10000;
    public static final int MAX_INPUT = 128;
    public static final int COST = 400;

    private int energy;
    private int ticker;

    public TeslaCoilBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.TESLA_COIL), pos, state, 0);
    }

    public int getEnergy() {
        return energy;
    }

    @Override
    public void serverTick() {
        if (level.hasNeighborSignal(worldPosition) && energy >= COST) {
            int damage = energy / COST;
            energy--;
            if (ticker++ % 32 == 0 && shock(damage)) {
                energy = 0;
            }
            setChanged();
        }
    }

    /** IC2 spawns its "reddust" particles on the server world, where they are never shown – so none here. */
    private boolean shock(int damage) {
        boolean shocked = false;
        List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, new AABB(worldPosition.getX() - 4,
                worldPosition.getY() - 4, worldPosition.getZ() - 4, worldPosition.getX() + 5,
                worldPosition.getY() + 5, worldPosition.getZ() + 5));
        for (LivingEntity victim : victims) {
            if (!HazmatArmorItem.hasCompleteHazmat(victim)) {
                shocked = true;
                victim.hurt(level.damageSources().source(CraftDamageTypes.ELECTRICITY), damage);
            }
        }
        return shocked;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return null;
    }

    // --- energy net ---

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

    @Override
    public boolean acceptsEnergyFrom(EnergyTile emitter, Direction direction) {
        return true;
    }

    @Override
    public int demandsEnergy() {
        return isRemoved() ? 0 : MAX_ENERGY - energy;
    }

    /** IC2 accepts up to {@code maxEnergy + maxInput − 1}. */
    @Override
    public int injectEnergy(Direction direction, int amount) {
        if (isRemoved()) {
            return amount;
        }
        if (amount > MAX_INPUT) {
            if (level instanceof ServerLevel serverLevel) {
                Ic2Explosion.explodeMachineAt(serverLevel, worldPosition);
            }
            return 0;
        }
        int need = amount;
        if (energy + amount >= MAX_ENERGY + MAX_INPUT) {
            need = MAX_ENERGY + MAX_INPUT - energy - 1;
        }
        energy += need;
        setChanged();
        return amount - need;
    }

    @Override
    public int getMaxSafeInput() {
        return MAX_INPUT;
    }

    // --- persistence ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getShort("energy");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putShort("energy", (short) energy);
    }

    /** Test hook. */
    public void setEnergyForTest(int energy) {
        this.energy = energy;
    }
}
