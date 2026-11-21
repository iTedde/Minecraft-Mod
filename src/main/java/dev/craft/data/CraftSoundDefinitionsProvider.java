package dev.craft.data;

import java.util.List;

import dev.craft.Craft;
import dev.craft.gravisuite.GsSounds;
import dev.craft.nuclearcontrol.NcSounds;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.music.MusicDiscs;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class CraftSoundDefinitionsProvider extends SoundDefinitionsProvider {
    public CraftSoundDefinitionsProvider(PackOutput output, ExistingFileHelper files) {
        super(output, Craft.MODID, files);
    }

    @Override
    public void registerSounds() {
        for (List<DeferredHolder<SoundEvent, SoundEvent>> sounds : List.of(Ic2Sounds.all(), GsSounds.all(), NcSounds.all())) {
            for (DeferredHolder<SoundEvent, SoundEvent> sound : sounds) {
                add(sound.get(), SoundDefinition.definition().with(sound(sound.getId())));
            }
        }
        // Long tracks: streamed instead of loaded into memory.
        for (MusicDiscs.Track track : MusicDiscs.tracks()) {
            add(track.sound().get(), SoundDefinition.definition().with(sound(track.sound().getId()).stream()));
        }
    }
}
