package dev.craft.powerplants.boiler;

import dev.craft.buildcraft.fluid.LiquidContainers;
import dev.craft.ic2.menu.FilteredSlot;
import dev.craft.ic2.menu.MachineMenu;
import dev.craft.ic2.menu.OutputSlot;
import dev.craft.ic2.menu.SyncedInts;
import dev.craft.powerplants.PpMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/** Boiler GUI (Railcraft solid boiler layout): fuel, ash, water container in/out, water and steam tank, heat. */
public class BoilerMenu extends MachineMenu<BoilerBlockEntity> {
    private static final int WATER = 0;
    private static final int STEAM = 1;
    private static final int TEMPERATURE = 2;
    private static final int BURN_TIME = 3;
    private static final int MAX_BURN_TIME = 4;
    private static final int FORMED = 5;

    public BoilerMenu(int id, Inventory inventory, BoilerBlockEntity boiler) {
        this(id, inventory, boiler, boiler.getBlockPos(), new SyncedInts(boiler::getWater, boiler::getSteam,
                boiler::getTemperature, boiler::getBurnTime, boiler::getMaxBurnTime,
                () -> boiler.isFormed() ? 1 : 0));
    }

    public BoilerMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new SyncedInts(6));
    }

    private BoilerMenu(int id, Inventory inventory, @Nullable BoilerBlockEntity boiler, BlockPos pos,
                       SyncedInts data) {
        super(PpMenus.BOILER.get(), id, boiler, pos, data);
        IItemHandler items = items(boiler, 4);
        addMachineSlot(new FilteredSlot(items, BoilerBlockEntity.SLOT_FUEL, 62, 39, BoilerBlockEntity::isFuel));
        addMachineSlot(new OutputSlot(items, BoilerBlockEntity.SLOT_ASH, 89, 38, inventory.player, false));
        addMachineSlot(new FilteredSlot(items, BoilerBlockEntity.SLOT_CONTAINER_IN, 143, 21,
                BoilerMenu::isWaterContainer));
        addMachineSlot(new OutputSlot(items, BoilerBlockEntity.SLOT_CONTAINER_OUT, 143, 56, inventory.player,
                false));
        addPlayerInventory(inventory);
    }

    private static boolean isWaterContainer(ItemStack stack) {
        return LiquidContainers.getLiquidForFilledItem(stack).is(Fluids.WATER);
    }

    public int getWater() {
        return data.getInt(WATER);
    }

    public int getSteam() {
        return data.getInt(STEAM);
    }

    public int getTemperature() {
        return data.getInt(TEMPERATURE);
    }

    public int getScaledBurnTime(int scale) {
        int max = data.getInt(MAX_BURN_TIME);
        return max <= 0 ? 0 : data.getInt(BURN_TIME) * scale / max;
    }

    public boolean isFormed() {
        return data.getInt(FORMED) != 0;
    }

    @Override
    protected int getInputSlot() {
        return BoilerBlockEntity.SLOT_FUEL;
    }
}
