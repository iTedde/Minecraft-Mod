package dev.craft.lightshield;

import dev.craft.Craft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import io.netty.buffer.ByteBuf;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Client → server: new flashlight setting from the GUI (spec 51 §2). */
public record FlashlightPayload(Flashlight setting) implements CustomPacketPayload {
    public static final Type<FlashlightPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "flashlight"));
    public static final StreamCodec<ByteBuf, FlashlightPayload> STREAM_CODEC =
            Flashlight.STREAM_CODEC.map(FlashlightPayload::new, FlashlightPayload::setting);

    @Override
    public Type<FlashlightPayload> type() {
        return TYPE;
    }

    static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC,
                (payload, context) -> handle(context.player(), payload.setting()));
    }

    /** Applies to the held light shield, main hand first; clamped to the valid stages. */
    public static void handle(Player player, Flashlight setting) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof ElectricLightShieldItem) {
                stack.set(LsDataComponents.FLASHLIGHT.get(), Flashlight.of(setting.range(), setting.width()));
                return;
            }
        }
    }
}
