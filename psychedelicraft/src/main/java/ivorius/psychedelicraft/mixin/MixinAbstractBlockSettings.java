package ivorius.psychedelicraft.mixin;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockBehaviour.Properties.class)
public interface MixinAbstractBlockSettings {
    @Accessor("offsetFunction")
    void setOffsetter(BlockBehaviour.OffsetFunction offsetter);
}
