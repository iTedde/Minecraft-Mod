package dev.craft.ic2.menu;

import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Menus;
import dev.craft.ic2.crop.CropMatronBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/** IC2 {@code ContainerCropmatron}: three columns of three slots (fertilizer, hydration cells, Weed-EX); syncs energy. */
public class CropMatronMenu extends MachineMenu<CropMatronBlockEntity> {
    private static final int ENERGY = 0;

    public CropMatronMenu(int id, Inventory inventory, CropMatronBlockEntity matron) {
        this(id, inventory, matron, matron.getBlockPos(), new SyncedInts(matron::getEnergy));
    }

    public CropMatronMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new SyncedInts(1));
    }

    private CropMatronMenu(int id, Inventory inventory, @Nullable CropMatronBlockEntity matron, BlockPos pos,
                           SyncedInts data) {
        super(Ic2Menus.get(Ic2Machine.CROP_MATRON), id, matron, pos, data);
        IItemHandler items = items(matron, 9);
        addColumn(items, CropMatronBlockEntity.SLOT_FERTILIZER, 62, Ic2Items.FERTILIZER.get());
        addColumn(items, CropMatronBlockEntity.SLOT_HYDRATION, 98, Ic2Items.HYDRATING_CELL.get());
        addColumn(items, CropMatronBlockEntity.SLOT_WEED_EX, 134, Ic2Items.WEED_EX.get());
        addPlayerInventory(inventory);
    }

    private void addColumn(IItemHandler items, int first, int x, Item item) {
        for (int row = 0; row < 3; row++) {
            addMachineSlot(new FilteredSlot(items, first + row, x, 20 + row * 18, stack -> stack.is(item)));
        }
    }

    public int getEnergy() {
        return data.getInt(ENERGY);
    }

    /** IC2 {@code getInput} returns 0 (the first fertilizer slot). */
    @Override
    protected int getInputSlot() {
        return CropMatronBlockEntity.SLOT_FERTILIZER;
    }
}
