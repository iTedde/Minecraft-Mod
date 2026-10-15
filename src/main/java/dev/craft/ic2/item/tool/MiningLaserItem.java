package dev.craft.ic2.item.tool;

import java.util.function.Supplier;

import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.keyboard.Ic2Keyboard;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Mining laser (IC2 {@code ItemToolMiningLaser}): seven firing modes, switched with the mode key. */
public class MiningLaserItem extends ElectricToolItem {
    public enum Mode {
        MINING("mining", 1250, Ic2Sounds.MINING_LASER),
        LOW_FOCUS("low_focus", 100, Ic2Sounds.MINING_LASER_LOW_FOCUS),
        LONG_RANGE("long_range", 5000, Ic2Sounds.MINING_LASER_LONG_RANGE),
        /** Fires only when right clicking a block, for {@link #HORIZONTAL_COST}; in the air it costs nothing. */
        HORIZONTAL("horizontal", 0, Ic2Sounds.MINING_LASER),
        SUPER_HEAT("super_heat", 2500, Ic2Sounds.MINING_LASER),
        SCATTER("scatter", 10000, Ic2Sounds.MINING_LASER_SCATTER),
        EXPLOSIVE("explosive", 5000, Ic2Sounds.MINING_LASER_EXPLOSIVE);

        private final String name;
        private final int cost;
        private final Supplier<SoundEvent> sound;

        Mode(String name, int cost, Supplier<SoundEvent> sound) {
            this.name = name;
            this.cost = cost;
            this.sound = sound;
        }

        public Component displayName() {
            return Component.translatable("message.craft.mining_laser.mode." + name);
        }
    }

    public static final int HORIZONTAL_COST = 3000;

    public MiningLaserItem(Properties properties) {
        super(properties, 200000, 120, 2, 100, 6.0F, null);
    }

    public static Mode getMode(ItemStack stack) {
        Mode[] modes = Mode.values();
        return modes[stack.getOrDefault(Ic2DataComponents.LASER_MODE.get(), 0) % modes.length];
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.pass(stack);
        }
        Mode mode = getMode(stack);
        if (Ic2Keyboard.isModeSwitchKeyDown(player)) {
            mode = Mode.values()[(mode.ordinal() + 1) % Mode.values().length];
            stack.set(Ic2DataComponents.LASER_MODE.get(), mode.ordinal());
            player.sendSystemMessage(Component.translatable("message.craft.mining_laser.mode", mode.displayName()));
            return InteractionResultHolder.pass(stack);
        }
        if (!ElectricItems.use(stack, mode.cost, player)) {
            return InteractionResultHolder.pass(stack);
        }
        float yaw = player.getYRot();
        float pitch = player.getXRot();
        double y = MiningLaserEntity.startY(player);
        switch (mode) {
            case MINING -> shoot(level, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false,
                    yaw, pitch, y);
            case LOW_FOCUS -> shoot(level, player, 4.0F, 5.0F, 1, false, false, yaw, pitch, y);
            case LONG_RANGE -> shoot(level, player, Float.POSITIVE_INFINITY, 20.0F, Integer.MAX_VALUE, false, false,
                    yaw, pitch, y);
            case HORIZONTAL -> {
                return InteractionResultHolder.pass(stack);
            }
            case SUPER_HEAT -> shoot(level, player, Float.POSITIVE_INFINITY, 8.0F, Integer.MAX_VALUE, false, true,
                    yaw, pitch, y);
            case SCATTER -> {
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        shoot(level, player, Float.POSITIVE_INFINITY, 12.0F, Integer.MAX_VALUE, false, false,
                                yaw + 20.0F * x, pitch + 20.0F * z, y);
                    }
                }
            }
            case EXPLOSIVE -> shoot(level, player, Float.POSITIVE_INFINITY, 12.0F, Integer.MAX_VALUE, true, false,
                    yaw, pitch, y);
        }
        playSound(level, player, mode);
        return InteractionResultHolder.pass(stack);
    }

    /** Horizontal mode: a level shot at the height of the clicked block; the normal right click follows anyway. */
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level.isClientSide() || player == null || Ic2Keyboard.isModeSwitchKeyDown(player)
                || getMode(stack) != Mode.HORIZONTAL) {
            return InteractionResult.PASS;
        }
        double y = context.getClickedPos().getY() + 0.5;
        if (Math.abs(MiningLaserEntity.startY(player) - y) < 1.5) {
            if (ElectricItems.use(stack, HORIZONTAL_COST, player)) {
                shoot(level, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false,
                        player.getYRot(), 0.0F, y);
                playSound(level, player, Mode.HORIZONTAL);
            }
        } else {
            player.sendSystemMessage(Component.translatable("message.craft.mining_laser.too_steep"));
        }
        return InteractionResult.PASS;
    }

    private static void shoot(Level level, Player player, float range, float power, int blockBreaks,
                              boolean explosive, boolean smelt, double yaw, double pitch, double y) {
        level.addFreshEntity(new MiningLaserEntity(level, player, range, power, blockBreaks, explosive, smelt,
                yaw, pitch, y));
    }

    private static void playSound(Level level, Player player, Mode mode) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), mode.sound.get(), SoundSource.PLAYERS,
                1.0F, 1.0F);
    }
}
