package dev.craft.powerplants.windturbine;

import java.util.Locale;

import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySource;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.energy.OutputMeter;
import dev.craft.core.energy.OutputReadout;
import dev.craft.core.multiblock.BlockMatcher;
import dev.craft.core.multiblock.MultiblockPattern;
import dev.craft.ic2.generator.Wind;
import dev.craft.ic2.generator.WindMillBlockEntity;
import dev.craft.powerplants.PpBlockEntityTypes;
import dev.craft.powerplants.PpBlocks;
import dev.craft.powerplants.block.PlantControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Large wind turbine (spec 23 §4): a nacelle on top of {@link #MAST_HEIGHT} wind turbine masts with a rotor item
 * inside. Every {@link #TICK_RATE} ticks it computes {@code 20 · sub · factor} EU/t (at most {@link #MAX_OUTPUT}) from
 * the wind mill formula {@code sub} at the nacelle's height and the rotor's factor; obstructing blocks around the
 * rotor lower the effective height like at the wind mill. Emits from the nacelle and every mast block in packets of up
 * to 384 EU (HV). No menu; right-clicking reports the state in chat.
 */
public class LargeWindTurbineBlockEntity extends PlantControllerBlockEntity implements EnergySource, OutputReadout {
    public static final int SLOT_ROTOR = 0;
    public static final int MAST_HEIGHT = 8;
    public static final int TICK_RATE = 128;
    public static final int OBSTRUCTION_INTERVAL = 8 * TICK_RATE;
    /** Output relative to the wind mill formula. */
    public static final double SCALE = 20.0;
    public static final int MAX_OUTPUT = 384;
    public static final int BUFFER_CAPACITY = 2 * MAX_OUTPUT;
    /** Obstruction area around the nacelle: ±radius horizontally, from below to above vertically. */
    public static final int AREA_RADIUS = 5;
    public static final int AREA_BELOW = 3;
    public static final int AREA_ABOVE = 5;

    private static final MultiblockPattern PATTERN = MultiblockPattern.builder()
            .layers(MAST_HEIGHT, "M")
            .layer("C")
            .where('M', BlockMatcher.blocks(PpBlocks.WIND_TURBINE_MAST.get()))
            .controller('C', BlockMatcher.blocks(PpBlocks.LARGE_WIND_TURBINE.get()))
            .build();

    private final OutputMeter outputMeter = new OutputMeter();
    private int ticker = -1;
    private int obstructed;
    /** EU/t from the last update. */
    private double power;
    private double buffer;

    public LargeWindTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(PpBlockEntityTypes.LARGE_WIND_TURBINE.get(), pos, state, 1);
    }

    /**
     * EU/t for the given conditions: the wind mill's {@code sub} (rain and thunder included) times {@link #SCALE},
     * the rotor factor and {@code energyGeneratorWind}, capped at {@link #MAX_OUTPUT}; 0 without wind.
     */
    public static double output(int wind, int y, int obstructed, boolean thunder, boolean rain, double factor) {
        double sub = WindMillBlockEntity.subproduction(wind, y, obstructed, thunder, rain);
        if (sub <= 0.0 || factor <= 0.0) {
            return 0.0;
        }
        double eu = SCALE * sub * factor * WindMillBlockEntity.ENERGY_GENERATOR_WIND / 100.0;
        return Math.min(eu, MAX_OUTPUT);
    }

    public ItemStack getRotor() {
        return inventory.get(SLOT_ROTOR);
    }

    public double getRotorFactor() {
        return getRotor().getItem() instanceof WindRotorItem rotor ? rotor.getFactor() : 0.0;
    }

    public double getPower() {
        return power;
    }

    public int getObstructed() {
        return obstructed;
    }

    /** Sets the output until the next regular update {@link #TICK_RATE} ticks later (GameTests). */
    public void setPowerForTest(double power) {
        this.power = power;
        ticker = 1;
    }

    /** Puts {@code rotor} in (may be empty) and returns the previous rotor. */
    public ItemStack swapRotor(ItemStack rotor) {
        ItemStack previous = inventory.get(SLOT_ROTOR);
        inventory.set(SLOT_ROTOR, rotor.copy());
        updatePower();
        sync();
        return previous;
    }

    /** Counts non-air blocks around the nacelle (the turbine's own blocks excluded). */
    public void updateObstruction() {
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-AREA_RADIUS, -AREA_BELOW, -AREA_RADIUS),
                worldPosition.offset(AREA_RADIUS, AREA_ABOVE, AREA_RADIUS))) {
            if (pos.getX() == worldPosition.getX() && pos.getZ() == worldPosition.getZ()
                    && pos.getY() <= worldPosition.getY()) {
                continue;
            }
            if (level.isLoaded(pos) && !level.getBlockState(pos).isAir()) {
                count++;
            }
        }
        obstructed = count;
        setChanged();
    }

    /** Recomputes the output from wind, weather, height, obstruction and rotor. */
    public void updatePower() {
        double before = power;
        power = isFormed() ? output(Wind.getStrength(), worldPosition.getY(), obstructed, level.isThundering(),
                level.isRaining(), getRotorFactor()) : 0.0;
        if (power != before) {
            sync();
        }
        setChanged();
    }

    @Override
    protected MultiblockPattern getPattern() {
        return PATTERN;
    }

    @Override
    protected void tickFormed() {
        if (ticker < 0) {
            // random offset so that neighbouring turbines do not all update in the same tick
            ticker = level.random.nextInt(TICK_RATE);
        }
        ticker++;
        if (ticker % TICK_RATE == 0) {
            if (ticker % OBSTRUCTION_INTERVAL == 0) {
                updateObstruction();
            }
            updatePower();
        }
        double bufferBefore = buffer;
        buffer = Math.min(buffer + power, BUFFER_CAPACITY);
        outputMeter.record(level, buffer - bufferBefore);
        int output = Math.min((int) buffer, MAX_OUTPUT);
        if (output > 0) {
            buffer += EnergyNets.emit(level, this, output) - output;
        }
        setActive(power > 0);
        setChanged();
    }

    @Override
    protected void tickUnformed() {
        if (power != 0.0) {
            power = 0.0;
            sync();
        }
        super.tickUnformed();
    }

    @Override
    protected void onFormedChanged(boolean formed) {
        if (formed) {
            updateObstruction();
        }
        updatePower();
        // the mast conducts only while formed
        EnergyNets.rejoin(level, this);
    }

    @Override
    public boolean hasMenu() {
        return false;
    }

    @Override
    public void onUse(Player player) {
        if (!isFormed()) {
            player.sendSystemMessage(Component.translatable("message.craft.water_turbine.incomplete"));
        } else if (getRotor().isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.craft.large_wind_turbine.no_rotor"));
        } else {
            player.sendSystemMessage(Component.translatable("message.craft.large_wind_turbine.status", obstructed,
                    String.format(Locale.ROOT, "%.2f", power)));
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return null;
    }

    /** No automation: the rotor is only put in and taken out by hand. */
    @Nullable
    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
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
    public boolean emitsEnergyTo(EnergyTile receiver, Direction direction) {
        return true;
    }

    @Override
    public int getMaxEnergyOutput() {
        return MAX_OUTPUT;
    }

    @Override
    public OutputReading getOutputReading() {
        return outputMeter.read(level, OutputReading.Unit.EU);
    }

    // --- client sync (rotor and speed for the renderer) ---

    private void sync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        if (!getRotor().isEmpty()) {
            tag.put("rotor", getRotor().save(registries));
        }
        tag.putDouble("power", power);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        inventory.set(SLOT_ROTOR, tag.contains("rotor")
                ? ItemStack.parseOptional(registries, tag.getCompound("rotor")) : ItemStack.EMPTY);
        power = tag.getDouble("power");
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        handleUpdateTag(packet.getTag(), registries);
    }

    // --- persistence ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        obstructed = tag.getInt("obstructed");
        power = tag.getDouble("power");
        buffer = tag.getDouble("buffer");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("obstructed", obstructed);
        tag.putDouble("power", power);
        tag.putDouble("buffer", buffer);
    }
}
