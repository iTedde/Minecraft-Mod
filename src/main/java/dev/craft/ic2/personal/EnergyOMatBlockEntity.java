package dev.craft.ic2.personal;

import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergySource;
import dev.craft.core.energy.EnergyTile;
import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * IC2 {@code TileEntityEnergyOMat}: sells EU. Slot 0 is the price pattern, payments in slot 1 unlock
 * {@code euOffer / pattern count} EU each, which the block then takes in through its front and emits on the other
 * sides; slot 2 takes an energy storage or transformer upgrade.
 */
public class EnergyOMatBlockEntity extends PersonalBlockEntity implements EnergySink, EnergySource {
    public static final int SLOT_PRICE = 0;
    public static final int SLOT_PAYMENT = 1;
    public static final int SLOT_UPGRADE = 2;
    public static final int BUTTON_MINUS_1000 = 0;
    public static final int BUTTON_MINUS_100 = 1;
    public static final int BUTTON_PLUS_1000 = 2;
    public static final int BUTTON_PLUS_100 = 3;

    private int euOffer = 1000;
    private int paidFor;
    private int euBuffer;
    private int euBufferMax = 10000;
    private int maxOutputRate = 32;

    public EnergyOMatBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.ENERGY_O_MAT), pos, state, 3);
    }

    public int getEuOffer() {
        return euOffer;
    }

    public int getPaidFor() {
        return paidFor;
    }

    public int getEuBuffer() {
        return euBuffer;
    }

    @Override
    public void serverTick() {
        euBufferMax = 10000;
        maxOutputRate = 32;
        ItemStack upgrade = inventory.get(SLOT_UPGRADE);
        if (upgrade.is(Ic2Items.ENERGY_STORAGE_UPGRADE.get())) {
            euBufferMax = 10000 * (upgrade.getCount() + 1);
        } else if (upgrade.is(Ic2Items.TRANSFORMER_UPGRADE.get())) {
            maxOutputRate = 32 * (int) Math.pow(4.0, Math.min(4, upgrade.getCount()));
        }
        ItemStack price = inventory.get(SLOT_PRICE);
        ItemStack payment = inventory.get(SLOT_PAYMENT);
        if (!payment.isEmpty() && !price.isEmpty() && ItemStack.isSameItem(payment, price)) {
            paidFor += euOffer / price.getCount() * payment.getCount();
            // Original: whatever does not fit into the first inventory that takes all of it is lost.
            ItemStack rest = payment.copy();
            for (IItemHandler target : PersonalInventories.adjacent(level, worldPosition, this)) {
                if (PersonalInventories.putInInventory(target, rest)) {
                    break;
                }
            }
            inventory.set(SLOT_PAYMENT, ItemStack.EMPTY);
            onInventoryChanged();
        }
        if (euBuffer > euBufferMax) {
            euBuffer = euBufferMax;
        }
        int amount = Math.min(maxOutputRate, euBuffer);
        if (amount > 0) {
            int rest = EnergyNets.emit(level, this, amount);
            euBuffer -= amount - rest;
        }
        setChanged();
    }

    /** IC2 client tile entity event: the owner's ±100/±1000 buttons; the offer never drops to 0 or below. */
    public void pressButton(Player player, int button) {
        if (!canAccess(player)) {
            return;
        }
        int amount = switch (button) {
            case BUTTON_MINUS_1000 -> -1000;
            case BUTTON_MINUS_100 -> -100;
            case BUTTON_PLUS_1000 -> 1000;
            case BUTTON_PLUS_100 -> 100;
            default -> 0;
        };
        if (euOffer + amount <= 0) {
            amount = 0;
        }
        euOffer += amount;
        setChanged();
    }

    @Override
    public boolean wrenchCanSetFacing(Player player, Direction side) {
        return getFacing() != side && canAccess(player);
    }

    /** IC2 {@code getStartInventorySide}: every side → payment slot. */
    @Override
    protected int getSlotForSide(Direction side) {
        return SLOT_PAYMENT;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory, boolean open) {
        return new EnergyOMatMenu(id, inventory, this, open);
    }

    // --- energy net: input on the front, output everywhere else ---

    @Override
    public void onLoad() {
        super.onLoad();
        EnergyNets.add(level, this);
    }

    @Override
    public void setRemoved() {
        EnergyNets.remove(level, this);
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        EnergyNets.remove(level, this);
        super.onChunkUnloaded();
    }

    @Override
    public boolean acceptsEnergyFrom(EnergyTile emitter, Direction direction) {
        return direction == getFacing();
    }

    @Override
    public boolean emitsEnergyTo(EnergyTile receiver, Direction direction) {
        return direction != getFacing();
    }

    @Override
    public int getMaxEnergyOutput() {
        return 32;
    }

    @Override
    public int demandsEnergy() {
        return isRemoved() ? 0 : Math.min(paidFor, euBufferMax - euBuffer);
    }

    @Override
    public int injectEnergy(Direction direction, int amount) {
        int toAdd = Math.min(Math.min(amount, paidFor), euBufferMax - euBuffer);
        paidFor -= toAdd;
        euBuffer += toAdd;
        setChanged();
        return amount - toAdd;
    }

    /** No overvoltage: the Energy-O-Mat takes any packet. */
    @Override
    public int getMaxSafeInput() {
        return Integer.MAX_VALUE;
    }

    // --- persistence ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        euOffer = tag.getInt("euOffer");
        paidFor = tag.getInt("paidFor");
        euBuffer = tag.getInt("euBuffer");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("euOffer", euOffer);
        tag.putInt("paidFor", paidFor);
        tag.putInt("euBuffer", euBuffer);
    }
}
