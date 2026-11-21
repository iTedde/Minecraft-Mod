package dev.craft.nuclearcontrol.client;

import java.util.Map;
import java.util.WeakHashMap;

import dev.craft.nuclearcontrol.NcSounds;
import dev.craft.nuclearcontrol.block.AlarmBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

/**
 * Client side of the alarms ({@code SoundHelper.playAlarm}): while powered, the chosen sound plays at the alarm and is
 * restarted every 3 ticks once it ended. The volume is {@code range / 16}; above 1 it widens the linear fade-out to
 * {@code range} blocks, as in the original. Started only for a player within that distance.
 */
final class AlarmSoundHandler implements AlarmBlockEntity.AlarmSoundHooks.Handler {
    private static final float DEFAULT_RANGE = 16.0F;

    private final Map<AlarmBlockEntity, AlarmSound> sounds = new WeakHashMap<>();

    @Override
    public void update(AlarmBlockEntity alarm) {
        if (!alarm.isPowered()) {
            stop(alarm);
            return;
        }
        AlarmSound current = sounds.get(alarm);
        Minecraft minecraft = Minecraft.getInstance();
        if (current != null && minecraft.getSoundManager().isActive(current) || minecraft.player == null) {
            return;
        }
        float volume = alarm.getNormalizedRange();
        float range = volume > 1.0F ? DEFAULT_RANGE * volume : DEFAULT_RANGE;
        if (minecraft.player.distanceToSqr(alarm.getBlockPos().getCenter()) >= range * range || volume <= 0.0F) {
            return;
        }
        AlarmSound sound = new AlarmSound(alarm, volume);
        sounds.put(alarm, sound);
        minecraft.getSoundManager().play(sound);
    }

    @Override
    public void stop(AlarmBlockEntity alarm) {
        AlarmSound sound = sounds.remove(alarm);
        if (sound != null) {
            sound.finish();
        }
    }

    private static final class AlarmSound extends AbstractTickableSoundInstance {
        private final AlarmBlockEntity alarm;

        AlarmSound(AlarmBlockEntity alarm, float volume) {
            super(NcSounds.alarm(alarm.getSoundName()), SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
            this.alarm = alarm;
            this.volume = volume;
            this.attenuation = Attenuation.LINEAR;
            this.x = alarm.getBlockPos().getX() + 0.5;
            this.y = alarm.getBlockPos().getY() + 0.5;
            this.z = alarm.getBlockPos().getZ() + 0.5;
        }

        void finish() {
            stop();
        }

        @Override
        public void tick() {
            if (alarm.isRemoved() || !alarm.isPowered()) {
                stop();
            }
        }
    }
}
