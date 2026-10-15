package dev.craft.gregtech.machine;

import java.util.Optional;

import dev.craft.core.multiblock.BlockMatcher;
import dev.craft.core.multiblock.MultiblockPattern;
import dev.craft.gregtech.GtBlockEntityTypes;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.block.GtCasingBlock;
import dev.craft.gregtech.menu.GtBlastFurnaceMenu;
import dev.craft.gregtech.recipe.GtRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * {@code GT_MetaTileEntity_BlastFurnace} (spec 13 §5): multiblock controller in front of a 3 × 3 × 4 shell of machine
 * casings. The heat capacity (casings, lava core, heating coils) decides which recipes run.
 */
public class BlastFurnaceBlockEntity extends GtMachineBlockEntity {
    public static final int SLOT_INPUT_1 = 0;
    public static final int SLOT_INPUT_2 = 1;
    public static final int SLOT_OUTPUT_1 = 2;
    public static final int SLOT_OUTPUT_2 = 3;
    public static final int COIL_HEAT = 500;
    public static final int LAVA_HEAT = 250;
    private static final int UPDATE_DELAY = 5;

    /**
     * Written for a controller facing north: the casing shell lies behind it (south), bottom layer at the controller's
     * height; the two middle cells of the inner layers hold air or lava.
     */
    public static final MultiblockPattern PATTERN = MultiblockPattern.builder()
            .layer(" K ", "CCC", "CCC", "CCC")
            .layers(2, "   ", "CCC", "CLC", "CCC")
            .layer("   ", "CCC", "CCC", "CCC")
            .controller('K', BlockMatcher.ANY)
            .where('C', BlockMatcher.state(state -> state.getBlock() instanceof GtCasingBlock))
            .where('L', BlockMatcher.AIR.or(BlockMatcher.blocks(Blocks.LAVA)))
            .build();

    private int progressTime;
    private int maxProgressTime;
    private int euPerTick;
    private int heatCapacity;
    private int update = UPDATE_DELAY;
    private int heatingCoilTier;
    private boolean machine;
    private int displayErrorCode;
    private ItemStack outputItem1 = ItemStack.EMPTY;
    private ItemStack outputItem2 = ItemStack.EMPTY;

    public BlastFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(GtBlockEntityTypes.get(GtMachine.INDUSTRIAL_BLAST_FURNACE), pos, state, 4);
    }

    // --- values ---

    @Override
    public int maxEUInput() {
        return 128;
    }

    @Override
    public int maxEUStore() {
        return 10000;
    }

    @Override
    public int maxMJStore() {
        return maxEUStore();
    }

    @Override
    protected boolean isInputFacing(Direction side) {
        return true;
    }

    @Override
    public boolean isFacingValid(Direction facing) {
        return facing.getAxis().isHorizontal();
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

    // --- structure ---

    @Override
    public void onMachineBlockUpdate() {
        update = UPDATE_DELAY;
    }

    /** {@code checkMachine}: casings, lava core and coils add up to the heat capacity. */
    private boolean checkMachine() {
        Optional<MultiblockPattern.Match> match = PATTERN.match(level, worldPosition, getFacing());
        if (match.isEmpty()) {
            heatCapacity = 0;
            return false;
        }
        int heat = heatingCoilTier * COIL_HEAT;
        for (BlockPos pos : match.get().get('C')) {
            heat += ((GtCasingBlock) level.getBlockState(pos).getBlock()).getType().getHeat();
        }
        for (BlockPos pos : match.get().get('L')) {
            if (level.getBlockState(pos).is(Blocks.LAVA)) {
                heat += LAVA_HEAT;
            }
        }
        heatCapacity = heat;
        return true;
    }

    /**
     * Heating coils: four Kanthal coils raise tier 0 to 1, four Nichrome coils tier 1 to 2. A matching coil stack
     * with fewer than four items uses up the click without effect.
     */
    @Override
    public boolean onRightclick(Player player, ItemStack stack) {
        Item next = heatingCoilTier <= 0 ? GtItems.KANTHAL_HEATING_COIL.get()
                : heatingCoilTier == 1 ? GtItems.NICHROME_HEATING_COIL.get() : null;
        if (next == null || !stack.is(next)) {
            return false;
        }
        boolean creative = player.getAbilities().instabuild;
        if (stack.getCount() > 3 || creative) {
            if (!creative) {
                stack.shrink(4);
            }
            heatingCoilTier++;
            update = UPDATE_DELAY;
            setChanged();
        }
        return true;
    }

    // --- tick ---

    @Override
    protected void onPostTick() {
        if (update-- == 0) {
            machine = checkMachine();
        }
        setActive(machine);
        if (machine && maxProgressTime > 0) {
            if (progressTime >= 0 && !decreaseStoredEnergy(euPerTick * (int) Math.pow(4.0, getOverclockers()),
                    false)) {
                // constant_need_of_energy: the recipe falls back to just before its start.
                progressTime = -10;
                displayErrorCode = 1;
            } else {
                progressTime += (int) Math.pow(2.0, getOverclockers());
                if (progressTime >= maxProgressTime) {
                    addOutput(SLOT_OUTPUT_1, outputItem1);
                    addOutput(SLOT_OUTPUT_2, outputItem2);
                    outputItem1 = ItemStack.EMPTY;
                    outputItem2 = ItemStack.EMPTY;
                    progressTime = 0;
                    maxProgressTime = 0;
                    displayErrorCode = 0;
                }
            }
            setChanged();
        } else if (getStoredEnergy() > 100) {
            checkRecipe();
        }
    }

    private void checkRecipe() {
        if (!machine) {
            return;
        }
        ItemStack input1 = inventory.get(SLOT_INPUT_1);
        ItemStack input2 = inventory.get(SLOT_INPUT_2);
        GtRecipe recipe = GtRecipe.find(level, GtRecipe.Kind.BLAST_FURNACE, input1, input2, true);
        if (recipe == null || heatCapacity < recipe.getSpecial() || !spaceForOutput(recipe)
                || !recipe.isInputEqual(true, true, input1, input2)) {
            return;
        }
        inventory.set(SLOT_INPUT_1, input1.isEmpty() ? ItemStack.EMPTY : input1);
        inventory.set(SLOT_INPUT_2, input2.isEmpty() ? ItemStack.EMPTY : input2);
        maxProgressTime = recipe.getDuration();
        euPerTick = recipe.getEuPerTick();
        outputItem1 = recipe.getOutput(0).copy();
        outputItem2 = recipe.getOutput(1).copy();
        setChanged();
    }

    private boolean spaceForOutput(GtRecipe recipe) {
        return fits(inventory.get(SLOT_OUTPUT_1), recipe.getOutput(0))
                && fits(inventory.get(SLOT_OUTPUT_2), recipe.getOutput(1));
    }

    private static boolean fits(ItemStack current, ItemStack product) {
        return current.isEmpty() || product.isEmpty() || current.getCount() + product.getCount()
                <= current.getMaxStackSize() && ItemStack.isSameItemSameComponents(current, product);
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
            inventory.set(slot, current.copyWithCount(Math.min(product.getMaxStackSize(),
                    product.getCount() + current.getCount())));
        }
    }

    // --- GUI values ---

    /** Progress arrow width 0–20 ({@code GT_Container_BlastFurnace}). */
    public int getProgressScale() {
        return Math.max(0, Math.min(20, (progressTime > 0 ? 1 : 0)
                + progressTime * 20 / (maxProgressTime < 1 ? 1 : maxProgressTime)));
    }

    public boolean isMachine() {
        return machine;
    }

    public int getHeatCapacity() {
        return heatCapacity;
    }

    public int getHeatingCoilTier() {
        return heatingCoilTier;
    }

    public int getDisplayErrorCode() {
        return displayErrorCode;
    }

    public int getProgressTime() {
        return progressTime;
    }

    // --- inventory ---

    /** {@code getInvSideIndex}/{@code getInvSideLength}: top → slot 0, bottom → slot 1, sides → outputs. */
    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return inventory;
        }
        return switch (side) {
            case UP -> new RangedWrapper(inventory, SLOT_INPUT_1, SLOT_INPUT_1 + 1);
            case DOWN -> new RangedWrapper(inventory, SLOT_INPUT_2, SLOT_INPUT_2 + 1);
            default -> new RangedWrapper(inventory, SLOT_OUTPUT_1, SLOT_OUTPUT_2 + 1);
        };
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new GtBlastFurnaceMenu(id, playerInventory, this);
    }

    // --- persistence (GT field names) ---

    /** {@code setItemNBT}: the coil tier stays with the wrenched machine. */
    @Override
    protected void saveItemData(CompoundTag tag) {
        if (heatingCoilTier > 0) {
            tag.putByte("mHeatingCoilTier", (byte) heatingCoilTier);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        update = UPDATE_DELAY;
        euPerTick = tag.getInt("mEUt");
        progressTime = tag.getInt("mProgresstime");
        maxProgressTime = tag.getInt("mMaxProgresstime");
        heatingCoilTier = tag.getByte("mHeatingCoilTier");
        outputItem1 = ItemStack.parseOptional(registries, tag.getCompound("mOutputItem1"));
        outputItem2 = ItemStack.parseOptional(registries, tag.getCompound("mOutputItem2"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("mEUt", euPerTick);
        tag.putInt("mProgresstime", progressTime);
        tag.putInt("mMaxProgresstime", maxProgressTime);
        tag.putByte("mHeatingCoilTier", (byte) heatingCoilTier);
        if (!outputItem1.isEmpty()) {
            tag.put("mOutputItem1", outputItem1.save(registries));
        }
        if (!outputItem2.isEmpty()) {
            tag.put("mOutputItem2", outputItem2.save(registries));
        }
    }
}
