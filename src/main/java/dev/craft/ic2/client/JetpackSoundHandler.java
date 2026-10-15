package dev.craft.ic2.client;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.item.armor.JetpackItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Jetpack loop of the local player while its jetpack fires (IC2 {@code ItemArmorJetpack.audioSource}). */
@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT)
public final class JetpackSoundHandler {
    private static LoopSound loopSound;

    private JetpackSoundHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        boolean used = JetpackItem.clientJetpackUsed;
        JetpackItem.clientJetpackUsed = false;
        LocalPlayer player = Minecraft.getInstance().player;
        if (used && player != null) {
            if (loopSound == null) {
                loopSound = new LoopSound(player);
                Minecraft.getInstance().getSoundManager().play(loopSound);
            }
        } else if (loopSound != null) {
            loopSound.stopPlaying();
            loopSound = null;
        }
    }

    private static final class LoopSound extends AbstractTickableSoundInstance {
        private final Entity entity;

        LoopSound(Entity entity) {
            super(Ic2Sounds.JETPACK_LOOP.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
            this.entity = entity;
            this.looping = true;
            this.delay = 0;
            updatePosition();
        }

        private void updatePosition() {
            x = entity.getX();
            y = entity.getY();
            z = entity.getZ();
        }

        void stopPlaying() {
            stop();
        }

        @Override
        public void tick() {
            if (entity.isRemoved()) {
                stop();
            } else {
                updatePosition();
            }
        }
    }
}
