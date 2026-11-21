package dev.craft.gravisuite.item;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.gravisuite.GsDataComponents;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.item.armor.ElectricArmorItem;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * GraviSuite {@code ItemGraviChestPlate} with {@code GraviChestPlate*Proxy} and the chest part of
 * {@code ServerTickHandler} (spec 28 §2, §5): creative-like flight for 416 EU per tick, protection like the
 * QuantumSuit chestplate; 10 000 000 EU, transfer 20 000, provides energy. Spec 37 §3.5: no boost, flight only as fast
 * as sprinting ({@link #limitsFlyingSpeed}), in return the gravitation magnet ({@link #switchMagnet}).
 */
@EventBusSubscriber(modid = Craft.MODID)
public class GraviChestplateItem extends ElectricArmorItem {
    public static final int MAX_CHARGE = 10000000;
    public static final int MIN_CHARGE = 80000;
    public static final int ENERGY_PER_TICK = 416;
    /** Spec 37 §3.5: the magnet pulls items and experience orbs within this radius. */
    public static final double MAGNET_RADIUS = 8.0;
    /** Spec 37 §3.5: EU per pulled item stack; experience orbs are free. */
    public static final int MAGNET_COST = 50;
    /** {@code ServerTickHandler}: 262 in 1.4.7 (256 + 6), here the build height + 6. */
    public static final int HEIGHT_ABOVE_BUILD_LIMIT = 6;

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Craft.MODID);
    /** The {@code ServerTickHandler} maps, per player and not saved (the pack keyed them by player object). */
    private static final Supplier<AttachmentType<FlightState>> FLIGHT_STATE =
            ATTACHMENT_TYPES.register("gravi_flight_state", () -> AttachmentType.builder(FlightState::new).build());

    public GraviChestplateItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.CHESTPLATE, properties, MAX_CHARGE, 20000, 2);
    }

    public static boolean isFlyActive(ItemStack stack) {
        return stack.getOrDefault(GsDataComponents.FLY_ACTIVE.get(), false);
    }

    private static void setFlyActive(ItemStack stack, boolean active) {
        stack.set(GsDataComponents.FLY_ACTIVE.get(), active);
    }

    public static boolean isMagnetActive(ItemStack stack) {
        return stack.getOrDefault(GsDataComponents.MAGNET_ACTIVE.get(), false);
    }

    /** Spec 37 §3.5: mode key while holding boost switches the magnet. */
    public static void switchMagnet(Player player, ItemStack stack) {
        boolean on = !isMagnetActive(stack);
        stack.set(GsDataComponents.MAGNET_ACTIVE.get(), on);
        player.sendSystemMessage(Component.translatable(on
                ? "message.craft.gravisuite.magnet_on" : "message.craft.gravisuite.magnet_off"));
    }

    /** Spec 37 §3.5: flight of a worn, active plate outside creative mode is capped at sprinting speed. */
    public static boolean limitsFlyingSpeed(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        return chest.getItem() instanceof GraviChestplateItem && isFlyActive(chest)
                && !player.getAbilities().instabuild;
    }

    /** Fly key ({@code switchFlyModeServer}). */
    public static void switchFlyMode(Player player, ItemStack stack) {
        Abilities abilities = player.getAbilities();
        FlightState state = player.getData(FLIGHT_STATE);
        if (isFlyActive(stack)) {
            if (!abilities.instabuild) {
                abilities.mayfly = false;
                abilities.flying = false;
            }
            player.sendSystemMessage(Component.translatable("message.craft.gravisuite.gravi_off"));
            state.flyActiveByMod = false;
            setFlyActive(stack, false);
        } else if (ElectricItems.getCharge(stack) < MIN_CHARGE && !abilities.instabuild) {
            player.sendSystemMessage(Component.translatable("message.craft.gravisuite.gravi_no_energy"));
        } else {
            player.sendSystemMessage(Component.translatable("message.craft.gravisuite.gravi_on"));
            abilities.mayfly = true;
            abilities.flying = true;
            state.flyActiveByMod = true;
            setFlyActive(stack, true);
        }
        syncAbilities(player);
    }

    /**
     * World load ({@code firstLoadServer}): an active engine is switched off and on again, so the minimum charge is
     * checked anew. Original error: if that fails, the abilities saved with the player keep {@code mayfly}.
     */
    public static void firstLoad(Player player, ItemStack stack) {
        if (isFlyActive(stack)) {
            setFlyActive(stack, false);
            switchFlyMode(player, stack);
        }
    }

    /** {@code ServerTickHandler.tickStart} for the chest slot (server side). */
    public static void serverTick(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        Abilities abilities = player.getAbilities();
        FlightState state = player.getData(FLIGHT_STATE);
        if (chest.getItem() instanceof GraviChestplateItem) {
            if (abilities.instabuild && !state.lastCreative) {
                state.lastCreative = true;
            } else if (!abilities.instabuild && state.lastCreative && state.flyActiveByMod) {
                abilities.mayfly = true;
                abilities.flying = true;
                state.lastCreative = false;
            }
            tickWorn(player, chest, state);
            if (isMagnetActive(chest)) {
                pullItems(player, chest);
            }
            double maxY = player.level().getMaxBuildHeight() + HEIGHT_ABOVE_BUILD_LIMIT;
            if (player.getY() > maxY && !abilities.instabuild) {
                player.teleportTo(player.getX(), maxY, player.getZ());
            }
        } else if (state.flyActiveByMod) {
            abilities.mayfly = false;
            abilities.flying = false;
            state.flyActiveByMod = false;
            state.lastUndressed = true;
            syncAbilities(player);
        }
    }

    /**
     * Spec 37 §3.5: moves items (50 EU each, only while the inventory takes them) and experience orbs within
     * {@link #MAGNET_RADIUS} onto the player, who then picks them up as usual.
     */
    private static void pullItems(Player player, ItemStack stack) {
        boolean creative = player.getAbilities().instabuild;
        AABB area = player.getBoundingBox().inflate(MAGNET_RADIUS);
        double maxDistance = MAGNET_RADIUS * MAGNET_RADIUS;
        for (ItemEntity item : player.level().getEntitiesOfClass(ItemEntity.class, area)) {
            double distance = item.distanceToSqr(player);
            if (item.hasPickUpDelay() || distance > maxDistance || distance < 1.0
                    || player.getInventory().getFreeSlot() < 0
                    && player.getInventory().getSlotWithRemainingSpace(item.getItem()) < 0) {
                continue;
            }
            if (!creative) {
                if (ElectricItems.getCharge(stack) < MAGNET_COST) {
                    return;
                }
                use(stack, MAGNET_COST);
            }
            item.setPos(player.getX(), player.getY(), player.getZ());
        }
        for (ExperienceOrb orb : player.level().getEntitiesOfClass(ExperienceOrb.class, area)) {
            if (orb.distanceToSqr(player) <= maxDistance) {
                orb.setPos(player.getX(), player.getY(), player.getZ());
            }
        }
    }

    /** {@code onTickServer}. */
    private static void tickWorn(Player player, ItemStack stack, FlightState state) {
        if (state.lastUndressed) {
            setFlyActive(stack, false);
            state.lastUndressed = false;
        }
        if (isFlyActive(stack)) {
            Abilities abilities = player.getAbilities();
            boolean creative = abilities.instabuild;
            int charge = ElectricItems.getCharge(stack);
            // Deviation (user, 2026-10-29): no consumption on the ground, the pack drains there too.
            if (!creative && !player.onGround()) {
                if (charge < ENERGY_PER_TICK) {
                    player.sendSystemMessage(Component.translatable("message.craft.gravisuite.gravi_depleted"));
                    switchFlyMode(player, stack);
                } else {
                    use(stack, ENERGY_PER_TICK);
                }
            }
            player.resetFallDistance();
        }
        syncAbilities(player);
    }

    /** Client side of {@code onTickClient}: height limit (the boost is gone, spec 37 §3.5). */
    @Override
    protected void onArmorTick(Level level, Player player, ItemStack stack) {
        if (!level.isClientSide() || !player.isLocalPlayer()) {
            return;
        }
        double maxY = level.getMaxBuildHeight() + HEIGHT_ABOVE_BUILD_LIMIT;
        if (player.getY() > maxY && !player.getAbilities().instabuild) {
            player.setPos(player.getX(), maxY, player.getZ());
        }
    }

    /** Discharge at tier 3 without transfer limit ({@code ElectricItem.discharge(stack, n, 3, true, false)}). */
    private static void use(ItemStack stack, int amount) {
        ElectricItems.discharge(stack, amount, 3, true, false);
    }

    private static void syncAbilities(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.onUpdateAbilities();
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!event.getEntity().level().isClientSide()) {
            serverTick(event.getEntity());
        }
    }

    /** Replaces the client's {@code worldLoad} packet: the server knows when a player joins. */
    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof GraviChestplateItem) {
            firstLoad(player, chest);
        }
    }

    @Override
    public boolean canProvideEnergy() {
        return true;
    }

    @Override
    public double getDamageAbsorptionRatio() {
        return 1.1;
    }

    @Override
    public int getEnergyPerDamage() {
        return 900;
    }

    /** Always 9, also when empty. */
    @Override
    public int getArmorDisplay(ItemStack stack) {
        return 9;
    }

    /** {@code isFlyActiveByMod}, {@code lastUndressed}, {@code isLastCreativeState}. */
    private static final class FlightState {
        boolean flyActiveByMod;
        boolean lastUndressed;
        boolean lastCreative;
    }
}
