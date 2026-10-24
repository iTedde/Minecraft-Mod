package dev.craft.lightshield;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dev.craft.ic2.item.ElectricItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Server side bookkeeping of the light blocks that follow light shield holders (spec 30 §3). */
public final class ShieldLights {
    private record Light(ResourceKey<Level> dimension, BlockPos pos) {
    }

    private static final Map<UUID, Light> LIGHTS = new HashMap<>();

    private ShieldLights() {
    }

    /** The lit shield {@code player} holds, main hand first, or an empty stack. */
    public static ItemStack litShield(Player player) {
        if (player.isSpectator()) {
            return ItemStack.EMPTY;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (ElectricLightShieldItem.isLit(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /** Server player tick: pays for the light and moves it to the player's eye (or feet) block. */
    public static void tick(Player player) {
        Level level = player.level();
        ItemStack shield = litShield(player);
        if (!shield.isEmpty() && level.getGameTime() % ElectricLightShieldItem.LIGHT_INTERVAL == 0) {
            ElectricItems.use(shield, 1, player);
        }
        BlockPos target = ElectricLightShieldItem.isLit(shield) ? findTarget(level, player) : null;
        Light old = LIGHTS.get(player.getUUID());
        if (target != null && old != null && old.dimension() == level.dimension() && old.pos().equals(target)
                && level.getBlockState(target).is(LsBlocks.SHIELD_LIGHT)) {
            return;
        }
        remove(player);
        if (target != null) {
            place(level, target);
            LIGHTS.put(player.getUUID(), new Light(level.dimension(), target));
        }
    }

    /** Eye block, else feet block, if it is air, water source or already a shield light. */
    @Nullable
    public static BlockPos findTarget(Level level, Player player) {
        BlockPos eye = BlockPos.containing(player.getEyePosition());
        if (canHost(level.getBlockState(eye))) {
            return eye;
        }
        BlockPos feet = player.blockPosition();
        return canHost(level.getBlockState(feet)) ? feet : null;
    }

    private static boolean canHost(BlockState state) {
        return state.isAir() || state.is(LsBlocks.SHIELD_LIGHT)
                || state.is(Blocks.WATER) && state.getFluidState().isSource();
    }

    private static void place(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(LsBlocks.SHIELD_LIGHT)) {
            level.setBlock(pos, LsBlocks.SHIELD_LIGHT.get().defaultBlockState()
                    .setValue(ShieldLightBlock.WATERLOGGED, state.is(Blocks.WATER)), Block.UPDATE_ALL);
        }
    }

    /** Drops the light of {@code player}, removing the block unless another holder shares it. */
    public static void remove(Player player) {
        Light light = LIGHTS.remove(player.getUUID());
        MinecraftServer server = player.getServer();
        if (light == null || server == null || isClaimed(light.dimension(), light.pos())) {
            return;
        }
        ServerLevel level = server.getLevel(light.dimension());
        if (level != null && level.isLoaded(light.pos())) {
            BlockState state = level.getBlockState(light.pos());
            if (state.is(LsBlocks.SHIELD_LIGHT)) {
                ShieldLightBlock.clear(level, light.pos(), state);
            }
        }
    }

    public static boolean isClaimed(Level level, BlockPos pos) {
        return isClaimed(level.dimension(), pos);
    }

    private static boolean isClaimed(ResourceKey<Level> dimension, BlockPos pos) {
        for (Light light : LIGHTS.values()) {
            if (light.dimension() == dimension && light.pos().equals(pos)) {
                return true;
            }
        }
        return false;
    }

    /** Position of the light {@code player} keeps, for tests. */
    @Nullable
    public static BlockPos lightOf(Player player) {
        Light light = LIGHTS.get(player.getUUID());
        return light == null ? null : light.pos();
    }

    public static void clear() {
        LIGHTS.clear();
    }
}
