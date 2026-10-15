package dev.craft.forestry.menu;

import dev.craft.buildcraft.fluid.LiquidContainers;
import dev.craft.forestry.FrMenus;
import dev.craft.forestry.machine.CarpenterBlockEntity;
import dev.craft.forestry.recipe.CarpenterRecipe;
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
 * Forestry {@code ContainerCarpenter}: stock, liquid container, box, product, the locked recipe display and the
 * phantom pattern. Changing the pattern or the box looks the recipe up again. Synced: package time, total time,
 * liquid id, liquid amount and whether the carpenter works.
 */
public class CarpenterMenu extends ForestryMenu {
    @Nullable
    private final CarpenterBlockEntity carpenter;
    private final Container productDisplay = new SimpleContainer(1);

    public CarpenterMenu(int id, Inventory inventory, CarpenterBlockEntity carpenter) {
        this(id, inventory, carpenter, carpenter.getBlockPos(), carpenter.getInventory(), new SyncedInts(
                carpenter::getPackageTime,
                carpenter::getTotalTime,
                () -> BuiltInRegistries.FLUID.getId(carpenter.getResourceTank().getFluid().getFluid()),
                () -> carpenter.getResourceTank().getFluidAmount(),
                () -> carpenter.isWorking() ? 1 : 0));
        carpenter.menuOpened();
    }

    public CarpenterMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new ItemStackHandler(CarpenterBlockEntity.SLOT_COUNT), new SyncedInts(5));
    }

    private CarpenterMenu(int id, Inventory inventory, @Nullable CarpenterBlockEntity carpenter, BlockPos pos,
                          IItemHandler items, SyncedInts data) {
        super(FrMenus.CARPENTER.get(), id, pos, data);
        this.carpenter = carpenter;
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new SlotItemHandler(items, CarpenterBlockEntity.SLOT_INVENTORY_1 + column + row * 9,
                        8 + column * 18, 90 + row * 18));
            }
        }
        addSlot(new SlotItemHandler(items, CarpenterBlockEntity.SLOT_CAN_INPUT, 120, 20) {
            /** {@code SlotLiquidContainer}: filled liquid containers only. */
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !LiquidContainers.getLiquidForFilledItem(stack).isEmpty();
            }
        });
        addSlot(new SlotItemHandler(items, CarpenterBlockEntity.SLOT_BOX, 83, 20) {
            /** {@code SlotCraftAuto}. */
            @Override
            public void setChanged() {
                super.setChanged();
                updateRecipe();
            }
        });
        addSlot(new SlotItemHandler(items, CarpenterBlockEntity.SLOT_PRODUCT, 120, 56));
        addSlot(new Slot(productDisplay, 0, 80, 51) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return false;
            }
        });
        setShiftSlots(slots.size());
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new PhantomSlot(items, CarpenterBlockEntity.SLOT_CRAFTING_1 + column + row * 3,
                        10 + column * 18, 20 + row * 18, this::updateRecipe));
            }
        }
        addPlayerInventory(inventory, 136, 194);
    }

    public int getPackageTime() {
        return data.getInt(0);
    }

    public int getTotalTime() {
        return data.getInt(1);
    }

    public Fluid getFluid() {
        return BuiltInRegistries.FLUID.byId(data.getInt(2));
    }

    public int getFluidAmount() {
        return data.getInt(3);
    }

    public boolean isWorking() {
        return data.getInt(4) != 0;
    }

    /** {@code getCraftingProgressScaled}. */
    public int getCraftingProgressScaled(int scale) {
        return getTotalTime() == 0 ? 0 : getPackageTime() * scale / getTotalTime();
    }

    /** {@code resetProductDisplay}. */
    private void updateRecipe() {
        if (carpenter != null) {
            carpenter.updateRecipe();
        }
    }

    /** {@code updateProductSlot}: the current recipe's result in the locked display slot. */
    @Override
    public void broadcastChanges() {
        if (carpenter != null) {
            CarpenterRecipe recipe = carpenter.getCurrentRecipe();
            productDisplay.setItem(0, recipe == null ? ItemStack.EMPTY : recipe.getResult().copy());
        }
        super.broadcastChanges();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (carpenter != null) {
            carpenter.menuClosed();
        }
    }
}
