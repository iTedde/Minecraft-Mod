package dev.craft.nuclearcontrol.block;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import dev.craft.ic2.machine.Wrenchable;
import dev.craft.nuclearcontrol.NcBlockEntityTypes;
import dev.craft.nuclearcontrol.NcItems;
import dev.craft.nuclearcontrol.card.CardData;
import dev.craft.nuclearcontrol.card.CardState;
import dev.craft.nuclearcontrol.card.PanelCard;
import dev.craft.nuclearcontrol.menu.InfoPanelMenu;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Industrial information panel ({@code TileEntityInfoPanel}, spec 46 §5.1). Slots: 0 card, 1 range upgrades (at most
 * 7 count), 2 colour upgrade. Every {@code infoPanelRefreshPeriod} (20, effectively 21) ticks the card is updated;
 * the panel shows it only with a redstone signal (then light 7, also on the extenders of its screen).
 * <p>
 * The screen ({@code ScreenManager}) is the rectangle of extenders with the same facing that grows from the panel in
 * the plane of the front: x−, x+, y−, y+, z−, z+, up to 20 rows each, a row only when all its extenders are free.
 * Unlike the client-side manager of the original, the server builds it and stores the core in the extenders.
 */
public class InfoPanelBlockEntity extends BlockEntity implements Wrenchable, MenuProvider {
    public static final int SLOT_CARD = 0;
    public static final int SLOT_RANGE = 1;
    public static final int SLOT_COLOR = 2;
    /** {@code infoPanelRefreshPeriod} of the pack config. */
    public static final int REFRESH_PERIOD = 20;
    public static final int MAX_SCREEN_STEPS = 20;
    public static final int DISPLAY_DEFAULT = Integer.MAX_VALUE;
    public static final int DEFAULT_BACKGROUND = 2;
    /** {@code COLORS_HEX}: text colours 0–14. */
    public static final int[] COLORS_HEX = {0x000000, 0xE93535, 0x82E306, 0x702B14, 0x1F3CE7, 0x8F1FEA, 0x1FD7E9,
            0xCBCBCB, 0x222222, 0xE60675, 0x1FE723, 0xE9CC1F, 0x06AAE4, 0xB006E3, 0xE7761F};

    private final ItemStackHandler inventory = new ItemStackHandler(3) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return InfoPanelBlockEntity.isItemValid(slot, stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                update();
                sync();
            }
        }
    };
    private final Map<String, Integer> displaySettings = new HashMap<>();
    private boolean initialized;
    private int updateTicker = REFRESH_PERIOD;
    private boolean powered;
    private int rotation;
    private boolean showLabels = true;
    private boolean colored;
    private int colorBackground = DEFAULT_BACKGROUND;
    private int colorText;
    @Nullable
    private BlockPos screenMin;
    @Nullable
    private BlockPos screenMax;

    public InfoPanelBlockEntity(BlockPos pos, BlockState state) {
        super(NcBlockEntityTypes.INFORMATION_PANEL.get(), pos, state);
    }

    public static boolean isItemValid(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_CARD -> stack.getItem() instanceof PanelCard;
            case SLOT_RANGE -> stack.is(NcItems.RANGE_UPGRADE.get());
            case SLOT_COLOR -> stack.is(NcItems.COLOR_UPGRADE.get());
            default -> false;
        };
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public void serverTick() {
        if (!initialized) {
            initialized = true;
            checkPowered();
            rebuildScreen();
            update();
        }
        if (updateTicker-- > 0) {
            return;
        }
        updateTicker = REFRESH_PERIOD;
        update();
    }

    /** {@code onInventoryChanged}: colour upgrade and card update within the range. */
    public void update() {
        boolean nowColored = inventory.getStackInSlot(SLOT_COLOR).is(NcItems.COLOR_UPGRADE.get());
        if (nowColored != colored) {
            colored = nowColored;
            setChanged();
            sync();
        }
        ItemStack card = inventory.getStackInSlot(SLOT_CARD);
        if (!(card.getItem() instanceof PanelCard source)) {
            return;
        }
        int range = range(rangeUpgrades());
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
            data.setState(source.update(level, worldPosition, data, range));
        }
        if (data.save(card)) {
            setChanged();
            sync();
        }
    }

    private int rangeUpgrades() {
        ItemStack stack = inventory.getStackInSlot(SLOT_RANGE);
        return stack.is(NcItems.RANGE_UPGRADE.get()) ? Math.min(stack.getCount(), 7) : 0;
    }

    /** {@code 8 · 2^n} blocks per axis. */
    public static int range(int upgrades) {
        return 8 * (1 << Math.min(upgrades, 7));
    }

    // --- redstone ---

    /** {@code RedstoneHelper.checkPowered}: signal at the block or a powered wire on one of the four sides. */
    public void checkPowered() {
        boolean signal = level.hasNeighborSignal(worldPosition);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockState wire = level.getBlockState(worldPosition.relative(side));
            if (wire.getBlock() instanceof RedStoneWireBlock && wire.getValue(RedStoneWireBlock.POWER) > 0) {
                signal = true;
            }
        }
        if (signal != powered) {
            powered = signal;
            setLit(level, worldPosition, powered);
            forEachScreenBlock(pos -> {
                if (level.getBlockEntity(pos) instanceof InfoPanelExtenderBlockEntity extender
                        && worldPosition.equals(extender.getCore())) {
                    setLit(level, pos, powered);
                }
            });
            setChanged();
            sync();
        }
    }

    static void setLit(Level level, BlockPos pos, boolean lit) {
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(InfoPanelBlock.LIT) && state.getValue(InfoPanelBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(InfoPanelBlock.LIT, lit), Block.UPDATE_CLIENTS);
        }
    }

    // --- screen ---

    public Direction getFacing() {
        return getBlockState().getValue(InfoPanelBlock.FACING);
    }

    /** {@code tryBuildFromPanel}, then frees the extenders that fell out and claims the new ones. */
    public void rebuildScreen() {
        if (level == null || level.isClientSide() || isRemoved()) {
            return;
        }
        Direction facing = getFacing();
        int[] min = {worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()};
        int[] max = min.clone();
        for (Direction.Axis axis : Direction.Axis.values()) {
            if (axis != facing.getAxis()) {
                grow(min, max, axis.ordinal(), -1, facing);
                grow(min, max, axis.ordinal(), 1, facing);
            }
        }
        BlockPos newMin = new BlockPos(min[0], min[1], min[2]);
        BlockPos newMax = new BlockPos(max[0], max[1], max[2]);
        boolean single = newMin.equals(newMax);
        forEachScreenBlock(pos -> {
            if (single || !inside(pos, newMin, newMax)) {
                if (level.getBlockEntity(pos) instanceof InfoPanelExtenderBlockEntity extender
                        && worldPosition.equals(extender.getCore())) {
                    extender.setCore(null);
                    setLit(level, pos, false);
                }
            }
        });
        screenMin = single ? null : newMin;
        screenMax = single ? null : newMax;
        forEachScreenBlock(pos -> {
            if (level.getBlockEntity(pos) instanceof InfoPanelExtenderBlockEntity extender) {
                extender.setCore(worldPosition);
                setLit(level, pos, powered);
            }
        });
        setChanged();
        sync();
    }

    /** {@code updateScreenBound}: adds whole rows of free extenders in one direction, at most 20. */
    private void grow(int[] min, int[] max, int axis, int dir, Direction facing) {
        for (int steps = 0; steps < MAX_SCREEN_STEPS; steps++) {
            int[] from = min.clone();
            int[] to = max.clone();
            int layer = dir < 0 ? min[axis] - 1 : max[axis] + 1;
            from[axis] = layer;
            to[axis] = layer;
            for (BlockPos pos : BlockPos.betweenClosed(from[0], from[1], from[2], to[0], to[1], to[2])) {
                if (!isFreeExtender(pos, facing)) {
                    return;
                }
            }
            if (dir < 0) {
                min[axis] = layer;
            } else {
                max[axis] = layer;
            }
        }
    }

    private boolean isFreeExtender(BlockPos pos, Direction facing) {
        if (!level.isLoaded(pos) || !(level.getBlockEntity(pos) instanceof InfoPanelExtenderBlockEntity extender)) {
            return false;
        }
        return extender.getFacing() == facing
                && (extender.getCore() == null || extender.getCore().equals(worldPosition));
    }

    /** Frees all extenders of the screen ({@code destroyScreen}). */
    public void releaseScreen() {
        forEachScreenBlock(pos -> {
            if (level.getBlockEntity(pos) instanceof InfoPanelExtenderBlockEntity extender
                    && worldPosition.equals(extender.getCore())) {
                extender.setCore(null);
                setLit(level, pos, false);
            }
        });
        screenMin = null;
        screenMax = null;
    }

    private void forEachScreenBlock(java.util.function.Consumer<BlockPos> action) {
        if (screenMin == null || screenMax == null) {
            return;
        }
        for (BlockPos pos : BlockPos.betweenClosed(screenMin, screenMax)) {
            if (!pos.equals(worldPosition) && level.isLoaded(pos)) {
                action.accept(pos.immutable());
            }
        }
    }

    private static boolean inside(BlockPos pos, BlockPos min, BlockPos max) {
        return pos.getX() >= min.getX() && pos.getX() <= max.getX() && pos.getY() >= min.getY()
                && pos.getY() <= max.getY() && pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
    }

    public BlockPos getScreenMin() {
        return screenMin != null ? screenMin : worldPosition;
    }

    public BlockPos getScreenMax() {
        return screenMax != null ? screenMax : worldPosition;
    }

    // --- settings ---

    public boolean isPowered() {
        return powered;
    }

    public int getRotation() {
        return rotation;
    }

    /** {@code IRotation.rotate}: 0 → 1 → 3 → 2 → 0. */
    public void rotate() {
        rotation = switch (rotation) {
            case 0 -> 1;
            case 1 -> 3;
            case 3 -> 2;
            default -> 0;
        };
        setChanged();
        sync();
    }

    public boolean getShowLabels() {
        return showLabels;
    }

    public void setShowLabels(boolean showLabels) {
        this.showLabels = showLabels;
        setChanged();
        sync();
    }

    public boolean isColored() {
        return colored;
    }

    public int getColorBackground() {
        return colorBackground;
    }

    public int getColorText() {
        return colorText;
    }

    public int getColorTextHex() {
        return COLORS_HEX[Math.min(colorText, COLORS_HEX.length - 1)];
    }

    /** {@code setScreenColor}: both values {@code & 15}. */
    public void setColors(int background, int text) {
        colorBackground = background & 15;
        colorText = text & 15;
        setChanged();
        sync();
    }

    public ItemStack getCard() {
        return inventory.getStackInSlot(SLOT_CARD);
    }

    /** Display bits of the current card type, all bits until set. */
    public int getDisplaySettings() {
        if (getCard().getItem() instanceof PanelCard card) {
            return displaySettings.getOrDefault(card.cardType(), DISPLAY_DEFAULT);
        }
        return 0;
    }

    public void setDisplaySettings(int settings) {
        if (getCard().getItem() instanceof PanelCard card) {
            displaySettings.put(card.cardType(), settings);
            setChanged();
            sync();
        }
    }

    /** Title or text line of the card in the panel, from the GUI. */
    public void editCard(java.util.function.Consumer<CardData> edit) {
        ItemStack card = getCard();
        if (card.getItem() instanceof PanelCard) {
            CardData data = CardData.of(card);
            edit.accept(data);
            if (data.save(card)) {
                setChanged();
                sync();
            }
        }
    }

    private void sync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // --- wrench: turn with a plain click, dismantle without sneaking; sneaking turns the text ---

    @Override
    public boolean wrenchCanSetFacing(Player player, Direction side) {
        return !player.isShiftKeyDown() && getFacing() != side;
    }

    @Override
    public void setFacing(Direction facing) {
        level.setBlock(worldPosition, getBlockState().setValue(InfoPanelBlock.FACING, facing), Block.UPDATE_ALL);
        rebuildScreen();
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
        return new InfoPanelMenu(id, playerInventory, this);
    }

    // --- persistence and sync ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Items"));
        powered = tag.getBoolean("powered");
        rotation = tag.getInt("rotation");
        showLabels = !tag.contains("showLabels") || tag.getBoolean("showLabels");
        colored = tag.getBoolean("colored");
        colorBackground = tag.contains("colorBackground") ? tag.getInt("colorBackground") : DEFAULT_BACKGROUND;
        colorText = tag.getInt("colorText");
        displaySettings.clear();
        CompoundTag settings = tag.getCompound("dSettings");
        for (String key : settings.getAllKeys()) {
            displaySettings.put(key, settings.getInt(key));
        }
        screenMin = tag.contains("screenMin") ? BlockPos.of(tag.getLong("screenMin")) : null;
        screenMax = tag.contains("screenMax") ? BlockPos.of(tag.getLong("screenMax")) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", inventory.serializeNBT(registries));
        tag.putBoolean("powered", powered);
        tag.putInt("rotation", rotation);
        tag.putBoolean("showLabels", showLabels);
        tag.putBoolean("colored", colored);
        tag.putInt("colorBackground", colorBackground);
        tag.putInt("colorText", colorText);
        CompoundTag settings = new CompoundTag();
        displaySettings.forEach(settings::putInt);
        tag.put("dSettings", settings);
        if (screenMin != null && screenMax != null) {
            tag.putLong("screenMin", screenMin.asLong());
            tag.putLong("screenMax", screenMax.asLong());
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
