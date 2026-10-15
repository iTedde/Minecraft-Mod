package dev.craft.data;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Sounds;
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
        for (DeferredHolder<SoundEvent, SoundEvent> sound : Ic2Sounds.all()) {
            add(sound.get(), SoundDefinition.definition().with(sound(sound.getId())));
        }
    }
}
