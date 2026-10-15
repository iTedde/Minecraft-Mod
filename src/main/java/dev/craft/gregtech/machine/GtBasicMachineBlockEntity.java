package dev.craft.gregtech.machine;

import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergySource;
import dev.craft.core.energy.EnergyTile;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.menu.GtBasicMachineMenu;
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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * GregTech {@code BaseMetaTileEntity} + {@code GT_MetaTileEntity_BasicMachine} (spec 13 §1): EU sink and optional
 * source, counter-based upgrades, energy explosions and the one-recipe-at-a-time machine loop. Subclasses implement
 * {@link #checkRecipe()}.
 */
public abstract class GtBasicMachineBlockEntity extends MachineBlockEntity implements EnergySink, EnergySource {
    public static final int SLOT_INPUT_1 = 1;
    public static final int SLOT_INPUT_2 = 2;
    public static final int SLOT_OUTPUT_1 = 3;
    public static final int SLOT_OUTPUT_2 = 4;
    public static final int SLOT_BATTERY = 5;
    public static final int MAX_UPGRADES = 16;

    // BaseMetaTileEntity
    private int storedEnergy;
    private int storedMJ;
    private int overclockers;
    private int transformers;
    private int batteries;
    private int liBatteries;
    private int rsEnergyCells;
    private boolean mjConverter;
    private long tickTimer;
    private boolean addedToNet;
    private boolean releaseEnergy;
    @Nullable
    private Direction oldFacing;
    private int oldOutput;

    // BasicMachine
    private boolean output;
    private boolean outputBlocked;
    private boolean itemTransfer = true;
    private boolean separatedInputs = hasTwoSeparateInputs();
    protected int progressTime;
    protected int maxProgressTime;
    protected int euPerTick;
    protected ItemStack outputItem1 = ItemStack.EMPTY;
    protected ItemStack outputItem2 = ItemStack.EMPTY;

    protected GtBasicMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 6);
    }

    public GtMachine getGtMachine() {
        return ((GtMachineBlock) getBlockState().getBlock()).getMachine();
    }

    /** Machine logic of a subclass: start a recipe by setting {@link #maxProgressTime}, {@link #euPerTick} and outputs. */
    protected abstract void checkRecipe();

    protected boolean hasTwoSeparateInputs() {
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
        int inputTier = getInputTier();
        for (int i = 0; i < inputTier; i++) {
            ItemStack battery = inventory.get(SLOT_BATTERY);
            if (!battery.isEmpty() && demandsEnergy() > 0 && battery.getItem() instanceof ElectricItem item
                    && item.canProvideEnergy()) {
                storedEnergy += ElectricItems.discharge(battery, getCapacity() - storedEnergy, inputTier, false, false);
                setChanged();
            }
        }
        onPostTick();
    }

    private void removeFromNet() {
        if (addedToNet) {
            EnergyNets.remove(level, this);
        }
        addedToNet = false;
    }

    // --- BasicMachine.onPostTick ---

    private void onPostTick() {
        BlockState state = getBlockState();
        if (state.getValue(GtMachineBlock.FRONT) == GtMachineBlock.Front.NONE && getFacing().getAxis().isHorizontal()) {
            level.setBlock(worldPosition, state.setValue(GtMachineBlock.FRONT, GtMachineBlock.Front.of(getFacing())),
                    Block.UPDATE_ALL);
        }
        if (maxProgressTime > 0 && (progressTime < 0
                || decreaseStoredEnergy(euPerTick * (int) Math.pow(4.0, overclockers), false))) {
            progressTime += (int) Math.pow(2.0, overclockers);
            if (progressTime >= maxProgressTime) {
                addOutputProducts();
                outputItem1 = ItemStack.EMPTY;
                outputItem2 = ItemStack.EMPTY;
                progressTime = 0;
                maxProgressTime = 0;
            }
            setChanged();
        }
        if (maxProgressTime <= 0 && getStoredEnergy() > 900) {
            checkRecipe();
            setChanged();
        }
        boolean active = isActive();
        Direction back = getFacing().getOpposite();
        if (itemTransfer && (active && maxProgressTime <= 0 || tickTimer % 1200L == 0L
                || outputBlocked && tickTimer % 20L == 0L)
                && back != getMainFacing() && getStoredEnergy() >= 500
                && (!inventory.get(SLOT_OUTPUT_1).isEmpty() || !inventory.get(SLOT_OUTPUT_2).isEmpty())) {
            IItemHandler target = level.getCapability(Capabilities.ItemHandler.BLOCK, worldPosition.relative(back),
                    getFacing());
            if (target != null) {
                int cost = moveOneStack(target);
                if (cost > 0) {
                    decreaseStoredEnergy(cost, true);
                    cost = moveOneStack(target);
                    if (cost > 0) {
                        decreaseStoredEnergy(cost, true);
                    }
                }
            }
        }
        setActive(maxProgressTime > 0);
    }

    /**
     * {@code GT_Utility.moveOneItemStack}: the first output slot whose stack fits into the first possible target slot
     * moves as a whole (as far as it fits).
     *
     * @return moved items
     */
    private int moveOneStack(IItemHandler target) {
        for (int slot = SLOT_OUTPUT_1; slot <= SLOT_OUTPUT_2; slot++) {
            ItemStack stack = inventory.get(slot);
            if (stack.isEmpty()) {
                continue;
            }
            for (int targetSlot = 0; targetSlot < target.getSlots(); targetSlot++) {
                ItemStack rest = target.insertItem(targetSlot, stack.copy(), true);
                int moved = stack.getCount() - rest.getCount();
                if (moved > 0) {
                    target.insertItem(targetSlot, stack.copyWithCount(moved), false);
                    stack.shrink(moved);
                    inventory.set(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
                    return moved;
                }
            }
        }
        return 0;
    }

    private void addOutputProducts() {
        addOutput(SLOT_OUTPUT_1, outputItem1);
        addOutput(SLOT_OUTPUT_2, outputItem2);
    }

    /** Puts {@code product} into {@code slot}; anything beyond the stack limit is lost (GT behaviour). */
    private void addOutput(int slot, ItemStack product) {
        if (product.isEmpty()) {
            return;
        }
        ItemStack current = inventory.get(slot);
        if (current.isEmpty()) {
            inventory.set(slot, product.copy());
        } else if (ItemStack.isSameItemSameComponents(current, product)) {
            current.setCount(Math.min(product.getMaxStackSize(), product.getCount() + current.getCount()));
            inventory.set(slot, current);
        }
    }

    /** {@code spaceForOutput}; sets the (never reset) output-blocked flag when the products do not fit. */
    protected boolean spaceForOutput(ItemStack output1, ItemStack output2) {
        if (!fits(inventory.get(SLOT_OUTPUT_1), output1) || !fits(inventory.get(SLOT_OUTPUT_2), output2)) {
            outputBlocked = true;
            return false;
        }
        return true;
    }

    private static boolean fits(ItemStack current, ItemStack product) {
        return current.isEmpty() || product.isEmpty() || current.getCount() + product.getCount()
                <= current.getMaxStackSize() && ItemStack.isSameItemSameComponents(current, product);
    }

    /**
     * {@code GT_Utility.moveStackFromSlotAToSlotB} within the machine with stack limits 64/1 and move limits 64/1:
     * merges {@code from} into {@code to}.
     */
    protected void moveStack(int from, int to) {
        ItemStack source = inventory.get(from);
        if (source.isEmpty()) {
            return;
        }
        ItemStack target = inventory.get(to);
        if (!target.isEmpty() && !ItemStack.isSameItemSameComponents(source, target)) {
            return;
        }
        int limit = Math.min(64, source.getMaxStackSize());
        int amount = Math.min(source.getCount(), limit - target.getCount());
        if (amount < 1) {
            return;
        }
        if (target.isEmpty()) {
            inventory.set(to, source.copyWithCount(amount));
        } else {
            target.grow(amount);
            inventory.set(to, target);
        }
        source.shrink(amount);
        inventory.set(from, source.isEmpty() ? ItemStack.EMPTY : source);
    }

    // --- facing ---

    @Override
    public Direction getFacing() {
        return getBlockState().getValue(GtMachineBlock.FACING);
    }

    /** {@code mMainFacing}, or {@code null} while unset. */
    @Nullable
    public Direction getMainFacing() {
        return getBlockState().getValue(GtMachineBlock.FRONT).getDirection();
    }

    public boolean isFacingValid(Direction facing) {
        return getMainFacing() != null || facing.getAxis().isHorizontal();
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
        if (getUpgradeCount() >= MAX_UPGRADES || !(stack.getItem() instanceof UpgradeModuleItem upgrade)) {
            return false;
        }
        boolean installed = switch (upgrade.getType()) {
            case OVERCLOCKER -> {
                if (overclockers < 4) {
                    overclockers++;
                    yield true;
                }
                yield false;
            }
            case TRANSFORMER -> {
                if (getMaxSafeInput() < 512 && getOutput() < 128) {
                    transformers++;
                    yield true;
                }
                yield false;
            }
            case ENERGY_STORAGE -> {
                batteries++;
                yield true;
            }
        };
        if (installed) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            setChanged();
        }
        return installed;
    }

    // --- energy values ---

    public int getMinimumStoredEU() {
        return 1000;
    }

    public int maxEUInput() {
        return 32;
    }

    public int maxEUOutput() {
        return output ? 32 : 0;
    }

    public int maxEUStore() {
        return 2000;
    }

    public int maxMJStore() {
        return maxEUStore();
    }

    public int getCapacity() {
        return maxEUStore() + batteries * 10000 + liBatteries * 100000;
    }

    public int getOutput() {
        return maxEUOutput() * (transformers > 0 ? (int) Math.pow(4.0, transformers - 1) : 1);
    }

    public int getMaxOutputPackets() {
        return transformers > 0 ? 4 : 1;
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

    private boolean isOutputFacing(Direction side) {
        return side != getFacing() && output && side == getFacing().getOpposite();
    }

    private boolean isInputFacing(Direction side) {
        return side != getFacing() && !isOutputFacing(side);
    }

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

    // --- inventory ---

    /** Sided access (spec 13 §1.7); the front has none. */
    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return inventory;
        }
        if (side == getMainFacing()) {
            return EmptyItemHandler.INSTANCE;
        }
        if (side == getFacing().getOpposite()) {
            return new RangedWrapper(inventory, SLOT_OUTPUT_1, SLOT_OUTPUT_2 + 1);
        }
        if (separatedInputs && side.getAxis().isVertical()) {
            return new RangedWrapper(inventory, SLOT_INPUT_2, SLOT_INPUT_2 + 1);
        }
        return new RangedWrapper(inventory, SLOT_INPUT_1, separatedInputs ? SLOT_INPUT_1 + 1 : SLOT_INPUT_2 + 1);
    }

    /** All slots but the unused slot 0. */
    @Override
    public void dropContents() {
        inventory.set(0, ItemStack.EMPTY);
        super.dropContents();
    }

    // --- GUI toggles and values ---

    public boolean isOutputEnabled() {
        return output;
    }

    public boolean isItemTransfer() {
        return itemTransfer;
    }

    public boolean isSeparatedInputs() {
        return separatedInputs;
    }

    public void toggleOutput() {
        output = !output;
        setChanged();
    }

    public void toggleItemTransfer() {
        itemTransfer = !itemTransfer;
        setChanged();
    }

    public void toggleSeparatedInputs() {
        separatedInputs = !separatedInputs;
        setChanged();
    }

    /** Progress arrow width 0–20 ({@code GT_Container_BasicMachine}). */
    public int getProgressBar() {
        return Math.max(0, Math.min(20, (progressTime > 0 ? 1 : 0)
                + progressTime * 20 / (maxProgressTime < 1 ? 1 : maxProgressTime)));
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

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new GtBasicMachineMenu(id, playerInventory, this);
    }

    // --- persistence (GT field names) ---

    private void saveUpgrades(CompoundTag tag) {
        if (mjConverter) {
            tag.putBoolean("mMJConverter", true);
        }
        putIfPositive(tag, "mTransformers", transformers);
        putIfPositive(tag, "mOverclockers", overclockers);
        putIfPositive(tag, "mLiBatteries", liBatteries);
        putIfPositive(tag, "mRSEnergyCells", rsEnergyCells);
        putIfPositive(tag, "mBatteries", batteries);
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
        if (tag.contains("bOutput")) {
            output = tag.getBoolean("bOutput");
            itemTransfer = tag.getBoolean("bItemTransfer");
            separatedInputs = tag.getBoolean("bSeperatedInputs");
        }
        euPerTick = tag.getInt("mEUt");
        progressTime = tag.getInt("mProgresstime");
        maxProgressTime = tag.getInt("mMaxProgresstime");
        outputItem1 = ItemStack.parseOptional(registries, tag.getCompound("mOutputItem1"));
        outputItem2 = ItemStack.parseOptional(registries, tag.getCompound("mOutputItem2"));
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
        tag.putBoolean("bOutput", output);
        tag.putBoolean("bItemTransfer", itemTransfer);
        tag.putBoolean("bSeperatedInputs", separatedInputs);
        tag.putInt("mEUt", euPerTick);
        tag.putInt("mProgresstime", progressTime);
        tag.putInt("mMaxProgresstime", maxProgressTime);
        if (!outputItem1.isEmpty()) {
            tag.put("mOutputItem1", outputItem1.save(registries));
        }
        if (!outputItem2.isEmpty()) {
            tag.put("mOutputItem2", outputItem2.save(registries));
        }
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
