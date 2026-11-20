package ivorius.psychedelicraft.mixin.client;

import java.util.List;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.sounds.Weighted;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WeighedSoundEvents.class)
public interface SoundsAccessor {
    @Accessor("list")
    List<Weighted<Sound>> getSounds();
}
