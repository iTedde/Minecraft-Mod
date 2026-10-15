package dev.craft.gregtech.machine;

import dev.craft.buildcraft.power.PowerProvider;
import dev.craft.buildcraft.power.PowerReceptor;
import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergySource;
import dev.craft.core.energy.EnergyTile;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.GtMachine;
import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.item.UpgradeModuleItem;
import dev.craft.ic2.machine.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * GregTech {@code BaseMetaTileEntity} (spec 13 §1.1–§1.6): start-up delay, EU sink and optional source,
 * counter-based upgrades, battery discharging and energy explosions. Subclasses are the meta tile entities and
 * implement {@link #onPostTick()} plus the value hooks ({@link #maxEUInput()} …).
 */
public abstract class GtMachineBlockEntity extends MachineBlockEntity implements EnergySink, EnergySource,
        PowerReceptor {
    public static final int MAX_UPGRADES = 16;

    private int storedEnergy;
    private int storedMJ;
    private int overclockers;
    private int transformers;
    private int batteries;
    private int liBatteries;
    private int rsEnergyCells;
    private boolean mjConverter;
    /** Not saved, as in GT. */
    @Nullable
    private PowerProvider powerProvider;
    protected long tickTimer;
    private boolean addedToNet;
    private boolean releaseEnergy;
    @Nullable
    private Direction oldFacing;
    private int oldOutput;

    protected GtMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slots) {
        super(type, pos, state, slots);
    }

    public GtMachine getGtMachine() {
        return ((GtMachineBlock) getBlockState().getBlock()).getMachine();
    }

    /** The meta tile entity's logic, run every tick after the base logic. */
    protected abstract void onPostTick();

    // --- meta tile entity hooks ---

    public abstract int maxEUInput();

    public int maxEUOutput() {
        return 0;
    }

    public abstract int maxEUStore();

    public int maxMJStore() {
        return 0;
    }

    public int getMinimumStoredEU() {
        return 0;
    }

    public int getMaxOutputPackets() {
        return 1;
    }

    /** First battery slot discharged into the machine, or -1. */
    protected int dechargerSlot() {
        return -1;
    }

    protected boolean isInputFacing(Direction side) {
        return false;
    }

    protected boolean isOutputFacing(Direction side) {
        return false;
    }

    public boolean isFacingValid(Direction facing) {
        return false;
    }

    public boolean isOverclockerUpgradable() {
        return false;
    }

    public boolean isTransformerUpgradable() {
        return false;
    }

    public boolean isBatteryUpgradable() {
        return false;
    }

    // --- tick (BaseMetaTileEntity.updateEntity) ---

    @Override
    public void serverTick() {
        tickTimer++;
        if (tickTimer <= 20) {
            return;
        }
        if (tickTimer > 30 && !addedToNet) {
            EnergyNets.add(level, this);
            addedToNet = true;
        }
        Direction facing = getFacing();
        if (facing != oldFacing) {
            oldFacing = facing;
            removeFromNet();
        }
        if (getOutput() != oldOutput) {
            oldOutput = getOutput();
            removeFromNet();
        }
        if (addedToNet && level.random.nextInt(1000) == 0) {
            Direction side = switch (level.random.nextInt(6)) {
                case 0 -> Direction.EAST;
                case 1 -> Direction.WEST;
                case 2 -> Direction.UP;
                case 3 -> Direction.DOWN;
                case 4 -> Direction.SOUTH;
                default -> Direction.NORTH;
            };
            if (level.getBlockState(worldPosition.relative(side)).is(BlockTags.FIRE) && doEnergyExplosion()) {
                return;
            }
        }
        if (addedToNet && storedEnergy >= Math.max(getOutput(), getMinimumStoredEU()) && getOutput() > 0) {
            for (int i = 0; i < getMaxOutputPackets(); i++) {
                setStoredEU(storedEnergy + EnergyNets.emit(level, this, getOutput()) - getOutput());
            }
        }
        int slot = dechargerSlot();
        if (slot >= 0) {
            int inputTier = getInputTier();
            for (int i = 0; i < inputTier; i++) {
                ItemStack battery = inventory.get(slot);
                if (!battery.isEmpty() && demandsEnergy() > 0 && battery.getItem() instanceof ElectricItem item
                        && item.canProvideEnergy()) {
                    storedEnergy += ElectricItems.discharge(battery, getCapacity() - storedEnergy, inputTier, false,
                            false);
                    setChanged();
                }
            }
        }
        updateStatus();
        onPostTick();
    }

    private void removeFromNet() {
        if (addedToNet) {
            EnergyNets.remove(level, this);
        }
        addedToNet = false;
    }

    // --- facing ---

    @Override
    public Direction getFacing() {
        return getBlockState().getValue(GtMachineBlock.FACING);
    }

    @Override
    public boolean wrenchCanSetFacing(Player player, Direction side) {
        return side != getFacing() && isFacingValid(side);
    }

    @Override
    public void setFacing(Direction facing) {
        if (level != null && isFacingValid(facing)) {
            level.setBlock(worldPosition, getBlockState().setValue(GtMachineBlock.FACING, facing), Block.UPDATE_ALL);
        }
    }

    @Override
    public boolean isActive() {
        return getBlockState().getValue(GtMachineBlock.ACTIVE);
    }

    @Override
    public void setActive(boolean active) {
        if (level != null && !isRemoved() && isActive() != active) {
            level.setBlock(worldPosition, getBlockState().setValue(GtMachineBlock.ACTIVE, active), Block.UPDATE_ALL);
        }
    }

    /** Not a simple machine: the wrench keeps the machine with a chance of 0.8. */
    @Override
    public float getWrenchDropRate() {
        return 0.8F;
    }

    /** The machine with its upgrades (energy and inventory stay behind). */
    @Override
    public ItemStack getWrenchDrop(Player player) {
        ItemStack stack = new ItemStack(getBlockState().getBlock());
        CompoundTag tag = new CompoundTag();
        saveUpgrades(tag);
        if (!tag.isEmpty()) {
            BlockItem.setBlockEntityData(stack, getType(), tag);
        }
        return stack;
    }

    /** {@code isUseableByPlayer}: after two seconds, within eight blocks. */
    public boolean isAccessAllowed(Player player) {
        return tickTimer > 40 && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) < 64.0;
    }

    @Override
    public boolean stillValid(Player player) {
        return isAccessAllowed(player);
    }

    // --- upgrades (BaseMetaTileEntity.onRightclick) ---

    public int getUpgradeCount() {
        return (mjConverter ? 1 : 0) + transformers + overclockers + batteries + liBatteries + rsEnergyCells;
    }

    /**
     * Installs the held item as upgrade if possible (one per click, nothing used up in creative).
     *
     * @return whether an upgrade was installed
     */
    public boolean installUpgrade(Player player, ItemStack stack) {
        if (getUpgradeCount() >= MAX_UPGRADES || stack.isEmpty()) {
            return false;
        }
        boolean installed = true;
        if (!mjConverter && getMJCapacity() > 0 && stack.is(GtItems.PNEUMATIC_GENERATOR_UPGRADE.get())) {
            mjConverter = true;
        } else if (isOverclockerUpgradable() && overclockers < 4
                && isUpgrade(stack, UpgradeModuleItem.Type.OVERCLOCKER)) {
            overclockers++;
        } else if (isTransformerUpgradable() && (getMaxSafeInput() < 512 && getOutput() < 128
                ? isUpgrade(stack, UpgradeModuleItem.Type.TRANSFORMER)
                : getMaxSafeInput() < 8192 && getOutput() < 2048 && stack.is(GtItems.HV_TRANSFORMER_UPGRADE.get()))) {
            transformers++;
        } else if (isBatteryUpgradable() && isUpgrade(stack, UpgradeModuleItem.Type.ENERGY_STORAGE)) {
            batteries++;
        } else if (isBatteryUpgradable() && stack.is(GtItems.LITHIUM_BATTERY_UPGRADE.get())) {
            liBatteries++;
        } else {
            installed = false;
        }
        if (installed) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            setChanged();
        }
        return installed;
    }

    private static boolean isUpgrade(ItemStack stack, UpgradeModuleItem.Type type) {
        return stack.getItem() instanceof UpgradeModuleItem upgrade && upgrade.getType() == type;
    }

    // --- MJ (BaseMetaTileEntityMJ) ---

    /** MJ receiver only once the MJ converter is installed and its provider exists. */
    @Nullable
    @Override
    public PowerProvider getPowerProvider() {
        return powerProvider;
    }

    /** GT never calls {@link PowerProvider#update}, so this never runs. */
    @Override
    public void doWork() {
    }

    /** {@code updateStatus}: create the provider, then move its MJ into the MJ storage every tick. */
    private void updateStatus() {
        if (!mjConverter) {
            return;
        }
        if (powerProvider == null) {
            powerProvider = new PowerProvider();
            powerProvider.configure(1, 100, 1, 10000);
        } else if (getMJCapacity() > 0 && getStoredMJ() < getMJCapacity()) {
            storedMJ += (int) powerProvider.useEnergy(1.0F, getMJCapacity() - getStoredMJ(), true);
            setChanged();
        }
    }

    /**
     * {@code MetaTileEntity.onRightclick} with a held item that is no upgrade.
     *
     * @return whether the click was used up (no GUI opens)
     */
    public boolean onRightclick(Player player, ItemStack stack) {
        return false;
    }

    /** {@code IMachineBlockUpdateable.onMachineBlockUpdate}: a casing or GT machine nearby changed. */
    public void onMachineBlockUpdate() {
    }

    public int getOverclockers() {
        return overclockers;
    }

    public int getTransformers() {
        return transformers;
    }

    public int getBatteries() {
        return batteries;
    }

    // --- energy values ---

    public int getCapacity() {
        return maxEUStore() + batteries * 10000 + liBatteries * 100000;
    }

    public int getOutput() {
        return maxEUOutput() * (transformers > 0 ? (int) Math.pow(4.0, transformers - 1) : 1);
    }

    public int getMJCapacity() {
        return maxMJStore() + rsEnergyCells * 100000;
    }

    /** Stored EU, at most the capacity ({@code getStored}). */
    public int getStored() {
        return Math.min(storedEnergy, getCapacity());
    }

    public int getStoredMJ() {
        return Math.min(storedMJ, getMJCapacity());
    }

    /** {@code getStoredEnergy}: EU or MJ · 2.5, whichever is larger. */
    public int getStoredEnergy() {
        return Math.max(getStoredMJ() * 5 / 2, getStored());
    }

    /** {@code getTier}: 32 → 1, 128 → 2, 512 → 3, 2048 → 4, 8192 → 5, above → 6. */
    public static int getTier(int value) {
        return value <= 32 ? 1 : value <= 128 ? 2 : value <= 512 ? 3 : value <= 2048 ? 4 : value <= 8192 ? 5 : 6;
    }

    public int getInputTier() {
        return getTier(getMaxSafeInput());
    }

    private void setStoredEU(int energy) {
        storedEnergy = Math.max(0, energy);
        setChanged();
    }

    private boolean decreaseStoredEU(int energy, boolean ignoreTooLessEnergy) {
        if (storedEnergy - energy < 0 && !ignoreTooLessEnergy) {
            return false;
        }
        storedEnergy -= energy;
        if (storedEnergy < 0) {
            storedEnergy = 0;
            return false;
        }
        return true;
    }

    private boolean decreaseStoredMJ(int energy, boolean ignoreTooLessEnergy) {
        if (storedMJ - energy < 0 && !ignoreTooLessEnergy) {
            return false;
        }
        storedMJ -= energy;
        if (storedMJ < 0) {
            storedMJ = 0;
            return false;
        }
        return true;
    }

    /** {@code decreaseStoredEnergy}: MJ first, then EU, then MJ down to zero. */
    public boolean decreaseStoredEnergy(int energy, boolean ignoreTooLessEnergy) {
        return decreaseStoredMJ(energy, false) || decreaseStoredEU(energy, ignoreTooLessEnergy)
                || decreaseStoredMJ(energy, true);
    }

    // --- energy net ---

    @Override
    public boolean acceptsEnergyFrom(EnergyTile emitter, Direction direction) {
        return !isRemoved() && !releaseEnergy && isInputFacing(direction);
    }

    @Override
    public boolean emitsEnergyTo(EnergyTile receiver, Direction direction) {
        if (isRemoved() || releaseEnergy) {
            return releaseEnergy;
        }
        return isOutputFacing(direction);
    }

    @Override
    public int getMaxEnergyOutput() {
        return releaseEnergy ? Integer.MAX_VALUE : getOutput();
    }

    @Override
    public int demandsEnergy() {
        return releaseEnergy || isRemoved() ? 0 : getCapacity() - getStored();
    }

    @Override
    public int injectEnergy(Direction direction, int amount) {
        if (isRemoved()) {
            return amount;
        }
        if (amount > getMaxSafeInput()) {
            doExplosion(amount);
            return 0;
        }
        setStoredEU(storedEnergy + amount);
        return 0;
    }

    @Override
    public int getMaxSafeInput() {
        return maxEUInput() * (int) Math.pow(4.0, transformers);
    }

    @Override
    public void setRemoved() {
        EnergyNets.remove(level, this);
        addedToNet = false;
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        EnergyNets.remove(level, this);
        addedToNet = false;
        super.onChunkUnloaded();
    }

    // --- explosions ---

    /**
     * {@code doEnergyExplosion}: explodes if at least a fifth of the capacity is stored.
     *
     * @return whether the machine exploded
     */
    public boolean doEnergyExplosion() {
        if (getCapacity() > 0 && getStoredEnergy() >= getCapacity() / 5) {
            doExplosion(getOutput() * (getStoredEnergy() >= getCapacity() ? 4
                    : getStoredEnergy() >= getCapacity() / 2 ? 2 : 1));
            return true;
        }
        return false;
    }

    /** {@code doExplosion}: wire fire through the net, then the block vanishes in a vanilla explosion. */
    public void doExplosion(int amount) {
        Level level = this.level;
        BlockPos pos = worldPosition;
        if (level == null) {
            return;
        }
        if (addedToNet) {
            releaseEnergy = true;
            EnergyNets.remove(level, this);
            EnergyNets.add(level, this);
            for (int packet : new int[]{32, 128, 512, 2048, 8192}) {
                EnergyNets.emit(level, this, packet);
            }
        }
        releaseEnergy = false;
        float strength = amount < 10 ? 1.0F : amount < 32 ? 2.0F : amount < 128 ? 3.0F : amount < 512 ? 4.0F
                : amount < 2048 ? 5.0F : amount < 4096 ? 6.0F : amount < 8192 ? 7.0F : 8.0F;
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, strength,
                Level.ExplosionInteraction.BLOCK);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    // --- persistence (GT field names) ---

    /** {@code setItemNBT}: extra values the wrenched machine item keeps (restored by {@link #loadAdditional}). */
    protected void saveItemData(CompoundTag tag) {
    }

    private void saveUpgrades(CompoundTag tag) {
        if (mjConverter) {
            tag.putBoolean("mMJConverter", true);
        }
        putIfPositive(tag, "mTransformers", transformers);
        putIfPositive(tag, "mOverclockers", overclockers);
        putIfPositive(tag, "mLiBatteries", liBatteries);
        putIfPositive(tag, "mRSEnergyCells", rsEnergyCells);
        putIfPositive(tag, "mBatteries", batteries);
        saveItemData(tag);
    }

    private static void putIfPositive(CompoundTag tag, String key, int value) {
        if (value > 0) {
            tag.putByte(key, (byte) value);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        storedEnergy = tag.getInt("mStoredEnergy");
        storedMJ = tag.getInt("mStoredMJ");
        overclockers = tag.getByte("mOverclockers");
        transformers = tag.getByte("mTransformers");
        liBatteries = tag.getByte("mLiBatteries");
        rsEnergyCells = tag.getByte("mRSEnergyCells");
        batteries = tag.getByte("mBatteries");
        mjConverter = tag.getBoolean("mMJConverter");
        tickTimer = 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("mStoredEnergy", storedEnergy);
        tag.putInt("mStoredMJ", storedMJ);
        tag.putByte("mOverclockers", (byte) overclockers);
        tag.putByte("mTransformers", (byte) transformers);
        tag.putByte("mLiBatteries", (byte) liBatteries);
        tag.putByte("mRSEnergyCells", (byte) rsEnergyCells);
        tag.putByte("mBatteries", (byte) batteries);
        tag.putBoolean("mMJConverter", mjConverter);
    }

    public boolean hasMJConverter() {
        return mjConverter;
    }

    /** Test hook: stored EU. */
    public int getStoredEU() {
        return storedEnergy;
    }

    /** Test hook. */
    public void setStoredEnergyForTest(int energy) {
        setStoredEU(energy);
    }

    /** Test hook: skips the two-second start-up delay. */
    public void skipStartupForTest() {
        tickTimer = Math.max(tickTimer, 40);
    }
}
