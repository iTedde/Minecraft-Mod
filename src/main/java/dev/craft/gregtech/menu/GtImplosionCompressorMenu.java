package dev.craft.gregtech.menu;

import dev.craft.gregtech.GtMenus;
import dev.craft.gregtech.machine.ImplosionCompressorBlockEntity;
import dev.craft.ic2.menu.MachineMenu;
import dev.craft.ic2.menu.MachineSlot;
import dev.craft.ic2.menu.OutputSlot;
import dev.craft.ic2.menu.SyncedInts;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/** {@code GT_Container_ImplosionCompressor} (spec 13 §8): two inputs, two outputs, progress arrow, structure state. */
public class GtImplosionCompressorMenu extends MachineMenu<ImplosionCompressorBlockEntity> {
    private static final int PROGRESS = 0;
    private static final int MACHINE = 1;
    private static final int MACHINE_SLOTS = 4;

    public GtImplosionCompressorMenu(int id, Inventory inventory, ImplosionCompressorBlockEntity compressor) {
        this(id, inventory, compressor, compressor.getBlockPos(), new SyncedInts(compressor::getProgressScale,
                () -> compressor.isMachine() ? 1 : 0));
    }

    public GtImplosionCompressorMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new SyncedInts(2));
    }

    private GtImplosionCompressorMenu(int id, Inventory inventory, @Nullable ImplosionCompressorBlockEntity compressor,
                                      BlockPos pos, SyncedInts data) {
        super(GtMenus.IMPLOSION_COMPRESSOR.get(), id, compressor, pos, data);
        IItemHandler items = items(compressor, MACHINE_SLOTS);
        addMachineSlot(new MachineSlot(items, ImplosionCompressorBlockEntity.SLOT_INPUT_1, 34, 16));
        addMachineSlot(new MachineSlot(items, ImplosionCompressorBlockEntity.SLOT_INPUT_2, 34, 34));
        addMachineSlot(new OutputSlot(items, ImplosionCompressorBlockEntity.SLOT_OUTPUT_1, 86, 25, inventory.player,
                false));
        addMachineSlot(new OutputSlot(items, ImplosionCompressorBlockEntity.SLOT_OUTPUT_2, 104, 25, inventory.player,
                false));
        addPlayerInventory(inventory);
    }

    /** Progress arrow width 0–20. */
    public int getProgressScale() {
        return Math.min(20, data.getInt(PROGRESS));
    }

    public boolean isMachine() {
        return data.getInt(MACHINE) != 0;
    }

    /** {@code GT_Container.transferStackInSlot}: machine → player inventory (from the back), player → the two inputs. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot source = slots.get(index);
        if (!source.hasItem() || !source.mayPickup(player)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = source.getItem();
        int before = stack.getCount();
        if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(stack, MACHINE_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, 2, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            source.setByPlayer(ItemStack.EMPTY);
        } else {
            source.setChanged();
        }
        if (stack.getCount() != before) {
            source.onTake(player, stack);
        }
        return ItemStack.EMPTY;
    }
}
