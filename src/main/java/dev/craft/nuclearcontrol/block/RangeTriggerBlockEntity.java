package dev.craft.nuclearcontrol.block;

import javax.annotation.Nullable;

import dev.craft.ic2.machine.Wrenchable;
import dev.craft.nuclearcontrol.NcBlockEntityTypes;
import dev.craft.nuclearcontrol.NcItems;
import dev.craft.nuclearcontrol.card.CardData;
import dev.craft.nuclearcontrol.card.CardState;
import dev.craft.nuclearcontrol.card.PanelCard;
import dev.craft.nuclearcontrol.menu.RangeTriggerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

/**
 * Range trigger ({@code TileEntityRangeTrigger}, spec 46 §7.3): reads the energy of the energy (array) card in slot 0
 * every 21 ticks ({@code updateTicker-- > 0} from 20) and on inventory changes. Energy ≥ upper limit → active, below
 * the lower limit → passive, in between the last state stays (hysteresis; from "unknown" it starts passive). Missing
 * card, invalid card, target out of range or not found → unknown. Redstone on all sides: active XOR inverted.
 */
public class RangeTriggerBlockEntity extends BlockEntity implements Wrenchable, MenuProvider {
    public static final int SLOT_CARD = 0;
    public static final int SLOT_UPGRADE = 1;
    public static final int STATE_UNKNOWN = -1;
    public static final int STATE_PASSIVE = 0;
    public static final int STATE_ACTIVE = 1;
    /** {@code rangeTriggerRefreshPeriod} of the pack config. */
    public static final int REFRESH_PERIOD = 20;
    public static final long DEFAULT_START = 10000000L;
    public static final long DEFAULT_END = 9000000L;
    /** Ten digits in the GUI. */
    public static final long MAX_LEVEL = 9999999999L;

    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return RangeTriggerBlockEntity.isItemValid(slot, stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                update();
            }
        }
    };
    /** {@code ISidedInventory}: bottom only the upgrade, top only the card, the sides both. */
    private final IItemHandler bottom = new RangedWrapper(inventory, SLOT_UPGRADE, SLOT_UPGRADE + 1);
    private final IItemHandler top = new RangedWrapper(inventory, SLOT_CARD, SLOT_CARD + 1);
    private int updateTicker = REFRESH_PERIOD;
    private boolean initialized;
    private int onFire = STATE_PASSIVE;
    private boolean invertRedstone;
    private long levelStart = DEFAULT_START;
    private long levelEnd = DEFAULT_END;

    public RangeTriggerBlockEntity(BlockPos pos, BlockState state) {
        super(NcBlockEntityTypes.RANGE_TRIGGER.get(), pos, state);
    }

    public static boolean isItemValid(int slot, ItemStack stack) {
        return slot == SLOT_CARD
                ? stack.is(NcItems.ENERGY_SENSOR_LOCATION_CARD.get()) || stack.is(NcItems.ENERGY_ARRAY_LOCATION_CARD.get())
                : stack.is(NcItems.RANGE_UPGRADE.get());
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public IItemHandler getInventory(@Nullable Direction side) {
        if (side == Direction.DOWN) {
            return bottom;
        }
        return side == Direction.UP ? top : inventory;
    }

    public void serverTick() {
        if (!initialized) {
            initialized = true;
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
        if (updateTicker-- > 0) {
            return;
        }
        updateTicker = REFRESH_PERIOD;
        update();
    }

    /** {@code onInventoryChanged}. */
    public void update() {
        ItemStack card = inventory.getStackInSlot(SLOT_CARD);
        int fire = STATE_UNKNOWN;
        if (card.getItem() instanceof PanelCard source) {
            ItemStack upgrade = inventory.getStackInSlot(SLOT_UPGRADE);
            int range = InfoPanelBlockEntity.range(upgrade.is(NcItems.RANGE_UPGRADE.get())
                    ? Math.min(upgrade.getCount(), 7) : 0);
            CardData data = CardData.of(card);
            boolean needUpdate = true;
            if (source.isRemoteSensor()) {
                BlockPos target = data.getTarget();
                if (target == null) {
                    needUpdate = false;
                    data.setState(CardState.INVALID_CARD);
                } else if (Math.abs(target.getX() - worldPosition.getX()) > range
                        || Math.abs(target.getY() - worldPosition.getY()) > range
                        || Math.abs(target.getZ() - worldPosition.getZ()) > range) {
                    needUpdate = false;
                    data.setState(CardState.OUT_OF_RANGE);
                }
            }
            if (needUpdate) {
                CardState state = source.update(level, worldPosition, data, range);
                data.setState(state);
                if (state == CardState.OK) {
                    long min = Math.min(levelStart, levelEnd);
                    long max = Math.max(levelStart, levelEnd);
                    long energy = data.getLong("energyL");
                    if (energy >= max) {
                        fire = STATE_ACTIVE;
                    } else if (energy < min || onFire == STATE_UNKNOWN) {
                        fire = STATE_PASSIVE;
                    } else {
                        fire = onFire;
                    }
                }
            }
            if (data.save(card)) {
                setChanged();
            }
        }
        if (fire != onFire) {
            setOnFire(fire);
        }
    }

    private void setOnFire(int fire) {
        onFire = fire;
        setChanged();
        BlockState state = getBlockState();
        int status = fire + 1;
        if (state.getValue(RangeTriggerBlock.STATUS) != status) {
            level.setBlock(worldPosition, state.setValue(RangeTriggerBlock.STATUS, status), Block.UPDATE_ALL);
        }
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        sync();
    }

    public boolean isEmittingSignal() {
        return onFire > 0 ^ invertRedstone;
    }

    public int getOnFire() {
        return onFire;
    }

    public boolean isInvertRedstone() {
        return invertRedstone;
    }

    /** Unlike the thermal monitors, the neighbours learn about the change at once. */
    public void setInvertRedstone(boolean invert) {
        if (invert == invertRedstone) {
            return;
        }
        invertRedstone = invert;
        setChanged();
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        sync();
    }

    public long getLevelStart() {
        return levelStart;
    }

    public long getLevelEnd() {
        return levelEnd;
    }

    public void setLevel(boolean end, long value) {
        if (value < 0 || value > MAX_LEVEL) {
            return;
        }
        if (end) {
            levelEnd = value;
        } else {
            levelStart = value;
        }
        setChanged();
        sync();
    }

    private void sync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // --- wrench: turn and dismantle only without sneaking ---

    @Override
    public Direction getFacing() {
        return getBlockState().getValue(RangeTriggerBlock.FACING);
    }

    @Override
    public boolean wrenchCanSetFacing(Player player, Direction side) {
        return !player.isShiftKeyDown() && getFacing() != side;
    }

    @Override
    public void setFacing(Direction facing) {
        level.setBlock(worldPosition, getBlockState().setValue(RangeTriggerBlock.FACING, facing), Block.UPDATE_ALL);
    }

    @Override
    public boolean wrenchCanRemove(Player player) {
        return !player.isShiftKeyDown();
    }

    @Override
    public float getWrenchDropRate() {
        return 1.0F;
    }

    @Override
    public ItemStack getWrenchDrop(Player player) {
        return new ItemStack(getBlockState().getBlock());
    }

    // --- menu ---

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new RangeTriggerMenu(id, playerInventory, this);
    }

    // --- persistence and sync ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        invertRedstone = tag.getBoolean("invert");
        if (tag.contains("levelStart")) {
            levelStart = tag.getLong("levelStart");
            levelEnd = tag.getLong("levelEnd");
        }
        onFire = tag.getInt("onFire");
        inventory.deserializeNBT(registries, tag.getCompound("Items"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("invert", invertRedstone);
        tag.putLong("levelStart", levelStart);
        tag.putLong("levelEnd", levelEnd);
        tag.putInt("onFire", onFire);
        tag.put("Items", inventory.serializeNBT(registries));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("invert", invertRedstone);
        tag.putLong("levelStart", levelStart);
        tag.putLong("levelEnd", levelEnd);
        tag.putInt("onFire", onFire);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        invertRedstone = tag.getBoolean("invert");
        levelStart = tag.getLong("levelStart");
        levelEnd = tag.getLong("levelEnd");
        onFire = tag.getInt("onFire");
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, (entity, registries) -> getUpdateTag(registries));
    }
}
