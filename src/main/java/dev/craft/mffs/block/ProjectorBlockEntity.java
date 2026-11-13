package dev.craft.mffs.block;

import dev.craft.mffs.MffsBlockEntityTypes;
import dev.craft.mffs.MffsBlocks;
import dev.craft.mffs.MffsConfig;
import dev.craft.mffs.MffsDevices;
import dev.craft.mffs.MffsItems;
import dev.craft.mffs.energy.PowerLinkItem;
import dev.craft.mffs.field.FieldParams;
import dev.craft.mffs.field.ForceFieldMap;
import dev.craft.mffs.field.ForceFieldType;
import dev.craft.mffs.field.ProjectorModule;
import dev.craft.mffs.field.ProjectorUpgrade;
import dev.craft.mffs.field.ProjectorUpgradeItem;
import dev.craft.mffs.item.ProjectorModuleItem;
import dev.craft.mffs.menu.ProjectorMenu;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * {@code TileEntityProjector} (spec 48 §9.1): projects the field of its module. Switches on after at least 40 ticks
 * when the link has more force energy than the estimate {@link #getPowerNeed(int) need(5)}; computes the field points
 * into the {@link ForceFieldMap} and places the blocks (creation cost ×10); every 20 ticks upkeep and placing missing
 * blocks (at most 5 000 per pass). Switches off below {@code need(1)}, without module or source, when burnt out or
 * switched off – the field is removed. Upgrades (slots 2–4), camouflage (11), security link (12) and burn-out follow in
 * phases 48.3/48.4.
 */
public class ProjectorBlockEntity extends FePoweredBlockEntity {
    public static final int SLOT_POWER_LINK = 0;
    public static final int SLOT_MODULE = 1;
    public static final int SLOT_UPGRADE_FIRST = 2;
    public static final int SLOT_UPGRADE_LAST = 4;
    public static final int SLOT_DISTANCE = 5;
    public static final int SLOT_STRENGTH = 6;
    public static final int SLOT_FOCUS_UP = 7;
    public static final int SLOT_FOCUS_DOWN = 8;
    public static final int SLOT_FOCUS_RIGHT = 9;
    public static final int SLOT_FOCUS_LEFT = 10;
    public static final int SLOT_CAMOUFLAGE = 11;
    public static final int SLOT_SECURITY_LINK = 12;
    public static final int EVENT_ACCESS_TYPE = 1;

    private static final int SWITCH_DELAY = 40;
    private static final int FIELD_INTERVAL = 20;

    /** Field points of the last calculation ({@code field_def}); kept after switching off (power estimate). */
    private final Set<BlockPos> fieldPoints = new LinkedHashSet<>();
    private final Set<BlockPos> interiorPoints = new LinkedHashSet<>();
    /** Every position this projector claimed in the field map ({@code field_queue}). */
    private final Set<BlockPos> claimed = new LinkedHashSet<>();
    private ForceFieldType fieldType = ForceFieldType.DEFAULT;
    private int accessType;
    private int linkPower;
    private boolean burnedOut;
    private int switchDelay;
    private int blockCounter;
    private boolean fieldInitialized;
    private boolean checkingSlots;
    private boolean unloading;

    public ProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(MffsBlockEntityTypes.PROJECTOR.get(), pos, state, 13);
    }

    @Override
    public MffsDevices.Kind getDeviceKind() {
        return MffsDevices.Kind.PROJECTOR;
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

    public ProjectorModule getModule() {
        return getStack(SLOT_MODULE).getItem() instanceof ProjectorModuleItem item ? item.getModule() : null;
    }

    public boolean hasModule() {
        return getModule() != null;
    }

    /** {@code hasOption}: an upgrade of this kind in slots 2–4. */
    public boolean hasUpgrade(ProjectorUpgrade upgrade) {
        for (int slot = SLOT_UPGRADE_FIRST; slot <= SLOT_UPGRADE_LAST; slot++) {
            if (getStack(slot).getItem() instanceof ProjectorUpgradeItem item && item.getUpgrade() == upgrade) {
                return true;
            }
        }
        return false;
    }

    private int count(int slot) {
        return getStack(slot).getCount();
    }

    /** {@code isItemValid}: upgrades only while inactive with module and source, unsupported parts not at all. */
    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (slot == SLOT_MODULE && stack.getItem() instanceof ProjectorModuleItem) {
            return true;
        }
        if (slot == SLOT_POWER_LINK && stack.getItem() instanceof PowerLinkItem) {
            return true;
        }
        ProjectorModule module = getModule();
        if (module == null) {
            return false;
        }
        return switch (slot) {
            case 2, 3, 4 -> isValidUpgrade(module, stack);
            case SLOT_DISTANCE -> stack.is(MffsItems.FIELD_MODULATOR_DISTANCE.get()) && module.supportsDistance();
            case SLOT_STRENGTH -> stack.is(MffsItems.FIELD_MODULATOR_STRENGTH.get()) && module.supportsStrength();
            case 7, 8, 9, 10 -> stack.is(MffsItems.PROJECTOR_FOCUS_MATRIX.get()) && module.supportsMatrix();
            default -> false;
        };
    }

    private boolean isValidUpgrade(ProjectorModule module, ItemStack stack) {
        if (isActive() || !(stack.getItem() instanceof ProjectorUpgradeItem item)) {
            return false;
        }
        ProjectorUpgrade upgrade = item.getUpgrade();
        if (upgrade == ProjectorUpgrade.TOUCH_DAMAGE && hasUpgrade(ProjectorUpgrade.CAMOUFLAGE)
                || upgrade == ProjectorUpgrade.CAMOUFLAGE && hasUpgrade(ProjectorUpgrade.TOUCH_DAMAGE)
                || !hasPowerSource()) {
            return false;
        }
        if (isPowerSourceItem() && needsCapacitor(upgrade)) {
            return false;
        }
        return module.supports(upgrade);
    }

    /** Jammer, field fusion and defence station need a capacitor as source (spec 48 §7.5). */
    private static boolean needsCapacitor(ProjectorUpgrade upgrade) {
        return upgrade == ProjectorUpgrade.FIELD_JAMMER || upgrade == ProjectorUpgrade.FIELD_FUSION
                || upgrade == ProjectorUpgrade.DEFENSE_STATION;
    }

    @Override
    public int getSlotStackLimit(int slot) {
        return slot >= SLOT_DISTANCE && slot <= SLOT_FOCUS_LEFT ? 64 : 1;
    }

    @Override
    protected void onInventoryChanged(int slot) {
        checkSlots();
        markForUpdate();
    }

    /**
     * {@code checkslots}: module type (block state for the texture) and field type; parts the module does not
     * support fall out; without a module slots 2–10 fall out.
     */
    public void checkSlots() {
        if (checkingSlots || level == null || level.isClientSide()) {
            return;
        }
        checkingSlots = true;
        try {
            ProjectorModule module = getModule();
            if (module != null) {
                fieldType = module.getFieldType();
            }
            updateModuleState(module);
            if (hasUpgrade(ProjectorUpgrade.CAMOUFLAGE)) {
                fieldType = ForceFieldType.CAMOUFLAGE;
            }
            if (hasUpgrade(ProjectorUpgrade.TOUCH_DAMAGE)) {
                fieldType = ForceFieldType.ZAPPER;
            }
            if (module == null) {
                for (int slot = SLOT_UPGRADE_FIRST; slot <= SLOT_FOCUS_LEFT; slot++) {
                    dropPlugin(slot);
                }
                return;
            }
            if (!module.supportsStrength()) {
                dropPlugin(SLOT_STRENGTH);
            }
            if (!module.supportsDistance()) {
                dropPlugin(SLOT_DISTANCE);
            }
            if (!module.supportsMatrix()) {
                for (int slot = SLOT_FOCUS_UP; slot <= SLOT_FOCUS_LEFT; slot++) {
                    dropPlugin(slot);
                }
            }
            for (int slot = SLOT_UPGRADE_FIRST; slot <= SLOT_UPGRADE_LAST; slot++) {
                if (getStack(slot).getItem() instanceof ProjectorUpgradeItem item
                        && (!module.supports(item.getUpgrade())
                        || isPowerSourceItem() && needsCapacitor(item.getUpgrade()))) {
                    dropPlugin(slot);
                }
            }
            if (!hasUpgrade(ProjectorUpgrade.CAMOUFLAGE)) {
                dropPlugin(SLOT_CAMOUFLAGE);
            }
        } finally {
            checkingSlots = false;
        }
    }

    private void updateModuleState(ProjectorModule module) {
        int type = module != null ? module.getTypeId() : 0;
        BlockState state = getBlockState();
        if (state.getBlock() instanceof ProjectorBlock && state.getValue(ProjectorBlock.MODULE) != type) {
            level.setBlock(worldPosition, state.setValue(ProjectorBlock.MODULE, type), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void tickMachine() {
        if (!fieldInitialized) {
            fieldInitialized = true;
            checkSlots();
            if (isActive()) {
                calculateField();
            }
        }
        if (hasPowerSource()) {
            linkPower = getAvailablePower();
            if (isPowerSourceItem() && accessType != 0) {
                setAccessType(0);
            }
        } else {
            linkPower = 0;
        }
        followRedstone();
        if (getSwitchValue() && switchDelay >= SWITCH_DELAY && hasModule() && hasPowerSource()
                && linkPower > getPowerNeed(5) && !isActive()) {
            setActive(true);
            switchDelay = 0;
            if (calculateField()) {
                generateField(true);
            }
        }
        if ((!getSwitchValue() && switchDelay >= SWITCH_DELAY || !hasModule() || !hasPowerSource() || burnedOut
                || linkPower <= getPowerNeed(1)) && isActive()) {
            setActive(false);
            switchDelay = 0;
            destroyField();
        }
        if (getTicker() == FIELD_INTERVAL) {
            if (isActive()) {
                generateField(false);
            }
            setTicker(0);
        }
        setTicker(getTicker() + 1);
        switchDelay++;
    }

    private FieldParams fieldParams() {
        return new FieldParams(getFacing().get3DDataValue(), count(SLOT_DISTANCE), count(SLOT_STRENGTH),
                count(SLOT_FOCUS_UP), count(SLOT_FOCUS_DOWN), count(SLOT_FOCUS_RIGHT), count(SLOT_FOCUS_LEFT),
                hasUpgrade(ProjectorUpgrade.FIELD_MANIPULATOR), worldPosition.getY(), level.getMinBuildHeight(),
                level.getMaxBuildHeight() - 1);
    }

    /** {@code calculateField}: field and interior points in world coordinates (below the top), claims for each point. */
    private boolean calculateField() {
        fieldPoints.clear();
        interiorPoints.clear();
        ProjectorModule module = getModule();
        if (module == null) {
            return false;
        }
        FieldParams params = fieldParams();
        Set<BlockPos> field = new LinkedHashSet<>();
        Set<BlockPos> interior = new LinkedHashSet<>();
        module.calculate(params, field, interior);
        for (BlockPos point : field) {
            if (point.getY() + worldPosition.getY() < params.maxY()) {
                BlockPos pos = worldPosition.offset(point);
                defineField(pos);
                fieldPoints.add(pos);
            }
        }
        for (BlockPos point : interior) {
            if (point.getY() + worldPosition.getY() < params.maxY()) {
                interiorPoints.add(worldPosition.offset(point));
            }
        }
        return true;
    }

    /** {@code Forcefielddefine}: claim the position (behind other projectors' claims). */
    private void defineField(BlockPos pos) {
        ForceFieldMap.Stack stack = ForceFieldMap.getOrCreate(level, pos);
        if (!stack.isEmpty()) {
            if (stack.getProjectorId() != getDeviceId()) {
                stack.removeByProjector(getDeviceId());
                stack.add(getPowerSourceId(), getDeviceId(), fieldType);
            }
        } else {
            stack.add(getPowerSourceId(), getDeviceId(), fieldType);
            stack.setSync(false);
        }
        claimed.add(pos.immutable());
    }

    /**
     * {@code FieldGenerate}: costs per field point 1 FE (creation ×10); fields of type {@link ForceFieldType#DEFAULT}
     * pay twice (original error, the zapper factor hits the wrong type). Places the blocks of owned, unplaced
     * positions in loaded chunks where there is air, liquid or already a force field.
     */
    private void generateField(boolean init) {
        int cost = init ? MffsConfig.FIELD_BLOCK_COST * MffsConfig.FIELD_BLOCK_CREATE_MODIFIER
                : MffsConfig.FIELD_BLOCK_COST;
        if (fieldType == ForceFieldType.DEFAULT) {
            cost *= MffsConfig.FIELD_BLOCK_ZAPPER_MODIFIER;
        }
        consumePower(cost * fieldPoints.size(), false);
        blockCounter = 0;
        for (BlockPos pos : fieldPoints) {
            if (blockCounter == MffsConfig.FIELD_MAX_BLOCKS_PER_TICK) {
                break;
            }
            ForceFieldMap.Stack stack = ForceFieldMap.get(level, pos);
            if (stack == null || stack.isSync() || !level.isLoaded(pos) || stack.isEmpty()
                    || stack.getProjectorId() != getDeviceId()) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            boolean field = state.is(MffsBlocks.FORCE_FIELD.get());
            if (state.isAir() || state.getBlock() instanceof LiquidBlock || field) {
                if (!field) {
                    level.setBlock(pos, MffsBlocks.FORCE_FIELD.get().defaultBlockState()
                            .setValue(ForceFieldBlock.TYPE, stack.getType()), Block.UPDATE_ALL);
                    blockCounter++;
                }
                stack.setSync(true);
            }
        }
    }

    /** {@code destroyField}: give up every claim; placed blocks of owned positions disappear. */
    private void destroyField() {
        if (level == null) {
            return;
        }
        for (BlockPos pos : claimed) {
            ForceFieldMap.Stack stack = ForceFieldMap.get(level, pos);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            if (stack.getProjectorId() == getDeviceId()) {
                stack.removeByProjector(getDeviceId());
                if (stack.isSync() && level.getBlockState(pos).is(MffsBlocks.FORCE_FIELD.get())) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
                stack.setSync(false);
            } else {
                stack.removeByProjector(getDeviceId());
            }
        }
        claimed.clear();
    }

    /**
     * {@code Forcepowerneed}: with a computed field its point count × 1 FE; before the first calculation an estimate
     * per type (wall, deflector, tube, cube/sphere; the others 0). With factor ≠ 1 ×10 + ×5.
     */
    public int getPowerNeed(int factor) {
        if (!fieldPoints.isEmpty()) {
            return fieldPoints.size() * MffsConfig.FIELD_BLOCK_COST;
        }
        int strength = count(SLOT_STRENGTH) != 0 ? count(SLOT_STRENGTH) : 1;
        ProjectorModule module = getModule();
        int type = module != null ? module.getTypeId() : 0;
        int blocks = switch (type) {
            case 1 -> (count(SLOT_FOCUS_DOWN) + count(SLOT_FOCUS_LEFT) + count(SLOT_FOCUS_RIGHT) + count(SLOT_FOCUS_UP)
                    + 1) * strength;
            case 2 -> (count(SLOT_FOCUS_DOWN) + count(SLOT_FOCUS_UP) + 1)
                    * (count(SLOT_FOCUS_LEFT) + count(SLOT_FOCUS_RIGHT) + 1);
            case 3 -> ((count(SLOT_DISTANCE) + 2 + count(SLOT_DISTANCE) + 2) * 4 + 4) * (count(SLOT_STRENGTH) + 1);
            case 4, 5 -> count(SLOT_DISTANCE) * count(SLOT_DISTANCE) * 6;
            default -> 0;
        };
        int power = blocks * MffsConfig.FIELD_BLOCK_COST;
        if (factor != 1) {
            power = power * MffsConfig.FIELD_BLOCK_CREATE_MODIFIER + power * 5;
        }
        return power;
    }

    public Set<BlockPos> getFieldPoints() {
        return Collections.unmodifiableSet(fieldPoints);
    }

    public Set<BlockPos> getInteriorPoints() {
        return Collections.unmodifiableSet(interiorPoints);
    }

    public ForceFieldType getFieldType() {
        return fieldType;
    }

    public int getAccessType() {
        return accessType;
    }

    public void setAccessType(int accessType) {
        this.accessType = accessType;
        markForUpdate();
    }

    public int getLinkPower() {
        return linkPower;
    }

    public boolean isBurnedOut() {
        return burnedOut;
    }

    public void setBurnedOut(boolean burnedOut) {
        this.burnedOut = burnedOut;
        markForUpdate();
    }

    /** Access type button (key 1): 0 → 1 → 2 → 0, only while inactive, not with an item source or own station. */
    @Override
    public void handleEvent(int key, String value) {
        if (!isActive() && key == EVENT_ACCESS_TYPE && !isPowerSourceItem() && accessType != 3) {
            setAccessType(accessType == 2 ? 0 : accessType + 1);
        }
        super.handleEvent(key, value);
    }

    /** Multitool wrench: not while active (spec 48 §8). */
    @Override
    public boolean canBeRemovedByMultitool() {
        return !isActive();
    }

    @Override
    public void onChunkUnloaded() {
        unloading = true;
        super.onChunkUnloaded();
    }

    /** {@code invalidate}: removing the projector removes its field (not on chunk unload). */
    @Override
    public void setRemoved() {
        if (!unloading && level != null && !level.isClientSide()) {
            // First out of the link grid, so neighbouring field blocks no longer find this projector.
            MffsDevices.unregister(this);
            destroyField();
        }
        super.setRemoved();
    }

    @Override
    protected void saveState(CompoundTag tag) {
        super.saveState(tag);
        tag.putInt("accesstyp", accessType);
        tag.putBoolean("burnout", burnedOut);
    }

    @Override
    protected void loadState(CompoundTag tag) {
        super.loadState(tag);
        accessType = tag.getInt("accesstyp");
        burnedOut = tag.getBoolean("burnout");
    }

    /** The client also gets the slots: the GUI's slot rules depend on the module. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.put("Items", inventory.serializeNBT(registries));
        return tag;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new ProjectorMenu(id, playerInventory, this);
    }
}
