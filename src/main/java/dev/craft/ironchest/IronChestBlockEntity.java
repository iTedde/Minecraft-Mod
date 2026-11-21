package dev.craft.ironchest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Iron Chests {@code TileEntityIronChest} (spec 22 §1–2): storage, lid with the chest sounds, user count and the
 * crystal chest's top stacks.
 */
public class IronChestBlockEntity extends BaseContainerBlockEntity {
    private static final int EVENT_USERS = 1;
    private static final String TAG_TOP_STACKS = "TopStacks";

    private final IronChestType type;
    private NonNullList<ItemStack> items;
    private final List<ItemStack> topStacks = new ArrayList<>();
    private int ticksSinceSync = -1;
    private int numUsingPlayers;
    /** Copper golems looking into the chest (spec 35 §2.3); server only, never recounted from players. */
    private int golemUsers;
    private float lidAngle;
    private float prevLidAngle;

    public IronChestBlockEntity(BlockPos pos, BlockState state) {
        super(IronChestBlockEntityTypes.IRON_CHEST.get(), pos, state);
        this.type = ((IronChestBlock) state.getBlock()).getType();
        this.items = NonNullList.withSize(type.getSize(), ItemStack.EMPTY);
    }

    public IronChestType getChestType() {
        return type;
    }

    @Override
    public int getContainerSize() {
        return type.getSize();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new IronChestMenu(id, inventory, this, type);
    }

    /** {@code isUseableByPlayer}: within 8 blocks and only while this is still the chest at its position. */
    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        sortTopStacks();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        sortTopStacks();
    }

    // --- saving and client sync ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Items", Tag.TAG_LIST)) {
            items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
            ContainerHelper.loadAllItems(tag, items, registries);
        }
        if (tag.contains(TAG_TOP_STACKS, Tag.TAG_LIST)) {
            topStacks.clear();
            for (Tag entry : tag.getList(TAG_TOP_STACKS, Tag.TAG_COMPOUND)) {
                CompoundTag stackTag = (CompoundTag) entry;
                ItemStack stack = ItemStack.parseOptional(registries, stackTag.getCompound("Item"));
                if (!stack.isEmpty()) {
                    // Summed counts exceed the item codec's limit, so the count travels on its own.
                    topStacks.add(stack.copyWithCount(stackTag.getInt("Count")));
                }
            }
        }
    }

    /** The crystal chest sends its top stacks; summed counts may exceed 99 and are stored apart. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        if (type.isTransparent()) {
            ListTag list = new ListTag();
            for (ItemStack stack : topStacks) {
                CompoundTag stackTag = new CompoundTag();
                stackTag.put("Item", stack.copyWithCount(1).save(registries));
                stackTag.putInt("Count", stack.getCount());
                list.add(stackTag);
            }
            tag.put(TAG_TOP_STACKS, list);
        }
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // --- crystal chest ---

    /**
     * {@code sortTopStacks}: equal items (item and damage, ignoring other data) are summed, sorted by count
     * (descending) and the first {@link IronChestType#TOP_STACKS} are sent to the clients.
     */
    void sortTopStacks() {
        if (!type.isTransparent() || level == null || level.isClientSide()) {
            return;
        }
        List<ItemStack> summed = new ArrayList<>();
        outer:
        for (ItemStack stack : items) {
            if (stack.isEmpty()) {
                continue;
            }
            for (ItemStack sum : summed) {
                if (ItemStack.isSameItem(sum, stack) && sum.getDamageValue() == stack.getDamageValue()) {
                    sum.grow(stack.getCount());
                    continue outer;
                }
            }
            summed.add(stack.copy());
        }
        summed.sort(Comparator.comparingInt(ItemStack::getCount).reversed());
        List<ItemStack> top = summed.subList(0, Math.min(summed.size(), IronChestType.TOP_STACKS));
        if (!sameStacks(top, topStacks)) {
            topStacks.clear();
            topStacks.addAll(top);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private static boolean sameStacks(List<ItemStack> a, List<ItemStack> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!ItemStack.matches(a.get(i), b.get(i))) {
                return false;
            }
        }
        return true;
    }

    /** The most common items (crystal chest only; on the client as last sent by the server). */
    public List<ItemStack> getTopStacks() {
        return topStacks;
    }

    // --- users and lid ---

    @Override
    public void startOpen(Player player) {
        if (!player.isSpectator()) {
            numUsingPlayers++;
            syncUsers();
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!player.isSpectator()) {
            numUsingPlayers--;
            syncUsers();
        }
    }

    public int getNumUsingPlayers() {
        return numUsingPlayers;
    }

    /** A copper golem opens ({@code true}) or closes the lid; it counts like a player for the lid only. */
    public void setGolemOpen(boolean open) {
        golemUsers = Math.max(0, golemUsers + (open ? 1 : -1));
        syncUsers();
    }

    private void syncUsers() {
        if (level != null) {
            level.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_USERS, numUsingPlayers + golemUsers);
        }
    }

    /** The server keeps its own player count; the client gets players and golems together for the lid. */
    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == EVENT_USERS) {
            if (level == null || level.isClientSide) {
                numUsingPlayers = param;
            }
            return true;
        }
        return super.triggerEvent(id, param);
    }

    /**
     * {@code updateEntity} on the server: the first tick sends the user count, then every 200 ticks (offset by the
     * position) the users are counted again from the players within 5 blocks that have this chest open.
     */
    void serverTick() {
        if (numUsingPlayers != 0
                && (ticksSinceSync + worldPosition.getX() + worldPosition.getY() + worldPosition.getZ()) % 200 == 0) {
            AABB area = new AABB(worldPosition).inflate(5.0);
            numUsingPlayers = 0;
            for (Player player : level.getEntitiesOfClass(Player.class, area)) {
                if (player.containerMenu instanceof IronChestMenu menu && menu.getContainer() == this) {
                    numUsingPlayers++;
                }
            }
        }
        if (ticksSinceSync < 0) {
            syncUsers();
        }
        ticksSinceSync++;
    }

    /** The lid swings by 0.1 per tick, with the chest sounds at volume 0.5. */
    void clientTick() {
        prevLidAngle = lidAngle;
        if (numUsingPlayers > 0 && lidAngle == 0.0F) {
            playSound(true);
        }
        if (numUsingPlayers == 0 && lidAngle > 0.0F || numUsingPlayers > 0 && lidAngle < 1.0F) {
            float previous = lidAngle;
            lidAngle += numUsingPlayers > 0 ? 0.1F : -0.1F;
            if (lidAngle > 1.0F) {
                lidAngle = 1.0F;
            }
            if (lidAngle < 0.5F && previous >= 0.5F) {
                playSound(false);
            }
            if (lidAngle < 0.0F) {
                lidAngle = 0.0F;
            }
        }
    }

    private void playSound(boolean open) {
        level.playLocalSound(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F,
                level.random.nextFloat() * 0.1F + 0.9F, false);
    }

    public float getLidAngle(float partialTick) {
        return prevLidAngle + (lidAngle - prevLidAngle) * partialTick;
    }
}
