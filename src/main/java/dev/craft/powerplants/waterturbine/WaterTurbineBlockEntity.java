package dev.craft.powerplants.waterturbine;

import java.util.Locale;

import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySource;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.energy.OutputMeter;
import dev.craft.core.energy.OutputReadout;
import dev.craft.core.multiblock.MultiblockPattern;
import dev.craft.ic2.machine.MachineBlock;
import dev.craft.powerplants.PpBlockEntityTypes;
import dev.craft.powerplants.block.PlantControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Water turbine (spec 14 §5): a full 3×3×3 cube of water turbine casings, the controller in the middle of one side.
 * Counts the flowing (non-source) water around and above the cube every {@link #COUNT_INTERVAL} ticks – up to
 * {@link #AREA_RADIUS} blocks out from every side face and from the bottom layer up to {@link #AREA_HEIGHT} blocks
 * above the top layer, the cube itself excluded – and produces 0.25 EU/t per flowing block from {@link #MIN_FLOWING} blocks on (× 1.5 in river biomes, at most 32 EU/t),
 * emitted from the controller and every casing block in packets of up to 32 EU (LV). No menu; right-clicking
 * reports the state in chat. The large turbine only changes the values ({@link WaterTurbineTier}).
 */
public class WaterTurbineBlockEntity extends PlantControllerBlockEntity implements EnergySource, OutputReadout {
    public static final int COUNT_INTERVAL = 128;
    // values of the small turbine; see WaterTurbineTier for the large one
    public static final int MIN_FLOWING = 16;
    /** Horizontal reach of the counted area beyond the cube's side faces. */
    public static final int AREA_RADIUS = 5;
    /** Height of the counted area above the cube's top layer. */
    public static final int AREA_HEIGHT = 20;
    public static final double YIELD_PER_BLOCK = 0.25;
    public static final double RIVER_FACTOR = 1.5;
    public static final int MAX_OUTPUT = 32;
    /** Accumulated EU are capped here so fractions carry over without piling up. */
    public static final int BUFFER_CAPACITY = 2 * MAX_OUTPUT;

    private int ticker = -1;
    private int flowing;
    /** EU/t from the last water count. */
    private double flow;
    private double buffer;
    private final OutputMeter outputMeter = new OutputMeter();

    public WaterTurbineBlockEntity(BlockPos pos, BlockState state) {
        this(PpBlockEntityTypes.WATER_TURBINE.get(), pos, state);
    }

    protected WaterTurbineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 0);
    }

    /** Size and values of this turbine; constant per class (it is needed before the constructor finishes). */
    public WaterTurbineTier getTier() {
        return WaterTurbineTier.SMALL;
    }

    /** EU/t of the small turbine for the given number of counted flowing water blocks. */
    public static double output(int flowing, boolean river) {
        return WaterTurbineTier.SMALL.output(flowing, river);
    }

    public int getFlowing() {
        return flowing;
    }

    public double getFlow() {
        return flow;
    }

    public double getBuffer() {
        return buffer;
    }

    public boolean isInRiver() {
        return level.getBiome(worldPosition).is(BiomeTags.IS_RIVER);
    }

    /** Recounts the flowing water around and above the cube and restarts the count interval. */
    public void updateWaterCount() {
        ticker = 0;
        WaterTurbineTier tier = getTier();
        int half = tier.size() / 2;
        BlockPos center = worldPosition.relative(getBlockState().getValue(MachineBlock.FACING).getOpposite(), half);
        int count = 0;
        int reach = half + tier.areaRadius();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-reach, -half, -reach),
                center.offset(reach, half + tier.areaHeight(), reach))) {
            if (Math.abs(pos.getX() - center.getX()) <= half && Math.abs(pos.getY() - center.getY()) <= half
                    && Math.abs(pos.getZ() - center.getZ()) <= half) {
                continue;
            }
            if (!level.isLoaded(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.WATER) && !state.getFluidState().isSource()) {
                count++;
            }
        }
        flowing = count;
        flow = tier.output(count, isInRiver());
        setChanged();
    }

    @Override
    protected MultiblockPattern getPattern() {
        return getTier().pattern();
    }

    @Override
    protected void tickFormed() {
        if (ticker < 0) {
            // random offset so that neighbouring turbines do not all count in the same tick
            ticker = level.random.nextInt(COUNT_INTERVAL);
        }
        if (++ticker >= COUNT_INTERVAL) {
            updateWaterCount();
        }
        WaterTurbineTier tier = getTier();
        double bufferBefore = buffer;
        buffer = Math.min(buffer + flow, tier.bufferCapacity());
        outputMeter.record(level, buffer - bufferBefore);
        int output = Math.min((int) buffer, tier.maxOutput());
        if (output > 0) {
            buffer += EnergyNets.emit(level, this, output) - output;
        }
        setActive(flow > 0);
        setChanged();
    }

    @Override
    protected void onFormedChanged(boolean formed) {
        if (formed) {
            updateWaterCount();
        }
        // the casing conducts only while formed
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
            return;
        }
        player.sendSystemMessage(Component.translatable("message.craft.water_turbine.status", flowing,
                String.format(Locale.ROOT, "%.2f", flow)));
    }

    @Nullable
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
    public boolean emitsEnergyTo(EnergyTile receiver, Direction direction) {
        return true;
    }

    @Override
    public int getMaxEnergyOutput() {
        return getTier().maxOutput();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        flowing = tag.getInt("flowing");
        flow = tag.getDouble("flow");
        buffer = tag.getDouble("buffer");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("flowing", flowing);
        tag.putDouble("flow", flow);
        tag.putDouble("buffer", buffer);
    }

    @Override
    public OutputReading getOutputReading() {
        return outputMeter.read(level, OutputReading.Unit.EU);
    }
}
