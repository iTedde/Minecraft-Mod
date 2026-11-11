package dev.craft.compat.oml;

import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.explosion.Ic2Explosion;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * EU input for the electric lights of Open Modular Lighting (phase 43, {@code docs/spec/43-oml-eu.md}): every electric
 * light joins the EU net as an LV sink (max. 32 EU per packet, explodes above) and passes EU 1:1 as FE into the light's
 * own energy storage. FE input stays untouched. Does nothing without the mod.
 *
 * <p>The lights are foreign block entities, so the net gets a small {@link LightSink} per position. It is added when a
 * chunk loads or a light appears (placement or any block change that notifies neighbours), and dropped again on
 * chunk unload, on such a change and by a sweep every second.
 */
public final class OmlEuInput {
    public static final String MOD_ID = "openmodularlighting";
    public static final int MAX_INPUT = 32;
    /** Electric lights only; the carbon floodlight burns fuel, phantom/UV light blocks are its beam. */
    private static final Set<String> ELECTRIC_LIGHTS = Set.of("electric_floodlight", "uv_floodlight", "grow_light",
            "small_fluorescent_light", "square_fluorescent_light", "cube_light");
    private static final int SWEEP_INTERVAL = 20;

    private static boolean loaded;
    private static final Map<ServerLevel, LevelSinks> LEVELS = new HashMap<>();

    private OmlEuInput() {
    }

    public static void init() {
        loaded = ModList.get().isLoaded(MOD_ID);
        if (!loaded) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(OmlEuInput::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(OmlEuInput::onChunkUnload);
        NeoForge.EVENT_BUS.addListener(OmlEuInput::onNeighborNotify);
        NeoForge.EVENT_BUS.addListener(OmlEuInput::onLevelTick);
        NeoForge.EVENT_BUS.addListener(OmlEuInput::onLevelUnload);
    }

    /** Whether {@code state} is an electric OML light that takes EU (client-safe; used for cable connections). */
    public static boolean isEuInput(BlockState state) {
        if (!loaded) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return id.getNamespace().equals(MOD_ID) && ELECTRIC_LIGHTS.contains(id.getPath());
    }

    // --- lifecycle ---

    private static LevelSinks sinks(ServerLevel level) {
        return LEVELS.computeIfAbsent(level, l -> new LevelSinks());
    }

    private static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level && event.getChunk() instanceof LevelChunk chunk) {
            LevelSinks sinks = sinks(level);
            for (BlockPos pos : chunk.getBlockEntitiesPos()) {
                sinks.pending.add(pos.immutable());
            }
        }
    }

    private static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            LevelSinks sinks = LEVELS.get(level);
            if (sinks == null) {
                return;
            }
            ChunkPos chunk = event.getChunk().getPos();
            Iterator<Map.Entry<BlockPos, LightSink>> it = sinks.active.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<BlockPos, LightSink> entry = it.next();
                if (new ChunkPos(entry.getKey()).equals(chunk)) {
                    EnergyNets.remove(level, entry.getValue());
                    it.remove();
                }
            }
            sinks.pending.removeIf(pos -> new ChunkPos(pos).equals(chunk));
        }
    }

    /** Any block change notifies its neighbours – covers breaking, commands, pistons and contraptions. */
    private static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos pos = event.getPos();
        if (isEuInput(event.getState())) {
            sinks(level).pending.add(pos.immutable());
        } else {
            LevelSinks sinks = LEVELS.get(level);
            if (sinks != null && sinks.active.containsKey(pos)) {
                sinks.pending.add(pos.immutable());
            }
        }
    }

    private static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        LevelSinks sinks = LEVELS.get(level);
        if (sinks == null) {
            return;
        }
        if (!sinks.pending.isEmpty()) {
            Set<BlockPos> batch = new HashSet<>(sinks.pending);
            sinks.pending.clear();
            for (BlockPos pos : batch) {
                update(level, sinks, pos);
            }
        }
        if (level.getGameTime() % SWEEP_INTERVAL == 0 && !sinks.active.isEmpty()) {
            for (BlockPos pos : Set.copyOf(sinks.active.keySet())) {
                update(level, sinks, pos);
            }
        }
    }

    private static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            LEVELS.remove(level);
        }
    }

    /** Adds a sink for a light at {@code pos} or drops a stale one. */
    private static void update(ServerLevel level, LevelSinks sinks, BlockPos pos) {
        LightSink sink = sinks.active.get(pos);
        boolean light = level.isLoaded(pos) && isEuInput(level.getBlockState(pos));
        if (light && sink == null) {
            EnergyTile occupant = EnergyNets.get(level).getTile(pos);
            if (occupant == null) {
                sink = new LightSink(level, pos);
                sinks.active.put(pos, sink);
                EnergyNets.add(level, sink);
            }
        } else if (!light && sink != null) {
            sinks.active.remove(pos);
            EnergyNets.remove(level, sink);
        } else if (light && !EnergyNets.isAdded(level, sink)) {
            EnergyNets.add(level, sink);
        }
    }

    private static final class LevelSinks {
        final Map<BlockPos, LightSink> active = new HashMap<>();
        final Set<BlockPos> pending = new HashSet<>();
    }

    /** EU sink standing in for one light; 1 EU = 1 FE. */
    static final class LightSink implements EnergySink {
        private final ServerLevel level;
        private final BlockPos pos;

        LightSink(ServerLevel level, BlockPos pos) {
            this.level = level;
            this.pos = pos;
        }

        @Override
        public BlockPos getBlockPos() {
            return pos;
        }

        private boolean valid() {
            return level.isLoaded(pos) && isEuInput(level.getBlockState(pos));
        }

        private IEnergyStorage storage(Direction side) {
            if (!valid()) {
                return null;
            }
            IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, side);
            return storage != null && storage.canReceive() ? storage : null;
        }

        /** Any receiving side, for the demand query (the net asks without a side). */
        private IEnergyStorage anyStorage() {
            IEnergyStorage storage = storage(null);
            if (storage != null) {
                return storage;
            }
            for (Direction side : Direction.values()) {
                storage = storage(side);
                if (storage != null) {
                    return storage;
                }
            }
            return null;
        }

        @Override
        public boolean acceptsEnergyFrom(EnergyTile emitter, Direction direction) {
            return storage(direction) != null;
        }

        @Override
        public int demandsEnergy() {
            IEnergyStorage storage = anyStorage();
            return storage == null ? 0 : Math.max(0, storage.getMaxEnergyStored() - storage.getEnergyStored());
        }

        @Override
        public int injectEnergy(Direction direction, int amount) {
            if (!valid()) {
                return amount;
            }
            if (amount > MAX_INPUT) {
                Ic2Explosion.explodeMachineAt(level, pos);
                return 0;
            }
            if (amount <= 0) {
                return 0;
            }
            IEnergyStorage storage = storage(direction);
            if (storage == null) {
                storage = anyStorage();
            }
            return storage == null ? amount : amount - storage.receiveEnergy(amount, false);
        }

        @Override
        public int getMaxSafeInput() {
            return MAX_INPUT;
        }
    }
}
