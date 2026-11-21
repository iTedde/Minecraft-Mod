package ivorius.psychedelicraft.item.component;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.phys.EntityHitResult;
import org.joml.Vector3f;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.advancement.PSCriteria;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.util.RaytraceUtil;

public record ItemDrugs(List<DrugInfluence> influences, Optional<Vector3f> smokeColor) implements TooltipProvider {
    public static final Vector3f DEFAULT_SMOKE_COLOR = new Vector3f(1, 1, 1);

    public static final ItemDrugs EMPTY = new ItemDrugs(List.of(), Optional.empty());
    public static final Codec<ItemDrugs> CODEC = RecordCodecBuilder.create(i -> i.group(
            DrugInfluence.CODEC.listOf().fieldOf("influences").forGetter(ItemDrugs::influences),
            DrugInfluence.COLOR_CODEC.optionalFieldOf("smoke_color").forGetter(ItemDrugs::smokeColor)
    ).apply(i, ItemDrugs::of));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemDrugs> PACKET_CODEC = StreamCodec.composite(
            DrugInfluence.PACKET_CODEC.apply(ByteBufCodecs.list()), ItemDrugs::influences,
            DrugInfluence.COLOR_PACKET_CODEC, ItemDrugs::smokeColor,
            ItemDrugs::of
    );

    public static ItemDrugs of(List<DrugInfluence> influences) {
        return of(influences, Optional.empty());
    }

    public static ItemDrugs of(List<DrugInfluence> influences, Optional<Vector3f> smokeColor) {
        return new ItemDrugs(influences, smokeColor);
    }

    public static ItemDrugs of(DrugInfluence...influences) {
        return of(List.of(influences));
    }

    public ItemDrugs {
        influences = List.copyOf(influences);
    }

    public ItemDrugs withSmoke(Vector3f smokeColor) {
        return new ItemDrugs(influences, Optional.of(smokeColor));
    }

    public static ItemDrugs get(ItemStack stack) {
        return stack.getOrDefault(PSComponents.DRUGS, EMPTY);
    }

    public void applyTo(ItemStack stack, DrugProperties properties) {
        Impurities impurities = Impurities.get(stack);
        properties.addAll(impurities.modifyEffects(influences));
        if (impurities.impurities().contains(Impurities.Impurity.SILICA)) {
            properties.asEntity().hurt(properties.damageOf(PSDamageTypes.GLASS_SHARD), 1.5F);
            properties.asEntity().makeSound(PSSounds.ITEM_BROKEN_GLASS_EAT);
        }
        smokeColor.ifPresent(smokeColor -> {
            properties.startBreathingSmoke(10 + properties.asEntity().level().random.nextInt(10), smokeColor);
            properties.rollCancerDance();

            EntityHitResult hit = RaytraceUtil.raycastEntities(properties.asEntity(), 3);

            if (hit != null) {
                PSCriteria.BREATHE_SMOKE_ON_ENTITY.trigger(properties.asEntity(), hit.getEntity());
                DrugProperties.of(hit.getEntity()).ifPresent(target -> {
                    target.addAll(influences.stream().map(i -> i.copyWithTarget(i.target() * 0.1F)).toList());
                    if (target.asEntity().level().random.nextInt(10) == 0) {
                        if (target.rollCancerDance()) {
                            PSCriteria.CANCER.trigger(target.asEntity(), properties.asEntity());
                        }
                    }
                });
                if (hit.getEntity() instanceof LivingEntity l) {
                    if (l instanceof Mob mob) {
                        mob.playAmbientSound();
                    }
                    if (l instanceof AbstractVillager villager) {
                        villager.setUnhappyCounter(100);
                    } else {
                        hit.getEntity().hurt(properties.asEntity().damageSources().playerAttack(properties.asEntity()), 0.1F);
                    }
                    if (l instanceof ReputationEventHandler observer) {
                        observer.onReputationEventFrom(ReputationEventType.VILLAGER_HURT, properties.asEntity());
                    }
                }
            }
        });
    }

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> tooltip, TooltipFlag type) {
        if (type.isAdvanced() && !influences.isEmpty()) {
            tooltip.accept(Component.translatable("psychedelicraft.item.contained_drug_effects").withStyle(ChatFormatting.GRAY));

            influences.forEach(influence -> {
                tooltip.accept(Component.translatable("psychedelicraft.item.contained_drug_effects.entry",
                        influence.drugType().id(),
                        Component.literal(String.format(Math.abs(influence.target()) > Mth.EPSILON ? "%.2f" : "%.0f", influence.target())),
                        Component.literal(String.format(Math.abs(influence.base()) > Mth.EPSILON ? "%.3f" : "%.0f", influence.base()))
                ).withStyle(ChatFormatting.DARK_PURPLE));
            });
        }
    }
}
