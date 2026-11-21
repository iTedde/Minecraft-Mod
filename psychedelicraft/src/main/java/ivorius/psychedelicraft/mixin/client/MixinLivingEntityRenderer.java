package ivorius.psychedelicraft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.entity.AddictTaskListProvider;
import ivorius.psychedelicraft.entity.PSTradeOffers;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.player.Player;

@Mixin(LivingEntityRenderer.class)
abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {
    MixinLivingEntityRenderer() { super(null); }

    @Inject(method = "render",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V",
                shift = Shift.AFTER))
    private void onRender(
            T entity,
            float yaw, float tickDelta,
            PoseStack matrices,
            MultiBufferSource vertices,
            int light,
            CallbackInfo into) {
        if (entity instanceof Player player) {
            DrugRenderer.INSTANCE.poseModel(player, (HumanoidModel<?>)getModel());
        }
    }

    @ModifyVariable(method = "setupRotations", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private float changeBodyYaw(float bodyYaw, T entity) {
        if (entity instanceof VillagerDataHolder v && v.getVillagerData().getProfession() == PSTradeOffers.DRUG_ADDICT_PROFESSION) {
            bodyYaw += AddictTaskListProvider.getShakeAmount(entity);
        }

        return bodyYaw;
    }
}
