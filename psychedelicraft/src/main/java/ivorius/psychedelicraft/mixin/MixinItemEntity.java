package ivorius.psychedelicraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ivorius.psychedelicraft.item.TickableItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

@Mixin(ItemEntity.class)
abstract class MixinItemEntity extends Entity {
    private MixinItemEntity() { super(null, null); }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo info) {
        ItemEntity self = (ItemEntity)(Object)this;
        if (self.getItem().getItem() instanceof TickableItem tickable) {
            tickable.onGroundTick(self);
        }
    }
}
