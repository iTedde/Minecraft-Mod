package ivorius.psychedelicraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import ivorius.psychedelicraft.entity.AddictTaskListProvider;
import ivorius.psychedelicraft.entity.DealerTaskListProvider;
import ivorius.psychedelicraft.entity.PSTradeOffers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.ResetProfession;
import net.minecraft.world.entity.ai.behavior.VillagerGoalPackages;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

@Mixin(ResetProfession.class)
abstract class MixinLoseJobOnSiteLossTask {
    @Inject(method = "lambda$create$0(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)Z", at = @At("HEAD"), cancellable = true)
    private static void onTryLoseJobSite(ServerLevel world, Villager entity, long time,
            CallbackInfoReturnable<Boolean> info) {
        if (entity.getVillagerData().getProfession() == PSTradeOffers.DRUG_ADDICT_PROFESSION) {
            info.setReturnValue(false);
        }
    }
}

@Mixin(VillagerGoalPackages.class)
abstract class MixinVillagerTaskListProvider {
    @Shadow
    static Pair<Integer, BehaviorControl<LivingEntity>> getMinimalLookBehavior() { return null; }

    @Inject(method = "getWorkPackage(Lnet/minecraft/world/entity/npc/VillagerProfession;F)Lcom/google/common/collect/ImmutableList;", at = @At("HEAD"), cancellable = true)
    private static void onCreateWorkTasks(VillagerProfession profession, float speed,
            CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>>> info) {
        if (profession == PSTradeOffers.DRUG_DEALER_PROFESSION) {
            info.setReturnValue(DealerTaskListProvider.createWorkTasks(getMinimalLookBehavior(), speed));
        }
        if (profession == PSTradeOffers.DRUG_ADDICT_PROFESSION) {
            info.setReturnValue(AddictTaskListProvider.createWorkTasks(getMinimalLookBehavior(), speed));
        }
    }
}