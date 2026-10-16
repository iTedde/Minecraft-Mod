package dev.craft.buildcraft.transport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import dev.craft.buildcraft.BcBlockEntityTypes;
import dev.craft.buildcraft.factory.MiningWellBlockEntity;
import dev.craft.buildcraft.factory.QuarryBlockEntity;
import dev.craft.buildcraft.util.ItemOutput;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * BuildCraft {@code PipeTransportItems} with the hooks of the item pipes ({@code PipeItemsWood}, {@code
 * PipeItemsGold}, {@code PipeItemsVoid}, {@code PipeItemsIron} …). Server and client both move the items; the server
 * decides the destinations and sends every entering or bouncing item to the client.
 */
public class ItemPipeBlockEntity extends PipeBlockEntity {
    /** {@code pipes.groupItemsTrigger} of the pack. */
    public static final int GROUP_ITEMS_TRIGGER = 32;
    /** {@code pipes.maxItems} of the pack. */
    public static final int MAX_ITEMS = 100;
    /** {@code Utils.getPipeFloorOf}. */
    public static final double PIPE_FLOOR = 0.25;
    /** Items from the saved data enter the pipe two ticks after loading. */
    private static final int LOAD_DELAY = 2;

    private final Map<Integer, TravelingItem> items = new LinkedHashMap<>();
    private final Set<Integer> toRemove = new IntOpenHashSet();
    private final List<TravelingItem> toLoad = new ArrayList<>();
    private int loadDelay;

    public ItemPipeBlockEntity(BlockPos pos, BlockState state) {
        super(BcBlockEntityTypes.ITEM_PIPE.get(), pos, state);
    }

    private PipeMaterial material() {
        return getPipeType().getMaterial();
    }

    /** Items currently in the pipe, for the renderer and tests. */
    public Collection<TravelingItem> getItems() {
        return Collections.unmodifiableCollection(items.values());
    }

    @Override
    protected boolean acceptsNonPipe(BlockEntity other, Direction side) {
        if (other instanceof QuarryBlockEntity || other instanceof MiningWellBlockEntity) {
            return true;
        }
        IItemHandler handler = PipeConnections.itemHandler(level, worldPosition, side);
        return handler != null && handler.getSlots() > 0;
    }

    // --- entering ---

    /** {@code entityEntering}: an item enters this pipe moving in {@code orientation}. */
    public void entityEntering(TravelingItem item, Direction orientation) {
        item.speed = material().readjustSpeed(item.speed);
        if (!items.containsKey(item.id)) {
            items.put(item.id, item);
            if (item.container != null && item.container != this) {
                item.container.scheduleRemoval(item);
            }
            item.container = this;
            item.enter(orientation);
            setChanged();
        }
        if (orientation.getAxis() != Direction.Axis.Y) {
            item.y = worldPosition.getY() + PIPE_FLOOR;
        }
        if (!level.isClientSide()) {
            item.output = resolveDestination(item);
        }
        entityEntered(item);
        if (!level.isClientSide()) {
            sendItemPacket(item);
            if (items.size() > GROUP_ITEMS_TRIGGER) {
                groupEntities();
                if (items.size() > MAX_ITEMS) {
                    level.explode(null, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 1.0F,
                            Level.ExplosionInteraction.NONE);
                }
            }
        }
    }

    /** {@code IPipeTransportItemsHook.entityEntered}: the gold pipe speeds the item up a second time. */
    private void entityEntered(TravelingItem item) {
        if (material() == PipeMaterial.GOLD) {
            item.speed = PipeMaterial.goldSpeed(item.speed);
        }
    }

    /** {@code reverseItem}: the target inventory did not take everything, the rest comes back. */
    private void reverseItem(TravelingItem item) {
        toRemove.remove(item.id);
        item.toCenter = true;
        item.input = item.output.getOpposite();
        item.speed = material().readjustSpeed(item.speed);
        if (item.input.getAxis() != Direction.Axis.Y) {
            item.y = worldPosition.getY() + PIPE_FLOOR;
        }
        item.output = resolveDestination(item);
        entityEntered(item);
        sendItemPacket(item);
    }

    // --- destination ---

    /** {@code resolveDestination}: random pick of the possible movements, {@code null} if there are none. */
    @Nullable
    private Direction resolveDestination(TravelingItem item) {
        List<Direction> possible = getPossibleMovements(item);
        return possible.isEmpty() ? null : possible.get(level.random.nextInt(possible.size()));
    }

    private List<Direction> getPossibleMovements(TravelingItem item) {
        List<Direction> result = new ArrayList<>();
        item.blacklist.add(item.input.getOpposite());
        for (Direction side : Direction.values()) {
            if (!item.blacklist.contains(side) && outputOpen(side) && canReceivePipeObjects(side, item.stack)) {
                result.add(side);
            }
        }
        if (result.isEmpty() && allowBouncing() && canReceivePipeObjects(item.input.getOpposite(), item.stack)) {
            result.add(item.input.getOpposite());
        }
        return result;
    }

    /** {@code PipeLogic.outputOpen}: iron only to its output side. */
    private boolean outputOpen(Direction side) {
        BlockState state = getBlockState();
        if (state.hasProperty(GenericPipeBlock.OUTPUT)) {
            return state.getValue(GenericPipeBlock.OUTPUT) == side.get3DDataValue();
        }
        return true;
    }

    /** Only the iron item pipe lets items bounce back ({@code allowBouncing}). */
    private boolean allowBouncing() {
        return material() == PipeMaterial.IRON;
    }

    /** {@code canReceivePipeObjects}: connected and an item pipe or an inventory that takes some of the stack. */
    private boolean canReceivePipeObjects(Direction side, ItemStack stack) {
        if (!PipeConnections.canConnect(level, worldPosition, side)) {
            return false;
        }
        if (level.getBlockEntity(worldPosition.relative(side)) instanceof PipeBlockEntity pipe) {
            return pipe instanceof ItemPipeBlockEntity;
        }
        IItemHandler handler = PipeConnections.itemHandler(level, worldPosition, side);
        return handler != null
                && ItemHandlerHelper.insertItemStacked(handler, stack.copy(), true).getCount() < stack.getCount();
    }

    // --- movement ---

    @Override
    protected void tickTransport() {
        if (!toLoad.isEmpty() && --loadDelay <= 0) {
            for (TravelingItem item : toLoad) {
                item.container = this;
                items.put(item.id, item);
            }
            toLoad.clear();
        }
        performRemoval();
        for (TravelingItem item : new ArrayList<>(items.values())) {
            if (toRemove.contains(item.id)) {
                continue;
            }
            item.prevX = item.x;
            item.prevY = item.y;
            item.prevZ = item.z;
            Direction motion = item.toCenter ? item.input : item.output;
            if (motion != null) {
                item.setPosition(item.x + motion.getStepX() * item.speed, item.y + motion.getStepY() * item.speed,
                        item.z + motion.getStepZ() * item.speed);
            }
            if (item.toCenter && middleReached(item) || outOfBounds(item)) {
                item.toCenter = false;
                item.setPosition(worldPosition.getX() + 0.5, worldPosition.getY() + PIPE_FLOOR,
                        worldPosition.getZ() + 0.5);
                if (item.output == null) {
                    drop(item);
                    if (!toRemove.contains(item.id)) {
                        toEntityItem(item, item.input);
                    }
                    scheduleRemoval(item);
                } else {
                    centerReached(item);
                }
            } else if (!item.toCenter && endReached(item)) {
                BlockEntity tile = level.getBlockEntity(worldPosition.relative(item.output));
                if (!toRemove.contains(item.id)) {
                    scheduleRemoval(item);
                    handleTileReached(item, tile);
                }
            }
        }
        performRemoval();
    }

    private boolean middleReached(TravelingItem item) {
        float limit = item.speed * 1.01F;
        return Math.abs(worldPosition.getX() + 0.5 - item.x) < limit
                && Math.abs(worldPosition.getY() + PIPE_FLOOR - item.y) < limit
                && Math.abs(worldPosition.getZ() + 0.5 - item.z) < limit;
    }

    private boolean endReached(TravelingItem item) {
        return item.x > worldPosition.getX() + 1 || item.x < worldPosition.getX()
                || item.y > worldPosition.getY() + 1 || item.y < worldPosition.getY()
                || item.z > worldPosition.getZ() + 1 || item.z < worldPosition.getZ();
    }

    private boolean outOfBounds(TravelingItem item) {
        return item.x > worldPosition.getX() + 2 || item.x < worldPosition.getX() - 1
                || item.y > worldPosition.getY() + 2 || item.y < worldPosition.getY() - 1
                || item.z > worldPosition.getZ() + 2 || item.z < worldPosition.getZ() - 1;
    }

    /** {@code IPipeTransportItemsHook}/travel hook {@code drop}: the void pipe drops nothing. */
    private void drop(TravelingItem item) {
        if (material() == PipeMaterial.VOID) {
            item.stack.setCount(0);
        }
    }

    /** Travel hook {@code centerReached}: items vanish in the void pipe. */
    private void centerReached(TravelingItem item) {
        if (material() == PipeMaterial.VOID) {
            scheduleRemoval(item);
        }
    }

    private void handleTileReached(TravelingItem item, @Nullable BlockEntity tile) {
        if (tile instanceof ItemPipeBlockEntity pipe) {
            pipe.entityEntering(item, item.output);
            return;
        }
        if (level.isClientSide()) {
            return;
        }
        IItemHandler handler = tile == null ? null : PipeConnections.itemHandler(level, worldPosition, item.output);
        if (handler != null) {
            item.stack = ItemHandlerHelper.insertItemStacked(handler, item.stack, false);
            if (!item.stack.isEmpty()) {
                reverseItem(item);
            }
        } else {
            drop(item);
            toEntityItem(item, item.output);
        }
    }

    /** {@code EntityPassiveItem.toEntityItem}: thrown out in {@code dir}, lifespan 1200, pickup after 10 ticks. */
    private void toEntityItem(TravelingItem item, Direction dir) {
        if (level.isClientSide() || item.stack.isEmpty()) {
            return;
        }
        double motion = 0.1 + item.speed * 2.0F;
        ItemEntity entity = new ItemEntity(level, item.x, item.y, item.z, item.stack);
        entity.lifespan = ItemOutput.ITEM_LIFESPAN;
        entity.setPickUpDelay(10);
        float noise = level.random.nextFloat() * 0.04F - 0.02F;
        entity.setDeltaMovement(level.random.nextGaussian() * noise + dir.getStepX() * motion,
                level.random.nextGaussian() * noise + dir.getStepY() * motion,
                level.random.nextGaussian() * noise + dir.getStepZ() * motion);
        level.addFreshEntity(entity);
    }

    private void scheduleRemoval(TravelingItem item) {
        toRemove.add(item.id);
    }

    private void performRemoval() {
        for (int id : toRemove) {
            TravelingItem item = items.remove(id);
            if (item != null && item.container == this) {
                item.container = null;
            }
        }
        if (!toRemove.isEmpty()) {
            toRemove.clear();
            setChanged();
        }
    }

    /** {@code groupEntities}: equal items (same item and components) are merged while below the stack size. */
    private void groupEntities() {
        List<TravelingItem> list = new ArrayList<>(items.values());
        Set<Integer> remove = new IntOpenHashSet();
        for (int i = 0; i < list.size(); i++) {
            TravelingItem first = list.get(i);
            for (int j = i + 1; j < list.size(); j++) {
                TravelingItem second = list.get(j);
                if (!remove.contains(first.id) && !remove.contains(second.id)
                        && ItemStack.isSameItemSameComponents(first.stack, second.stack)
                        && first.stack.getCount() + second.stack.getCount() < first.stack.getMaxStackSize()) {
                    first.stack.grow(second.stack.getCount());
                    remove.add(second.id);
                }
            }
        }
        for (int id : remove) {
            items.remove(id);
        }
    }

    @Override
    public void dropContents() {
        if (level.isClientSide()) {
            return;
        }
        items.putAll(toLoadMap());
        toLoad.clear();
        groupEntities();
        for (TravelingItem item : items.values()) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    item.stack);
        }
        items.clear();
    }

    private Map<Integer, TravelingItem> toLoadMap() {
        Map<Integer, TravelingItem> map = new LinkedHashMap<>();
        for (TravelingItem item : toLoad) {
            map.put(item.id, item);
        }
        return map;
    }

    // --- wooden pipe ---

    /**
     * {@code PipeItemsWood.doWork}: from the first non-empty slot of the source side, as many items as there is MJ
     * (at most the slot). The item starts 0.6 in front of the inventory's center, moving away from it.
     */
    @Override
    public void doWork() {
        Direction source = getSource();
        if (source == null || material() != PipeMaterial.WOOD) {
            return;
        }
        BlockPos sourcePos = worldPosition.relative(source);
        IItemHandler handler = PipeConnections.itemHandler(level, worldPosition, source);
        if (handler == null) {
            return;
        }
        ItemStack extracted = ItemStack.EMPTY;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty() && !handler.extractItem(slot, 1, true).isEmpty()) {
                int amount = (int) provider.useEnergy(1.0F, stack.getCount(), true);
                extracted = handler.extractItem(slot, amount, false);
                break;
            }
        }
        if (extracted.isEmpty()) {
            provider.useEnergy(1.0F, 1.0F, true);
            return;
        }
        Direction away = source.getOpposite();
        TravelingItem item = new TravelingItem(extracted, sourcePos.getX() + 0.5 + away.getStepX() * 0.6,
                sourcePos.getY() + 0.5 + away.getStepY() * 0.6, sourcePos.getZ() + 0.5 + away.getStepZ() * 0.6);
        entityEntering(item, away);
    }

    // --- sync ---

    private void sendItemPacket(TravelingItem item) {
        if (level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(worldPosition), PipeItemPayload.of(worldPosition, item));
        }
    }

    /** Client: an item entered this pipe or bounced back on the server. */
    void handleItemPacket(PipeItemPayload payload) {
        TravelingItem item = items.get(payload.id());
        if (item == null) {
            item = new TravelingItem(payload.id(), payload.stack());
            item.setPosition(payload.x(), payload.y(), payload.z());
            item.prevX = payload.x();
            item.prevY = payload.y();
            item.prevZ = payload.z();
            items.put(item.id, item);
            item.container = this;
        } else {
            item.stack = payload.stack();
            item.setPosition(payload.x(), payload.y(), payload.z());
        }
        toRemove.remove(item.id);
        item.speed = payload.speed();
        item.input = Direction.from3DDataValue(payload.input());
        item.output = payload.output() < 6 ? Direction.from3DDataValue(payload.output()) : null;
        item.toCenter = payload.toCenter();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        for (TravelingItem item : items.values()) {
            list.add(item.save(registries));
        }
        for (TravelingItem item : toLoad) {
            list.add(item.save(registries));
        }
        tag.put("items", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.clear();
        toLoad.clear();
        ListTag list = tag.getList("items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            TravelingItem item = TravelingItem.load(list.getCompound(i), registries);
            if (item != null) {
                toLoad.add(item);
            }
        }
        loadDelay = LOAD_DELAY;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (TravelingItem item : items.values()) {
            CompoundTag itemTag = item.save(registries);
            itemTag.putInt("id", item.id);
            list.add(itemTag);
        }
        tag.put("items", list);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        items.clear();
        ListTag list = tag.getList("items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            TravelingItem item = TravelingItem.load(itemTag, registries, itemTag.getInt("id"));
            if (item != null) {
                item.container = this;
                items.put(item.id, item);
            }
        }
    }
}
