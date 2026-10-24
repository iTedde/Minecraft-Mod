package dev.craft.gravisuite.item;

import dev.craft.gravisuite.GsDataComponents;
import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.item.armor.ElectricArmorItem;
import dev.craft.ic2.item.armor.JetpackItem;
import dev.craft.ic2.keyboard.Ic2Keyboard;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * GraviSuite {@code ItemAdvancedJetPack} (spec 28 §4): electric jetpack with an engine switch (fly key), hover mode
 * (mode key while jumping) and boost; 1 000 000 EU, 12 EU per tick, boost ×5; provides energy like a LapPack.
 */
public class AdvancedJetpackItem extends ElectricArmorItem {
    public static final int MAX_CHARGE = 1000000;
    public static final int ENERGY_PER_TICK = 12;
    public static final int BOOST_MULTIPLIER = 5;

    public AdvancedJetpackItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.CHESTPLATE, properties, MAX_CHARGE, 1000, 2);
    }

    public static boolean isEngineOn(ItemStack stack) {
        return stack.getOrDefault(GsDataComponents.FLY_ACTIVE.get(), false);
    }

    public static boolean isHoverMode(ItemStack stack) {
        return stack.getOrDefault(Ic2DataComponents.HOVER_MODE.get(), false);
    }

    /** Fly key ({@code switchFlyState}): engine on/off. */
    public static void switchEngine(Player player, ItemStack stack) {
        boolean on = !isEngineOn(stack);
        stack.set(GsDataComponents.FLY_ACTIVE.get(), on);
        player.sendSystemMessage(Component.translatable(on
                ? "message.craft.gravisuite.jetpack_on" : "message.craft.gravisuite.jetpack_off"));
    }

    /** Mode key pressed while jumping ({@code switchWorkMode}); the toggle timer is set but never evaluated. */
    public static void switchHoverMode(Player player, ItemStack stack) {
        boolean hover = !isHoverMode(stack);
        stack.set(Ic2DataComponents.HOVER_MODE.get(), hover);
        stack.set(Ic2DataComponents.TOGGLE_TIMER.get(), 10);
        player.sendSystemMessage(Component.translatable(hover
                ? "message.craft.gravisuite.hover_enabled" : "message.craft.gravisuite.hover_disabled"));
    }

    @Override
    protected void onArmorTick(Level level, Player player, ItemStack stack) {
        boolean hover = isHoverMode(stack);
        boolean used = false;
        if ((Ic2Keyboard.isJumpKeyDown(player) || hover && player.getDeltaMovement().y < -0.35F)
                && isEngineOn(stack)) {
            used = useJetpack(player, stack, hover);
        }
        if (!level.isClientSide()) {
            int toggleTimer = stack.getOrDefault(Ic2DataComponents.TOGGLE_TIMER.get(), 0);
            if (toggleTimer > 1) {
                stack.set(Ic2DataComponents.TOGGLE_TIMER.get(), toggleTimer - 1);
            } else if (toggleTimer == 1) {
                stack.remove(Ic2DataComponents.TOGGLE_TIMER.get());
            }
        }
        // The GraviSuite loop sound is the same file as IC2's: the IC2 handler plays it.
        if (used && level.isClientSide() && player.isLocalPlayer()) {
            JetpackItem.clientJetpackUsed = true;
        }
    }

    public boolean useJetpack(Player player, ItemStack stack, boolean hoverMode) {
        boolean creative = player.getAbilities().instabuild;
        int charge = ElectricItems.getCharge(stack);
        if (charge < ENERGY_PER_TICK && !creative) {
            return false;
        }
        float power = 1.0F;
        // Original error: below 0.1 % the power drops from 1.0 to charge / 50 000 (at most 0.02).
        if ((float) charge / MAX_CHARGE <= 0.001F) {
            power *= charge / (MAX_CHARGE / 20.0F);
        }
        if (creative) {
            power = 1.0F;
        }
        if (Ic2Keyboard.isForwardKeyDown(player)) {
            float forwardPower = power * (hoverMode ? 0.65F : 0.3F) * 2.0F;
            float boost = 0.0F;
            if (Ic2Keyboard.isBoostKeyDown(player) && (charge > ENERGY_PER_TICK * BOOST_MULTIPLIER || creative)) {
                boost = hoverMode ? 0.07F : 0.09F;
            }
            if (forwardPower > 0.0F) {
                player.moveRelative(0.02F + boost, new Vec3(0.0, 0.0, 0.4F * forwardPower + boost));
                if (boost > 0.0F && !creative) {
                    use(stack, ENERGY_PER_TICK * BOOST_MULTIPLIER);
                }
            }
        }
        int maxHeight = player.level().getMaxBuildHeight();
        double y = player.getY();
        if (y > maxHeight - 25) {
            if (y > maxHeight) {
                y = maxHeight;
            }
            power = (float) (power * ((maxHeight - y) / 25.0));
        }
        Vec3 motion = player.getDeltaMovement();
        double prevMotion = motion.y;
        double motionY = Math.min(motion.y + power * 0.2F, 0.6F);
        if (hoverMode) {
            float maxHoverY = Ic2Keyboard.isJumpKeyDown(player) ? 0.1F : -0.1F;
            if (motionY > maxHoverY) {
                motionY = maxHoverY;
                if (prevMotion > motionY) {
                    motionY = prevMotion;
                }
            }
        }
        player.setDeltaMovement(motion.x, motionY, motion.z);
        if (!creative) {
            use(stack, ENERGY_PER_TICK);
        }
        player.resetFallDistance();
        player.walkDist = 0.0F;
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.aboveGroundTickCount = 0;
        }
        return true;
    }

    /** Discharge without transfer limit and without recharging from other armor. */
    protected static void use(ItemStack stack, int amount) {
        ElectricItems.discharge(stack, amount, Integer.MAX_VALUE, true, false);
    }

    @Override
    public boolean canProvideEnergy() {
        return true;
    }

    @Override
    public double getDamageAbsorptionRatio() {
        return 0.0;
    }

    @Override
    public int getEnergyPerDamage() {
        return 0;
    }
}
