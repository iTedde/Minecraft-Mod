package dev.craft.ic2.personal;

import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * IC2 {@code TileEntityTradeOMat}: slot 0 wanted payment, 1 offered goods (patterns), 2 payment in, 3 goods out.
 * Trades as often as possible with the first adjacent inventory (≥ 18 slots) that has goods and room.
 */
public class TradeOMatBlockEntity extends PersonalBlockEntity {
    public static final int SLOT_WANT = 0;
    public static final int SLOT_OFFER = 1;
    public static final int SLOT_PAYMENT = 2;
    public static final int SLOT_GOODS = 3;
    private static final int EVENT_TRADE = 0;

    private int totalTradeCount;
    private int stock;
    private boolean stockLoaded;

    public TradeOMatBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.TRADE_O_MAT), pos, state, 4);
    }

    public int getTotalTradeCount() {
        return totalTradeCount;
    }

    public int getStock() {
        return stock;
    }

    @Override
    public void serverTick() {
        if (!stockLoaded) {
            stockLoaded = true;
            updateStock();
        }
        ItemStack want = inventory.get(SLOT_WANT);
        ItemStack offer = inventory.get(SLOT_OFFER);
        ItemStack payment = inventory.get(SLOT_PAYMENT);
        ItemStack goods = inventory.get(SLOT_GOODS);
        if (want.isEmpty() || offer.isEmpty() || payment.isEmpty() || !PersonalInventories.isStackEqual(want, payment)
                || payment.getCount() < want.getCount() || !goods.isEmpty()
                && (!PersonalInventories.isStackEqual(offer, goods)
                || goods.getCount() + offer.getCount() > goods.getMaxStackSize())) {
            return;
        }
        for (IItemHandler target : PersonalInventories.adjacent(level, worldPosition, this)) {
            if (target.getSlots() < 18) {
                continue;
            }
            int inputSpace = 0;
            int outputAvailable = 0;
            for (int i = 0; i < target.getSlots(); i++) {
                ItemStack stack = target.getStackInSlot(i);
                if (stack.isEmpty()) {
                    inputSpace += want.getMaxStackSize();
                } else {
                    if (PersonalInventories.isStackEqual(stack, want)) {
                        inputSpace += stack.getMaxStackSize() - stack.getCount();
                    }
                    if (PersonalInventories.isStackEqual(stack, offer)) {
                        outputAvailable += stack.getCount();
                    }
                }
            }
            int outputSpace = goods.isEmpty() ? offer.getMaxStackSize() : goods.getMaxStackSize() - goods.getCount();
            int tradeCount = Math.min(Math.min(Math.min(payment.getCount() / want.getCount(),
                    inputSpace / want.getCount()), outputSpace / offer.getCount()), outputAvailable / offer.getCount());
            if (tradeCount <= 0) {
                continue;
            }
            int inputCount = want.getCount() * tradeCount;
            int outputCount = offer.getCount() * tradeCount;
            payment.shrink(inputCount);
            ItemStack received = PersonalInventories.getFromInventory(target, offer, outputCount);
            if (received != null) {
                if (goods.isEmpty()) {
                    inventory.set(SLOT_GOODS, received);
                } else {
                    goods.grow(received.getCount());
                }
            }
            PersonalInventories.putInInventory(target, want.copyWithCount(inputCount));
            totalTradeCount += tradeCount;
            level.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_TRADE, 0);
            onInventoryChanged();
            updateStock();
            return;
        }
    }

    /** IC2 {@code updateStock}: goods in all adjacent inventories with at least 18 slots. */
    public void updateStock() {
        stock = 0;
        ItemStack offer = inventory.get(SLOT_OFFER);
        for (IItemHandler target : PersonalInventories.adjacent(level, worldPosition, this)) {
            if (target.getSlots() >= 18) {
                for (int i = 0; i < target.getSlots(); i++) {
                    ItemStack stack = target.getStackInSlot(i);
                    if (PersonalInventories.isStackEqual(offer, stack)) {
                        stock += stack.getCount();
                    }
                }
            }
        }
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == EVENT_TRADE) {
            if (level != null && level.isClientSide()) {
                level.playLocalSound(worldPosition, Ic2Sounds.O_MAT.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
            return true;
        }
        return super.triggerEvent(id, param);
    }

    /** IC2 {@code getStartInventorySide}: bottom → goods out, every other side → payment in. */
    @Override
    protected int getSlotForSide(Direction side) {
        return side == Direction.DOWN ? SLOT_GOODS : SLOT_PAYMENT;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory, boolean open) {
        if (!open) {
            updateStock();
        }
        return new TradeOMatMenu(id, inventory, this, open);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        totalTradeCount = tag.getInt("totalTradeCount");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("totalTradeCount", totalTradeCount);
    }
}
