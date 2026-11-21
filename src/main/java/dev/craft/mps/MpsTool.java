package dev.craft.mps;

import java.util.List;

import dev.craft.Craft;
import dev.craft.mps.entity.PlasmaBoltEntity;
import dev.craft.mps.item.PowerToolItem;
import dev.craft.mps.module.MpsModules;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * MPS {@code ItemPowerTool}, {@code EventHandler} (harvest check, break speed) and the right-click modules (spec 29
 * §8): mining by tool type, melee assist, modes, Blink Drive, Railgun, Plasma Cannon and In-Place Assembler.
 */
@EventBusSubscriber(modid = Craft.MODID)
public final class MpsTool {
    public static final double RAILGUN_RANGE = 64.0;
    /** Plasma Cannon starts charging above this pool. */
    public static final double PLASMA_MIN_ENERGY = 500.0;
    public static final int USE_DURATION = 72000;

    private MpsTool() {
    }

    /** The tool type mining a block; the diamond drill uses the pickaxe values. */
    public enum ToolType {
        PICKAXE("Pickaxe"), SHOVEL("Shovel"), AXE("Axe"), DIAMOND("Pickaxe");

        private final String values;

        ToolType(String values) {
            this.values = values;
        }

        public String energyProperty() {
            return values + " Energy Consumption";
        }

        public String speedProperty() {
            return values + " Harvest Speed";
        }
    }

    /**
     * {@code useIronPickaxe} … {@code useDiamondPickaxe} in that order. Forge 1.4.7's {@code isToolEffective} ignores
     * the harvest level, so the pickaxe alone covers every pickaxe block (also obsidian) and the diamond drill only
     * acts without an active pickaxe (with 0 harvest speed then).
     */
    public static ToolType toolType(ItemStack stack, BlockState state) {
        if (ModularItems.hasActiveModule(stack, MpsModules.PICKAXE) && state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return ToolType.PICKAXE;
        }
        if (ModularItems.hasActiveModule(stack, MpsModules.SHOVEL) && state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return ToolType.SHOVEL;
        }
        if (ModularItems.hasActiveModule(stack, MpsModules.AXE) && (state.is(BlockTags.MINEABLE_WITH_AXE)
                || isPlant(state))) {
            return ToolType.AXE;
        }
        if (ModularItems.hasActiveModule(stack, MpsModules.DIAMOND_DRILL_UPGRADE)
                && state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return ToolType.DIAMOND;
        }
        return null;
    }

    /** 1.4.7 materials {@code plants} and {@code vine}: flowers, crops, saplings, grass, vines, reeds … */
    private static boolean isPlant(BlockState state) {
        return state.getBlock() instanceof BushBlock || state.getBlock() instanceof VineBlock
                || state.getBlock() instanceof SugarCaneBlock;
    }

    /** {@code canHarvestBlock}: a matching type and more energy than it costs. */
    public static boolean canHarvest(Player player, ItemStack stack, BlockState state) {
        ToolType type = toolType(stack, state);
        return type != null && MpsEnergy.getPlayerEnergy(player)
                > ModularItems.computeProperty(stack, type.energyProperty());
    }

    @SubscribeEvent
    public static void onHarvestCheck(PlayerEvent.HarvestCheck event) {
        Player player = event.getEntity();
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof PowerToolItem && canHarvest(player, stack, event.getTargetBlock())) {
            event.setCanHarvest(true);
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof PowerToolItem) {
            event.setNewSpeed((float) breakSpeed(player, stack, event.getState(), event.getOriginalSpeed()));
        }
    }

    /**
     * {@code handleBreakSpeed}: the vanilla speed times the type's harvest speed, 1 without a type; under water with
     * Aqua Affinity × 5 × Underwater Harvest Speed.
     */
    public static double breakSpeed(Player player, ItemStack stack, BlockState state, double speed) {
        double energy = MpsEnergy.getPlayerEnergy(player);
        ToolType type = toolType(stack, state);
        double harvestSpeed = speed;
        if (type != null && energy > ModularItems.computeProperty(stack, type.energyProperty())) {
            harvestSpeed *= ModularItems.computeProperty(stack, type.speedProperty());
        } else {
            harvestSpeed = 1.0;
        }
        if (harvestSpeed > 1.0 && player.isEyeInFluid(FluidTags.WATER)
                && ModularItems.hasActiveModule(stack, MpsModules.AQUA_AFFINITY)
                && energy > ModularItems.computeProperty(stack, "Underwater Energy Consumption")) {
            harvestSpeed *= 5.0 * ModularItems.computeProperty(stack, "Underwater Harvest Speed");
        }
        return harvestSpeed;
    }

    /** {@code onBlockDestroyed}: the type's consumption (plus Aqua Affinity under water) for every mined block. */
    public static double miningCost(ItemStack stack, BlockState state, LivingEntity miner) {
        ToolType type = toolType(stack, state);
        double drain = type == null ? 0.0 : ModularItems.computeProperty(stack, type.energyProperty());
        if (drain > 0.0 && miner.isEyeInFluid(FluidTags.WATER)
                && ModularItems.hasActiveModule(stack, MpsModules.AQUA_AFFINITY)) {
            drain += ModularItems.computeProperty(stack, "Underwater Energy Consumption");
        }
        return drain;
    }

    /**
     * {@code hitEntity} with Melee Assist: a second hit of Melee Damage (only the part above the first hit gets through
     * the hurt cooldown) and, if it hurt, the knockback along the look vector.
     */
    public static void meleeAssist(ItemStack stack, LivingEntity target, Player player) {
        if (!ModularItems.hasActiveModule(stack, MpsModules.MELEE_ASSIST)) {
            return;
        }
        double drain = ModularItems.computeProperty(stack, "Punch Energy Consumption");
        if (MpsEnergy.getPlayerEnergy(player) > drain) {
            MpsEnergy.drainPlayerEnergy(player, drain);
            double damage = ModularItems.computeProperty(stack, "Melee Damage");
            double knockback = ModularItems.computeProperty(stack, "Melee Knockback");
            if (target.hurt(player.damageSources().playerAttack(player), (int) damage)) {
                Vec3 look = player.getLookAngle();
                target.push(look.x * knockback, Math.abs(look.y + 0.2F) * knockback, look.z * knockback);
            }
        }
    }

    /** {@code onItemRightClick}: the active right-click module; false if there is none. */
    public static boolean rightClick(Player player, Level level, ItemStack stack, InteractionHand hand) {
        String mode = ModularItems.getMode(stack);
        if (!ModularItems.hasActiveModule(stack, mode)) {
            return false;
        }
        switch (mode) {
            case MpsModules.BLINK_DRIVE -> blinkDrive(player, level, stack);
            case MpsModules.RAILGUN -> railgun(player, level, stack);
            case MpsModules.PLASMA_CANNON -> {
                if (MpsEnergy.getPlayerEnergy(player) > PLASMA_MIN_ENERGY) {
                    player.startUsingItem(hand);
                }
            }
            case MpsModules.IN_PLACE_ASSEMBLER -> inPlaceAssembler(player, level);
            default -> {
                return false;
            }
        }
        return true;
    }

    /** {@code BlinkDriveModule}: teleport to the first hit within range; the energy is paid even without a hit. */
    static void blinkDrive(Player player, Level level, ItemStack stack) {
        double range = ModularItems.computeProperty(stack, "Blink Drive Range");
        double energyConsumption = ModularItems.computeProperty(stack, "Blink Drive Energy Consuption");
        if (MpsEnergy.getPlayerEnergy(player) > energyConsumption && !level.isClientSide()) {
            MpsEnergy.drainPlayerEnergy(player, energyConsumption);
            playSound(level, player, SoundEvents.ENDERMAN_TELEPORT);
            teleport(player, rayTrace(level, player, range));
            playSound(level, player, SoundEvents.ENDERMAN_TELEPORT);
        }
    }

    /** {@code MusePlayerUtils.teleportEntity}: on an entity to the hit point; on a block side half a block out. */
    static void teleport(Player player, HitResult hit) {
        if (hit == null || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        Vec3 pos = hit.getLocation();
        double x = pos.x;
        double y = pos.y;
        double z = pos.z;
        if (hit instanceof BlockHitResult blockHit) {
            Direction side = blockHit.getDirection();
            switch (side) {
                case DOWN -> y -= 2.0;
                case NORTH -> z -= 0.5;
                case SOUTH -> z += 0.5;
                case WEST -> x -= 0.5;
                case EAST -> x += 0.5;
                default -> {
                }
            }
        }
        serverPlayer.teleportTo(x, y, z);
    }

    /**
     * {@code RailgunModule}: Impulse / 100 damage to the entity hit within 64 m, knockback damage / 20 to it and
     * backwards to the player; particle trail and a second sound only on a hit.
     */
    static void railgun(Player player, Level level, ItemStack stack) {
        double energyConsumption = ModularItems.computeProperty(stack, "Railgun Energy Cost");
        if (MpsEnergy.getPlayerEnergy(player) <= energyConsumption) {
            return;
        }
        if (!level.isClientSide()) {
            MpsEnergy.drainPlayerEnergy(player, energyConsumption);
        }
        HitResult hit = rayTrace(level, player, RAILGUN_RANGE);
        playSound(level, player, SoundEvents.ARROW_SHOOT);
        double damage = ModularItems.computeProperty(stack, "Railgun Total Impulse") / 100.0;
        double knockback = damage / 20.0;
        Vec3 look = player.getLookAngle();
        if (hit != null) {
            drawParticleStreamTo(player, level, hit.getLocation());
            if (hit instanceof EntityHitResult entityHit && !level.isClientSide()
                    && entityHit.getEntity().hurt(player.damageSources().playerAttack(player), (int) damage)) {
                entityHit.getEntity().push(look.x * knockback, Math.abs(look.y + 0.2F) * knockback,
                        look.z * knockback);
            }
            player.push(-look.x * knockback, Math.abs(-look.y + 0.2F) * knockback, -look.z * knockback);
            playSound(level, player, SoundEvents.ARROW_SHOOT);
        }
    }

    /** {@code drawParticleStreamTo}: {@code townaura} (mycelium) every 0.1 m from beside the hand to the hit. */
    private static void drawParticleStreamTo(Player player, Level level, Vec3 target) {
        if (!level.isClientSide()) {
            return;
        }
        Vec3 start = muzzle(player, 0.0);
        double dx = target.x - start.x;
        double dy = target.y - start.y;
        double dz = target.z - start.z;
        double ratio = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double cx = start.x;
        double cy = start.y;
        double cz = start.z;
        for (int i = 0; i < 2000 && Math.abs(cx - target.x) > Math.abs(dx / ratio); i++) {
            level.addParticle(ParticleTypes.MYCELIUM, cx, cy, cz, 0.0, 0.0, 0.0);
            cx += dx * 0.1 / ratio;
            cy += dy * 0.1 / ratio;
            cz += dz * 0.1 / ratio;
        }
    }

    /**
     * Start of the railgun trail and the plasma bolt: 1.3 + {@code extra} along the look, 0.2 down and 0.3 to the
     * right of the eyes.
     */
    public static Vec3 muzzle(Player player, double extra) {
        Vec3 direction = player.getLookAngle();
        Vec3 horz = new Vec3(direction.x, 0.0, direction.z).normalize();
        double xoffset = 1.3F + extra;
        double yoffset = -0.2;
        double zoffset = 0.3F;
        return new Vec3(
                player.getX() + direction.x * xoffset - direction.y * horz.x * yoffset - horz.z * zoffset,
                player.getEyeY() + direction.y * xoffset + (1.0 - Math.abs(direction.y)) * yoffset,
                player.getZ() + direction.z * xoffset - direction.y * horz.z * yoffset + horz.x * zoffset);
    }

    /** {@code onPlayerStoppedUsing}: at least 10 ticks; the bolt fires only if the pool pays the whole charge. */
    public static void releasePlasma(Player player, Level level, ItemStack stack, int timeLeft) {
        if (level.isClientSide() || !ModularItems.hasActiveModule(stack, MpsModules.PLASMA_CANNON)) {
            return;
        }
        int chargeTicks = Math.max(USE_DURATION - timeLeft, 10);
        double energyConsumption = ModularItems.computeProperty(stack, "Plasma Energy Per Tick") * chargeTicks;
        if (MpsEnergy.getPlayerEnergy(player) > energyConsumption) {
            MpsEnergy.drainPlayerEnergy(player, energyConsumption);
            double explosiveness = ModularItems.computeProperty(stack, "Plasma Explosiveness");
            double damagingness = ModularItems.computeProperty(stack, "Plasma Damage At Full Charge");
            level.addFreshEntity(new PlasmaBoltEntity(level, player, explosiveness, damagingness, chargeTicks));
        }
    }

    /** {@code PortableCraftingGui}: a 3×3 crafting grid that stays open anywhere. */
    static void inPlaceAssembler(Player player, Level level) {
        if (level.isClientSide()) {
            return;
        }
        player.openMenu(new SimpleMenuProvider((id, inventory, owner) ->
                new CraftingMenu(id, inventory, ContainerLevelAccess.create(level, owner.blockPosition())) {
                    @Override
                    public boolean stillValid(Player viewer) {
                        return true;
                    }
                }, Component.translatable("container.crafting")));
    }

    /**
     * {@code MusePlayerUtils.doCustomRayTrace}: the nearest collidable entity (an entity around the eyes wins with
     * distance 0) or the first block including liquids, whichever is closer.
     */
    public static HitResult rayTrace(Level level, Player player, double reach) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(reach));
        double border = 1.1 * reach;
        AABB box = player.getBoundingBox().inflate(border);
        EntityHitResult pickedEntity = null;
        double closestEntity = reach;
        List<Entity> entities = level.getEntities(player, box);
        for (Entity entity : entities) {
            if (!entity.isPickable()) {
                continue;
            }
            float pickBorder = entity.getPickRadius();
            AABB aabb = entity.getBoundingBox().inflate(pickBorder);
            var hit = aabb.clip(start, end);
            if (aabb.contains(start)) {
                if (0.0 < closestEntity || closestEntity == 0.0) {
                    pickedEntity = new EntityHitResult(entity, hit.orElse(start));
                    closestEntity = 0.0;
                }
            } else if (hit.isPresent()) {
                double distance = start.distanceTo(hit.get());
                if (distance < closestEntity || closestEntity == 0.0) {
                    pickedEntity = new EntityHitResult(entity, hit.get());
                    closestEntity = distance;
                }
            }
        }
        BlockHitResult pickedBlock = level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE,
                ClipContext.Fluid.ANY, player));
        if (pickedBlock.getType() == HitResult.Type.MISS) {
            return pickedEntity;
        }
        if (pickedEntity == null) {
            return pickedBlock;
        }
        return closestEntity < pickedBlock.getLocation().distanceTo(start) ? pickedEntity : pickedBlock;
    }

    /** {@code playSoundAtEntity(player, name, 0.5, 0.4 / (random × 0.4 + 0.8))}. */
    private static void playSound(Level level, Player player, SoundEvent sound) {
        if (!level.isClientSide()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 0.5F,
                    0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        }
    }

    /** {@code ClientTickHandler.clampMode}: wraps the wheel selection into the mode list. */
    public static int clampMode(int selection, int modesSize) {
        return selection > 0 ? selection % modesSize : (selection + modesSize * -selection) % modesSize;
    }

    /** {@code MusePacketModeChangeRequest}: hotbar slot 0–8, only to an installed right-click module. */
    public static void changeMode(Player player, int slot, String mode) {
        if (slot < 0 || slot > 8) {
            return;
        }
        ItemStack stack = player.getInventory().getItem(slot);
        if (ModularItems.isModular(stack) && ModularItems.getModes(stack).contains(mode)) {
            stack.set(MpsDataComponents.MODE.get(), mode);
        }
    }
}
