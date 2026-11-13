package dev.craft.mffs.block;

import dev.craft.core.CraftDamageTypes;
import dev.craft.mffs.MffsBlockEntityTypes;
import dev.craft.mffs.MffsDevices;
import dev.craft.mffs.MffsInventories;
import dev.craft.mffs.MffsItems;
import dev.craft.mffs.energy.PowerLinkItem;
import dev.craft.mffs.item.SecurityLinkCardItem;
import dev.craft.mffs.menu.DefenseStationMenu;
import dev.craft.mffs.security.SecurityRight;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

/**
 * {@code TileEntityAreaDefenseStation} (spec 48 §10): 36 slots – 0 power link, 1 security link, 2/3 distance modulators
 * (warning / action perimeter), 5–14 item list, 15–34 confiscation storage. Active while switched on with a power
 * source, force energy and a linked station; scanning costs 10 · W FE every tick. Players and NPCs entering the
 * perimeters are warned and dealt with by the action mode.
 */
public class DefenseStationBlockEntity extends FePoweredBlockEntity {
    public static final int SLOT_POWER_LINK = 0;
    public static final int SLOT_SECURITY_LINK = 1;
    public static final int SLOT_WARN_DISTANCE = 2;
    public static final int SLOT_ACTION_DISTANCE = 3;
    public static final int FIRST_LIST = 5;
    public static final int LAST_LIST = 14;
    public static final int FIRST_STORAGE = 15;
    public static final int STORAGE_SLOTS = 20;
    public static final int EVENT_CONTRA_TYPE = 100;
    public static final int EVENT_ACTION_MODE = 101;
    public static final int EVENT_SCAN_MODE = 102;
    public static final int KILL_COST = 10000;
    public static final int SEARCH_COST = 1000;
    public static final int SCAN_COST = 10;
    private static final int ACTION_INTERVAL = 100;

    private int contraType = 1;
    private int actionMode;
    private int scanMode = 1;
    private final Set<Player> warnList = new HashSet<>();
    private final Set<Player> actionList = new HashSet<>();
    private final Set<Mob> npcList = new HashSet<>();

    public DefenseStationBlockEntity(BlockPos pos, BlockState state) {
        super(MffsBlockEntityTypes.DEFENSE_STATION.get(), pos, state, 36);
    }

    @Override
    public MffsDevices.Kind getDeviceKind() {
        return MffsDevices.Kind.DEFENSE_STATION;
    }

    @Override
    public int getPowerLinkSlot() {
        return SLOT_POWER_LINK;
    }

    @Override
    public int getMinSwitchMode() {
        return 1;
    }

    @Override
    public int getMaxSwitchMode() {
        return 3;
    }

    @Override
    public boolean canBeRemovedByMultitool() {
        return !isActive();
    }

    @Override
    public SecurityStationBlockEntity getLinkedSecurityStation() {
        return SecurityLinkCardItem.getLinkedStation(this, SLOT_SECURITY_LINK);
    }

    public boolean hasSecurityCard() {
        return getStack(SLOT_SECURITY_LINK).is(MffsItems.CARD_SECURITY_LINK.get());
    }

    public ItemStackHandler getRawInventory() {
        return inventory;
    }

    /** Automation reaches the confiscation storage (slots 15–34) from every side. */
    public IItemHandler getAutomationHandler() {
        return new RangedWrapper(inventory, FIRST_STORAGE, FIRST_STORAGE + STORAGE_SLOTS);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_POWER_LINK -> stack.getItem() instanceof PowerLinkItem;
            case SLOT_SECURITY_LINK -> stack.getItem() instanceof SecurityLinkCardItem;
            case SLOT_WARN_DISTANCE, SLOT_ACTION_DISTANCE -> stack.is(MffsItems.FIELD_MODULATOR_DISTANCE.get());
            default -> slot >= FIRST_LIST && slot <= LAST_LIST || slot >= FIRST_STORAGE;
        };
    }

    /** The GUI cannot put anything into the confiscation storage; automation can (ISidedInventory from 15). */
    @Override
    public boolean mayPlaceInGui(int slot, ItemStack stack) {
        return slot < FIRST_STORAGE && isItemValid(slot, stack);
    }

    @Override
    public int getSlotStackLimit(int slot) {
        if (slot == SLOT_POWER_LINK || slot == SLOT_SECURITY_LINK || slot >= FIRST_LIST && slot <= LAST_LIST) {
            return 1;
        }
        return 64;
    }

    /** Action perimeter A: the modulators in slot 3. */
    public int getActionDistance() {
        ItemStack stack = getStack(SLOT_ACTION_DISTANCE);
        return stack.is(MffsItems.FIELD_MODULATOR_DISTANCE.get()) ? stack.getCount() : 0;
    }

    /** Warning perimeter W = A + modulators in slot 2 + 3. */
    public int getInfoDistance() {
        ItemStack stack = getStack(SLOT_WARN_DISTANCE);
        return stack.is(MffsItems.FIELD_MODULATOR_DISTANCE.get()) ? getActionDistance() + stack.getCount() + 3
                : getActionDistance() + 3;
    }

    public int getContraType() {
        return contraType;
    }

    public int getActionMode() {
        return actionMode;
    }

    public int getScanMode() {
        return scanMode;
    }

    @Override
    protected void tickMachine() {
        followRedstone();
        int scanCost = SCAN_COST * getInfoDistance();
        SecurityStationBlockEntity station = getLinkedSecurityStation();
        if (getSwitchValue() && hasPowerSource() && getAvailablePower() > 0 && station != null && !isActive()) {
            setActive(true);
        }
        if ((!getSwitchValue() || !hasPowerSource() || getAvailablePower() < scanCost || station == null)
                && isActive()) {
            setActive(false);
        }
        if (isActive() && consumePower(scanCost, true)) {
            consumePower(scanCost, false);
            scan();
        }
        if (getTicker() == ACTION_INTERVAL) {
            if (isActive()) {
                for (Player player : new ArrayList<>(actionList)) {
                    defenceAction(player);
                }
            }
            setTicker(0);
        }
        setTicker(getTicker() + 1);
    }

    /** {@code PointXYZ.distance} from the machine to the entity's truncated coordinates. */
    private double distance(LivingEntity entity) {
        int dx = (int) entity.getX() - worldPosition.getX();
        int dy = (int) entity.getY() - worldPosition.getY();
        int dz = (int) entity.getZ() - worldPosition.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private AABB box(int radius) {
        return new AABB(worldPosition.getX() - radius, worldPosition.getY() - radius, worldPosition.getZ() - radius,
                worldPosition.getX() + radius + 1, worldPosition.getY() + radius + 1,
                worldPosition.getZ() + radius + 1);
    }

    /**
     * {@code scanner}: players newly in the warning perimeter (scan mode 1 sphere, 0 cube) without SR get a warning and
     * 1 damage; players and NPCs newly in the action perimeter get the action.
     */
    private void scan() {
        SecurityStationBlockEntity station = getLinkedSecurityStation();
        if (station == null) {
            return;
        }
        int info = getInfoDistance();
        int action = getActionDistance();
        List<LivingEntity> infoEntities = level.getEntitiesOfClass(LivingEntity.class, box(info));
        List<LivingEntity> actionEntities = level.getEntitiesOfClass(LivingEntity.class, box(action));
        for (LivingEntity entity : infoEntities) {
            if (entity instanceof Player player && ((int) distance(player) <= info || scanMode != 1)
                    && warnList.add(player) && !station.isAccessGranted(player, SecurityRight.SR)) {
                player.sendSystemMessage(Component.translatable("message.craft.mffs.defence.warning",
                        station.getDeviceName()));
                player.hurt(areaDefense(), 1.0F);
            }
        }
        for (LivingEntity entity : actionEntities) {
            boolean inside = (int) Math.round(distance(entity)) <= action || scanMode != 1;
            if (!inside) {
                continue;
            }
            if (entity instanceof Player player) {
                if (actionList.add(player)) {
                    defenceAction(player);
                }
            } else if (entity instanceof Mob mob && npcList.add(mob)) {
                defenceAction(mob);
            }
        }
        actionList.removeIf(player -> !actionEntities.contains(player));
        warnList.removeIf(player -> !infoEntities.contains(player));
        npcList.removeIf(Mob::isRemoved);
    }

    private DamageSource areaDefense() {
        return level.damageSources().source(CraftDamageTypes.AREA_DEFENSE);
    }

    /** NPC modes 3 (all), 4 (hostile: monsters, bats, slimes, ghasts), 5 (friendly: all but monsters/slimes/ghasts). */
    private void defenceAction(Mob mob) {
        if (!hasPowerSource() || getLinkedSecurityStation() == null) {
            return;
        }
        boolean hostile = mob instanceof Monster || mob instanceof Slime || mob instanceof Ghast;
        boolean kill = switch (actionMode) {
            case 3 -> true;
            case 4 -> hostile || mob instanceof AmbientCreature;
            case 5 -> !hostile;
            default -> false;
        };
        if (kill) {
            // Mode 3 pays without checking; 4 and 5 too (the original's check result is unused).
            consumePower(KILL_COST, false);
            mob.setHealth(0.0F);
            npcList.remove(mob);
        }
    }

    /** Player modes 0 (inform), 1 (kill and confiscate everything), 2 (search for listed items). */
    private void defenceAction(Player player) {
        SecurityStationBlockEntity station = getLinkedSecurityStation();
        if (!hasPowerSource() || station == null) {
            return;
        }
        switch (actionMode) {
            case 0 -> {
                if (!station.isAccessGranted(player, SecurityRight.SR)) {
                    player.sendSystemMessage(Component.translatable("message.craft.mffs.defence.get_out"));
                }
            }
            case 1 -> {
                if (consumePower(KILL_COST, true) && !station.isAccessGranted(player, SecurityRight.SR)) {
                    player.sendSystemMessage(Component.translatable("message.craft.mffs.defence.bye"));
                    Inventory inventory = player.getInventory();
                    for (int slot : playerSlots(inventory)) {
                        ItemStack stack = inventory.getItem(slot);
                        if (!stack.isEmpty()) {
                            stacksToInventory(stack.copy(), true);
                            inventory.setItem(slot, ItemStack.EMPTY);
                        }
                    }
                    actionList.remove(player);
                    DamageSource source = areaDefense();
                    player.setHealth(0.0F);
                    player.die(source);
                    consumePower(KILL_COST, false);
                }
            }
            case 2 -> {
                if (consumePower(SEARCH_COST, true) && !station.isAccessGranted(player, SecurityRight.AAI)) {
                    search(player);
                }
            }
            default -> {
            }
        }
    }

    /** Armor first (4), then the 36 main slots; the offhand (not in 1.4.7) last. */
    private static List<Integer> playerSlots(Inventory inventory) {
        List<Integer> slots = new ArrayList<>();
        int armor = inventory.items.size();
        for (int i = 0; i < inventory.armor.size(); i++) {
            slots.add(armor + i);
        }
        for (int i = 0; i < inventory.items.size(); i++) {
            slots.add(i);
        }
        for (int i = 0; i < inventory.offhand.size(); i++) {
            slots.add(armor + inventory.armor.size() + i);
        }
        return slots;
    }

    /** Contra type 1: listed items are banned; 0: only listed items are allowed. */
    private void search(Player player) {
        Set<Item> contra = new HashSet<>();
        for (int slot = FIRST_LIST; slot <= LAST_LIST; slot++) {
            if (!getStack(slot).isEmpty()) {
                contra.add(getStack(slot).getItem());
            }
        }
        Inventory inventory = player.getInventory();
        for (int slot : playerSlots(inventory)) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || contra.contains(stack.getItem()) != (contraType == 1)) {
                continue;
            }
            player.sendSystemMessage(Component.translatable("message.craft.mffs.defence.illegal",
                    stack.getHoverName()));
            stacksToInventory(stack.copy(), true);
            inventory.setItem(slot, ItemStack.EMPTY);
            consumePower(SEARCH_COST, false);
        }
    }

    /**
     * {@code StacksToInventory}: into the own storage from slot 15 (empty slot or same item with room), the rest into
     * the largest attached inventory; what does not fit is lost.
     */
    private void stacksToInventory(ItemStack stack, boolean loop) {
        if (fill(new RangedWrapper(inventory, FIRST_STORAGE, inventory.getSlots()), stack) || !loop) {
            return;
        }
        IItemHandler attached = MffsInventories.findAttachedInventory(level, worldPosition, FIRST_STORAGE);
        if (attached != null) {
            fill(attached, stack);
        }
    }

    private static boolean fill(IItemHandler handler, ItemStack stack) {
        for (int slot = 0; slot < handler.getSlots() && !stack.isEmpty(); slot++) {
            ItemStack present = handler.getStackInSlot(slot);
            if (present.isEmpty() || ItemStack.isSameItemSameComponents(present, stack)
                    && present.getCount() < present.getMaxStackSize()) {
                ItemStack rest = handler.insertItem(slot, stack, false);
                if (rest.isEmpty()) {
                    return true;
                }
                stack.setCount(rest.getCount());
            }
        }
        return false;
    }

    @Override
    public void handleEvent(int key, String value) {
        if (!isActive()) {
            switch (key) {
                case EVENT_CONTRA_TYPE -> contraType = contraType == 0 ? 1 : 0;
                case EVENT_ACTION_MODE -> actionMode = actionMode == 5 ? 0 : actionMode + 1;
                case EVENT_SCAN_MODE -> scanMode = scanMode == 0 ? 1 : 0;
                default -> {
                }
            }
            markForUpdate();
        }
        super.handleEvent(key, value);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new DefenseStationMenu(id, inventory, this);
    }

    @Override
    protected void saveState(CompoundTag tag) {
        super.saveState(tag);
        tag.putInt("contratyp", contraType);
        tag.putInt("actionmode", actionMode);
        tag.putInt("scanmode", scanMode);
    }

    @Override
    protected void loadState(CompoundTag tag) {
        super.loadState(tag);
        contraType = tag.contains("contratyp") ? tag.getInt("contratyp") : 1;
        actionMode = tag.getInt("actionmode");
        scanMode = tag.contains("scanmode") ? tag.getInt("scanmode") : 1;
    }
}
