package dev.craft.gravitygun;

import dev.craft.Craft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Client → server: gravity gun keys ({@code sendKeybind}: 1 attack pressed, 2 use pressed, 3 use released). */
public record GravityGunPayload(int key) implements CustomPacketPayload {
    public static final int THROW = 1;
    public static final int USE = 2;
    public static final int RELEASE = 3;

    public static final Type<GravityGunPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "gravity_gun"));
    public static final StreamCodec<ByteBuf, GravityGunPayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(GravityGunPayload::new, GravityGunPayload::key);

    @Override
    public Type<GravityGunPayload> type() {
        return TYPE;
    }

    static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC,
                (payload, context) -> handle(context.player(), payload.key()));
    }

    /** {@code MapPacketHandler.handleServerPacket}. */
    public static void handle(Player player, int key) {
        boolean holding = !GravityGunHandler.gun(player).isEmpty();
        switch (key) {
            case THROW -> {
                if (holding) {
                    GravityGunHandler.tryThrow(player);
                }
            }
            case USE -> {
                if (holding || GravityGunHandler.grabbed(player) != null) {
                    GravityGunHandler.useKey(player);
                }
            }
            case RELEASE -> GravityGunHandler.releaseKey(player);
            default -> {
            }
        }
    }
}
