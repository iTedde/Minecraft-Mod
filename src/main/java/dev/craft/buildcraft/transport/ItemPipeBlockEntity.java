package dev.craft.buildcraft.transport;

import java.util.ArrayList;
import java.util.Arrays;
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
import dev.craft.buildcraft.factory.SpecialInventory;
import dev.craft.buildcraft.menu.PipeFilterMenu;
import dev.craft.buildcraft.util.ItemOutput;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * BuildCraft {@code PipeTransportItems} with the hooks of the item pipes ({@code PipeItemsWood}, {@code
 * PipeItemsGold}, {@code PipeItemsVoid}, {@code PipeItemsIron}, {@code PipeItemsDiamond}, {@code PipeItemsEmerald},
 * {@code PipeItemsObsidian} …). Server and client both move the items; the server decides the destinations and
 * sends every entering or bouncing item to the client.
 */
public class ItemPipeBlockEntity extends PipeBlockEntity implements MenuProvider {
    /** {@code pipes.groupItemsTrigger} of the pack. */
    public static final int GROUP_ITEMS_TRIGGER = 32;
    /** {@code pipes.maxItems} of the pack. */
    public static final int MAX_ITEMS = 100;
    /** {@code Utils.getPipeFloorOf}. */
    public static final double PIPE_FLOOR = 0.25;
    /** Items from the saved data enter the pipe two ticks after loading. */
    private static final int LOAD_DELAY = 2;
    public static final int DIAMOND_FILTERS = 54;
    public static final int EMERALD_FILTERS = 9;
    private static final int OBSIDIAN_DROPPED_MEMORY = 32;
    /** {@code PipeItemsObsidian.doWork}: sucking boxes at distance 1 to 4. */
    private static final int OBSIDIAN_MAX_DISTANCE = 4;

    private final Map<Integer, TravelingItem> items = new LinkedHashMap<>();
    private final Set<Integer> toRemove = new IntOpenHashSet();
    private final List<TravelingItem> toLoad = new ArrayList<>();
    private int loadDelay;
    /** Phantom filters: diamond 6 rows of 9 (row = side), emerald one row of 9, none for the other pipes. */
    private final ItemStackHandler filters;
    /** {@code PipeItemsEmerald.currentFilter}. */
    private int currentFilter;
    /** {@code PipeItemsObsidian.entitiesDropped}: ids of the last 32 item entities this pipe threw out. */
    private final int[] entitiesDropped = new int[OBSIDIAN_DROPPED_MEMORY];
    private int entitiesDroppedIndex;

    public ItemPipeBlockEntity(BlockPos pos, BlockState state) {
        super(BcBlockEntityTypes.ITEM_PIPE.get(), pos, state);
        int filterCount = switch (getPipeType().getMaterial()) {
            case DIAMOND -> DIAMOND_FILTERS;
            case EMERALD -> EMERALD_FILTERS;
            default -> 0;
        };
        filters = new ItemStackHandler(filterCount) {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
        Arrays.fill(entitiesDropped, -1);
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
        // IInventory.getSizeInventory() > 0: any slot counts, also when the facing side exposes none (sided TE machines)
        IItemHandler handler = PipeConnections.itemHandler(level, worldPosition, side);
        if (handler != null && handler.getSlots() > 0) {
            return true;
        }
        IItemHandler whole = level.getCapability(Capabilities.ItemHandler.BLOCK, other.getBlockPos(), null);
        return whole != null && whole.getSlots() > 0;
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
        return material() == PipeMaterial.DIAMOND ? filterPossibleMovements(result, item.stack) : result;
    }

    /**
     * {@code PipeItemsDiamond.filterPossibleMovements}: each side is added once per matching filter of its row (damage
     * is ignored for damageable items, and 1.21 items have no other meta). Sides with an empty row are the fallback;
     * sides with filters but no match are closed.
     */
    private List<Direction> filterPossibleMovements(List<Direction> possible, ItemStack stack) {
        List<Direction> filtered = new ArrayList<>();
        List<Direction> fallback = new ArrayList<>();
        for (Direction side : possible) {
            boolean foundFilter = false;
            for (int slot = 0; slot < 9; slot++) {
                ItemStack filter = filters.getStackInSlot(side.get3DDataValue() * 9 + slot);
                if (!filter.isEmpty()) {
                    foundFilter = true;
                    if (ItemStack.isSameItem(filter, stack)) {
                        filtered.add(side);
                    }
                }
            }
            if (!foundFilter) {
                fallback.add(side);
            }
        }
        return filtered.isEmpty() ? fallback : filtered;
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
        if (material() == PipeMaterial.OBSIDIAN) {
            entitiesDroppedIndex = entitiesDroppedIndex + 1 >= entitiesDropped.length ? 0 : entitiesDroppedIndex + 1;
            entitiesDropped[entitiesDroppedIndex] = entity.getId();
        }
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

    // --- wooden and emerald pipe ---

    /**
     * {@code PipeItemsWood.doWork}: from the first non-empty slot of the source side, as many items as there is MJ
     * (at most the slot). The item starts 0.6 in front of the inventory's center, moving away from it. The emerald
     * pipe only takes the first slot matching its current filter ({@code PipeItemsEmerald.checkExtractGeneric}).
     * The obsidian pipe sucks up items instead.
     */
    @Override
    public void doWork() {
        if (material() == PipeMaterial.OBSIDIAN) {
            obsidianWork();
            return;
        }
        Direction source = getSource();
        if (source == null || !material().isWood()) {
            return;
        }
        BlockPos sourcePos = worldPosition.relative(source);
        IItemHandler handler = PipeConnections.itemHandler(level, worldPosition, source);
        if (handler == null) {
            return;
        }
        ItemStack extracted;
        if (level.getBlockEntity(sourcePos) instanceof SpecialInventory special) {
            extracted = material() == PipeMaterial.EMERALD ? extractSpecialFiltered(special) : special.extractItem(true);
            if (extracted.isEmpty()) {
                // a special inventory with nothing to give costs no MJ (useEnergy(1, 1, false))
                return;
            }
            provider.useEnergy(extracted.getCount(), extracted.getCount(), true);
        } else {
            extracted = material() == PipeMaterial.EMERALD ? extractFiltered(handler) : extractAny(handler);
            if (extracted.isEmpty()) {
                provider.useEnergy(1.0F, 1.0F, true);
                return;
            }
        }
        Direction away = source.getOpposite();
        TravelingItem item = new TravelingItem(extracted, sourcePos.getX() + 0.5 + away.getStepX() * 0.6,
                sourcePos.getY() + 0.5 + away.getStepY() * 0.6, sourcePos.getZ() + 0.5 + away.getStepZ() * 0.6);
        entityEntering(item, away);
    }

    private ItemStack extractAny(IItemHandler handler) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty() && !handler.extractItem(slot, 1, true).isEmpty()) {
                return extract(handler, slot, stack);
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * {@code PipeItemsEmerald.checkExtractGeneric}: the first slot matching the current filter; the filter only moves
     * on when something was taken. Without any filter nothing is taken.
     */
    private ItemStack extractFiltered(IItemHandler handler) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty() || handler.extractItem(slot, 1, true).isEmpty()) {
                continue;
            }
            ItemStack filter = getCurrentFilter();
            if (filter.isEmpty()) {
                return ItemStack.EMPTY;
            }
            if (isItemEqual(filter, stack)) {
                incrementFilter();
                return extract(handler, slot, stack);
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * {@code PipeItemsEmerald.checkExtract} for special inventories: takes the output only if it matches any of the
     * filters; the current filter does not move on.
     */
    private ItemStack extractSpecialFiltered(SpecialInventory special) {
        ItemStack preview = special.extractItem(false);
        if (preview.isEmpty()) {
            return ItemStack.EMPTY;
        }
        IItemHandlerModifiable filters = getFilters();
        for (int i = 0; i < filters.getSlots(); i++) {
            ItemStack filter = filters.getStackInSlot(i);
            if (!filter.isEmpty() && isItemEqual(filter, preview)) {
                return special.extractItem(true);
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack extract(IItemHandler handler, int slot, ItemStack stack) {
        int amount = (int) provider.useEnergy(1.0F, stack.getCount(), true);
        return handler.extractItem(slot, amount, false);
    }

    /** 1.4.7 {@code ItemStack.isItemEqual}: same item and damage, components ignored. */
    private static boolean isItemEqual(ItemStack filter, ItemStack stack) {
        return ItemStack.isSameItem(filter, stack) && filter.getDamageValue() == stack.getDamageValue();
    }

    private ItemStack getCurrentFilter() {
        if (filters.getStackInSlot(currentFilter % filters.getSlots()).isEmpty()) {
            incrementFilter();
        }
        return filters.getStackInSlot(currentFilter % filters.getSlots());
    }

    private void incrementFilter() {
        currentFilter++;
        for (int count = 0; filters.getStackInSlot(currentFilter % filters.getSlots()).isEmpty()
                && count < filters.getSlots(); count++) {
            currentFilter++;
        }
    }

    // --- obsidian pipe ---

    /** {@code getOpenOrientation}: the side opposite the only connection, {@code null} unless exactly one. */
    @Nullable
    public Direction getOpenOrientation() {
        Direction connected = null;
        for (Direction side : Direction.values()) {
            if (isConnected(side)) {
                if (connected != null) {
                    return null;
                }
                connected = side;
            }
        }
        return connected == null ? null : connected.getOpposite();
    }

    /** {@code PipeItemsObsidian.doWork}: the boxes at distance 1 to 4, the first success ends the tick. */
    private void obsidianWork() {
        for (int distance = 1; distance <= OBSIDIAN_MAX_DISTANCE; distance++) {
            if (trySuck(distance)) {
                return;
            }
        }
        provider.useEnergy(1.0F, 1.0F, true);
    }

    /**
     * {@code getSuckingBox}: a slice of thickness 1 at {@code distance} towards the open side, (2d+1)² around the
     * pipe's axis.
     */
    @Nullable
    private AABB getSuckingBox(int distance) {
        Direction open = getOpenOrientation();
        if (open == null) {
            return null;
        }
        Direction.Axis axis = open.getAxis();
        return new AABB(worldPosition.relative(open, distance)).inflate(axis == Direction.Axis.X ? 0 : distance,
                axis == Direction.Axis.Y ? 0 : distance, axis == Direction.Axis.Z ? 0 : distance);
    }

    private boolean trySuck(int distance) {
        AABB box = getSuckingBox(distance);
        if (box == null) {
            return false;
        }
        for (Entity entity : level.getEntities((Entity) null, box)) {
            if (canSuck(entity, distance)) {
                pullItemIntoPipe(entity, distance);
                return true;
            }
            if (distance == 1 && entity instanceof MinecartChest cart && cart.isAlive()) {
                ItemStack stack = extractFirst(cart);
                if (!stack.isEmpty() && provider.useEnergy(1.0F, 1.0F, true) == 1.0F) {
                    ItemEntity item = new ItemEntity(level, cart.getX(), cart.getY() + 0.3, cart.getZ(), stack);
                    item.setPickUpDelay(10);
                    level.addFreshEntity(item);
                    pullItemIntoPipe(item, 1);
                    return true;
                }
            }
        }
        return false;
    }

    /** {@code checkExtractGeneric} of the obsidian pipe: one item of the first non-empty slot. */
    private static ItemStack extractFirst(Container container) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (!container.getItem(slot).isEmpty()) {
                return container.removeItem(slot, 1);
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * {@code canSuck}: living item entities not recently thrown out by this pipe, and arrows, if the MJ for the
     * distance are there. Distance 0 (touching the pipe) is always free.
     */
    public boolean canSuck(Entity entity, int distance) {
        if (!entity.isAlive()) {
            return false;
        }
        if (entity instanceof ItemEntity item) {
            if (item.getItem().isEmpty()) {
                return false;
            }
            for (int id : entitiesDropped) {
                if (id == item.getId()) {
                    return false;
                }
            }
            return provider.useEnergy(1.0F, distance, false) >= distance;
        }
        return entity instanceof Arrow && provider.useEnergy(1.0F, distance, false) >= distance;
    }

    /**
     * {@code pullItemIntoPipe}: costs {@code distance} MJ per item (an arrow costs {@code distance}); without enough MJ
     * only part of the stack is split off. The item starts in the middle moving towards the connection.
     */
    public void pullItemIntoPipe(Entity entity, int distance) {
        Direction open = getOpenOrientation();
        if (level.isClientSide() || open == null) {
            return;
        }
        Direction orientation = open.getOpposite();
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_PICKUP,
                SoundSource.BLOCKS, 0.2F, ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        ItemStack stack;
        double speed = TravelingItem.NORMAL_SPEED;
        if (entity instanceof ItemEntity item) {
            ItemStack contained = item.getItem().copy();
            float energyUsed = provider.useEnergy(distance, contained.getCount() * distance, true);
            if (distance != 0 && energyUsed / distance != contained.getCount()) {
                stack = contained.split((int) (energyUsed / distance));
                item.setItem(contained);
            } else {
                stack = contained;
                item.discard();
            }
            speed = Math.max(item.getDeltaMovement().length() / 2.0 - 0.05, TravelingItem.NORMAL_SPEED);
        } else if (entity instanceof Arrow) {
            provider.useEnergy(distance, distance, true);
            stack = new ItemStack(Items.ARROW);
            entity.discard();
        } else {
            return;
        }
        if (stack.isEmpty()) {
            return;
        }
        TravelingItem passive = new TravelingItem(stack, worldPosition.getX() + 0.5,
                worldPosition.getY() + PIPE_FLOOR, worldPosition.getZ() + 0.5);
        passive.speed = (float) speed;
        entityEntering(passive, orientation);
    }

    // --- filters (diamond, emerald) ---

    public IItemHandlerModifiable getFilters() {
        return filters;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.craft.pipe_filters");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return filters.getSlots() == 0 ? null : new PipeFilterMenu(id, inventory, this);
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
        if (filters.getSlots() > 0) {
            tag.put("filters", filters.serializeNBT(registries));
            tag.putInt("currentFilter", currentFilter);
        }
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
        if (filters.getSlots() > 0 && tag.contains("filters")) {
            CompoundTag filterTag = tag.getCompound("filters");
            filterTag.putInt("Size", filters.getSlots());
            filters.deserializeNBT(registries, filterTag);
            currentFilter = tag.getInt("currentFilter");
        }
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
