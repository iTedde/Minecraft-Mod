package dev.craft.enderstorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dev.craft.Craft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * EnderStorage {@code TankSynchroniser} (spec 27 §5): keeps the liquid of ender tank <em>items</em> up to date on the
 * client. Every client tick the client tells the server which tank frequencies it drew since the last tick
 * ({@link Track}); the server follows those storages for that player like a placed tank and sends their liquid
 * ({@link Liquid}). Display only.
 */
public final class TankSynchroniser {
    /** Server: per player, storage key → followed storage. */
    private static final Map<UUID, Map<String, PlayerTank>> SERVER = new HashMap<>();
    /** Client: storage key → shown liquid. */
    private static final Map<String, EnderTankState> CLIENT = new HashMap<>();
    /** Client: frequencies drawn this tick and last tick ({@code a_visible}, {@code b_visible}). */
    private static Map<String, Frequency> visible = new HashMap<>();
    private static Map<String, Frequency> wasVisible = new HashMap<>();

    private TankSynchroniser() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(Track.TYPE, Track.STREAM_CODEC, TankSynchroniser::handleTrack)
                .playToClient(Liquid.TYPE, Liquid.STREAM_CODEC, TankSynchroniser::handleLiquid);
    }

    // --- client ---

    /** {@code getClientLiquid}: the liquid to draw for a tank item; marks the frequency as visible. */
    public static FluidStack clientLiquid(Frequency frequency) {
        String key = frequency.key(EnderStorageData.LIQUID);
        visible.put(key, frequency);
        EnderTankState state = CLIENT.get(key);
        return state == null ? FluidStack.EMPTY : state.getShown();
    }

    /** Client tick: glide all liquids, then send which frequencies became visible or invisible. */
    public static void clientTick() {
        CLIENT.values().forEach(EnderTankState::clientUpdate);
        List<Frequency> added = new ArrayList<>();
        List<Frequency> removed = new ArrayList<>();
        visible.forEach((key, frequency) -> {
            if (!wasVisible.containsKey(key)) {
                added.add(frequency);
            }
        });
        wasVisible.forEach((key, frequency) -> {
            if (!visible.containsKey(key)) {
                removed.add(frequency);
            }
        });
        if (!added.isEmpty() || !removed.isEmpty()) {
            PacketDistributor.sendToServer(new Track(added, removed));
        }
        Map<String, Frequency> swap = wasVisible;
        swap.clear();
        wasVisible = visible;
        visible = swap;
    }

    /** Client left the world. */
    public static void clientReset() {
        CLIENT.clear();
        visible.clear();
        wasVisible.clear();
    }

    private static void handleLiquid(Liquid payload, IPayloadContext context) {
        CLIENT.computeIfAbsent(payload.frequency().key(EnderStorageData.LIQUID), key -> new EnderTankState())
                .sync(payload.liquid());
    }

    // --- server ---

    private static void handleTrack(Track payload, IPayloadContext context) {
        Map<String, PlayerTank> tanks = SERVER.computeIfAbsent(context.player().getUUID(), uuid -> new HashMap<>());
        for (Frequency frequency : payload.added()) {
            tanks.computeIfAbsent(frequency.key(EnderStorageData.LIQUID), key -> new PlayerTank(frequency))
                    .tracking = true;
        }
        for (Frequency frequency : payload.removed()) {
            PlayerTank tank = tanks.get(frequency.key(EnderStorageData.LIQUID));
            if (tank != null) {
                tank.tracking = false;
            }
        }
    }

    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Post event) {
        EnderStorageData data = null;
        for (Map.Entry<UUID, Map<String, PlayerTank>> entry : SERVER.entrySet()) {
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                continue;
            }
            for (PlayerTank tank : entry.getValue().values()) {
                if (!tank.tracking) {
                    continue;
                }
                if (data == null) {
                    data = EnderStorageData.get(event.getServer());
                }
                FluidStack liquid = data.liquid(tank.frequency).getFluid();
                if (tank.state.serverUpdate(liquid)) {
                    PacketDistributor.sendToPlayer(player, new Liquid(tank.frequency, liquid.copy()));
                }
            }
        }
    }

    @SubscribeEvent
    public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        SERVER.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void serverStopped(ServerStoppedEvent event) {
        SERVER.clear();
    }

    /** {@code PlayerItemTankState}: one followed storage of one player. */
    private static final class PlayerTank {
        private final Frequency frequency;
        private final EnderTankState state = new EnderTankState();
        private boolean tracking;

        private PlayerTank(Frequency frequency) {
            this.frequency = frequency;
        }
    }

    /** Client → server: frequencies drawn since last tick, and those no longer drawn. */
    public record Track(List<Frequency> added, List<Frequency> removed) implements CustomPacketPayload {
        public static final Type<Track> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "ender_tank_track"));
        public static final StreamCodec<ByteBuf, Track> STREAM_CODEC = StreamCodec.composite(
                Frequency.STREAM_CODEC.apply(ByteBufCodecs.list(64)), Track::added,
                Frequency.STREAM_CODEC.apply(ByteBufCodecs.list(64)), Track::removed,
                Track::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Server → client: the liquid of a followed storage. */
    public record Liquid(Frequency frequency, FluidStack liquid) implements CustomPacketPayload {
        public static final Type<Liquid> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "ender_tank_liquid"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Liquid> STREAM_CODEC = StreamCodec.composite(
                Frequency.STREAM_CODEC, Liquid::frequency,
                FluidStack.OPTIONAL_STREAM_CODEC, Liquid::liquid,
                Liquid::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
