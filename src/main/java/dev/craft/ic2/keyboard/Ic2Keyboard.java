package dev.craft.ic2.keyboard;

import java.util.function.Supplier;

import dev.craft.Craft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * IC2 {@code Keyboard}: the state of the IC2 keys of each player as sent by the client. The state lives in a
 * non-persistent player attachment, so it is forgotten on logout (and, as in IC2, on respawn) and reads as "not
 * pressed" until the client sends a change.
 */
public final class Ic2Keyboard {
    public static final int ALT = 1;
    public static final int BOOST = 1 << 1;
    public static final int FORWARD = 1 << 2;
    public static final int MODE_SWITCH = 1 << 3;
    public static final int JUMP = 1 << 4;
    public static final int SIDE_INVENTORY = 1 << 5;

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Craft.MODID);

    private static final Supplier<AttachmentType<Integer>> KEY_STATE =
            ATTACHMENT_TYPES.register("key_state", () -> AttachmentType.builder(() -> 0).build());

    private Ic2Keyboard() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(KeyStatePayload.TYPE, KeyStatePayload.STREAM_CODEC,
                (payload, context) -> processKeyUpdate(context.player(), payload.keyState()));
    }

    public static void processKeyUpdate(Player player, int keyState) {
        player.setData(KEY_STATE, keyState);
    }

    private static boolean isDown(Player player, int key) {
        return (player.getData(KEY_STATE) & key) != 0;
    }

    public static boolean isAltKeyDown(Player player) {
        return isDown(player, ALT);
    }

    public static boolean isBoostKeyDown(Player player) {
        return isDown(player, BOOST);
    }

    public static boolean isForwardKeyDown(Player player) {
        return isDown(player, FORWARD);
    }

    public static boolean isModeSwitchKeyDown(Player player) {
        return isDown(player, MODE_SWITCH);
    }

    public static boolean isJumpKeyDown(Player player) {
        return isDown(player, JUMP);
    }

    public static boolean isSideinventoryKeyDown(Player player) {
        return isDown(player, SIDE_INVENTORY);
    }

    public static boolean isSneakKeyDown(Player player) {
        return player.isShiftKeyDown();
    }
}
