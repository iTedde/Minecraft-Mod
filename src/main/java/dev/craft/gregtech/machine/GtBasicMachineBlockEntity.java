package dev.craft.gregtech.machine;

import dev.craft.gregtech.menu.GtBasicMachineMenu;
import dev.craft.ic2.machine.SideAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * GregTech {@code GT_MetaTileEntity_BasicMachine} (spec 13 §1.3–§1.10): two inputs, two outputs, a battery slot,
 * optional EU pass-through and the one-recipe-at-a-time loop. Subclasses implement {@link #checkRecipe()}.
 */
public abstract class GtBasicMachineBlockEntity extends GtMachineBlockEntity {
    public static final int SLOT_INPUT_1 = 1;
    public static final int SLOT_INPUT_2 = 2;
    public static final int SLOT_OUTPUT_1 = 3;
    public static final int SLOT_OUTPUT_2 = 4;
    public static final int SLOT_BATTERY = 5;

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

    /** Machine logic of a subclass: start a recipe by setting {@link #maxProgressTime}, {@link #euPerTick} and outputs. */
    protected abstract void checkRecipe();

    protected boolean hasTwoSeparateInputs() {
        return false;
    }

    // --- values ---

    @Override
    public int maxEUInput() {
        return 32;
    }

    @Override
    public int maxEUOutput() {
        return output ? 32 : 0;
    }

    @Override
    public int maxEUStore() {
        return 2000;
    }

    @Override
    public int maxMJStore() {
        return maxEUStore();
    }

    @Override
    public int getMinimumStoredEU() {
        return 1000;
    }

    @Override
    public int getMaxOutputPackets() {
        return getTransformers() > 0 ? 4 : 1;
    }

    @Override
    protected int dechargerSlot() {
        return SLOT_BATTERY;
    }

    @Override
    public boolean isOverclockerUpgradable() {
        return true;
    }

    @Override
    public boolean isTransformerUpgradable() {
        return true;
    }

    @Override
    public boolean isBatteryUpgradable() {
        return true;
    }

    // --- BasicMachine.onPostTick ---

    @Override
    protected void onPostTick() {
        BlockState state = getBlockState();
        if (state.getValue(GtMachineBlock.FRONT) == GtMachineBlock.Front.NONE && getFacing().getAxis().isHorizontal()) {
            level.setBlock(worldPosition, state.setValue(GtMachineBlock.FRONT, GtMachineBlock.Front.of(getFacing())),
                    Block.UPDATE_ALL);
        }
        if (maxProgressTime > 0 && (progressTime < 0
                || decreaseStoredEnergy(euPerTick * (int) Math.pow(4.0, getOverclockers()), false))) {
            progressTime += (int) Math.pow(2.0, getOverclockers());
            if (progressTime >= maxProgressTime) {
                addOutput(SLOT_OUTPUT_1, outputItem1);
                addOutput(SLOT_OUTPUT_2, outputItem2);
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

    /** Puts {@code product} into {@code slot}; anything beyond the stack limit is lost (GT behaviour). */
    protected void addOutput(int slot, ItemStack product) {
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

    /** Whether {@code product} can be added to {@code current}. */
    protected static boolean fits(ItemStack current, ItemStack product) {
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

    /** {@code mMainFacing}, or {@code null} while unset. */
    @Nullable
    public Direction getMainFacing() {
        return getBlockState().getValue(GtMachineBlock.FRONT).getDirection();
    }

    @Override
    public boolean isFacingValid(Direction facing) {
        return getMainFacing() != null || facing.getAxis().isHorizontal();
    }

    // --- energy net ---

    @Override
    protected boolean isOutputFacing(Direction side) {
        return side != getFacing() && output && side == getFacing().getOpposite();
    }

    @Override
    protected boolean isInputFacing(Direction side) {
        return side != getFacing() && !isOutputFacing(side);
    }

    // --- inventory ---

    /**
     * Sided access (spec 13 §1.7); the front has none. The back also takes items into the inputs like the other
     * sides (deviation, see {@code abweichungen.md}).
     */
    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return inventory;
        }
        if (side == getMainFacing()) {
            return EmptyItemHandler.INSTANCE;
        }
        if (side == getFacing().getOpposite()) {
            return new SideAccess(inventory)
                    .inputs(SLOT_INPUT_1, separatedInputs ? SLOT_INPUT_1 : SLOT_INPUT_2, stack -> true)
                    .outputs(SLOT_OUTPUT_1, SLOT_OUTPUT_2);
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

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new GtBasicMachineMenu(id, playerInventory, this);
    }

    // --- persistence (GT field names) ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
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
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
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
}
