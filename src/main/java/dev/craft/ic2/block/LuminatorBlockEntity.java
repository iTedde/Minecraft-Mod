package dev.craft.ic2.block;

import dev.craft.core.energy.ConsumptionReadout;
import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.energy.OutputMeter;
import dev.craft.core.energy.OutputReadout.OutputReading;
import dev.craft.core.explosion.Ic2Explosion;
import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.cable.CableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Luminator tile (IC2 {@code TileEntityLuminator}): takes EU only from a cable on its mounting side and burns 1 EU every
 * 4 ticks. IC2 swaps between the dark and the lit block, which creates a fresh tile each time – switching {@link
 * LuminatorBlock#LIT} resets energy and ticker the same way. The dark luminator keeps counting its energy down below
 * zero, which only raises its demand.
 */
public class LuminatorBlockEntity extends BlockEntity implements EnergySink, ConsumptionReadout {
    public static final int MAX_ENERGY = 10000;
    public static final int MAX_INPUT = 32;
    public static final float EXPLOSION_POWER = 0.5F;
    public static final float EXPLOSION_DROP_RATE = 0.85F;
    public static final float EXPLOSION_ENTITY_DAMAGE = 2.0F;

    private final OutputMeter consumptionMeter = new OutputMeter();
    private int energy;
    private int ticker = -1;

    public LuminatorBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.LUMINATOR.get(), pos, state);
    }

    public int getEnergy() {
        return energy;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LuminatorBlockEntity luminator) {
        luminator.ticker++;
        if (luminator.ticker % 4 == 0) {
            if (luminator.energy > 0) {
                luminator.consumptionMeter.record(level, 1);
            }
            luminator.energy--;
            if (luminator.energy <= 0 && state.getValue(LuminatorBlock.LIT)) {
                luminator.setLit(false);
            }
            luminator.setChanged();
        }
    }

    /** Lights up a dark luminator (as a fresh tile) and adds {@code amount} EU. */
    void charge(int amount) {
        if (!getBlockState().getValue(LuminatorBlock.LIT)) {
            setLit(true);
        }
        energy += amount;
        setChanged();
    }

    private void setLit(boolean lit) {
        energy = 0;
        ticker = -1;
        level.setBlock(worldPosition, getBlockState().setValue(LuminatorBlock.LIT, lit), Block.UPDATE_ALL);
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

    /** IC2 {@code canCableConnectFrom}: only the mounting side. */
    public boolean canCableConnectFrom(BlockPos cablePos) {
        return cablePos.equals(worldPosition.relative(getBlockState().getValue(LuminatorBlock.FACING).getOpposite()));
    }

    @Override
    public boolean acceptsEnergyFrom(EnergyTile emitter, Direction direction) {
        return emitter instanceof CableBlockEntity;
    }

    @Override
    public int demandsEnergy() {
        return isRemoved() ? 0 : MAX_ENERGY - energy;
    }

    @Override
    public int injectEnergy(Direction direction, int amount) {
        if (isRemoved()) {
            return amount;
        }
        if (amount > MAX_INPUT) {
            poof();
            return 0;
        }
        if (amount <= 0) {
            return 0;
        }
        if (!getBlockState().getValue(LuminatorBlock.LIT)) {
            setLit(true);
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

    private void poof() {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.setBlock(worldPosition, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            new Ic2Explosion(serverLevel, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                    worldPosition.getZ() + 0.5, EXPLOSION_POWER, EXPLOSION_DROP_RATE, EXPLOSION_ENTITY_DAMAGE)
                    .explode();
        }
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

    @Override
    public OutputReading getConsumptionReading() {
        return consumptionMeter.read(level, OutputReading.Unit.EU);
    }
}
