package ivorius.psychedelicraft.advancement;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.item.component.ItemFluids;

public class MashingTubEventCriterion extends SimpleCriterionTrigger<MashingTubEventCriterion.Conditions> {
    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(Player player, ItemStack stack) {
        if (player instanceof ServerPlayer p) {
            trigger(p, c -> c.test(p, stack));
        }
    }

    public interface Trigger {
        void trigger(@Nullable Player player);
    }

    public record Conditions (Optional<ContextAwarePredicate> player, AlcoholicFluid fluid, Ints fermentation, Ints maturation, Ints distillation) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                SimpleFluid.CODEC.xmap(AlcoholicFluid.class::cast, Function.identity()).fieldOf("fluid").forGetter(Conditions::fluid),
                Ints.CODEC.optionalFieldOf("fermentation", Ints.ANY).forGetter(Conditions::fermentation),
                Ints.CODEC.optionalFieldOf("maturation", Ints.ANY).forGetter(Conditions::maturation),
                Ints.CODEC.optionalFieldOf("distillation", Ints.ANY).forGetter(Conditions::distillation)
        ).apply(instance, Conditions::new));

        public static Criterion<Conditions> create(AlcoholicFluid fluid) {
            return PSCriteria.SIMPLY_MASHING.createCriterion(new Conditions(Optional.empty(), fluid, Ints.ANY, Ints.ANY, Ints.ANY));
        }

        public static Criterion<Conditions> create(AlcoholicFluid fluid, Ints fermentation, Ints maturation, Ints distillation) {
            return PSCriteria.SIMPLY_MASHING.createCriterion(new Conditions(Optional.empty(), fluid, fermentation, maturation, distillation));
        }

        public boolean test(ServerPlayer player, ItemStack stack) {
            return ItemFluids.of(stack).fluid() == fluid
                    && fermentation.matches(AlcoholicFluid.FERMENTATION.get(stack))
                    && maturation.matches(AlcoholicFluid.MATURATION.get(stack))
                    && distillation.matches(AlcoholicFluid.DISTILLATION.get(stack));
        }
    }
}
