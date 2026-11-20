package ivorius.psychedelicraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.advancement.PSCriteria;
import ivorius.psychedelicraft.entity.PSTradeOffers;
import ivorius.psychedelicraft.item.PSItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

@Mixin(Villager.class)
abstract class MixinVillagerEntity extends AbstractVillager implements VillagerDataHolder {
    MixinVillagerEntity() { super(null, null); }

    @Inject(method = "mobInteract",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onInteractMob(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> info) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(PSItems.HASH_MUFFIN)) {
            if (isBaby()) {
                if (!level().isClientSide) {
                    PSCriteria.FEED_BABY_VILLAGER.trigger(player, this);
                }
            } else {
                VillagerProfession profession = getVillagerData().getProfession();
                if (profession == VillagerProfession.NITWIT || profession == VillagerProfession.NONE) {
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    if (!level().isClientSide) {
                        level().playSound(null, this, SoundEvents.GENERIC_EAT, getSoundSource(),
                                1 + random.nextFloat(),
                                random.nextFloat() * 0.7F + 0.3F
                        );
                        setVillagerData(getVillagerData().setProfession(PSTradeOffers.DRUG_ADDICT_PROFESSION));
                        ((Villager)(Object)this).refreshBrain((ServerLevel)level());
                        PSCriteria.FEED_VILLAGER.trigger(player);
                    }
                    info.setReturnValue(InteractionResult.SUCCESS);
                } else {
                    info.setReturnValue(InteractionResult.CONSUME);
                }
            }
        }
    }

    @Inject(method = "rewardTradeXp", at = @At("RETURN"))
    private void onAfterUsing(MerchantOffer offer, CallbackInfo info) {
        if (getVillagerData().getProfession() == PSTradeOffers.DRUG_ADDICT_PROFESSION) {
            hurt(PSDamageTypes.create(level(), PSDamageTypes.OVERDOSE), (offer.getUses() * offer.getResult().getCount()) + 1);
        }
    }
}
