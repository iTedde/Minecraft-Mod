package dev.craft.forestry.menu;

import dev.craft.forestry.FrMenus;
import dev.craft.forestry.machine.FabricatorBlockEntity;
import dev.craft.ic2.menu.SyncedInts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Forestry {@code ContainerFabricator}: stock, melting slot, plan, result ({@code SlotCrafter}) and crafting grid.
 * Synced: liquid id, liquid amount, heat, melting point and for the power ledger stored MJ (float bits), MJ storage
 * and maximum input.
 */
public class FabricatorMenu extends ForestryMenu {
    @Nullable
    private final FabricatorBlockEntity fabricator;
    private final Container resultDisplay = new SimpleContainer(1);

    public FabricatorMenu(int id, Inventory inventory, FabricatorBlockEntity fabricator) {
        this(id, inventory, fabricator, fabricator.getBlockPos(), fabricator.getInventory(), new SyncedInts(
                () -> BuiltInRegistries.FLUID.getId(fabricator.getMoltenTank().getFluid().getFluid()),
                () -> fabricator.getMoltenTank().getFluidAmount(),
                fabricator::getHeat,
                fabricator::getMeltingPoint,
                () -> Float.floatToIntBits(fabricator.getPowerProvider().getEnergyStored()),
                () -> fabricator.getPowerProvider().getMaxEnergyStored(),
                () -> fabricator.getPowerProvider().getMaxEnergyReceived()));
    }

    public FabricatorMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new ItemStackHandler(FabricatorBlockEntity.SLOT_COUNT), new SyncedInts(7));
    }

    private FabricatorMenu(int id, Inventory inventory, @Nullable FabricatorBlockEntity fabricator, BlockPos pos,
                           IItemHandler items, SyncedInts data) {
        super(FrMenus.FABRICATOR.get(), id, pos, data);
        this.fabricator = fabricator;
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new SlotItemHandler(items, FabricatorBlockEntity.SLOT_INVENTORY_1 + column + row * 9,
                        8 + column * 18, 84 + row * 18));
            }
        }
        addSlot(new SlotItemHandler(items, FabricatorBlockEntity.SLOT_METAL, 26, 21));
        addSlot(new SlotItemHandler(items, FabricatorBlockEntity.SLOT_PLAN, 139, 17));
        addSlot(new CrafterSlot(139, 53));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new SlotItemHandler(items, FabricatorBlockEntity.SLOT_CRAFTING_1 + column + row * 3,
                        67 + column * 18, 17 + row * 18));
            }
        }
        setShiftSlots(slots.size());
        addPlayerInventory(inventory, 129, 187);
    }

    public Fluid getFluid() {
        return BuiltInRegistries.FLUID.byId(data.getInt(0));
    }

    public int getFluidAmount() {
        return data.getInt(1);
    }

    public int getHeat() {
        return data.getInt(2);
    }

    public int getMeltingPoint() {
        return data.getInt(3);
    }

    public float getEnergyStored() {
        return Float.intBitsToFloat(data.getInt(4));
    }

    public int getMaxEnergyStored() {
        return data.getInt(5);
    }

    public int getMaxEnergyReceived() {
        return data.getInt(6);
    }

    private void refreshResult() {
        if (fabricator != null) {
            resultDisplay.setItem(0, fabricator.getResult());
        }
    }

    @Override
    public void broadcastChanges() {
        refreshResult();
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        refreshResult();
        return super.quickMoveStack(player, index);
    }

    /**
     * Forestry {@code SlotCrafter}: shows the recipe result; taking it crafts ({@code takeResult(true)}). The
     * server recomputes the result before every access, the client sees the synced copy.
     */
    private final class CrafterSlot extends Slot {
        CrafterSlot(int x, int y) {
            super(resultDisplay, 0, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            refreshResult();
            return true;
        }

        @Override
        public ItemStack remove(int amount) {
            refreshResult();
            return super.remove(getItem().getCount());
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            if (fabricator != null) {
                fabricator.takeResult(true);
                refreshResult();
            }
            super.onTake(player, stack);
        }
    }
}
