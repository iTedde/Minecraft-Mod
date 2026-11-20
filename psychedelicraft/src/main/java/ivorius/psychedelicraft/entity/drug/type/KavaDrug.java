/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.type;

import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.entity.drug.DrugAttributeFunctions;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

/**
 * Created by Sollace on Feb 17 2023.
 *
 * Drowsiness, slurred speech, poor concentration, confusion, dizziness, problems with movement and memory, lowered blood pressure, slowed breathing.
 *
 */
public class KavaDrug extends SimpleDrug {
    public static final DrugAttributeFunctions FUNCTIONS = DrugAttributeFunctions.builder()
            .put(VIEW_WOBBLYNESS, 0.5F)
            .put(DROWSYNESS, 0.4F)
            .put(SOUND_VOLUME, f -> 1 - f)
            .put(SPEED, f -> 1 - (f * 0.2F))
            .put(DIG_SPEED, f -> 1 - (f * 0.2F))
            .put(DESATURATION_HALLUCINATION_STRENGTH, (f, t) -> f * 0.75f * ((Mth.clamp(t, 50, 250) - 50) / 200F))
            .put(HEART_BEAT_VOLUME, (f, t) -> MathUtils.project(f, 0.4F, 1) * 1.2F * ((Mth.clamp(t, 50, 250) - 50) / 200F))
            .build();

    public KavaDrug(double decSpeed, double decSpeedPlus) {
        super(DrugType.KAVA, decSpeed, decSpeedPlus);
    }

    @Override
    protected boolean tickSideEffects(DrugProperties properties, RandomSource random) {
        Player entity = properties.asEntity();

        double activeValue = getActiveValue();

        if ((getTicksActive() % 20) == 0 && getTicksActive() > 100 && random.nextFloat() < (activeValue - 1.3F) * 2) {
            entity.hurt(properties.damageOf(PSDamageTypes.HEART_FAILURE), (int) ((activeValue - 0.9f) * 50.0f + 4.0f));
            return true;
        }

        return super.tickSideEffects(properties, random);
    }

    @Override
    protected void tickClientEffects(DrugProperties properties, RandomSource random) {
        Player entity = properties.asEntity();
        double activeValue = getActiveValue();
        double motionEffect = Math.min(activeValue, 0.8);
        rotateEntityPitch(entity, Mth.sin(entity.tickCount / 600F * Mth.PI) / 2F * motionEffect * (random.nextFloat() + 0.5F));
        rotateEntityYaw(entity, Mth.cos(entity.tickCount / 500F * Mth.PI) / 1.3F * motionEffect * (random.nextFloat() + 0.5F));

        rotateEntityPitch(entity, Mth.sin(entity.tickCount / 180F * Mth.PI) / 3F * motionEffect * (random.nextFloat() + 0.5F));
        rotateEntityYaw(entity, Mth.cos(entity.tickCount / 150F * Mth.PI) / 2F * motionEffect * (random.nextFloat() + 0.5F));
    }
}
