package ivorius.psychedelicraft.advancement;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds.Doubles;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;

public class DrugEffectsChangedCriterion extends SimpleCriterionTrigger<DrugEffectsChangedCriterion.Conditions> {
    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(DrugProperties properties) {
        if (properties.asEntity() instanceof ServerPlayer p) {
            trigger(p, c -> c.test(p, properties));
        }
    }

    public record Conditions(Optional<ContextAwarePredicate> player, List<DrugPredicate> drugs) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                DrugPredicate.CODEC.listOf().fieldOf("drugs").forGetter(Conditions::drugs)
        ).apply(instance, Conditions::new));

        public static Criterion<Conditions> create(Collection<DrugType<?>> types) {
            return PSCriteria.DRUG_EFFECTS_CHANGED.createCriterion(new Conditions(Optional.empty(), types.stream().map(type -> new DrugPredicate(type, Doubles.atLeast(Mth.EPSILON))).toList()));
        }

        public boolean test(ServerPlayer player, DrugProperties properties) {
            return drugs.stream().allMatch(predicate -> predicate.test(properties));
        }

        public record DrugPredicate (DrugType<?> type, Doubles range) implements Predicate<DrugProperties> {
            public static final Codec<DrugPredicate> CODEC = Codec.either(
                    DrugType.REGISTRY.byNameCodec().xmap(id -> new DrugPredicate(id, Doubles.atLeast(Mth.EPSILON)), DrugPredicate::type),
                    RecordCodecBuilder.<DrugPredicate>create(instance -> instance.group(
                            DrugType.REGISTRY.byNameCodec().fieldOf("id").forGetter(DrugPredicate::type),
                            Doubles.CODEC.fieldOf("value").forGetter(DrugPredicate::range)
                    ).apply(instance, DrugPredicate::new))
            ).xmap(Either::unwrap, Either::right);

            @Override
            public boolean test(DrugProperties properties) {
                return range.matches(properties.getDrugValue(type));
            }
        }
    }
}
