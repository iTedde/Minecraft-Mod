package dev.craft.lightshield;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import dev.craft.ic2.item.ElectricItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Who shines a light shield, what it costs and where its light goes (spec 51 §3): a cone of invisible light blocks in
 * the looking direction plus a torch-like glow at the holder, placed by the server and kept in a list per player so
 * every light block can tell whether it is still wanted.
 */
public final class ShieldLights {
    /** Glow around the holder, like a torch. */
    public static final int GLOW_LEVEL = 14;
    public static final int MAX_LIGHTS = 160;
    /** Blocks between neighbouring rays at the end of the cone. */
    public static final double RAY_SPACING = 6.0;
    public static final int UPDATE_INTERVAL = 5;
    public static final int IDLE_UPDATE_INTERVAL = 20;

    private static final Map<UUID, Lights> LIGHTS = new HashMap<>();

    /** Light blocks one player currently wants, with the pose they were computed for. */
    private static final class Lights {
        ResourceKey<Level> dimension;
        Map<BlockPos, Integer> blocks = Map.of();
        Vec3 eye = Vec3.ZERO;
        float yRot;
        float xRot;
        Flashlight setting;
        long computedAt;
    }

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

    /** Server player tick: pays for the light and moves the light blocks. */
    public static void tick(Player player) {
        ItemStack shield = litShield(player);
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (shield.isEmpty()) {
            remove(level.getServer(), player.getUUID());
            return;
        }
        Flashlight setting = ElectricLightShieldItem.flashlight(shield);
        if (level.getGameTime() % Flashlight.COST_INTERVAL == 0) {
            ElectricItems.use(shield, setting.euPerInterval(), player);
        }
        Lights lights = LIGHTS.computeIfAbsent(player.getUUID(), id -> new Lights());
        if (lights.dimension != null && lights.dimension != level.dimension()) {
            remove(level.getServer(), player.getUUID());
            lights = LIGHTS.computeIfAbsent(player.getUUID(), id -> new Lights());
        }
        long now = level.getGameTime();
        Vec3 eye = player.getEyePosition();
        boolean changed = !setting.equals(lights.setting) || lights.dimension == null
                || eye.distanceToSqr(lights.eye) >= 0.25 * 0.25
                || Math.abs(Mth.wrapDegrees(player.getYRot() - lights.yRot)) >= 2.0F
                || Math.abs(player.getXRot() - lights.xRot) >= 2.0F;
        long since = now - lights.computedAt;
        if (lights.dimension != null && (since < UPDATE_INTERVAL || !changed && since < IDLE_UPDATE_INTERVAL)) {
            return;
        }
        Map<BlockPos, Integer> wanted = compute(level, eye, player.getLookAngle(), setting);
        Map<BlockPos, Integer> old = lights.blocks;
        lights.dimension = level.dimension();
        lights.blocks = wanted;
        lights.eye = eye;
        lights.yRot = player.getYRot();
        lights.xRot = player.getXRot();
        lights.setting = setting;
        lights.computedAt = now;
        Set<BlockPos> touched = new HashSet<>(old.keySet());
        touched.addAll(wanted.keySet());
        for (BlockPos pos : touched) {
            if (!Integer.valueOf(old.getOrDefault(pos, 0)).equals(wanted.get(pos))) {
                refresh(level, pos);
            }
        }
    }

    /** Takes all of a player's lights away (put away, switched off, logged out, changed dimension). */
    public static void remove(MinecraftServer server, UUID player) {
        Lights lights = LIGHTS.remove(player);
        if (lights == null || lights.dimension == null || server == null) {
            return;
        }
        ServerLevel level = server.getLevel(lights.dimension);
        if (level != null) {
            lights.blocks.keySet().forEach(pos -> refresh(level, pos));
        }
    }

    /** Server stopping: remove every light so none stays in the saved world. */
    public static void removeAll(MinecraftServer server) {
        for (Iterator<UUID> it = Set.copyOf(LIGHTS.keySet()).iterator(); it.hasNext();) {
            remove(server, it.next());
        }
        LIGHTS.clear();
    }

    /** Brightest level any player wants at {@code pos}, 0 for none. */
    public static int wantedLevel(Level level, BlockPos pos) {
        int best = 0;
        for (Lights lights : LIGHTS.values()) {
            if (lights.dimension == level.dimension()) {
                best = Math.max(best, lights.blocks.getOrDefault(pos, 0));
            }
        }
        return best;
    }

    /** Sets the light block at {@code pos} to what the players want there, or removes it. */
    private static void refresh(ServerLevel level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        int wanted = wantedLevel(level, pos);
        if (state.is(LsBlocks.SHIELD_LIGHT.get())) {
            if (wanted == 0) {
                ShieldLightBlock.clear(level, pos, state);
            } else if (state.getValue(ShieldLightBlock.LEVEL) != wanted) {
                level.setBlock(pos, state.setValue(ShieldLightBlock.LEVEL, wanted), Block.UPDATE_ALL);
            }
        } else if (wanted > 0 && canPlace(state)) {
            boolean water = state.is(Blocks.WATER);
            level.setBlock(pos, LsBlocks.SHIELD_LIGHT.get().defaultBlockState()
                    .setValue(ShieldLightBlock.LEVEL, wanted)
                    .setValue(ShieldLightBlock.WATERLOGGED, water), Block.UPDATE_ALL);
        }
    }

    /** Air, a water source or an existing shield light. */
    public static boolean canPlace(BlockState state) {
        return state.isAir() || state.is(LsBlocks.SHIELD_LIGHT.get())
                || state.is(Blocks.WATER) && state.getFluidState().isSource();
    }

    /**
     * Light blocks for a holder with eyes at {@code eye} looking along {@code look} (spec 51 §3): a ray fan over the
     * cone, light in the last free block before each hit (or at the ray end), 15 up to half the range and down to 11
     * at the end; plus the glow of 14 in the eye block (or the feet block).
     */
    public static Map<BlockPos, Integer> compute(Level level, Vec3 eye, Vec3 look, Flashlight setting) {
        Map<BlockPos, Integer> result = new HashMap<>();
        BlockPos eyePos = BlockPos.containing(eye);
        if (canPlace(level.getBlockState(eyePos))) {
            result.put(eyePos, GLOW_LEVEL);
        } else if (canPlace(level.getBlockState(eyePos.below()))) {
            result.put(eyePos.below(), GLOW_LEVEL);
        }
        double range = setting.rangeBlocks();
        double half = Math.toRadians(setting.widthDegrees() / 2.0);
        Vec3 forward = look.normalize();
        Vec3 side = Math.abs(forward.y) > 0.99 ? new Vec3(1, 0, 0) : forward.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 up = side.cross(forward).normalize();
        int rings = Math.max(1, Mth.ceil(half / (RAY_SPACING / range)));
        for (int ring = 0; ring <= rings && result.size() < MAX_LIGHTS; ring++) {
            double angle = half * ring / rings;
            int count = ring == 0 ? 1 : Math.max(3, Mth.ceil(2 * Math.PI * Math.sin(angle) * range / RAY_SPACING));
            for (int i = 0; i < count && result.size() < MAX_LIGHTS; i++) {
                double around = 2 * Math.PI * i / count + ring * 0.5;
                Vec3 dir = forward.scale(Math.cos(angle)).add(side.scale(Math.sin(angle) * Math.cos(around)))
                        .add(up.scale(Math.sin(angle) * Math.sin(around)));
                BlockPos target = rayTarget(level, eye, dir, range);
                if (target != null) {
                    int light = lightAt(Math.sqrt(target.getCenter().distanceToSqr(eye)), range);
                    result.merge(target, light, Math::max);
                }
            }
        }
        return result;
    }

    /** 15 up to half the range, then linear down to 11 at the end. */
    public static int lightAt(double distance, double range) {
        if (distance <= range / 2) {
            return 15;
        }
        double t = Math.min(1.0, (distance - range / 2) / (range / 2));
        return Mth.clamp((int) Math.round(15 - 4 * t), 11, 15);
    }

    /** Free block just before the first solid block along the ray, or at its end; null if none is free. */
    private static BlockPos rayTarget(Level level, Vec3 eye, Vec3 dir, double range) {
        Vec3 end = eye.add(dir.scale(range));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                CollisionContext.empty()));
        if (hit.getType() != HitResult.Type.BLOCK) {
            BlockPos pos = BlockPos.containing(end);
            return canPlace(level.getBlockState(pos)) ? pos : null;
        }
        BlockPos front = hit.getBlockPos().relative(hit.getDirection());
        if (canPlace(level.getBlockState(front))) {
            return front;
        }
        // Grass, flowers & co. stand in the way: step back along the ray.
        for (double back = 1.0; back <= 2.0; back += 1.0) {
            BlockPos pos = BlockPos.containing(hit.getLocation().subtract(dir.scale(back)));
            if (canPlace(level.getBlockState(pos))) {
                return pos;
            }
        }
        return null;
    }
}
