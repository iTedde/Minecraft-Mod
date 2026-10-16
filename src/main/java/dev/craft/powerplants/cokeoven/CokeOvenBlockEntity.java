package dev.craft.powerplants.cokeoven;

import dev.craft.core.multiblock.BlockMatcher;
import dev.craft.core.multiblock.MultiblockPattern;
import dev.craft.powerplants.PpBlockEntityTypes;
import dev.craft.powerplants.PpBlocks;
import dev.craft.powerplants.PpItems;
import dev.craft.powerplants.block.PlantControllerBlockEntity;
import dev.craft.powerplants.block.SlotAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Coke oven (spec 14 §2): 3×3×3 of coke oven bricks with an air core, the controller in the middle of one side.
 * Turns one coal into one coke every {@link #OPERATION_LENGTH} ticks without power; progress is kept while coal is
 * missing or the output is full.
 */
public class CokeOvenBlockEntity extends PlantControllerBlockEntity {
    public static final int OPERATION_LENGTH = 900;
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;

    private static final MultiblockPattern PATTERN = MultiblockPattern.builder()
            .layer("BBB", "BBB", "BBB")
            .layer("BCB", "BAB", "BBB")
            .layer("BBB", "BBB", "BBB")
            .where('B', BlockMatcher.blocks(PpBlocks.COKE_OVEN_BRICKS.get()))
            .where('A', BlockMatcher.AIR)
            .controller('C', BlockMatcher.blocks(PpBlocks.COKE_OVEN.get()))
            .build();

    private int progress;

    public CokeOvenBlockEntity(BlockPos pos, BlockState state) {
        super(PpBlockEntityTypes.COKE_OVEN.get(), pos, state, 2);
    }

    public static boolean isInput(ItemStack stack) {
        return stack.is(Items.COAL);
    }

    public int getProgress() {
        return progress;
    }

    @Override
    protected MultiblockPattern getPattern() {
        return PATTERN;
    }

    @Override
    protected void tickFormed() {
        ItemStack input = inventory.get(SLOT_INPUT);
        ItemStack output = inventory.get(SLOT_OUTPUT);
        boolean canWork = isInput(input)
                && (output.isEmpty() || output.is(PpItems.COKE.get()) && output.getCount() < output.getMaxStackSize());
        setActive(canWork);
        if (!canWork) {
            return;
        }
        if (++progress >= OPERATION_LENGTH) {
            progress = 0;
            input.shrink(1);
            inventory.notifyChanged(SLOT_INPUT);
            if (output.isEmpty()) {
                inventory.set(SLOT_OUTPUT, new ItemStack(PpItems.COKE.get()));
            } else {
                output.grow(1);
                inventory.notifyChanged(SLOT_OUTPUT);
            }
        }
        setChanged();
    }

    @Nullable
    @Override
    public IItemHandler getPartItemHandler(@Nullable Direction side) {
        if (side == Direction.DOWN) {
            return SlotAccess.output(inventory, SLOT_OUTPUT);
        }
        return SlotAccess.input(inventory, SLOT_INPUT, CokeOvenBlockEntity::isInput);
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        return side == null ? inventory : getPartItemHandler(side);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new CokeOvenMenu(id, playerInventory, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        progress = tag.getInt("progress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("progress", progress);
    }
}
