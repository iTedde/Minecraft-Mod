/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.advancement.PSCriteria;
import ivorius.psychedelicraft.entity.drug.DrugAttributeFunctions;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

/**
 * Created by lukas on 01.11.14.
 */
public class AlcoholDrug extends SimpleDrug {
    public static final DrugAttributeFunctions FUNCTIONS = DrugAttributeFunctions.builder()
            .put(VIEW_WOBBLYNESS, 0.5F)
            .put(DOUBLE_VISION, f -> MathUtils.project(f, 0.25F, 1))
            .put(MOTION_BLUR, f -> MathUtils.project(f, 0.5F, 1) * 0.3F)
            .build();

    public AlcoholDrug(DrugType<AlcoholDrug> type, double decSpeed, double decSpeedPlus) {
        super(type, decSpeed, decSpeedPlus);
    }

    @Override
    protected boolean tickSideEffects(DrugProperties properties, RandomSource random) {
        Player entity = properties.asEntity();

        double activeValue = getActiveValue();

        if ((getTicksActive() % 20) == 0 && random.nextFloat() < (activeValue - 0.9F) * 2) {
            PSDamageTypes.damage(entity.level(), entity, entity.level().damageSources().source(PSDamageTypes.ALCOHOL_POSIONING), (int) ((activeValue - 0.9F) * 50 + 4));
            if (entity.isDeadOrDying()) {
                return true;
            }
        }

        return super.tickSideEffects(properties, random);
    }

    @Override
    protected void tickClientEffects(DrugProperties properties, RandomSource random) {
        Player entity = properties.asEntity();
        double activeValue = getActiveValue();
        double motionEffect = Math.min(activeValue, 0.8) + (Mth.clamp(entity.getDeltaMovement().length(), 0, 1) * 3) * activeValue;
        rotateEntityPitch(entity, Mth.sin(entity.tickCount / 600F * Mth.PI) / 2F * motionEffect * (random.nextFloat() + 0.5F));
        rotateEntityYaw(entity, Mth.cos(entity.tickCount / 500F * Mth.PI) / 1.3F * motionEffect * (random.nextFloat() + 0.5F));

        rotateEntityPitch(entity, Mth.sin(entity.tickCount / 180F * Mth.PI) / 3F * motionEffect * (random.nextFloat() + 0.5F));
        rotateEntityYaw(entity, Mth.cos(entity.tickCount / 150F * Mth.PI) / 2F * motionEffect * (random.nextFloat() + 0.5F));
    }

    @Override
    public void onWakeUp(DrugProperties drugProperties) {
        double value = getActiveValue();
        super.onWakeUp(drugProperties);

        if (value > 0) {
            Player player = drugProperties.asEntity();
            RandomSource random = player.level().random;

            if (random.nextFloat() > (1 - value)) {
                player.animateHurt(random.nextFloat() * Mth.TWO_PI);
                player.playSound(SoundEvents.PLAYER_HURT, 1, 1);
                drugProperties.addToDrug(DrugType.SLEEP_DEPRIVATION, 0.25F);
                PSCriteria.HANGOVER.trigger(player);
            }
        }
    }
}
