package dev.craft.buildcraft.menu;

import dev.craft.buildcraft.BcMenus;
import dev.craft.buildcraft.factory.RefineryBlockEntity;
import dev.craft.buildcraft.fluid.LiquidContainers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/**
 * BuildCraft {@code ContainerRefinery}: only the player inventory; the two filters are synced as fluid ids. A click
 * on a filter field with a filled container on the cursor sets that filter (menu button 0 or 1).
 */
public class RefineryMenu extends AbstractContainerMenu {
    @Nullable
    private final RefineryBlockEntity refinery;
    private final BlockPos pos;
    private final ContainerData filters;

    public RefineryMenu(int id, Inventory inventory, RefineryBlockEntity refinery) {
        this(id, inventory, refinery, refinery.getBlockPos(), refinery.getFilterData());
    }

    public RefineryMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new SimpleContainerData(2));
    }

    private RefineryMenu(int id, Inventory inventory, @Nullable RefineryBlockEntity refinery, BlockPos pos,
                         ContainerData filters) {
        super(BcMenus.REFINERY.get(), id);
        this.refinery = refinery;
        this.pos = pos;
        this.filters = filters;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 123 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 181));
        }
        addDataSlots(filters);
    }

    public BlockPos getPos() {
        return pos;
    }

    public Fluid getFilter(int number) {
        return BuiltInRegistries.FLUID.byId(filters.get(number));
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (refinery == null || id < 0 || id > 1) {
            return false;
        }
        FluidStack liquid = LiquidContainers.getLiquidForFilledItem(getCarried());
        if (liquid.isEmpty()) {
            return false;
        }
        refinery.setFilter(id, liquid.getFluid());
        return true;
    }

    /** The original container has no shift-click transfer. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    /** {@code isUseableByPlayer}: only checks that the refinery is still there. */
    @Override
    public boolean stillValid(Player player) {
        return refinery == null || refinery.getLevel() != null
                && refinery.getLevel().getBlockEntity(refinery.getBlockPos()) == refinery;
    }
}
