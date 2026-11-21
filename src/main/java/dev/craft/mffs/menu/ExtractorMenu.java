package dev.craft.mffs.menu;

import dev.craft.ic2.menu.SyncedInts;
import dev.craft.mffs.MffsMenus;
import dev.craft.mffs.block.ExtractorBlockEntity;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * {@code ContainerForceEnergyExtractor}: forcicium (82, 26), power link (145, 40), capacity (20, 66), booster
 * (39, 66), cell (112, 26), player inventory from (8, 104). Synced: work done %, work cycle, FE buffer and its max.
 */
public class ExtractorMenu extends MffsMachineMenu {
    public ExtractorMenu(int id, Inventory inventory, ExtractorBlockEntity extractor) {
        this(id, inventory, extractor.getBlockPos(), extractor, extractor.getInventory(),
                new SyncedInts(extractor::getWorkDone, extractor::getWorkCycle, extractor::getForceEnergyBuffer,
                        extractor::getMaxForceEnergyBuffer), extractor);
    }

    public ExtractorMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, pos, null, new ItemStackHandler(5), new SyncedInts(4),
                rulesAt(inventory, pos));
    }

    private ExtractorMenu(int id, Inventory inventory, BlockPos pos, ExtractorBlockEntity extractor,
                          IItemHandler items, SyncedInts data, MffsMachineBlockEntity rules) {
        super(MffsMenus.EXTRACTOR.get(), id, pos, extractor, data);
        addMachineSlot(items, ExtractorBlockEntity.SLOT_FORCICIUM, 82, 26, rules);
        addMachineSlot(items, ExtractorBlockEntity.SLOT_POWER_LINK, 145, 40, rules);
        addMachineSlot(items, ExtractorBlockEntity.SLOT_CAPACITY, 20, 66, rules);
        addMachineSlot(items, ExtractorBlockEntity.SLOT_BOOSTER, 39, 66, rules);
        addMachineSlot(items, ExtractorBlockEntity.SLOT_CELL, 112, 26, rules);
        addPlayerInventory(inventory, 8, 104);
    }

    static MffsMachineBlockEntity rulesAt(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        return blockEntity instanceof MffsMachineBlockEntity machine ? machine : null;
    }

    public int getWorkDone() {
        return getInt(0);
    }

    public int getWorkCycle() {
        return getInt(1);
    }

    public int getForceEnergyBuffer() {
        return getInt(2);
    }

    public int getMaxForceEnergyBuffer() {
        return Math.max(1, getInt(3));
    }
}
