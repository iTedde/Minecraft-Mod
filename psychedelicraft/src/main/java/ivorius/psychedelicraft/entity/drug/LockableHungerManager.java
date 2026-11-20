package ivorius.psychedelicraft.entity.drug;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.food.FoodData;
import org.jetbrains.annotations.Nullable;

public interface LockableHungerManager {
    default void lockHunger(int hunger, float saturation, boolean full, float strength) {
        setLockedState(new State(new Ratio(hunger, strength), new Ratio(saturation, strength), full));
    }

    default void lockHunger(boolean full, float ratio) {
        lockHunger(getHungerManager().getFoodLevel(), getHungerManager().getSaturationLevel(), full, ratio);
    }

    default void unlockHunger() {
        setLockedState(null);
    }

    float getActualSaturationLevel();

    int getActualFoodLevel();

    void setShanksShaking(boolean shaking);

    default void makePermanent() {
        if (getLockedState() != null) {
            FoodData hunger = getHungerManager();
            float saturation = hunger.getSaturationLevel();
            int food = hunger.getFoodLevel();
            unlockHunger();
            hunger.setSaturation(saturation);
            hunger.setFoodLevel(food);
        }
    }

    FoodData getHungerManager();

    @Nullable
    State getLockedState();

    void setLockedState(State state);

    record State(Ratio hunger, Ratio saturation, boolean full) {
        static State fromNbt(CompoundTag compound) {
            return new State(
                    Ratio.fromNbt(compound.getCompound("hunger")),
                    Ratio.fromNbt(compound.getCompound("saturation")),
                    compound.getBoolean("full")
            );
        }

        public CompoundTag toNbt() {
            CompoundTag compound = new CompoundTag();
            compound.put("hunger", hunger.toNbt());
            compound.put("saturation", saturation.toNbt());
            compound.putBoolean("full", full);
            return compound;
        }

        public void setRate(float rate) {
            hunger.setRate(rate);
            saturation.setRate(rate);
        }
    }

    final class Ratio {
        private float initial;
        private float rate;

        Ratio(float initial, float rate) {
            this.initial = initial;
            this.rate = rate;
        }

        public void setRate(float newRate) {
            this.rate = newRate;
        }

        public float getRate() {
            return rate;
        }

        static Ratio fromNbt(CompoundTag compound) {
            return new Ratio(compound.getFloat("initial"), compound.getFloat("rate"));
        }

        public float toFloat(float reference) {
            initial = rate > 0 ? Math.max(initial, reference) : Math.min(initial, reference);

            return Math.max(0, Mth.lerp(reference > initial ? -rate : rate, reference, initial));
        }

        public CompoundTag toNbt() {
            CompoundTag compound = new CompoundTag();
            compound.putFloat("initial", initial);
            compound.putFloat("rate", rate);
            return compound;
        }
    }
}
