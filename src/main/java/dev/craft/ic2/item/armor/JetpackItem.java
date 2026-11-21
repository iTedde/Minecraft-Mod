package dev.craft.ic2.item.armor;

import dev.craft.core.MovementCap;
import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.keyboard.Ic2Keyboard;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * IC2 {@code ItemArmorJetpack}: fuel jetpack whose fuel is the item damage (max damage 18 002). Runs on both sides
 * like IC2; the client moves its own player.
 */
public class JetpackItem extends Ic2ArmorItem {
    public static final int MAX_DAMAGE = 18002;

    /** Set by the local player's jetpack tick, read and reset once per client tick for the loop sound. */
    public static boolean clientJetpackUsed;

    private final boolean electric;

    public JetpackItem(Properties properties) {
        this(properties.durability(MAX_DAMAGE), false);
    }

    protected JetpackItem(Properties properties, boolean electric) {
        super(Ic2ArmorMaterials.JETPACK, Type.CHESTPLATE, properties.stacksTo(1));
        this.electric = electric;
    }

    /** The creative-tab jetpack (damage 1, full). */
    public ItemStack createFull() {
        ItemStack stack = new ItemStack(this);
        stack.setDamageValue(1);
        return stack;
    }

    public int getCharge(ItemStack stack) {
        int charge = getMaxCharge(stack) - stack.getDamageValue() - 1;
        return Math.max(charge, 0);
    }

    public int getMaxCharge(ItemStack stack) {
        return stack.getMaxDamage() - 2;
    }

    /** IC2 sets {@code damage = 1 + maxDamage - newCharge}, so each use costs 4 more than {@code amount}. */
    public void use(ItemStack stack, int amount) {
        int newCharge = Math.max(getCharge(stack) - amount, 0);
        stack.setDamageValue(1 + stack.getMaxDamage() - newCharge);
    }

    public boolean useJetpack(Player player, ItemStack jetpack, boolean hoverMode) {
        if (getCharge(jetpack) == 0) {
            return false;
        }
        float power = 1.0F;
        float dropPercentage = 0.2F;
        if (electric) {
            power = 0.7F;
            dropPercentage = 0.05F;
        }
        if ((float) getCharge(jetpack) / getMaxCharge(jetpack) <= dropPercentage) {
            power *= getCharge(jetpack) / (getMaxCharge(jetpack) * dropPercentage);
        }
        if (Ic2Keyboard.isForwardKeyDown(player)) {
            float retruster = hoverMode ? 0.5F : 0.15F;
            if (electric) {
                retruster += 0.15F;
            }
            float forwardPower = power * retruster * 2.0F;
            if (forwardPower > 0.0F) {
                player.moveRelative(0.02F, new Vec3(0.0, 0.0, 0.4F * forwardPower));
            }
        }
        int worldHeight = player.level().getMaxBuildHeight();
        int maxFlightHeight = electric ? (int) (worldHeight / 1.28F) : worldHeight;
        double y = player.getY();
        if (y > maxFlightHeight - 25) {
            if (y > maxFlightHeight) {
                y = maxFlightHeight;
            }
            power = (float) (power * ((maxFlightHeight - y) / 25.0));
        }
        Vec3 motion = player.getDeltaMovement();
        double prevMotion = motion.y;
        double motionY = Math.min(motion.y + power * 0.2F, 0.6F);
        if (hoverMode) {
            float maxHoverY = electric && Ic2Keyboard.isJumpKeyDown(player) ? 0.1F : -0.1F;
            if (motionY > maxHoverY) {
                motionY = maxHoverY;
                if (prevMotion > motionY) {
                    motionY = prevMotion;
                }
            }
        }
        player.setDeltaMovement(MovementCap.clampHorizontal(new Vec3(motion.x, motionY, motion.z)));
        int consume = hoverMode ? 6 : 9;
        if (electric) {
            consume -= 2;
        }
        use(jetpack, consume);
        player.resetFallDistance();
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.aboveGroundTickCount = 0;
        }
        return true;
    }

    @Override
    protected void onArmorTick(Level level, Player player, ItemStack stack) {
        boolean hoverMode = stack.getOrDefault(Ic2DataComponents.HOVER_MODE.get(), false);
        int toggleTimer = stack.getOrDefault(Ic2DataComponents.TOGGLE_TIMER.get(), 0);
        boolean jetpackUsed = false;
        if (Ic2Keyboard.isJumpKeyDown(player) && Ic2Keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0) {
            toggleTimer = 10;
            hoverMode = !hoverMode;
            if (!level.isClientSide()) {
                stack.set(Ic2DataComponents.HOVER_MODE.get(), hoverMode);
                player.sendSystemMessage(Component.translatable(hoverMode
                        ? "message.craft.jetpack.hover_enabled" : "message.craft.jetpack.hover_disabled"));
            }
        }
        if (Ic2Keyboard.isJumpKeyDown(player) || hoverMode && player.getDeltaMovement().y < -0.35F) {
            jetpackUsed = useJetpack(player, stack, hoverMode);
        }
        if (!level.isClientSide() && toggleTimer > 0) {
            toggleTimer--;
            if (toggleTimer > 0) {
                stack.set(Ic2DataComponents.TOGGLE_TIMER.get(), toggleTimer);
            } else {
                stack.remove(Ic2DataComponents.TOGGLE_TIMER.get());
            }
        }
        if (jetpackUsed && level.isClientSide() && player.isLocalPlayer()) {
            clientJetpackUsed = true;
        }
    }
}
