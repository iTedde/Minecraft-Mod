package dev.craft.core.energy;

import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import dev.craft.Craft;
import dev.craft.core.CraftDamageTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Owns one {@link EnergyNet} per server level. Tiles call {@link #add} from {@code onLoad} and {@link #remove} from
 * {@code setRemoved}/{@code onChunkUnloaded}. NeoForge calls {@code onLoad} from the block entity tick loop, which
 * matches IC2 adding tiles in their first update tick, so tiles join the net immediately.
 */
@EventBusSubscriber(modid = Craft.MODID)
public final class EnergyNets {
    private static final Map<ServerLevel, LevelEnergy> LEVELS = new WeakHashMap<>();

    private EnergyNets() {
    }

    public static EnergyNet get(ServerLevel level) {
        return LEVELS.computeIfAbsent(level, LevelEnergy::new).net;
    }

    public static void add(Level level, EnergyTile tile) {
        if (level instanceof ServerLevel serverLevel) {
            get(serverLevel).add(tile);
        }
    }

    public static void remove(Level level, EnergyTile tile) {
        if (level instanceof ServerLevel serverLevel) {
            LevelEnergy energy = LEVELS.get(serverLevel);
            if (energy != null) {
                energy.net.remove(tile);
            }
        }
    }

    /** Whether {@code tile} is currently part of the net. */
    public static boolean isAdded(Level level, EnergyTile tile) {
        if (level instanceof ServerLevel serverLevel) {
            LevelEnergy energy = LEVELS.get(serverLevel);
            return energy != null && energy.net.contains(tile);
        }
        return false;
    }

    /**
     * Emits one packet.
     *
     * @return EU that could not be delivered
     */
    public static int emit(Level level, EnergySource source, int amount) {
        if (level instanceof ServerLevel serverLevel) {
            LevelEnergy energy = LEVELS.get(serverLevel);
            if (energy != null) {
                return energy.net.emitEnergyFrom(source, amount);
            }
        }
        return amount;
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        LevelEnergy energy = LEVELS.get(level);
        if (energy == null) {
            return;
        }
        for (Map.Entry<Object, Integer> entry : energy.net.drainShockEnergy().entrySet()) {
            LivingEntity entity = (LivingEntity) entry.getKey();
            int damage = (entry.getValue() + 63) / 64;
            if (entity.isAlive()) {
                entity.hurt(level.damageSources().source(CraftDamageTypes.ELECTRICITY), damage);
            }
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            LEVELS.remove(level);
        }
    }

    private static final class LevelEnergy {
        final EnergyNet net;

        LevelEnergy(ServerLevel level) {
            this.net = new EnergyNet(area -> level.getEntitiesOfClass(LivingEntity.class, area).stream()
                    .map(entity -> new ShockTarget(entity, entity.getBoundingBox()))
                    .toList(), new Random());
        }
    }
}
