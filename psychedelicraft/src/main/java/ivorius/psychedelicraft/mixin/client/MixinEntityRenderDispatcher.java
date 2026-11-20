package ivorius.psychedelicraft.mixin.client;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.hallucination.EntityIdentitySwapHallucination;

@Mixin(EntityRenderDispatcher.class)
abstract class MixinEntityRenderDispatcher {
    @ModifyVariable(method = "render", at = @At("HEAD"), index = 1)
    private Entity swapEntity(Entity entity) {
        return DrugProperties.of((Entity)Minecraft.getInstance().player)
            .stream()
            .flatMap(properties -> properties.getHallucinations()
                    .getEntities().<EntityIdentitySwapHallucination>getHallucinations(EntityIdentitySwapHallucination.class).stream()
            )
            .map(h -> h.matchOrAttach(entity))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(entity);
    }
}
