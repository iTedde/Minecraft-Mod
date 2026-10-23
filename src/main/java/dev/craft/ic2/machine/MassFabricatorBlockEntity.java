package dev.craft.ic2.machine;

import java.util.Map;

import dev.craft.core.energy.ConsumptionReadout;
import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.energy.OutputMeter;
import dev.craft.core.energy.OutputReadout.OutputReading;
import dev.craft.core.explosion.Ic2Explosion;
import dev.craft.gregtech.GtItems;
import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.menu.MassFabricatorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Mass fabricator, 1:1 with IC2 {@code TileEntityMatter}: 1 000 000 EU per UU matter; amplifiers add a scrap value
 * that multiplies incoming energy by 6 until it is used up.
 */
public class MassFabricatorBlockEntity extends MachineBlockEntity implements EnergySink, ConsumptionReadout {
    public static final int MAX_ENERGY = 1_100_000;
    public static final int MAX_INPUT = 512;
    public static final int ENERGY_PER_MATTER = 1_000_000;
    public static final int SLOT_AMPLIFIER = 0;
    public static final int SLOT_OUTPUT = 1;

    public static final int STATE_IDLE = 0;
    public static final int STATE_RUNNING = 1;
    public static final int STATE_RUNNING_SCRAP = 2;

    private final OutputMeter consumptionMeter = new OutputMeter();
    private int energy;
    private int scrap;

    public MassFabricatorBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.MASS_FABRICATOR), pos, state, 2);
    }

    /** Amplifier value of {@code stack}, or 0: IC2 scrap/scrap box plus GT's redstone, glowstone and gem dusts. */
    public static int getAmplifierValue(ItemStack stack) {
        Integer value = amplifiers().get(stack.getItem());
        return value == null ? 0 : value;
    }

    /** All amplifiers with their values. */
    public static Map<Item, Integer> amplifiers() {
        return Map.ofEntries(
                Map.entry(Ic2Items.SCRAP.get(), 5000),
                Map.entry(Ic2Items.SCRAP_BOX.get(), 45000),
                Map.entry(Items.REDSTONE, 5000),
                Map.entry(Items.GLOWSTONE_DUST, 25000),
                Map.entry(GtItems.DIAMOND_DUST.get(), 125000),
                Map.entry(GtItems.EMERALD_DUST.get(), 50000),
                Map.entry(GtItems.URANIUM_DUST.get(), 1000000),
                Map.entry(GtItems.PLUTONIUM_DUST.get(), 2000000),
                Map.entry(GtItems.THORIUM_DUST.get(), 500000),
                Map.entry(GtItems.ENDER_PEARL_DUST.get(), 50000),
                Map.entry(GtItems.RUBY_DUST.get(), 50000),
                Map.entry(GtItems.SAPPHIRE_DUST.get(), 50000),
                Map.entry(GtItems.GREEN_SAPPHIRE_DUST.get(), 50000),
                Map.entry(GtItems.OLIVINE_DUST.get(), 50000),
                Map.entry(GtItems.PLATINUM_DUST.get(), 100000),
                Map.entry(GtItems.TUNGSTEN_DUST.get(), 50000),
                Map.entry(GtItems.MANGANESE_DUST.get(), 5000));
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public int getScrap() {
        return scrap;
    }

    /** IC2 {@code getProgressAsString} without the percent sign, capped at 100. */
    public int getProgress() {
        return Math.min(energy / 10000, 100);
    }

    /** Networked state: idle, running, running with scrap (drives the two loop sounds). */
    public int getState() {
        if (!isActive()) {
            return STATE_IDLE;
        }
        return getBlockState().getValue(MassFabricatorBlock.SCRAP) ? STATE_RUNNING_SCRAP : STATE_RUNNING;
    }

    @Override
    public void serverTick() {
        if (!isRedstonePowered() && energy > 0) {
            setRunning(true, scrap > 0);
            boolean needsInvUpdate = false;
            ItemStack amplifier = inventory.get(SLOT_AMPLIFIER);
            if (scrap < 1000 && !amplifier.isEmpty()) {
                int value = getAmplifierValue(amplifier);
                if (value > 0) {
                    if (amplifier.hasCraftingRemainingItem()) {
                        inventory.set(SLOT_AMPLIFIER, amplifier.getCraftingRemainingItem());
                    } else {
                        amplifier.shrink(1);
                        inventory.set(SLOT_AMPLIFIER, amplifier.isEmpty() ? ItemStack.EMPTY : amplifier);
                    }
                    scrap += value;
                }
            }
            if (energy >= ENERGY_PER_MATTER) {
                needsInvUpdate = attemptGeneration();
            }
            if (needsInvUpdate) {
                onInventoryChanged();
            }
        } else {
            setRunning(false, false);
        }
    }

    private boolean attemptGeneration() {
        ItemStack output = inventory.get(SLOT_OUTPUT);
        if (output.isEmpty()) {
            inventory.set(SLOT_OUTPUT, new ItemStack(Ic2Items.UU_MATTER.get()));
        } else if (output.is(Ic2Items.UU_MATTER.get()) && output.getCount() < output.getMaxStackSize()) {
            output.grow(1);
            inventory.set(SLOT_OUTPUT, output);
        } else {
            return false;
        }
        energy -= ENERGY_PER_MATTER;
        return true;
    }

    private void setRunning(boolean active, boolean withScrap) {
        if (level != null && !isRemoved()) {
            BlockState state = getBlockState();
            BlockState newState = state.setValue(MachineBlock.ACTIVE, active).setValue(MassFabricatorBlock.SCRAP, withScrap);
            if (newState != state) {
                level.setBlock(worldPosition, newState, Block.UPDATE_ALL);
            }
        }
    }

    private boolean isRedstonePowered() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    @Override
    public void clientTick() {
        int state = getState();
        MachineSoundHooks.updateLoop(this, Ic2Sounds.MASS_FAB_LOOP.get(), state != STATE_IDLE);
        MachineSoundHooks.updateLoop(this, Ic2Sounds.MASS_FAB_SCRAP_SOLO.get(), state == STATE_RUNNING_SCRAP);
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
        return isRedstonePowered() ? 0 : MAX_ENERGY - energy;
    }

    /** Overvoltage removes the block and explodes with power 15 (instead of IC2's machine explosion). */
    @Override
    public int injectEnergy(Direction direction, int amount) {
        if (isRemoved()) {
            return amount;
        }
        if (amount > MAX_INPUT) {
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.setBlock(worldPosition, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                new Ic2Explosion(serverLevel, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                        worldPosition.getZ() + 0.5, 15.0F, 0.01F, 1.5F).explode();
            }
            return 0;
        }
        int bonus = Math.min(amount, scrap);
        scrap -= bonus;
        energy += amount + 5 * bonus;
        int rest = 0;
        if (energy > MAX_ENERGY) {
            rest = energy - MAX_ENERGY;
            energy = MAX_ENERGY;
        }
        // all of it goes into the next UU matter, so what it takes in is what it uses (counted here as one big
        // operation every million EU would never read as a steady rate)
        consumptionMeter.record(level, Math.max(0, amount - rest));
        return rest;
    }

    @Override
    public int getMaxSafeInput() {
        return MAX_INPUT;
    }

    // --- inventory / menu ---

    /** IC2: bottom → amplifier slot, other sides → output. */
    @Override
    protected int getSlotForSide(Direction side) {
        return side == Direction.DOWN ? SLOT_AMPLIFIER : SLOT_OUTPUT;
    }

    /** The output sides also put amplifiers into the amplifier slot (deviation, see {@code abweichungen.md}). */
    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null || side == Direction.DOWN) {
            return super.getItemHandler(side);
        }
        return new SideAccess(inventory)
                .input(SLOT_AMPLIFIER, stack -> getAmplifierValue(stack) > 0)
                .outputs(SLOT_OUTPUT, SLOT_OUTPUT);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new MassFabricatorMenu(id, playerInventory, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getInt("energy");
        scrap = tag.getInt("scrap");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy);
        tag.putInt("scrap", scrap);
    }

    @Override
    public OutputReading getConsumptionReading() {
        return consumptionMeter.read(level, OutputReading.Unit.EU);
    }
}
