/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug;

import ivorius.psychedelicraft.*;
import ivorius.psychedelicraft.advancement.PSCriteria;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.entity.*;
import ivorius.psychedelicraft.entity.drug.hallucination.HallucinationManager;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluenceInstance;
import ivorius.psychedelicraft.entity.drug.sound.DrugMusicManager;
import ivorius.psychedelicraft.entity.effect.PSEffects;
import ivorius.psychedelicraft.item.PacifierItem;
import ivorius.psychedelicraft.network.Channel;
import ivorius.psychedelicraft.network.MsgDrugProperties;
import ivorius.psychedelicraft.particle.DrugDustParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

public class DrugProperties implements NbtSerialisable {
    public static final ResourceLocation DRUG_EFFECT = Psychedelicraft.id("drugs");
    public static final int MAX_CARDIAC_ARREST_TIME = 2000;
    public static final int RECOVERY_COOLDOWN = 20;

    private static final Codec<Map<DrugType<?>, Drug>> DRUGS_CODEC = Codec.unboundedMap(DrugType.REGISTRY.byNameCodec(), Drug.CODEC);

    private final Map<DrugType<?>, Drug> drugs = DrugType.REGISTRY.stream().collect(Collectors.toMap(Function.identity(), DrugType::create));
    private final List<DrugInfluenceInstance> influences = new ArrayList<>();

    private boolean initial;
    private boolean dirty;

    private final HallucinationManager hallucinations = new HallucinationManager(this);
    private final DrugMusicManager soundManager = new DrugMusicManager(this);

    private int timeBreathingSmoke;
    @Nullable
    private Vector3f breathSmokeColor;

    private final Player entity;

    private final Stomach stomach;
    private final Hangover hangover;

    private float teethGrindingRate;
    private int pacifierSqueakDelay = -1;

    private int cancerCountdown = -1;
    private boolean prevHadCancer = false;

    private int prevCardiacArrestTicks;
    private int cardiacArrestTicks;
    private int cardiacArrestRegenCooldown;

    private int strokeIntensity;
    private int strokeRecoveryCooldown;

    public DrugProperties(Player entity) {
        this.entity = entity;
        this.stomach = new Stomach(this);
        this.hangover = new Hangover(this);
    }

    public static DrugProperties of(Player player) {
        return ((DrugPropertiesContainer)player).getDrugProperties();
    }

    public static Stream<DrugProperties> stream(Entity entity) {
        if (entity instanceof DrugPropertiesContainer c) {
            return Stream.of(c.getDrugProperties());
        }
        return Stream.empty();
    }

    public static Optional<DrugProperties> of(Entity entity) {
        if (entity instanceof DrugPropertiesContainer c) {
            return Optional.of(c.getDrugProperties());
        }
        return Optional.empty();
    }

    public Player asEntity() {
        return entity;
    }

    public DamageSource damageOf(ResourceKey<DamageType> type) {
        return PSDamageTypes.create(entity.level(), type);
    }

    public Hangover getHangover() {
        return hangover;
    }

    public Stomach getStomach() {
        return stomach;
    }

    public void markDirty() {
        dirty = true;
    }

    public HallucinationManager getHallucinations() {
        return hallucinations;
    }

    public DrugMusicManager getMusicManager() {
        return soundManager;
    }

    @SuppressWarnings("unchecked")
    public <T extends Drug> T getDrug(DrugType<T> type) {
        return (T)drugs.computeIfAbsent(type, DrugType::create);
    }

    public float getDrugValue(DrugType<?> type) {
        return (float)getDrug(type).getActiveValue();
    }

    public boolean isDrugActive(DrugType<?> type) {
        return getDrugValue(type) > Mth.EPSILON;
    }

    public boolean isTripping() {
        float f = getModifier(Drug.MOVEMENT_HALLUCINATION_STRENGTH)
                + getModifier(Drug.CONTEXTUAL_HALLUCINATION_STRENGTH)
                + getModifier(Drug.COLOR_HALLUCINATION_STRENGTH);
        return f > 0.7F;
    }

    public void addToDrug(DrugType<?> type, double effect) {
        getDrug(type).addToDesiredValue(effect);
        PSCriteria.DRUG_EFFECTS_CHANGED.trigger(this);
        markDirty();
    }

    public void addToDrug(DrugType<?> type, double effect, DrugInfluenceInstance influence) {
        getDrug(type).addToDesiredValue(effect, influence);
        PSCriteria.DRUG_EFFECTS_CHANGED.trigger(this);
        markDirty();
    }

    public void setDrugValue(DrugType<?> type, double effect) {
        getDrug(type).setDesiredValue(effect);
        PSCriteria.DRUG_EFFECTS_CHANGED.trigger(this);
        markDirty();
    }

    public void addToDrug(DrugInfluence influence) {
        influences.add(new DrugInfluenceInstance(influence));
        markDirty();
    }

    public void addAll(Iterable<DrugInfluence> influences) {
        influences.forEach(influence -> this.influences.add(new DrugInfluenceInstance(influence)));
        markDirty();
    }

    public Collection<Drug> getAllDrugs() {
        return drugs.values();
    }

    public Set<DrugType<?>> getAllDrugNames() {
        return drugs.keySet();
    }

    public void startBreathingSmoke(int time, Vector3f color) {
        this.breathSmokeColor = color;
        this.timeBreathingSmoke = time + 10; //10 is the time spent breathing in
        markDirty();

        entity.level().playSound(entity, entity, PSSounds.ENTITY_PLAYER_BREATH, SoundSource.PLAYERS, 0.02F, 1.5F);
    }

    public boolean cureAll() {
        boolean changed = cancerCountdown != -1
                || teethGrindingRate > 0
                || breathSmokeColor != null
                || timeBreathingSmoke != 0
                || pacifierSqueakDelay != -1
                || cardiacArrestTicks != 0
                || strokeIntensity != 0
                || influences.stream().anyMatch(i -> !i.isOf(DrugType.SUGAR) && i.isOf(DrugType.SLEEP_DEPRIVATION))
                || drugs.values().stream().anyMatch(i -> i.getType() != DrugType.SUGAR && i.getType() != DrugType.SLEEP_DEPRIVATION && i.getActiveValue() > 0.1);
        cancerCountdown = -1;
        teethGrindingRate = 0;
        breathSmokeColor = null;
        timeBreathingSmoke = 0;
        pacifierSqueakDelay = -1;
        prevCardiacArrestTicks = 0;
        cardiacArrestTicks = 0;
        cardiacArrestRegenCooldown = 0;
        strokeIntensity = 0;
        strokeRecoveryCooldown = 0;
        influences.clear();
        drugs.values().forEach(i -> i.setDesiredValue(0));
        changed |= stomach.reset();
        markDirty();
        return changed;
    }

    public boolean hasCancer() {
        return cancerCountdown > -1;
    }

    public float getCancerProgression() {
        return !hasCancer() || cancerCountdown >= 10_000 ? 0 : (1 - (cancerCountdown / 10_000F));
    }

    public boolean rollCancerDance() {
        if (entity.level().isClientSide) {
            return false;
        }

        if (cancerCountdown < 0) {
            if (entity.level().random.nextInt(1_000_000_000) == 0) {
                cancerCountdown = 10_000 + entity.level().random.nextInt(1000);
                markDirty();
                PSCriteria.CANCER.trigger(entity);
                return true;
            }
        } else {
            cancerCountdown = Math.max(10, -1 - entity.level().random.nextInt(10));
            markDirty();
        }

        return false;
    }

    public void increaseTeethGrindingSideEffect() {
        if (entity.tickCount % 10 == 0) {
            teethGrindingRate = Mth.clamp(teethGrindingRate + 0.001F, 0, 100);
        }
    }

    public void increaseCardiacArrestSideEffect() {
        cardiacArrestTicks ++;
        cardiacArrestRegenCooldown = RECOVERY_COOLDOWN;
        markDirty();
    }

    public float getCardiacArrestProgress(float delta) {
        return Mth.clamp(Mth.lerp(delta, prevCardiacArrestTicks, (float)this.cardiacArrestTicks) / MAX_CARDIAC_ARREST_TIME, 0, 1);
    }

    public boolean rollStroke() {
        if (getCardiacArrestProgress(1) > 0.25F) {
            return false;
        }
        int baseDamage = 1;
        strokeIntensity++;
        strokeRecoveryCooldown = RECOVERY_COOLDOWN;

        PSDamageTypes.damage(entity.level(), entity, damageOf(PSDamageTypes.STROKE), Math.min(baseDamage + strokeIntensity, 10));
        return entity.isDeadOrDying();
    }

    public boolean isBreathingSmoke() {
        return timeBreathingSmoke > 0;
    }

    public int getAge() {
        return entity.tickCount;
    }

    public void onTick() {
        //4 times / sec is enough
        if (entity.tickCount % 5 == 0 && influences.removeIf(influence -> influence.update(this))) {
            markDirty();
        }

        if (strokeRecoveryCooldown > 0 && --strokeRecoveryCooldown <= 0) {
            strokeIntensity = 0;
        }

        if (cancerCountdown >= 0 && cancerCountdown <= 10 && --cancerCountdown == 0) {
            prevHadCancer = false;
            cancerCountdown = -1;
            PSDamageTypes.damage(entity.level(), entity, damageOf(PSDamageTypes.CANCER), Integer.MAX_VALUE);
        }

        prevCardiacArrestTicks = cardiacArrestTicks;
        if (getModifier(Drug.HEART_BEAT_SPEED) <= 0.5F) {
            int tries = 10 + entity.level().random.nextInt(50);
            for (int i = 0; i < tries; i++) {
                if (entity.level().random.nextInt(2) == 0) {
                    increaseCardiacArrestSideEffect();
                }
            }
        } else if (cardiacArrestTicks > 0 && getModifier(Drug.HEART_BEAT_SPEED) > 0.8F && getModifier(Drug.HEART_BEAT_SPEED) < 2.8F) {
            cardiacArrestTicks *= 0.5F;
        }

        if (cardiacArrestTicks > 0 && (cardiacArrestRegenCooldown <= 0 || --cardiacArrestRegenCooldown <= 0)) {
            cardiacArrestTicks--;
        }

        drugs.values().forEach(drug -> drug.update(this));

        stomach.onTick();
        hangover.onTick();
        soundManager.update();

        RandomSource random = entity.getRandom();

        if (entity.level().isClientSide) {
            hallucinations.update();

            if (entity.onGround() && random.nextFloat() < getModifier(Drug.JUMP_CHANCE)) {
                ((LivingEntityDuck)entity).invokeJump();
            }

            if (!entity.swinging && random.nextFloat() < getModifier(Drug.PUNCH_CHANCE)) {
                entity.swing(InteractionHand.MAIN_HAND);
            }
        } else {
            if (random.nextFloat() < getModifier(Drug.DROWSYNESS)) {
                entity.causeFoodExhaustion(0.05F);
            }

            if (!entity.hasEffect(PSEffects.TEETH_GRINDING) && ((getDrugValue(DrugType.METHAMPHETAMINE) <= 0.001F
                    && teethGrindingRate > 1
                    && entity.tickCount % 200 == 0
                    && random.nextFloat() * (entity.isSleeping() ? 2 : 1) < teethGrindingRate / 100F))) {
                PSCriteria.SIDE_EFFECT.trigger(entity);
                entity.addEffect(new MobEffectInstance(PSEffects.TEETH_GRINDING, 1000));
                if (!PacifierItem.consumePacifier(entity)) {
                    teethGrindingRate = Math.max(0, teethGrindingRate - 0.00001F);
                    entity.hurt(damageOf(PSDamageTypes.TEETH_GRINDING), 1);
                } else {
                    pacifierSqueakDelay = 5 + entity.getRandom().nextInt(15);
                    entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), PSSounds.ENTITY_PLAYER_PACIFIER_SQUEAK,
                            entity.getSoundSource(),
                            (float)entity.getRandom().triangle(1, 0.2F),
                            (float)entity.getRandom().triangle(1, 0.2F)
                    );
                    entity.level().gameEvent(entity, GameEvent.BLOCK_PLACE, entity.blockPosition());
                    PSCriteria.SUCK_PACIFIER.trigger(entity);
                }
            }

            if (pacifierSqueakDelay > 0 && --pacifierSqueakDelay == 0) {
                entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), PSSounds.ENTITY_PLAYER_PACIFIER_SQUEAK,
                        entity.getSoundSource(),
                        (float)entity.getRandom().triangle(1, 0.2F),
                        (float)entity.getRandom().triangle(1, 0.2F)
                );
                entity.level().gameEvent(entity, GameEvent.BLOCK_PLACE, entity.blockPosition());
            }

            if (entity.isOnFire()) {
                BlockPos headPos = BlockPos.containing(entity.getEyePosition());
                if (entity.level().getBlockState(headPos).is(PSBlocks.FLAMMABLE_GAS)) {
                    entity.level().setBlockAndUpdate(headPos, Blocks.FIRE.defaultBlockState());
                }
            }

            if (Psychedelicraft.getConfig().randomTicksUntilRiftSpawn.get() > 0
                    && random.nextInt(Psychedelicraft.getConfig().randomTicksUntilRiftSpawn.get()) == 0) {
                RealityRiftEntity.spawn(entity);
            }

            if (cardiacArrestTicks > MAX_CARDIAC_ARREST_TIME) {
                PSCriteria.HEART_ATTACK.trigger(entity);
                if (random.nextInt(200) == 0 || entity.isSleeping()) {
                    cardiacArrestTicks = 0;
                    PSDamageTypes.damage(entity.level(), entity, damageOf(PSDamageTypes.HEART_ATTACK), Integer.MAX_VALUE);
                }
            }
        }

        if (entity.isAlive()) {
            if (prevHadCancer && !hasCancer()) {
                PSCriteria.CURE_CANCER.trigger(entity);
            }
            prevHadCancer = hasCancer();
        } else {
            prevHadCancer = false;
        }

        if (isBreathingSmoke()) {
            timeBreathingSmoke--;

            if (timeBreathingSmoke > 10 && entity.level().isClientSide) {
                if (random.nextInt(2) == 0) {
                    ParticleHelper.spawnParticleAtFace(entity, new DrugDustParticleEffect(PSParticles.EXHALED_SMOKE, breathSmokeColor, 1), random.nextFloat() * 0.05F + 0.1F);
                }

                if (random.nextInt(5) == 0) {
                    ParticleHelper.spawnParticleAtFace(entity, new DrugDustParticleEffect(PSParticles.EXHALED_SMOKE, breathSmokeColor, 2.5F), random.nextFloat() * 0.05F + 0.1F);
                }
            }
        }

        float speed = getModifier(Drug.SPEED) - getCardiacArrestProgress(1) * 0.25F;

        changeDrugModifierMultiply(entity, Attributes.MOVEMENT_SPEED, speed);
        changeDrugModifierMultiply(entity, Attributes.ATTACK_SPEED, speed);

        if (hasCancer() && entity.tickCount % 10 == 0) {
            float progression = getCancerProgression();
            float saturation = entity.getFoodData().getSaturationLevel();
            entity.getFoodData().setSaturation(saturation * (1 - progression));
            entity.causeFoodExhaustion(progression);
        }

        if (dirty) {
            dirty = false;
            sendCapabilities();
        }
    }

    public void sendCapabilities() {
        if (!entity.level().isClientSide) {
            var message = new MsgDrugProperties(this, entity.registryAccess());
            Channel.UPDATE_DRUG_PROPERTIES.sendToSurroundingPlayers(message, entity);
            // We have to ensure it's sent to ourselves as well (Send to surrounding players ends to us but that doesn't seem to work when loading into a world??)
            Channel.UPDATE_DRUG_PROPERTIES.sendToPlayer(message, (ServerPlayer)entity);
        }
    }

    public CompoundTag toTrackedNbt(CompoundTag compound, Provider lookup) {
        dirty = false;
        DRUGS_CODEC.encodeStart(NbtOps.INSTANCE, drugs).result().ifPresent(drugs -> compound.put("Drugs", drugs));
        DrugInfluenceInstance.LIST_CODEC.encodeStart(NbtOps.INSTANCE, influences).result().ifPresent(influenceTagList -> compound.put("drugInfluences", influenceTagList));
        compound.put("stomach", stomach.toNbt(lookup));
        compound.put("hangover", hangover.toNbt(lookup));
        compound.putInt("cancerCountdown", cancerCountdown);
        compound.putFloat("teethGrindingRate", teethGrindingRate);
        if (initial) {
            compound.put("soundManager", soundManager.toNbt(lookup));
            initial = false;
        }

        return compound;
    }

    public void fromTrackedNbt(CompoundTag compound, Provider lookup) {
        dirty = false;
        drugs.clear();
        DRUGS_CODEC.decode(NbtOps.INSTANCE, compound.getCompound("Drugs")).result().map(Pair::getFirst).ifPresent(drugs::putAll);
        influences.clear();
        DrugInfluenceInstance.LIST_CODEC.decode(NbtOps.INSTANCE, compound.getList("drugInfluences", Tag.TAG_COMPOUND)).result().map(Pair::getFirst).ifPresent(influences::addAll);
        stomach.fromNbt(compound.getCompound("stomach"), lookup);
        hangover.fromNbt(compound.getCompound("hangover"), lookup);
        cancerCountdown = compound.contains("cancerCountdown", Tag.TAG_INT) ? compound.getInt("cancerCountdown") : -1;
        teethGrindingRate = compound.getFloat("teethGrindingRate");
        if (compound.contains("soundManager", Tag.TAG_COMPOUND)) {
            soundManager.fromNbt(compound.getCompound("soundManager"), lookup);
        }
    }

    @Override
    public void fromNbt(CompoundTag compound, Provider lookup) {
        fromTrackedNbt(compound, lookup);
        initial = true;
    }

    @Override
    public void toNbt(CompoundTag compound, Provider lookup) {
        toTrackedNbt(compound, lookup);
    }

    public void copyFrom(DrugProperties old, boolean alive) {
        if (alive) {
            influences.clear();
            influences.addAll(old.influences);
            drugs.clear();
            drugs.putAll(old.drugs);
            timeBreathingSmoke = old.timeBreathingSmoke;
            breathSmokeColor = old.breathSmokeColor;
            teethGrindingRate = old.teethGrindingRate;
            pacifierSqueakDelay = old.pacifierSqueakDelay;
            cancerCountdown = old.cancerCountdown;
            prevHadCancer = old.prevHadCancer;
        } else {
            cancerCountdown = -1;
            prevHadCancer = false;
            initial = true;
        }
        soundManager.copyFrom(old.soundManager, alive);
        stomach.copyFrom(old.getStomach(), alive);
        hangover.copyFrom(old.getHangover(), alive);
        markDirty();
    }

    public boolean onAwoken() {
        drugs.values().forEach(drug -> drug.onWakeUp(this));
        influences.clear();
        stomach.reset();
        markDirty();

        // TODO: (Sollace) Implement longer sleeping/comas
        return true;
    }

    public boolean canResetTimeBySleeping(boolean passedBaseCheck) {
        if (getSleepTimeModifier() > 1) {
            return passedBaseCheck && entity.getSleepTimer() >= 100;
        }
        return entity.isSleeping() && entity.getSleepTimer() >= 100;
    }

    public int getSleepTimer(int sleepTime) {
        return Math.min(Mth.ceil(sleepTime / getSleepTimeModifier()), 100);
    }

    private float getSleepTimeModifier() {
        return 1 + Mth.clamp((getDrugValue(DrugType.CAFFEINE) + getDrugValue(DrugType.COCAINE)) / 2F, 0, 1) - getDrugValue(DrugType.ALCOHOL) * 0.9F;
    }

    public float onDamaged(DamageSource source, float initial) {
        if (source.is(DamageTypes.FELL_OUT_OF_WORLD) || initial >= Integer.MAX_VALUE) {
            return initial;
        }

        initial *= getModifier(Drug.PAIN_SUPPRESSION);
        if (hasCancer()) {
            initial *= (1 + getCancerProgression());
        }

        return initial;
    }

    public Optional<Component> trySleep(BlockPos pos) {
        return getAllDrugs().stream().flatMap(drug -> drug.trySleep(pos).stream()).findFirst();
    }

    public float getModifier(Attribute modifier) {
        return modifier.get(this);
    }

    private void changeDrugModifierMultiply(LivingEntity entity, Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double value) {
        // 2: ret *= 1.0 + value
        changeDrugModifier(entity, attribute, value - 1.0, Operation.ADD_MULTIPLIED_TOTAL);
    }

    private void changeDrugModifier(LivingEntity entity, Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double value, Operation operation) {
        AttributeInstance speedInstance = entity.getAttribute(attribute);
        speedInstance.removeModifier(DRUG_EFFECT);
        speedInstance.addTransientModifier(new AttributeModifier(DRUG_EFFECT, value, operation));
    }
}
