package dev.craft.compat.euinput;

import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.explosion.Ic2Explosion;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
 * EU input for FE machines of other mods: Open Modular Lighting lights (phase 43, {@code docs/spec/43-oml-eu.md},
 * 1 EU = 1 FE) and selected Ad Astra and AE2 machines (phase 45, {@code docs/spec/45-fe-eu-eingang.md},
 * 1 EU = 4 FE). Each listed block joins the EU net as an LV sink (max. 32 EU per packet, explodes above) and passes the
 * EU as FE into the block's own energy storage; only whole EU are taken. FE input stays untouched. Rules of mods that
 * are not installed do nothing.
 *
 * <p>The blocks are foreign block entities, so the net gets a small {@link ForeignSink} per position. It is added when
 * a chunk loads or a listed block appears (placement or any block change that notifies neighbours), and dropped again
 * on chunk unload, on such a change and by a sweep every second.
 */
public final class ForeignEuInputs {
    public static final int MAX_INPUT = 32;
    private static final int SWEEP_INTERVAL = 20;

    /** One mod: its listed blocks take EU at {@code fePerEu} FE per EU. */
    public record Rule(String modId, Set<String> blocks, int fePerEu) {
    }

    public static final Rule OML = new Rule("openmodularlighting", Set.of("electric_floodlight", "uv_floodlight",
            "grow_light", "small_fluorescent_light", "square_fluorescent_light", "cube_light"), 1);
    /** Machines only: no generators (coal generator, solar panel), storage (energizer) or cables. */
    public static final Rule AD_ASTRA = new Rule("ad_astra", Set.of("compressor", "fuel_refinery", "oxygen_loader",
            "oxygen_distributor", "water_pump", "cryo_freezer", "gravity_normalizer", "etrionic_blast_furnace"), 4);
    /** AE2's {@code AEBasePoweredBlockEntity} blocks; not the cable bus (energy acceptor part) or energy cells. */
    public static final Rule AE2 = new Rule("ae2", Set.of("controller", "energy_acceptor", "charger", "inscriber",
            "growth_accelerator", "chest"), 4);
    private static final List<Rule> RULES = List.of(OML, AD_ASTRA, AE2);

    private static final Map<String, Rule> ACTIVE = new HashMap<>();
    private static final Map<ServerLevel, LevelSinks> LEVELS = new HashMap<>();

    private ForeignEuInputs() {
    }

    public static void init() {
        for (Rule rule : RULES) {
            if (ModList.get().isLoaded(rule.modId())) {
                ACTIVE.put(rule.modId(), rule);
            }
        }
        if (ACTIVE.isEmpty()) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(ForeignEuInputs::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(ForeignEuInputs::onChunkUnload);
        NeoForge.EVENT_BUS.addListener(ForeignEuInputs::onNeighborNotify);
        NeoForge.EVENT_BUS.addListener(ForeignEuInputs::onLevelTick);
        NeoForge.EVENT_BUS.addListener(ForeignEuInputs::onLevelUnload);
    }

    /** The rule for {@code state}, or {@code null} if it takes no EU (client-safe; used for cable connections). */
    public static Rule ruleFor(BlockState state) {
        if (ACTIVE.isEmpty()) {
            return null;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        Rule rule = ACTIVE.get(id.getNamespace());
        return rule != null && rule.blocks().contains(id.getPath()) ? rule : null;
    }

    public static boolean isEuInput(BlockState state) {
        return ruleFor(state) != null;
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
            Iterator<Map.Entry<BlockPos, ForeignSink>> it = sinks.active.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<BlockPos, ForeignSink> entry = it.next();
                if (new ChunkPos(entry.getKey()).equals(chunk)) {
                    EnergyNets.remove(level, entry.getValue());
                    it.remove();
                }
            }
            sinks.pending.removeIf(pos -> new ChunkPos(pos).equals(chunk));
        }
    }

    /** Any block change notifies its neighbours – covers placing, breaking, commands, pistons and contraptions. */
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

    /** Adds a sink for a listed block at {@code pos}, or drops a stale one (also when the block's rule changed). */
    private static void update(ServerLevel level, LevelSinks sinks, BlockPos pos) {
        ForeignSink sink = sinks.active.get(pos);
        Rule rule = level.isLoaded(pos) ? ruleFor(level.getBlockState(pos)) : null;
        if (sink != null && sink.rule != rule) {
            sinks.active.remove(pos);
            EnergyNets.remove(level, sink);
            sink = null;
        }
        if (rule == null) {
            return;
        }
        if (sink == null) {
            if (EnergyNets.get(level).getTile(pos) == null) {
                sink = new ForeignSink(level, pos, rule);
                sinks.active.put(pos, sink);
                EnergyNets.add(level, sink);
            }
        } else if (!EnergyNets.isAdded(level, sink)) {
            EnergyNets.add(level, sink);
        }
    }

    private static final class LevelSinks {
        final Map<BlockPos, ForeignSink> active = new HashMap<>();
        final Set<BlockPos> pending = new HashSet<>();
    }

    /** EU sink standing in for one foreign block. */
    static final class ForeignSink implements EnergySink {
        private final ServerLevel level;
        private final BlockPos pos;
        final Rule rule;

        ForeignSink(ServerLevel level, BlockPos pos, Rule rule) {
            this.level = level;
            this.pos = pos;
            this.rule = rule;
        }

        @Override
        public BlockPos getBlockPos() {
            return pos;
        }

        private boolean valid() {
            return level.isLoaded(pos) && ruleFor(level.getBlockState(pos)) == rule;
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
            if (storage == null) {
                return 0;
            }
            return Math.max(0, storage.getMaxEnergyStored() - storage.getEnergyStored()) / rule.fePerEu();
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
            if (storage == null) {
                return amount;
            }
            int rate = rule.fePerEu();
            int eu = Math.min(amount, storage.receiveEnergy(amount * rate, true) / rate);
            if (eu > 0) {
                storage.receiveEnergy(eu * rate, false);
            }
            return amount - eu;
        }

        @Override
        public int getMaxSafeInput() {
            return MAX_INPUT;
        }
    }
}
