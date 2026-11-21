package ivorius.psychedelicraft.advancement;

import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class CustomEventCriterion extends SimpleCriterionTrigger<CustomEventCriterion.Conditions> {
    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public CustomEventCriterion.Trigger createTrigger(String event) {
        return (player, entity) -> {
            if (player instanceof ServerPlayer p) {
                trigger(p, c -> c.test(entity == null ? null : EntityPredicate.createContext(p, entity), event));
            }
        };
    }

    public interface Trigger {
        default void trigger(@Nullable Player player) {
            trigger(player, null);
        }

        void trigger(@Nullable Player player, @Nullable Entity target);
    }

    public record Conditions (Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> entity, String event) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(Conditions::entity),
                Codec.STRING.fieldOf("event").forGetter(Conditions::event)
        ).apply(instance, Conditions::new));

        public static Criterion<Conditions> create(String event) {
            return PSCriteria.CUSTOM.createCriterion(new Conditions(Optional.empty(), Optional.empty(), event));
        }

        public static Criterion<Conditions> create(String event, ContextAwarePredicate entity) {
            return PSCriteria.CUSTOM.createCriterion(new Conditions(Optional.empty(), Optional.of(entity), event));
        }

        public boolean test(LootContext entity, String event) {
            return this.event.contentEquals(event) && (this.entity().isEmpty() || (entity != null && this.entity().get().matches(entity)));
        }
    }
}
