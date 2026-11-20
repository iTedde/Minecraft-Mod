package ivorius.psychedelicraft.mixin;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Either;

import ivorius.psychedelicraft.entity.drug.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(Player.class)
abstract class MixinPlayerEntity extends LivingEntity implements DrugPropertiesContainer {
    MixinPlayerEntity() {super(null, null);}

    @Nullable
    private Supplier<DrugProperties> drugProperties = Suppliers.memoize(() -> new DrugProperties((Player)(Object)this));

    @Override
    public DrugProperties getDrugProperties() {
        return drugProperties.get();
    }

    @Inject(method = "isSleepingLongEnough", at = @At("RETURN"), cancellable = true)
    private void onCanResetTimeBySleeping(CallbackInfoReturnable<Boolean> info) {
        info.setReturnValue(getDrugProperties().canResetTimeBySleeping(info.getReturnValue()));
    }

    @Inject(method = "getSleepTimer", at = @At("RETURN"), cancellable = true)
    private void onGetSleepTimer(CallbackInfoReturnable<Integer> info) {
        info.setReturnValue(getDrugProperties().getSleepTimer(info.getReturnValue()));
    }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void afterTick(CallbackInfo info) {
        getDrugProperties().onTick();
    }

    @Inject(method = "stopSleepInBed(ZZ)V", at = @At("HEAD"), cancellable = true)
    private void onWakeUp(boolean skipSleepTimer, boolean updateSleepingPlayers, CallbackInfo info) {
        if (!getDrugProperties().onAwoken()) {
            info.cancel();
        }
    }

    @Inject(method = "startSleepInBed(Lnet/minecraft/core/BlockPos;)Lcom/mojang/datafixers/util/Either;",
            at = @At("HEAD"),
            cancellable = true)
    private void onTrySleep(BlockPos pos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> info) {
        if (!level().isClientSide) {
            getDrugProperties().trySleep(pos).ifPresent(reason -> {
                ((Player)(Object)this).displayClientMessage(reason, true);

                info.setReturnValue(Either.right(Unit.INSTANCE));
            });
        }
    }

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void onGetBlockBreakingSpeed(BlockState block, CallbackInfoReturnable<Float> info) {
        info.setReturnValue(info.getReturnValue() * getDrugProperties().getModifier(Drug.DIG_SPEED));
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void onWriteCustomDataToTag(CompoundTag tag, CallbackInfo info) {
        tag.put("psychedelicraft_drug_properties", getDrugProperties().toNbt(registryAccess()));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void onReadCustomDataFromTag(CompoundTag tag, CallbackInfo info) {
        if (tag.contains("psychedelicraft_drug_properties", Tag.TAG_COMPOUND)) {
            getDrugProperties().fromNbt(tag.getCompound("psychedelicraft_drug_properties"), registryAccess());
        }
    }
}
