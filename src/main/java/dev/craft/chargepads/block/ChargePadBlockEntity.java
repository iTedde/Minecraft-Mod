package dev.craft.chargepads.block;

import java.util.ArrayList;
import java.util.List;

import dev.craft.chargepads.ChargePadsConfig;
import dev.craft.chargepads.CpBlockEntityTypes;
import dev.craft.chargepads.PadTier;
import dev.craft.chargepads.item.EfficiencyUpgradeItem;
import dev.craft.chargepads.item.PadModule;
import dev.craft.chargepads.item.PadModuleItem;
import dev.craft.chargepads.menu.ChargePadMenu;
import dev.craft.core.CraftDamageTypes;
import dev.craft.core.energy.ConsumptionReadout;
import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.energy.OutputMeter;
import dev.craft.core.energy.OutputReadout.OutputReading;
import dev.craft.core.explosion.Ic2Explosion;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.UpgradeModuleItem;
import dev.craft.ic2.machine.Wrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import static dev.craft.ic2.item.ElectricItems.charge;
import static dev.craft.ic2.item.ElectricItems.discharge;

/**
 * Charge pad tile (spec 47 §3–§7, original {@code w} with {@code ac}/{@code ad}/{@code ab}): EU sink from below only,
 * battery slot, IC2 upgrades, emitter modules and the charge field. One block entity type for all three pads; the
 * tier comes from the block.
 */
public class ChargePadBlockEntity extends BlockEntity implements EnergySink, Wrenchable, MenuProvider,
        ConsumptionReadout {
    public static final int REDSTONE_EU = 500;
    public static final int SU_BATTERY_EU = 1000;
    /** Charging only starts with at least this much stored after the idle drain (§5 step 2). */
    public static final int MIN_CHARGE_STORAGE = 32;
    public static final int MAX_OVERCLOCKERS = 16;
    public static final int MAX_EFFICIENCY = 16;
    public static final int MAX_TRANSFORMERS = 4;
    /** Inventory: battery, projector, field, three upgrade slots; a static pad uses slots 0 and 1 only. */
    public static final int INVENTORY_SIZE = 6;

    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isValidForSlot(getTier(), slot, stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slotLimit(getTier(), slot);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final OutputMeter consumptionMeter = new OutputMeter();

    private int storage;
    private int overclockers;
    private int efficiency;
    private int energyStorageUpgrades;
    private int transformers;
    private boolean drainModule;
    private boolean damageModule;
    private boolean armourPriority;
    private boolean proximityBooster;
    private boolean wideBandBooster;
    private int fieldExpansion;
    /** Set when the field found nothing to charge; the block switches off at its next check (§4). */
    private boolean nothingToCharge;

    public ChargePadBlockEntity(BlockPos pos, BlockState state) {
        super(CpBlockEntityTypes.CHARGE_PAD.get(), pos, state);
    }

    public PadTier getTier() {
        return getBlockState().getBlock() instanceof ChargePadBlock pad ? pad.getTier() : PadTier.STATIC;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public int getStorage() {
        return storage;
    }

    public void setStorage(int storage) {
        this.storage = storage;
        setChanged();
    }

    /** Capacity = base + tier × 10 000 × energy storage upgrades. */
    public int getCapacity() {
        PadTier tier = getTier();
        return tier.getBaseCapacity() + tier.getStoragePerUpgrade() * energyStorageUpgrades;
    }

    /** Maximum safe input = base × 4^transformers. */
    public int getMaxInput() {
        return getTier().getBaseRate() << 2 * transformers;
    }

    /** Charge rate per tick before the storage cap: ⌊base × 1.05^overclockers⌋. */
    public int getChargeRate() {
        return (int) (getTier().getBaseRate() * Math.pow(1.05, overclockers));
    }

    public int getFieldExpansion() {
        return fieldExpansion;
    }

    public boolean hasDrainModule() {
        return drainModule;
    }

    public boolean hasDamageModule() {
        return damageModule;
    }

    /** Drain or damage module: GUI and wrench only from below (§2.1). */
    public boolean hasEmitterModule() {
        return drainModule || damageModule;
    }

    public boolean isNothingToCharge() {
        return nothingToCharge;
    }

    public void setNothingToCharge(boolean nothingToCharge) {
        this.nothingToCharge = nothingToCharge;
    }

    /** Modules stack to 1 ({@code SlotModule}), everything else to 16. */
    public static int slotLimit(PadTier tier, int slot) {
        return slot != PadTier.BATTERY_SLOT && slot < tier.getFirstUpgradeSlot() ? 1 : PadTier.SLOT_LIMIT;
    }

    public static boolean isValidForSlot(PadTier tier, int slot, ItemStack stack) {
        if (slot >= tier.getSlotCount()) {
            return false;
        }
        if (slot == PadTier.BATTERY_SLOT) {
            return isBatteryItem(stack, tier.getTier());
        }
        if (slot < tier.getFirstUpgradeSlot()) {
            return stack.getItem() instanceof PadModuleItem module
                    && module.getModule().isField() == (slot == PadTier.FIELD_SLOT)
                    && module.getModule().getMinTier() <= tier.getTier();
        }
        return stack.getItem() instanceof UpgradeModuleItem || stack.getItem() instanceof EfficiencyUpgradeItem;
    }

    /** IC2 {@code SlotDischarge} with the pad tier: redstone, single-use battery, dischargeable items up to the tier. */
    public static boolean isBatteryItem(ItemStack stack, int tier) {
        if (stack.getItem() instanceof ElectricItem item) {
            return item.canProvideEnergy() && item.getTier() <= tier;
        }
        return stack.is(Items.REDSTONE) || stack.is(Ic2Items.SU_BATTERY.get());
    }

    // --- tick (§3, §5) ---

    public void serverTick() {
        int before = storage;
        boolean changed = updateUpgrades();
        boolean demands = false;
        if (!changed) {
            demands = demandsEnergy() > 0;
            changed = demands;
        }
        if (demands && dischargeBattery()) {
            changed = true;
        }
        if (getBlockState().getValue(ChargePadBlock.ACTIVE)) {
            int idle = getTier().getTier() + fieldExpansion * 2 + Math.max(0, overclockers - efficiency);
            int drained = Math.min(storage, idle);
            storage -= drained;
            consumptionMeter.record(level, drained);
            if (storage >= MIN_CHARGE_STORAGE) {
                chargeField();
            }
        } else {
            consumptionMeter.record(level, 0);
        }
        if (changed || before != storage) {
            setChanged();
        }
    }

    /** Battery slot (§3): one discharge per tick, only when the pad needs energy. */
    private boolean dischargeBattery() {
        ItemStack stack = inventory.getStackInSlot(PadTier.BATTERY_SLOT);
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof ElectricItem item) {
            if (!item.canProvideEnergy()) {
                return false;
            }
            int gained = discharge(stack, getCapacity() - storage, getTier().getTier() + transformers, false, false);
            storage += gained;
            return gained > 0;
        }
        int amount = stack.is(Items.REDSTONE) ? REDSTONE_EU : stack.is(Ic2Items.SU_BATTERY.get()) ? SU_BATTERY_EU : 0;
        if (amount > 0 && amount <= getCapacity() - storage) {
            inventory.extractItem(PadTier.BATTERY_SLOT, 1, false);
            storage += amount;
            return true;
        }
        return false;
    }

    /**
     * Re-reads modules and upgrades ({@code D()}); upgrades count over all upgrade slots (overclocker and efficiency
     * at most 16, transformer at most 4). A lower capacity cuts the storage.
     *
     * @return whether anything changed
     */
    boolean updateUpgrades() {
        PadTier tier = getTier();
        boolean changed = false;
        if (tier.hasModuleSlots()) {
            PadModule projector = moduleIn(PadTier.PROJECTOR_SLOT);
            PadModule field = moduleIn(PadTier.FIELD_SLOT);
            boolean proximity = projector == PadModule.PROXIMITY_BOOSTER;
            boolean wideBand = projector == PadModule.WIDE_BAND_BOOSTER;
            int expansion = projector != null ? projector.getExpansion() : 0;
            boolean drain = field == PadModule.DRAIN_CONVERSION;
            boolean damage = field == PadModule.DAMAGE_CONVERSION;
            boolean armour = field == PadModule.ARMOUR_PRIORITY;
            changed = proximity != proximityBooster || wideBand != wideBandBooster || expansion != fieldExpansion
                    || drain != drainModule || damage != damageModule || armour != armourPriority;
            proximityBooster = proximity;
            wideBandBooster = wideBand;
            fieldExpansion = expansion;
            drainModule = drain;
            damageModule = damage;
            armourPriority = armour;
        }
        int newEfficiency = 0;
        int newOverclockers = 0;
        int newStorage = 0;
        int newTransformers = 0;
        for (int slot = tier.getFirstUpgradeSlot(); slot < tier.getSlotCount(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.getItem() instanceof EfficiencyUpgradeItem) {
                newEfficiency += stack.getCount();
            } else if (stack.getItem() instanceof UpgradeModuleItem upgrade) {
                switch (upgrade.getType()) {
                    case OVERCLOCKER -> newOverclockers += stack.getCount();
                    case TRANSFORMER -> newTransformers += stack.getCount();
                    case ENERGY_STORAGE -> newStorage += stack.getCount();
                }
            }
        }
        newEfficiency = Math.min(newEfficiency, MAX_EFFICIENCY);
        newOverclockers = Math.min(newOverclockers, MAX_OVERCLOCKERS);
        newTransformers = Math.min(newTransformers, MAX_TRANSFORMERS);
        if (newEfficiency != efficiency || newOverclockers != overclockers || newTransformers != transformers) {
            changed = true;
        }
        efficiency = newEfficiency;
        overclockers = newOverclockers;
        transformers = newTransformers;
        if (newStorage != energyStorageUpgrades) {
            energyStorageUpgrades = newStorage;
            storage = Math.min(storage, getCapacity());
            changed = true;
        }
        return changed;
    }

    private @Nullable PadModule moduleIn(int slot) {
        return inventory.getStackInSlot(slot).getItem() instanceof PadModuleItem module ? module.getModule() : null;
    }

    /** Field (§4): the collision box (16 × 4 × 16 px) grown by the expansion horizontally and 0.5 vertically. */
    public AABB getFieldBox() {
        return ChargePadBlock.SHAPE.bounds().move(worldPosition).inflate(fieldExpansion, 0.5, fieldExpansion);
    }

    /** §5 steps 2–7. */
    private void chargeField() {
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, getFieldBox());
        if (entities.isEmpty()) {
            return;
        }
        int rate = Math.min(storage, getChargeRate());
        double costFactor = Math.pow(0.995, efficiency);
        int tier = getTier().getTier();
        if (damageModule && ChargePadsConfig.ALLOW_EMITTER_UPGRADE) {
            for (LivingEntity entity : entities) {
                entity.hurt(level.damageSources().source(CraftDamageTypes.EMITTER_RADIATION), 1.0F);
                if (!entity.hasEffect(MobEffects.POISON)) {
                    entity.addEffect(new MobEffectInstance(MobEffects.POISON, tier * 20, tier * 4));
                }
            }
            storage -= (int) Math.ceil(rate * costFactor);
            return;
        }
        boolean drain = drainModule && ChargePadsConfig.ALLOW_EMITTER_UPGRADE;
        List<ItemStack> items = new ArrayList<>();
        for (LivingEntity entity : entities) {
            if (entity instanceof ServerPlayer player) {
                int count = items.size();
                if (armourPriority) {
                    collectArmour(player, items, drain);
                    if (items.size() == count) {
                        collectHand(player, items, drain);
                    }
                } else {
                    collectHand(player, items, drain);
                    if (items.size() == count) {
                        collectArmour(player, items, drain);
                    }
                }
            }
        }
        if (items.isEmpty()) {
            nothingToCharge = true;
            return;
        }
        int remaining = items.size();
        for (ItemStack stack : items) {
            int offer = rate / remaining--;
            int moved = drain ? discharge(stack, offer, tier, true, false) : charge(stack, offer, tier, true, false);
            if (moved > 0) {
                int cost = (int) Math.ceil(moved * costFactor);
                storage -= cost;
                rate -= cost;
                consumptionMeter.record(level, cost);
            }
        }
    }

    private void collectArmour(Player player, List<ItemStack> items, boolean drain) {
        for (ItemStack stack : player.getInventory().armor) {
            if (canTransfer(stack, drain)) {
                items.add(stack);
            }
        }
    }

    /** Held slot; with the proximity booster ± 1, with the wide-band booster the whole hotbar. */
    private void collectHand(Player player, List<ItemStack> items, boolean drain) {
        int first = player.getInventory().selected;
        int last = first;
        if (proximityBooster) {
            first = Math.max(0, first - 1);
            last = Math.min(Inventory.getSelectionSize() - 1, last + 1);
        } else if (wideBandBooster) {
            first = 0;
            last = Inventory.getSelectionSize() - 1;
        }
        for (int slot = first; slot <= last; slot++) {
            ItemStack stack = player.getInventory().items.get(slot);
            if (canTransfer(stack, drain)) {
                items.add(stack);
            }
        }
    }

    /** Original {@code b(ItemStack)}: electric items up to the pad tier that can take (or give) energy. */
    private boolean canTransfer(ItemStack stack, boolean drain) {
        if (!(stack.getItem() instanceof ElectricItem item) || item.getTier() > getTier().getTier()) {
            return false;
        }
        int tier = getTier().getTier();
        return (drain ? discharge(stack, Integer.MAX_VALUE, tier, true, true)
                : charge(stack, Integer.MAX_VALUE, tier, true, true)) > 0;
    }

    // --- dangers (§6) ---

    /**
     * Breaking without a wrench by a non-creative player: discharge zap around the pad, extra damage for the
     * breaker, then every stack loses 0 … count items.
     */
    public void onBrokenByPlayer(Player player) {
        if (ChargePadsConfig.DISCHARGE_ZAP) {
            dischargeZap();
            player.hurt(level.damageSources().source(CraftDamageTypes.ELECTROCUTION), getTier().getBreakerZap());
        }
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                stack.shrink(level.random.nextInt(stack.getCount() + 1));
                inventory.setStackInSlot(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
            }
        }
    }

    /** Living entities within the zap radius take {@code D − ⌊distance to the block corner⌋}. */
    private void dischargeZap() {
        PadTier tier = getTier();
        int damage = tier.getDischargeDamage(storage);
        int radius = tier.getZapRadius(damage);
        AABB box = ChargePadBlock.SHAPE.bounds().move(worldPosition).inflate(radius);
        Vec3 corner = Vec3.atLowerCornerOf(worldPosition);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            int amount = damage - (int) Math.floor(entity.position().distanceTo(corner));
            if (amount > 0) {
                entity.hurt(level.damageSources().source(CraftDamageTypes.ELECTROCUTION), amount);
            }
        }
    }

    // --- energy net (§3) ---

    @Override
    public void onLoad() {
        super.onLoad();
        if (!level.isClientSide()) {
            updateUpgrades();
        }
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
        return direction == Direction.DOWN;
    }

    @Override
    public int demandsEnergy() {
        return isRemoved() ? 0 : getCapacity() - storage;
    }

    @Override
    public int getMaxSafeInput() {
        return getMaxInput();
    }

    /** IC2 storage quirk: a packet that does not fit returns {@code packet − max(1, free)}. */
    @Override
    public int injectEnergy(Direction direction, int amount) {
        if (isRemoved()) {
            return amount;
        }
        updateUpgrades();
        if (direction != Direction.DOWN) {
            return amount;
        }
        if (amount > getMaxInput()) {
            if (level instanceof ServerLevel serverLevel) {
                Ic2Explosion.explodeMachineAt(serverLevel, worldPosition);
            }
            return 0;
        }
        int free = getCapacity() - storage;
        setChanged();
        if (free >= amount) {
            storage += amount;
            return 0;
        }
        storage += free;
        return amount - Math.max(1, free);
    }

    @Override
    public @Nullable OutputReading getConsumptionReading() {
        return consumptionMeter.read(level, OutputReading.Unit.EU);
    }

    // --- wrench (§2.1): never rotates, always drops the empty pad ---

    @Override
    public Direction getFacing() {
        return Direction.UP;
    }

    @Override
    public boolean wrenchCanSetFacing(Player player, Direction side) {
        return false;
    }

    @Override
    public void setFacing(Direction facing) {
    }

    /** With a drain or damage module only when the player looks at the bottom face (5-block ray). */
    @Override
    public boolean wrenchCanRemove(Player player) {
        if (!hasEmitterModule()) {
            return true;
        }
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(5.0));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE,
                player));
        return hit.getType() == HitResult.Type.BLOCK && hit.getDirection() == Direction.DOWN;
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
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new ChargePadMenu(id, playerInventory, this);
    }

    public boolean stillValid(Player player) {
        return level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getCenter()) <= 64.0;
    }

    // --- persistence ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        storage = tag.getInt("storage");
        inventory.deserializeNBT(registries, tag.getCompound("Items"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("storage", storage);
        tag.put("Items", inventory.serializeNBT(registries));
    }
}
