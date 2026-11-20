package ivorius.psychedelicraft;

import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;

public interface PSGameRules {
    GameRules.Key<BooleanValue> DO_SLEEP_DEPRIVATION = GameRules.register("doSleepDeprivation", GameRules.Category.SPAWNING, BooleanValue.create(false));

    static void bootstrap() { }
}
