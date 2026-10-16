package dev.craft.ic2.reactor;

import com.mojang.logging.LogUtils;
import dev.craft.Craft;
import dev.craft.core.CraftDamageTypes;
import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySource;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.explosion.Ic2Explosion;
import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.machine.MachineBlockEntity;
import dev.craft.ic2.machine.MachineSoundHooks;
import dev.craft.ic2.menu.NuclearReactorMenu;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.IItemHandler;
import org.slf4j.Logger;

/**
 * IC2 {@code TileEntityNuclearReactorElectric} (spec 06): 9 × 6 matrix, usable width 3 + adjacent chambers, a
 * reactor cycle every 20 ticks and one EU packet of {@code output · 5} per tick.
 */
public class NuclearReactorBlockEntity extends MachineBlockEntity implements EnergySource, Reactor {
    /** IC2 config {@code energyGeneratorNuclear}. */
    public static final int ENERGY_GENERATOR_NUCLEAR = 5;
    /** IC2 config {@code explosionPowerReactorMax}. */
    public static final float EXPLOSION_POWER_REACTOR_MAX = 45.0F;
    public static final int TICK_RATE = 20;
    public static final int COLUMNS = 9;
    public static final int ROWS = 6;

    /** Unbreakable blocks of 1.4.7 materials rock/iron/lava/ground/clay: a meltdown turns them into lava. */
    public static final TagKey<Block> MELTDOWN_LAVA =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Craft.MODID, "reactor_meltdown_lava"));
    /** 1.4.7 materials wood, leaves and cloth: set on fire from 40 % hull heat on. */
    public static final TagKey<Block> MELTDOWN_FLAMMABLE = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "reactor_meltdown_flammable"));

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Random RANDOMIZER = new Random();

    private int output;
    private int updateTicker;
    private int heat;
    private int maxHeat = 10000;
    private float hem = 1.0F;
    private final IItemHandler itemHandler = new ReactorItemHandler(this);

    public NuclearReactorBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.NUCLEAR_REACTOR), pos, state, COLUMNS * ROWS);
        this.updateTicker = RANDOMIZER.nextInt(TICK_RATE);
    }

    @Override
    public void serverTick() {
        sendEnergyToChambers(output);
        if (updateTicker++ % TICK_RATE == 0) {
            runCycle();
        }
    }

    /** One reactor cycle (every 20 ticks); public for tests. */
    public void runCycle() {
        int x = worldPosition.getX();
        int z = worldPosition.getZ();
        if (!level.hasChunksAt(x - 2, z - 2, x + 2, z + 2)) {
            output = 0;
        } else {
            dropAllUnfittingStuff();
            output = 0;
            maxHeat = 10000;
            hem = 1.0F;
            processChambers();
            if (calculateHeatEffects()) {
                return;
            }
            setActive(heat >= 1000 || output > 0);
            onInventoryChanged();
        }
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    /** Loop sound while producing, Geiger counter by output band (&lt; 40, &lt; 80, ≥ 80). */
    @Override
    public void clientTick() {
        MachineSoundHooks.updateLoop(this, Ic2Sounds.NUCLEAR_REACTOR_LOOP.get(), output > 0);
        MachineSoundHooks.updateLoop(this, Ic2Sounds.GEIGER_LOW_EU.get(), output > 0 && output < 40);
        MachineSoundHooks.updateLoop(this, Ic2Sounds.GEIGER_MED_EU.get(), output >= 40 && output < 80);
        MachineSoundHooks.updateLoop(this, Ic2Sounds.GEIGER_HIGH_EU.get(), output >= 80);
    }

    // --- matrix ---

    public void dropAllUnfittingStuff() {
        int size = getReactorSize();
        for (int x = 0; x < COLUMNS; x++) {
            for (int y = 0; y < ROWS; y++) {
                ItemStack stack = getMatrixCoord(x, y);
                if (stack.isEmpty()) {
                    continue;
                }
                if (x >= size || !isUsefulItem(stack)) {
                    eject(stack);
                    setMatrixCoord(x, y, ItemStack.EMPTY);
                }
            }
        }
    }

    /** Reactor components plus re-enriched and near-depleted uranium cells may stay in the matrix. */
    public static boolean isUsefulItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() instanceof ReactorComponent
                || stack.is(Ic2Items.RE_ENRICHED_URANIUM_CELL.get())
                || stack.is(Ic2Items.NEAR_DEPLETED_URANIUM_CELL.get());
    }

    private void eject(ItemStack drop) {
        float f = 0.7F;
        double dx = level.random.nextFloat() * f + (1.0F - f) * 0.5;
        double dy = level.random.nextFloat() * f + (1.0F - f) * 0.5;
        double dz = level.random.nextFloat() * f + (1.0F - f) * 0.5;
        ItemEntity entity = new ItemEntity(level, worldPosition.getX() + dx, worldPosition.getY() + dy,
                worldPosition.getZ() + dz, drop);
        entity.setPickUpDelay(10);
        level.addFreshEntity(entity);
    }

    public void processChambers() {
        int size = getReactorSize();
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < size; x++) {
                ItemStack stack = getMatrixCoord(x, y);
                if (!stack.isEmpty() && stack.getItem() instanceof ReactorComponent component) {
                    component.processChamber(this, stack, x, y);
                }
            }
        }
    }

    public ItemStack getMatrixCoord(int x, int y) {
        return x >= 0 && x < COLUMNS && y >= 0 && y < ROWS ? inventory.get(x + y * COLUMNS) : ItemStack.EMPTY;
    }

    public void setMatrixCoord(int x, int y, ItemStack stack) {
        if (x >= 0 && x < COLUMNS && y >= 0 && y < ROWS) {
            inventory.set(x + y * COLUMNS, stack);
        }
    }

    /** IC2 inventory redirect: slots right of the usable width map to the last usable column of the row. */
    public int redirectSlot(int slot) {
        int x = slot % COLUMNS;
        int size = getReactorSize();
        return x >= size ? size - 1 + slot / COLUMNS * COLUMNS : slot;
    }

    /** 3 + number of adjacent reactor chambers (9 without a level). */
    public int getReactorSize() {
        if (level == null) {
            return COLUMNS;
        }
        int rows = 3;
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof ReactorChamberBlockEntity) {
                rows++;
            }
        }
        return rows;
    }

    // --- heat ---

    /** Spec 06 §4; returns {@code true} if the reactor exploded. */
    public boolean calculateHeatEffects() {
        if (heat < 4000 || EXPLOSION_POWER_REACTOR_MAX <= 0.0F) {
            return false;
        }
        float power = (float) heat / maxHeat;
        if (power >= 1.0F) {
            explode();
            return true;
        }
        if (power >= 0.85F && level.random.nextFloat() <= 0.2F * hem) {
            BlockPos coord = getRandCoord(2);
            if (coord != null) {
                BlockState state = level.getBlockState(coord);
                if (state.isAir()) {
                    level.setBlockAndUpdate(coord, BaseFireBlock.getState(level, coord));
                } else if (state.getDestroySpeed(level, coord) <= -1.0F) {
                    if (state.is(MELTDOWN_LAVA)) {
                        level.setBlockAndUpdate(coord, Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL, 15));
                    } else {
                        level.setBlockAndUpdate(coord, BaseFireBlock.getState(level, coord));
                    }
                }
            }
        }
        if (power >= 0.7F) {
            AABB area = new AABB(worldPosition.getX() - 3, worldPosition.getY() - 3, worldPosition.getZ() - 3,
                    worldPosition.getX() + 4, worldPosition.getY() + 4, worldPosition.getZ() + 4);
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
                entity.hurt(level.damageSources().source(CraftDamageTypes.RADIATION),
                        (int) (level.random.nextInt(4) * hem));
            }
        }
        if (power >= 0.5F && level.random.nextFloat() <= hem) {
            BlockPos coord = getRandCoord(2);
            if (coord != null && level.getBlockState(coord).is(Blocks.WATER)) {
                level.setBlockAndUpdate(coord, Blocks.AIR.defaultBlockState());
            }
        }
        if (power >= 0.4F && level.random.nextFloat() <= hem) {
            BlockPos coord = getRandCoord(2);
            if (coord != null && level.getBlockState(coord).is(MELTDOWN_FLAMMABLE)) {
                level.setBlockAndUpdate(coord, BaseFireBlock.getState(level, coord));
            }
        }
        return false;
    }

    @Nullable
    private BlockPos getRandCoord(int radius) {
        BlockPos coord = worldPosition.offset(level.random.nextInt(2 * radius + 1) - radius,
                level.random.nextInt(2 * radius + 1) - radius, level.random.nextInt(2 * radius + 1) - radius);
        return coord.equals(worldPosition) ? null : coord;
    }

    /** Meltdown: removes reactor and chambers without drops and explodes with the summed component influence. */
    public void explode() {
        float boomPower = 10.0F;
        float boomMod = 1.0F;
        int size = getReactorSize();
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < size; x++) {
                ItemStack stack = getMatrixCoord(x, y);
                if (!stack.isEmpty() && stack.getItem() instanceof ReactorComponent component) {
                    float f = component.influenceExplosion(this, stack);
                    if (f > 0.0F && f < 1.0F) {
                        boomMod *= f;
                    } else {
                        boomPower += f;
                    }
                }
                setMatrixCoord(x, y, ItemStack.EMPTY);
            }
        }
        boomPower *= hem * boomMod;
        LOGGER.info("Nuclear Reactor at {}:({},{},{}) melted (explosion power {})", level.dimension().location(),
                worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), boomPower);
        if (boomPower > EXPLOSION_POWER_REACTOR_MAX) {
            boomPower = EXPLOSION_POWER_REACTOR_MAX;
        }
        for (Direction direction : Direction.values()) {
            BlockPos neighbour = worldPosition.relative(direction);
            if (level.getBlockEntity(neighbour) instanceof ReactorChamberBlockEntity) {
                level.setBlockAndUpdate(neighbour, Blocks.AIR.defaultBlockState());
            }
        }
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.set(i, ItemStack.EMPTY);
        }
        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos pos = worldPosition;
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        new Ic2Explosion(serverLevel, pos.getX(), pos.getY(), pos.getZ(), boomPower, 0.01F, 1.5F,
                serverLevel.damageSources().source(CraftDamageTypes.NUKE)).explode();
    }

    // --- energy ---

    /** Reactor packet first (×5), the rest is offered again by each adjacent chamber. */
    public int sendEnergyToChambers(int send) {
        if (send == 0) {
            return 0;
        }
        send = EnergyNets.emit(level, this, send * ENERGY_GENERATOR_NUCLEAR);
        for (Direction direction : Direction.values()) {
            if (send > 0 && level.getBlockEntity(worldPosition.relative(direction))
                    instanceof ReactorChamberBlockEntity chamber) {
                send = EnergyNets.emit(level, chamber, send);
            }
        }
        return send;
    }

    @Override
    public boolean emitsEnergyTo(EnergyTile receiver, Direction direction) {
        return true;
    }

    @Override
    public int getMaxEnergyOutput() {
        return 1512 * ENERGY_GENERATOR_NUCLEAR;
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

    // --- Reactor ---

    @Override
    public Level getReactorLevel() {
        return level;
    }

    @Override
    public BlockPos getReactorPos() {
        return worldPosition;
    }

    @Override
    public int getHeat() {
        return heat;
    }

    @Override
    public void setHeat(int heat) {
        this.heat = heat;
    }

    @Override
    public int addHeat(int amount) {
        heat += amount;
        return heat;
    }

    @Override
    public int getMaxHeat() {
        return maxHeat;
    }

    @Override
    public void setMaxHeat(int maxHeat) {
        this.maxHeat = maxHeat;
    }

    @Override
    public float getHeatEffectModifier() {
        return hem;
    }

    @Override
    public void setHeatEffectModifier(float hem) {
        this.hem = hem;
    }

    @Override
    public int getOutput() {
        return output;
    }

    /** IC2 keeps the output in a {@code short}. */
    @Override
    public int addOutput(int energy) {
        output = (short) (output + energy);
        return output;
    }

    @Override
    public ItemStack getItemAt(int x, int y) {
        return getMatrixCoord(x, y);
    }

    @Override
    public void setItemAt(int x, int y, ItemStack stack) {
        setMatrixCoord(x, y, stack);
    }

    @Override
    public boolean produceEnergy() {
        return level.hasNeighborSignal(worldPosition) && ENERGY_GENERATOR_NUCLEAR != 0;
    }

    /** Client: heat as last received through the reactor GUI (IC2 only syncs it there). */
    public void setClientHeat(int heat) {
        this.heat = heat;
    }

    // --- inventory, menu, sync ---

    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        return itemHandler;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new NuclearReactorMenu(id, inventory, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        heat = tag.getInt("heat");
        output = tag.getShort("output");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("heat", heat);
        tag.putShort("output", (short) output);
    }

    /** Only the output reaches the clients (sounds); IC2 networks no other reactor field. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putShort("output", (short) output);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        output = tag.getShort("output");
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        handleUpdateTag(packet.getTag(), registries);
    }
}
