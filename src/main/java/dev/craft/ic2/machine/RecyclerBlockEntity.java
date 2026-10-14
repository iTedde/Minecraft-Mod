package dev.craft.ic2.machine;

import dev.craft.Craft;
import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.menu.ElectricMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Recycler (IC2 {@code TileEntityRecycler}): any item, 1 EU/t, 45 ticks, 1/8 chance of scrap. */
public class RecyclerBlockEntity extends ElectricMachineBlockEntity {
    public static final int ENERGY_PER_TICK = 1;
    public static final int OPERATION_LENGTH = 45;
    public static final int MAX_INPUT = 32;
    public static final int RECYCLE_CHANCE = 8;
    /** Items that never give scrap (IC2 blacklist + pack config {@code recyclerBlacklist}). */
    public static final TagKey<Item> BLACKLIST =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Craft.MODID, "recycler_blacklist"));

    public RecyclerBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.RECYCLER), pos, state, ENERGY_PER_TICK, OPERATION_LENGTH, MAX_INPUT);
    }

    @Override
    public void operate() {
        if (!canOperate()) {
            return;
        }
        ItemStack input = inventory.get(SLOT_INPUT);
        boolean blacklisted = input.is(BLACKLIST);
        input.shrink(1);
        if (level.random.nextInt(RECYCLE_CHANCE) == 0 && !blacklisted) {
            ItemStack output = inventory.get(SLOT_OUTPUT);
            if (output.isEmpty()) {
                inventory.set(SLOT_OUTPUT, new ItemStack(Ic2Items.SCRAP.get()));
            } else {
                output.grow(1);
            }
        }
    }

    @Override
    public boolean canOperate() {
        if (inventory.isEmpty(SLOT_INPUT)) {
            return false;
        }
        ItemStack output = inventory.get(SLOT_OUTPUT);
        return output.isEmpty() || output.is(Ic2Items.SCRAP.get()) && output.getCount() < output.getMaxStackSize();
    }

    @Nullable
    @Override
    public ItemStack getResultFor(ItemStack input, boolean adjustInput) {
        return null;
    }

    @Override
    public String getStartSound() {
        return "machines/recycler_op";
    }

    @Override
    public String getInterruptSound() {
        return "machines/interrupt_one";
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new ElectricMachineMenu(id, playerInventory, this);
    }
}
