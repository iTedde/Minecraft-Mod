package ivorius.psychedelicraft.client.sound;

import java.lang.ref.WeakReference;
import java.util.Comparator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.Sound.Type;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.mixin.client.SoundsAccessor;

/**
 * Created by Sollace on 11 June 2023
 */
public class ClientDrugMusicManager {
    private static final ResourceLocation EMPTY_SOUND_ID = Psychedelicraft.id("drugs/generic");

    private WeakReference<MovingSoundDrug> activeSound = new WeakReference<>(null);

    public void update(DrugProperties properties) {
        if (!PsychedelicraftClient.getConfig().drugsBackgroundMusic.get()) {
            return;
        }
        MovingSoundDrug sound = getActiveSound();
        DrugType.REGISTRY
            .stream()
            .filter(type -> properties.getDrugValue(type) >= PsychedelicraftClient.getConfig().getBGMThreshold() && hasSoundsDefined(type.soundEvent().getLocation()))
            .sorted(Comparator.comparing(properties::getDrugValue).reversed())
            .findFirst()
            .filter(type -> sound == null || type != sound.getType())
            .ifPresent(drugType -> startPlayingSound(properties, drugType));
    }

    @Nullable
    private MovingSoundDrug getActiveSound() {
        MovingSoundDrug sound = activeSound.get();
        if (sound != null && (sound.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(sound))) {
            activeSound = new WeakReference<>(null);
            return null;
        }
        return sound;
    }

    private void startPlayingSound(DrugProperties properties, DrugType<?> type) {
        Psychedelicraft.LOGGER.info("Playing drug background music for " + type.id());
        MovingSoundDrug sound = getActiveSound();
        if (sound != null) {
            Minecraft.getInstance().getSoundManager().stop(sound);
            sound.markCompleted();
        }
        MovingSoundDrug newSound = new MovingSoundDrug(properties, type, sound == null ? 0.1F : sound.getVolume());
        Minecraft.getInstance().getSoundManager().play(newSound);
        activeSound = new WeakReference<>(newSound);
    }

    private boolean hasSoundsDefined(ResourceLocation soundEventId) {
        return hasSoundsDefined(Minecraft.getInstance().getSoundManager().getSoundEvent(soundEventId));
    }

    private boolean hasSoundsDefined(Weighted<Sound> sound) {
        if (sound instanceof SoundsAccessor set) {
            for (var s : set.getSounds()) {
                if (hasSoundsDefined(s)) {
                    return true;
                }
            }
        }

        if (sound instanceof Sound s) {
            if (s.getType() == Type.SOUND_EVENT) {
                return hasSoundsDefined(s.getLocation());
            }
            return !EMPTY_SOUND_ID.equals(s.getLocation());
        }

        return false;
    }
}
