package dev.craft.ic2.cable;

import dev.craft.core.energy.EnergyConductor;
import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergySource;
import dev.craft.core.energy.EnergyTile;
import dev.craft.ic2.Ic2BlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

/**
 * Cable tile: energy conductor with paint color, foam color and – for detector/splitter cables – an active flag.
 * Type and foam state live in the block state ({@link CableBlock}).
 */
public class CableBlockEntity extends BlockEntity implements EnergyConductor {
    /** Texture column 0–15 of the cable sprite row (paint color, or active flag for detector/splitter). */
    public static final ModelProperty<Integer> TEXTURE_INDEX = new ModelProperty<>();
    public static final ModelProperty<Integer> FOAM_COLOR = new ModelProperty<>();

    public static final int DETECTOR_TICK_RATE = 20;
    public static final int SPLITTER_TICK_RATE = 20;
    /** Foam color of wet construction foam (light gray). */
    public static final int WET_FOAM_COLOR = 7;

    /** Paint color; 0 = unpainted (the black painter also paints 0, as in IC2). */
    private int color;
    private int foamColor;
    private boolean active;

    private long detectorLastValue = -1L;
    private int detectorTicker;
    private int splitterTicksUntilUpdate;

    public CableBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.CABLE.get(), pos, state);
    }

    public CableType getCableType() {
        return ((CableBlock) getBlockState().getBlock()).getCableType();
    }

    public int getColor() {
        return color;
    }

    public int getFoamColor() {
        return foamColor;
    }

    public boolean isActive() {
        return active;
    }

    public boolean canInteractWithCable(CableBlockEntity cable) {
        return color == 0 || cable.color == 0 || color == cable.color;
    }

    /** Whether this cable visually connects to {@code tile} (IC2 {@code canInteractWith}). */
    public boolean canInteractWith(BlockEntity tile) {
        if (tile instanceof CableBlockEntity cable) {
            return canInteractWithCable(cable);
        }
        return tile instanceof EnergySink || tile instanceof EnergySource || tile instanceof EnergyConductor;
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
        return !(emitter instanceof CableBlockEntity cable) || canInteractWithCable(cable);
    }

    @Override
    public boolean emitsEnergyTo(EnergyTile receiver, Direction direction) {
        return !(receiver instanceof CableBlockEntity cable) || canInteractWithCable(cable);
    }

    @Override
    public double getConductionLoss() {
        return getCableType().getConductionLoss();
    }

    @Override
    public int getInsulationEnergyAbsorption() {
        return getCableType().getInsulationEnergyAbsorption();
    }

    @Override
    public int getInsulationBreakdownEnergy() {
        return CableType.INSULATION_BREAKDOWN_ENERGY;
    }

    @Override
    public int getConductorBreakdownEnergy() {
        return getCableType().getConductorBreakdownEnergy();
    }

    @Override
    public void removeInsulation() {
    }

    @Override
    public void removeConductor() {
        if (!(level instanceof ServerLevel serverLevel) || serverLevel.getBlockEntity(worldPosition) != this) {
            return;
        }
        serverLevel.setBlock(worldPosition, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        float pitch = 2.6F + (serverLevel.random.nextFloat() - serverLevel.random.nextFloat()) * 0.8F;
        serverLevel.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, pitch);
        for (int i = 0; i < 8; i++) {
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                    worldPosition.getX() + serverLevel.random.nextDouble(), worldPosition.getY() + 1.2,
                    worldPosition.getZ() + serverLevel.random.nextDouble(), 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    // --- painting and foam ---

    /**
     * Paints the cable, or the foam if foamed (IC2 {@code changeColor}).
     *
     * @return whether anything changed
     */
    public boolean changeColor(int newColor) {
        CableBlock.Foam foam = getBlockState().getValue(CableBlock.FOAM);
        boolean foamed = foam != CableBlock.Foam.NONE;
        if (!(foamed || color != newColor && getCableType().isColorable()) || foamed && foamColor == newColor) {
            return false;
        }
        if (level != null && !level.isClientSide) {
            if (!foamed) {
                boolean added = EnergyNets.isAdded(level, this);
                if (added) {
                    EnergyNets.get((ServerLevel) level).remove(this);
                }
                color = newColor;
                EnergyNets.get((ServerLevel) level).add(this);
                CableBlock.refreshConnections(level, worldPosition);
            } else {
                foamColor = newColor;
            }
            sync();
        }
        return true;
    }

    /** Copies paint and foam color from the cable this one replaces (insulation change). */
    void copyFrom(int color, int foamColor) {
        this.color = color;
        this.foamColor = foamColor;
        sync();
    }

    void setFoamColor(int foamColor) {
        this.foamColor = foamColor;
        sync();
    }

    private void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;
            sync();
        }
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    // --- ticking ---

    static void serverTick(Level level, BlockPos pos, BlockState state, CableBlockEntity cable) {
        if (state.getValue(CableBlock.FOAM) == CableBlock.Foam.WET
                && level.random.nextInt(500) == 0
                && level.getMaxLocalRawBrightness(pos) * 6 >= level.random.nextInt(1000)) {
            CableBlock.setFoam(level, pos, CableBlock.Foam.HARD);
        }
        switch (cable.getCableType()) {
            case DETECTOR -> cable.tickDetector(state);
            case SPLITTER -> cable.tickSplitter();
            default -> {
            }
        }
    }

    private void tickDetector(BlockState state) {
        if (detectorTicker++ % DETECTOR_TICK_RATE != 0) {
            return;
        }
        long newValue = EnergyNets.get((ServerLevel) level).getTotalEnergyEmitted(this);
        if (detectorLastValue != -1L) {
            boolean nowActive = newValue > detectorLastValue;
            if (nowActive != active) {
                setActive(nowActive);
                level.updateNeighborsAt(worldPosition, state.getBlock());
            }
        }
        detectorLastValue = newValue;
    }

    private void tickSplitter() {
        if (splitterTicksUntilUpdate == 0) {
            splitterTicksUntilUpdate = SPLITTER_TICK_RATE;
            boolean added = EnergyNets.isAdded(level, this);
            if ((level.getDirectSignalTo(worldPosition) > 0) == added) {
                if (added) {
                    EnergyNets.get((ServerLevel) level).remove(this);
                } else {
                    EnergyNets.get((ServerLevel) level).add(this);
                }
                added = !added;
            }
            setActive(added);
        }
        splitterTicksUntilUpdate--;
    }

    // --- persistence and sync ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        color = tag.getByte("color");
        foamColor = tag.getByte("foamColor");
        active = tag.getBoolean("active");
        if (level != null && level.isClientSide) {
            requestModelDataUpdate();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putByte("color", (byte) color);
        tag.putByte("foamColor", (byte) foamColor);
        tag.putBoolean("active", active);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        super.onDataPacket(net, pkt, registries);
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public ModelData getModelData() {
        CableType type = getCableType();
        int texture = type == CableType.DETECTOR || type == CableType.SPLITTER ? (active ? 1 : 0) : color;
        return ModelData.builder().with(TEXTURE_INDEX, texture).with(FOAM_COLOR, foamColor).build();
    }
}
