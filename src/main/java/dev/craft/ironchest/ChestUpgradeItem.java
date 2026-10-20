package dev.craft.ironchest;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Iron Chests {@code ItemChestChanger} (spec 22 §3): turns a closed chest of the source type into the target type,
 * keeping contents and facing. Works before the chest would open ({@code onItemUseFirst}).
 */
public class ChestUpgradeItem extends Item {
    private final ChestUpgradeType type;

    public ChestUpgradeItem(ChestUpgradeType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public ChestUpgradeType getType() {
        return type;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Direction facing;
        List<ItemStack> contents;
        if (level.getBlockEntity(pos) instanceof IronChestBlockEntity chest) {
            if (chest.getNumUsingPlayers() > 0 || !type.canUpgrade(chest.getChestType())) {
                return InteractionResult.PASS;
            }
            facing = state.getValue(IronChestBlock.FACING);
            contents = takeAll(chest);
        } else if (state.is(Blocks.CHEST) && level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            // Only the plain chest (the trapped chest came with 1.5); the other half of a double chest stays.
            if (ChestBlockEntity.getOpenCount(level, pos) > 0 || !type.canUpgrade(null)) {
                return InteractionResult.PASS;
            }
            facing = state.getValue(ChestBlock.FACING);
            contents = takeAll(chest);
        } else {
            return InteractionResult.PASS;
        }
        upgrade(level, pos, facing, contents, type.getTarget());
        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    /** Empties the old chest first, so replacing its block drops nothing. */
    private static List<ItemStack> takeAll(Container container) {
        List<ItemStack> contents = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            contents.add(container.removeItemNoUpdate(i));
        }
        return contents;
    }

    /** Slot by slot into the new chest; what does not fit drops one block above (never with the original upgrades). */
    static void upgrade(Level level, BlockPos pos, Direction facing, List<ItemStack> contents, IronChestType target) {
        level.setBlock(pos, IronChestBlocks.chest(target).get().defaultBlockState()
                .setValue(IronChestBlock.FACING, facing), 3);
        if (!(level.getBlockEntity(pos) instanceof IronChestBlockEntity chest)) {
            return;
        }
        for (int i = 0; i < contents.size(); i++) {
            ItemStack stack = contents.get(i);
            if (i < chest.getContainerSize()) {
                chest.setItem(i, stack);
            } else if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY() + 1, pos.getZ(), stack);
            }
        }
        chest.setChanged();
    }
}
