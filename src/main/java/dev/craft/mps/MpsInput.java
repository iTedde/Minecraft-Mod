package dev.craft.mps;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * MPS {@code PlayerInputMap} (spec 29 §6): the movement keys of one client tick. Strafe is positive to the left,
 * forward is already scaled while sneaking; "down" is the flight control key.
 */
public record MpsInput(float forward, float strafe, boolean jump, boolean sneak, boolean down) {
    public static final MpsInput NONE = new MpsInput(0.0F, 0.0F, false, false, false);

    public static final StreamCodec<ByteBuf, MpsInput> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, MpsInput::forward, ByteBufCodecs.FLOAT, MpsInput::strafe,
            ByteBufCodecs.BOOL, MpsInput::jump, ByteBufCodecs.BOOL, MpsInput::sneak,
            ByteBufCodecs.BOOL, MpsInput::down, MpsInput::new);
}
