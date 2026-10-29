package dev.craft.gravisuite;

import dev.craft.Craft;
import dev.craft.gravisuite.item.AdvancedJetpackItem;
import dev.craft.gravisuite.item.GraviChestplateItem;
import dev.craft.ic2.keyboard.Ic2Keyboard;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Client → server: GraviSuite key presses ({@code keyFLY}, {@code keyModePressed}; spec 28 §6). */
public record GsKeyPayload(int key) implements CustomPacketPayload {
    public static final int FLY = 0;
    public static final int MODE = 1;

    public static final Type<GsKeyPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "gravisuite_key"));
    public static final StreamCodec<ByteBuf, GsKeyPayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(GsKeyPayload::new, GsKeyPayload::key);

    @Override
    public Type<GsKeyPayload> type() {
        return TYPE;
    }

    static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC,
                (payload, context) -> handle(context.player(), payload.key()));
    }

    /** {@code ServerPacketHandler}: acts on the worn chest piece. */
    public static void handle(Player player, int key) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof GraviChestplateItem) {
            if (key == FLY) {
                GraviChestplateItem.switchFlyMode(player, chest);
            } else if (key == MODE && Ic2Keyboard.isBoostKeyDown(player)) {
                GraviChestplateItem.switchMagnet(player, chest);
            }
            return;
        }
        if (!(chest.getItem() instanceof AdvancedJetpackItem)) {
            return;
        }
        if (key == FLY) {
            AdvancedJetpackItem.switchEngine(player, chest);
        } else if (key == MODE && Ic2Keyboard.isJumpKeyDown(player)) {
            AdvancedJetpackItem.switchHoverMode(player, chest);
        }
    }
}
