package dev.craft.coppergolem;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;

/**
 * Copper golem rebuilt from MC 1.21.9 (spec 35 §2): a small passive golem that oxidizes over game time, can be
 * waxed and scraped, and never turns into a statue.
 */
public class CopperGolem extends AbstractGolem {
    /** 420–460 minutes per stage (spec 35 §2.4). */
    private static final int MIN_STAGE_TICKS = 420 * 60 * 20;
    private static final int MAX_STAGE_TICKS = 460 * 60 * 20;

    private static final EntityDataAccessor<Integer> WEATHER_STATE =
            SynchedEntityData.defineId(CopperGolem.class, EntityDataSerializers.INT);

    private boolean waxed;
    /** Game time of the next oxidation step, 0 until the first tick. */
    private long nextWeatheringTime;
    /** A bolt hits for several ticks; only its first hit counts. */
    private UUID lastLightning;

    public CopperGolem(EntityType<? extends CopperGolem> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(WEATHER_STATE, WeatherState.UNAFFECTED.ordinal());
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    public WeatherState getWeatherState() {
        return WeatherState.values()[entityData.get(WEATHER_STATE)];
    }

    public void setWeatherState(WeatherState stage) {
        entityData.set(WEATHER_STATE, stage.ordinal());
    }

    public boolean isWaxed() {
        return waxed;
    }

    public void setWaxed(boolean waxed) {
        this.waxed = waxed;
    }

    public long getNextWeatheringTime() {
        return nextWeatheringTime;
    }

    public void setNextWeatheringTime(long nextWeatheringTime) {
        this.nextWeatheringTime = nextWeatheringTime;
    }

    private static long stageTicks(RandomSource random) {
        return MIN_STAGE_TICKS + random.nextInt(MAX_STAGE_TICKS - MIN_STAGE_TICKS + 1);
    }

    private void restartWeathering() {
        nextWeatheringTime = level().getGameTime() + stageTicks(random);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            weather();
        }
    }

    /** Catches up on all stages that passed while the chunk was unloaded, since the clock is the game time. */
    private void weather() {
        long now = level().getGameTime();
        if (nextWeatheringTime == 0) {
            restartWeathering();
            return;
        }
        WeatherState stage = getWeatherState();
        while (!waxed && stage != WeatherState.OXIDIZED && now >= nextWeatheringTime) {
            stage = WeatherState.values()[stage.ordinal() + 1];
            nextWeatheringTime += stageTicks(random);
        }
        if (stage != getWeatherState()) {
            setWeatherState(stage);
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockPos pos = blockPosition();
        if (stack.is(Items.HONEYCOMB) && !waxed) {
            if (!level().isClientSide) {
                waxed = true;
                level().levelEvent(player, 3003, pos, 0);
                stack.consume(1, player);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (waxed && stack.canPerformAction(ItemAbilities.AXE_WAX_OFF)) {
            if (!level().isClientSide) {
                waxed = false;
                restartWeathering();
                scrape(player, hand, stack, SoundEvents.AXE_WAX_OFF, 3004);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (getWeatherState() != WeatherState.UNAFFECTED && stack.canPerformAction(ItemAbilities.AXE_SCRAPE)) {
            if (!level().isClientSide) {
                setWeatherState(WeatherState.values()[getWeatherState().ordinal() - 1]);
                restartWeathering();
                scrape(player, hand, stack, SoundEvents.AXE_SCRAPE, 3005);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    private void scrape(Player player, InteractionHand hand, ItemStack axe, SoundEvent sound, int levelEvent) {
        level().playSound(null, blockPosition(), sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
        level().levelEvent(player, levelEvent, blockPosition(), 0);
        axe.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
    }

    /** Lightning takes one stage off an unwaxed golem; it does not burn or hurt it. */
    @Override
    public void thunderHit(ServerLevel level, LightningBolt lightning) {
        if (lightning.getUUID().equals(lastLightning)) {
            return;
        }
        lastLightning = lightning.getUUID();
        if (!waxed && getWeatherState() != WeatherState.UNAFFECTED) {
            setWeatherState(WeatherState.values()[getWeatherState().ordinal() - 1]);
            restartWeathering();
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        playSound(SoundEvents.COPPER_STEP, 0.5F, 1.4F);
    }

    /** 1.21.1 has no copper golem sounds; the iron golem's sound higher. */
    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 1.6F;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("WeatherState", getWeatherState().ordinal());
        tag.putBoolean("Waxed", waxed);
        tag.putLong("NextWeatheringTime", nextWeatheringTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        int stage = tag.getInt("WeatherState");
        setWeatherState(WeatherState.values()[Math.clamp(stage, 0, WeatherState.values().length - 1)]);
        waxed = tag.getBoolean("Waxed");
        nextWeatheringTime = tag.getLong("NextWeatheringTime");
    }
}
