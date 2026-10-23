package dev.craft.gregtech.machine;

import dev.craft.core.multiblock.BlockMatcher;
import dev.craft.core.multiblock.MultiblockPattern;
import dev.craft.gregtech.GtBlockEntityTypes;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.block.GtCasingBlock;
import dev.craft.gregtech.menu.GtImplosionCompressorMenu;
import dev.craft.gregtech.recipe.GtRecipe;
import dev.craft.ic2.machine.SideAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * {@code GT_MetaTileEntity_ImplosionCompressor} (spec 13 §8): sits on top of a hollow 3 × 3 × 3 cube of machine casings
 * and compresses materials with industrial TNT.
 */
public class ImplosionCompressorBlockEntity extends GtMachineBlockEntity {
    public static final int SLOT_INPUT_1 = 0;
    public static final int SLOT_INPUT_2 = 1;
    public static final int SLOT_OUTPUT_1 = 2;
    public static final int SLOT_OUTPUT_2 = 3;
    private static final int UPDATE_DELAY = 5;

    /** Standard casings on the eight corners, reinforced casings elsewhere, air in the middle; independent of facing. */
    public static final MultiblockPattern PATTERN = MultiblockPattern.builder()
            .layer("SRS", "RRR", "SRS")
            .layer("RRR", "RAR", "RRR")
            .layer("SRS", "RRR", "SRS")
            .layer("   ", " K ", "   ")
            .controller('K', BlockMatcher.ANY)
            .where('S', casing(GtCasingBlock.Type.STANDARD))
            .where('R', casing(GtCasingBlock.Type.REINFORCED))
            .where('A', BlockMatcher.state(BlockState::isAir))
            .build();

    private int progressTime;
    private int maxProgressTime;
    private int euPerTick;
    private int update = UPDATE_DELAY;
    private boolean machine;
    private ItemStack outputItem1 = ItemStack.EMPTY;
    private ItemStack outputItem2 = ItemStack.EMPTY;

    public ImplosionCompressorBlockEntity(BlockPos pos, BlockState state) {
        super(GtBlockEntityTypes.get(GtMachine.IMPLOSION_COMPRESSOR), pos, state, 4);
    }

    private static BlockMatcher casing(GtCasingBlock.Type type) {
        return BlockMatcher.state(state -> state.getBlock() instanceof GtCasingBlock block && block.getType() == type);
    }

    // --- values ---

    @Override
    public int maxEUInput() {
        return 32;
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
        return facing == Direction.DOWN;
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

    private boolean checkMachine() {
        return PATTERN.match(level, worldPosition, Direction.NORTH).isPresent();
    }

    // --- tick ---

    @Override
    protected void onPostTick() {
        if (update-- == 0) {
            machine = checkMachine();
        }
        setActive(machine);
        if (!machine || maxProgressTime <= 0) {
            if (getStoredEnergy() > 100) {
                checkRecipe();
            }
        } else if (decreaseStoredEnergy(euPerTick, false)) {
            if (++progressTime > maxProgressTime) {
                addOutput(SLOT_OUTPUT_1, outputItem1);
                addOutput(SLOT_OUTPUT_2, outputItem2);
                outputItem1 = ItemStack.EMPTY;
                outputItem2 = ItemStack.EMPTY;
                progressTime = 0;
                maxProgressTime = 0;
            }
            // Also true right after a finished recipe (0 == 0 / 2): GT plays the bang twice.
            if (progressTime == maxProgressTime / 2) {
                level.playSound(null, worldPosition, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4.0F,
                        (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F);
            }
            setChanged();
        }
    }

    private void checkRecipe() {
        if (!machine) {
            return;
        }
        ItemStack input1 = inventory.get(SLOT_INPUT_1);
        ItemStack input2 = inventory.get(SLOT_INPUT_2);
        // Looked up in slot order only, but consumed with GT's shapeless check like every GT machine.
        GtRecipe recipe = GtRecipe.find(level, GtRecipe.Kind.IMPLOSION, input1, input2, false);
        if (recipe == null || !spaceForOutput(recipe) || !recipe.isInputEqual(true, true, input1, input2)) {
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

    /** Progress arrow width 0–20 ({@code GT_Container_ImplosionCompressor}). */
    public int getProgressScale() {
        return Math.max(0, Math.min(20, (progressTime > 0 ? 1 : 0)
                + progressTime * 20 / (maxProgressTime < 1 ? 1 : maxProgressTime)));
    }

    public boolean isMachine() {
        return machine;
    }

    public int getProgressTime() {
        return progressTime;
    }

    public int getMaxProgressTime() {
        return maxProgressTime;
    }

    // --- inventory ---

    /**
     * {@code getInvSideIndex}/{@code getInvSideLength} on absolute sides: top → 0, east/west → outputs, rest → 1.
     * The output sides also put recipe inputs into the input slots (deviation, see {@code abweichungen.md}).
     */
    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return inventory;
        }
        return switch (side) {
            case UP -> new RangedWrapper(inventory, SLOT_INPUT_1, SLOT_INPUT_1 + 1);
            case EAST, WEST -> new SideAccess(inventory)
                    .input(SLOT_INPUT_1, stack -> GtRecipe.isInput(level, GtRecipe.Kind.IMPLOSION, 0, stack))
                    .input(SLOT_INPUT_2, stack -> !GtRecipe.isInput(level, GtRecipe.Kind.IMPLOSION, 0, stack)
                            && GtRecipe.isInput(level, GtRecipe.Kind.IMPLOSION, 1, stack))
                    .outputs(SLOT_OUTPUT_1, SLOT_OUTPUT_2);
            default -> new RangedWrapper(inventory, SLOT_INPUT_2, SLOT_INPUT_2 + 1);
        };
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new GtImplosionCompressorMenu(id, playerInventory, this);
    }

    // --- persistence (GT field names) ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        update = UPDATE_DELAY;
        euPerTick = tag.getInt("mEUt");
        progressTime = tag.getInt("mProgresstime");
        maxProgressTime = tag.getInt("mMaxProgresstime");
        outputItem1 = ItemStack.parseOptional(registries, tag.getCompound("mOutputItem1"));
        outputItem2 = ItemStack.parseOptional(registries, tag.getCompound("mOutputItem2"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
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
