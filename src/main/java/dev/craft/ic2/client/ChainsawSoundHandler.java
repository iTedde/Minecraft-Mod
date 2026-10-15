package dev.craft.ic2.client;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.item.tool.ChainsawItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Chainsaw sounds of the local player (IC2 {@code ItemElectricToolChainsaw.onUpdate}): idle loop while selected,
 * stop sound when put away.
 */
@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT)
public final class ChainsawSoundHandler {
    private static IdleSound idleSound;
    private static boolean wasEquipped;

    private ChainsawSoundHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        boolean equipped = player != null && ChainsawItem.isEquipped(player);
        if (equipped && !wasEquipped) {
            idleSound = new IdleSound(player);
            minecraft.getSoundManager().play(idleSound);
        } else if (!equipped && idleSound != null) {
            idleSound.stopPlaying();
            idleSound = null;
            if (player != null) {
                minecraft.getSoundManager().play(new SimpleSoundInstance(Ic2Sounds.CHAINSAW_STOP.get(),
                        SoundSource.PLAYERS, 1.0F, 1.0F, SoundInstance.createUnseededRandom(),
                        player.getX(), player.getY(), player.getZ()));
            }
        }
        wasEquipped = equipped;
    }

    private static final class IdleSound extends AbstractTickableSoundInstance {
        private final Entity entity;

        IdleSound(Entity entity) {
            super(Ic2Sounds.CHAINSAW_IDLE.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
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
