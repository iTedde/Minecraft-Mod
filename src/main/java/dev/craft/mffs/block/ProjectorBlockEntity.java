package dev.craft.mffs.block;

import dev.craft.core.CraftDamageTypes;
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
import dev.craft.mffs.security.MffsSecurity;
import dev.craft.mffs.security.SecurityRight;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * {@code TileEntityProjector} (spec 48 §9.1): projects the field of its module. Switches on after at least 40 ticks
 * when the link has more force energy than the estimate {@link #getPowerNeed(int) need(5)}; computes the field points
 * into the {@link ForceFieldMap} and places the blocks (creation cost ×10); every 20 ticks upkeep and placing missing
 * blocks (at most 5 000 per pass). Switches off below {@code need(1)}, without module or source, when burnt out or
 * switched off – the field is removed. Upgrades (slots 2–4, spec 48 §9.3): touch damage and camouflage set the field
 * type, the sponge removes liquids and field fusion foreign fusion blocks in the interior, the block breaker clears
 * field positions, every projector burns out when a point lies inside the interior of another capacitor's active
 * jammer, NPC defence and defence station hurt entities in the field box every 20 ticks. The security link (12)
 * follows in phase 48.4.
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
    /** NPC defence and defence station: damage and force energy per hit ({@code fieldDefense}, 10 000 FE). */
    private static final int DEFENSE_DAMAGE = 10;
    private static final int DEFENSE_COST = 10000;

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
    /** Look of the camouflage field blocks (slot 11), null without a block. */
    private BlockState camouflage;

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
        if (slot == SLOT_CAMOUFLAGE && camouflageOf(stack) != null && hasUpgrade(ProjectorUpgrade.CAMOUFLAGE)) {
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
            updateCamouflage();
            checkingSlots = false;
        }
    }

    /** Block look for slot 11: a block item, or water/lava for their buckets (the original's texture indices). */
    public static BlockState camouflageOf(ItemStack stack) {
        if (stack.is(Items.WATER_BUCKET)) {
            return Blocks.WATER.defaultBlockState();
        }
        if (stack.is(Items.LAVA_BUCKET)) {
            return Blocks.LAVA.defaultBlockState();
        }
        if (stack.getItem() instanceof BlockItem item && !item.getBlock().defaultBlockState().is(MffsBlocks.FORCE_FIELD)) {
            return item.getBlock().defaultBlockState();
        }
        return null;
    }

    public BlockState getCamouflage() {
        return camouflage;
    }

    /** {@code UpdateForcefieldTexttur}: a new look reaches all placed camouflage blocks of an active field. */
    private void updateCamouflage() {
        BlockState look = hasUpgrade(ProjectorUpgrade.CAMOUFLAGE) ? camouflageOf(getStack(SLOT_CAMOUFLAGE)) : null;
        if (Objects.equals(look, camouflage)) {
            return;
        }
        camouflage = look;
        if (isActive() && hasUpgrade(ProjectorUpgrade.CAMOUFLAGE)) {
            for (BlockPos pos : fieldPoints) {
                if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof ForceFieldBlockEntity field) {
                    field.setCamouflage(camouflage);
                }
            }
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
                if (hasUpgrade(ProjectorUpgrade.NPC_DEFENSE)) {
                    npcDefense();
                }
                if (hasUpgrade(ProjectorUpgrade.DEFENSE_STATION)) {
                    playerDefense();
                }
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
                if (!defineField(pos)) {
                    return false;
                }
                fieldPoints.add(pos);
            }
        }
        for (BlockPos point : interior) {
            if (point.getY() + worldPosition.getY() < params.maxY()) {
                BlockPos pos = worldPosition.offset(point);
                checkInteriorBlock(pos);
                interiorPoints.add(pos);
            }
        }
        return true;
    }

    /**
     * {@code getOptions(true)}: the own upgrades by slot, the jammer check (which concerns every projector) inserted
     * after the first slot like in the original.
     */
    private List<ProjectorUpgrade> getOptions() {
        List<ProjectorUpgrade> options = new ArrayList<>();
        for (int slot = SLOT_UPGRADE_FIRST; slot <= SLOT_UPGRADE_LAST; slot++) {
            if (getStack(slot).getItem() instanceof ProjectorUpgradeItem item) {
                options.add(item.getUpgrade());
            }
            if (!options.contains(ProjectorUpgrade.FIELD_JAMMER)) {
                options.add(ProjectorUpgrade.FIELD_JAMMER);
            }
        }
        return options;
    }

    private List<ProjectorBlockEntity> projectorsWith(ProjectorUpgrade upgrade) {
        List<ProjectorBlockEntity> found = new ArrayList<>();
        for (var machine : MffsDevices.all(level, MffsDevices.Kind.PROJECTOR)) {
            if (machine instanceof ProjectorBlockEntity projector && !projector.isRemoved()
                    && projector.hasUpgrade(upgrade)) {
                found.add(projector);
            }
        }
        return found;
    }

    /** {@code CheckJammerinfluence}: inside the interior of another capacitor's active jammer this projector burns out. */
    private boolean checkJammer(BlockPos pos) {
        for (ProjectorBlockEntity jammer : projectorsWith(ProjectorUpgrade.FIELD_JAMMER)) {
            if (jammer.getPowerSourceId() != getPowerSourceId() && jammer.isActive()
                    && jammer.interiorPoints.contains(pos)) {
                burnOut();
                return true;
            }
        }
        return false;
    }

    /** {@code checkFieldFusioninfluence}: inside another active fusion projector of the same capacitor. */
    private boolean checkFusion(BlockPos pos) {
        if (isPowerSourceItem()) {
            return false;
        }
        for (ProjectorBlockEntity other : projectorsWith(ProjectorUpgrade.FIELD_FUSION)) {
            if (other.getPowerSourceId() == getPowerSourceId() && other.getDeviceId() != getDeviceId()
                    && other.isActive() && other.interiorPoints.contains(pos)) {
                return true;
            }
        }
        return false;
    }

    /** {@code ProjektorBurnout}: burnt out until repaired with the multitool wrench; every slot falls out. */
    public void burnOut() {
        setBurnedOut(true);
        dropPlugins();
    }

    /**
     * {@code calculateBlock}: interior checks of the own upgrades – the sponge removes liquid blocks, field fusion
     * removes the field block of another fusion projector of the same capacitor (merged rooms).
     */
    private void checkInteriorBlock(BlockPos pos) {
        for (ProjectorUpgrade option : getOptions()) {
            if (option == ProjectorUpgrade.SPONGE) {
                if (level.getBlockState(pos).getBlock() instanceof LiquidBlock) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            } else if (option == ProjectorUpgrade.FIELD_FUSION) {
                ForceFieldMap.Stack stack = ForceFieldMap.getOrCreate(level, pos);
                if (stack.isEmpty() || stack.getCapacitorId() != getPowerSourceId()) {
                    continue;
                }
                if (MffsDevices.get(level, MffsDevices.Kind.PROJECTOR, stack.getProjectorId())
                        instanceof ProjectorBlockEntity owner && owner.hasUpgrade(ProjectorUpgrade.FIELD_FUSION)) {
                    owner.claimed.remove(pos);
                    stack.removeByProjector(owner.getDeviceId());
                    if (level.getBlockState(pos).is(MffsBlocks.FORCE_FIELD.get())) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    /**
     * {@code Forcefielddefine}: the jammer check aborts the calculation (burn-out), a point inside a fused field is
     * kept without a claim; otherwise claim the position (behind other projectors' claims).
     */
    private boolean defineField(BlockPos pos) {
        for (ProjectorUpgrade option : getOptions()) {
            if (option == ProjectorUpgrade.FIELD_JAMMER && checkJammer(pos)) {
                return false;
            }
            if (option == ProjectorUpgrade.FIELD_FUSION && checkFusion(pos)) {
                return true;
            }
        }
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
        return true;
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
            if (hasUpgrade(ProjectorUpgrade.BLOCK_BREAKER)) {
                breakBlock(pos);
            }
            BlockState state = level.getBlockState(pos);
            boolean field = state.is(MffsBlocks.FORCE_FIELD.get());
            if (state.isAir() || state.getBlock() instanceof LiquidBlock || field) {
                if (!field) {
                    placeFieldBlock(pos, stack.getType());
                    blockCounter++;
                }
                stack.setSync(true);
            }
        }
    }

    /** Places a field block of the given type; camouflage blocks get the projector's look. */
    public void placeFieldBlock(BlockPos pos, ForceFieldType type) {
        level.setBlock(pos, MffsBlocks.FORCE_FIELD.get().defaultBlockState().setValue(ForceFieldBlock.TYPE, type),
                Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof ForceFieldBlockEntity field) {
            field.setCamouflage(camouflage);
        }
    }

    /**
     * Block breaker ({@code MFFSProjectorOptionCutter}): removes anything but air, field blocks, unbreakable blocks
     * and blocks with a block entity. Wall, deflector and diagonal wall give the drops to the largest attached
     * inventory – one single item (original error), the rest is lost; other shapes lose everything.
     */
    private void breakBlock(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(MffsBlocks.FORCE_FIELD.get()) || state.getDestroySpeed(level, pos) < 0
                || level.getBlockEntity(pos) != null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, null);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        ProjectorModule module = getModule();
        if (module != null && module.isBlockDropper() && !drops.isEmpty()) {
            IItemHandler inventory = findAttachedInventory();
            if (inventory != null && inventory.getSlots() > 0) {
                addOneItem(inventory, drops);
            }
        }
    }

    /** {@code InventoryHelper.findAttachedInventory}: the neighbour inventory with the most slots. */
    private IItemHandler findAttachedInventory() {
        IItemHandler best = null;
        for (BlockPos pos : List.of(worldPosition.east(), worldPosition.west(), worldPosition.above(),
                worldPosition.below(), worldPosition.south(), worldPosition.north())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            IItemHandler handler = blockEntity == null ? null
                    : level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
            if (handler != null && (best == null || best.getSlots() < handler.getSlots())) {
                best = handler;
            }
        }
        return best;
    }

    /**
     * {@code addStacksToInventory}: the first slot that takes one item of any drop gets exactly that one item and
     * the method ends (original error). The security storage's first slot is skipped – that storage follows in 48.4.
     */
    private static void addOneItem(IItemHandler inventory, List<ItemStack> drops) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            for (ItemStack drop : drops) {
                if (!drop.isEmpty() && inventory.insertItem(slot, drop.copyWithCount(1), false).isEmpty()) {
                    return;
                }
            }
        }
    }

    /** Field box: the projector and every claimed position ({@code field_queue}). */
    private AABB fieldBox() {
        int minX = worldPosition.getX();
        int minY = worldPosition.getY();
        int minZ = worldPosition.getZ();
        int maxX = minX;
        int maxY = minY;
        int maxZ = minZ;
        for (BlockPos pos : claimed) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /** The sphere only reaches entities within distance D + 4 (truncated coordinates like the original). */
    private boolean inReach(LivingEntity entity) {
        if (getModule() != ProjectorModule.SPHERE) {
            return true;
        }
        int dx = (int) entity.getX() - worldPosition.getX();
        int dy = (int) entity.getY() - worldPosition.getY();
        int dz = (int) entity.getZ() - worldPosition.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz) <= count(SLOT_DISTANCE) + 4;
    }

    private void defend(LivingEntity entity) {
        entity.hurt(level.damageSources().source(CraftDamageTypes.FIELD_DEFENSE), DEFENSE_DAMAGE);
    }

    /**
     * {@code ProjectorNPCDefence}: monsters, slimes and ghasts in the field box take 10 damage for 10 000 FE each; stops
     * once the link power of this tick is below 10 000.
     */
    private void npcDefense() {
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, fieldBox())) {
            if (!(entity instanceof Monster || entity instanceof Slime || entity instanceof Ghast) || !inReach(entity)) {
                continue;
            }
            if (linkPower < DEFENSE_COST) {
                break;
            }
            if (consumePower(DEFENSE_COST, true)) {
                defend(entity);
                consumePower(DEFENSE_COST, false);
            }
        }
    }

    /**
     * {@code ProjectorPlayerDefence}: players in the field box whom the security station refuses right SR (access type
     * 2: the capacitor's station, 3: the projector's own) are warned and take 10 damage for 10 000 FE.
     */
    private void playerDefense() {
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, fieldBox())) {
            if (!(entity instanceof Player player) || !inReach(entity)) {
                continue;
            }
            if (linkPower < DEFENSE_COST) {
                break;
            }
            if (linkPower <= DEFENSE_COST) {
                continue;
            }
            boolean kill = false;
            if (accessType == 2 && MffsDevices.get(level, MffsDevices.Kind.CAPACITOR, getPowerSourceId())
                    instanceof CapacitorBlockEntity capacitor) {
                kill = MffsSecurity.isDeniedByStation(capacitor, player, SecurityRight.SR);
            }
            if (accessType == 3) {
                kill = MffsSecurity.isDeniedByStation(this, player, SecurityRight.SR);
            }
            if (kill && consumePower(DEFENSE_COST, true)) {
                player.sendSystemMessage(Component.translatable("message.craft.mffs.area_defence_leave"));
                defend(player);
                consumePower(DEFENSE_COST, false);
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
        // Fused projectors of the same capacitor take over the positions they gave up.
        for (ProjectorBlockEntity other : projectorsWith(ProjectorUpgrade.FIELD_FUSION)) {
            if (other != this && other.getPowerSourceId() == getPowerSourceId() && other.isActive()) {
                other.calculateField();
            }
        }
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
