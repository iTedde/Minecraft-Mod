package ivorius.psychedelicraft.fluid.alcohol;

import java.util.function.Predicate;

import ivorius.psychedelicraft.fluid.AlcoholicFluid;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.util.TriState;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;

public record StatePredicate (
        Ints fermentationRange,
        Ints maturationRange,
        Ints distillationRange,
        TriState vinegar,
        AlcoholicFluidState state
) implements Predicate<ItemFluids> {
    public static final StatePredicate ANY = StatePredicate.builder().build();
    public static final StatePredicate BASE = StatePredicate.builder().vinegar(false).distills(0).ferments(0).maturation(0).build();
    public static final StatePredicate VINEGAR = StatePredicate.builder().vinegar(true).build();
    public static final StatePredicate FERMENTED_1 = StatePredicate.builder().vinegar(false).maturation(0).distills(0).ferments(1).build();
    public static final StatePredicate FERMENTED_2 = StatePredicate.builder().vinegar(false).maturation(0).distills(0).ferments(2).build();
    public static final StatePredicate FERMENTED_DISTILLED = StatePredicate.builder().vinegar(false).minFerments(1).minDistills(1).maturation(0).build();
    public static final StatePredicate FERMENTED_MATURED = StatePredicate.builder().vinegar(false).minFerments(1).minMaturity(1).distills(0).build();
    public static final StatePredicate FERMENTED_MATURED_DISTILLED = StatePredicate.builder().vinegar(false).minFerments(1).minMaturity(1).minDistills(1).build();

    @Override
    public boolean test(ItemFluids stack) {
        return test(
                AlcoholicFluid.FERMENTATION.get(stack),
                AlcoholicFluid.DISTILLATION.get(stack),
                AlcoholicFluid.MATURATION.get(stack),
                AlcoholicFluid.VINEGAR.get(stack)
        );
    }

    public boolean test(AlcoholicFluidState state) {
        return test(state.fermentation(), state.distillation(), state.maturation(), state.vinegar());
    }

    public boolean test(int fermentation, int distillation, int maturation, boolean vinegar) {
        return fermentationRange.matches(fermentation)
                && distillationRange.matches(distillation)
                && maturationRange.matches(maturation)
                && this.vinegar.orElse(vinegar) == vinegar;
    }

    public static StatePredicate.Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Ints fermentationRange = Ints.ANY;
        private Ints maturationRange = Ints.ANY;
        private Ints distillationRange = Ints.ANY;
        private TriState vinegar = TriState.DEFAULT;

        public StatePredicate.Builder vinegar(boolean vinegar) {
            this.vinegar = TriState.of(vinegar);
            return this;
        }

        public StatePredicate.Builder minFerments(int fermented) {
            return ferments(Ints.atLeast(fermented));
        }

        public StatePredicate.Builder ferments(int fermentation) {
            return ferments(Ints.exactly(fermentation));
        }

        public StatePredicate.Builder ferments(Ints range) {
            fermentationRange = range;
            return this;
        }

        public StatePredicate.Builder maturation(int maturation) {
            return maturation(Ints.exactly(maturation));
        }

        public StatePredicate.Builder minMaturity(int maturity) {
            return maturation(Ints.atLeast(maturity));
        }

        public StatePredicate.Builder maturation(Ints range) {
            maturationRange = range;
            return this;
        }

        public StatePredicate.Builder minDistills(int distills) {
            return distillation(Ints.atLeast(distills));
        }

        public StatePredicate.Builder distills(int distillations) {
            return distillation(Ints.exactly(distillations));
        }

        public StatePredicate.Builder distillation(Ints range) {
            distillationRange = range;
            return this;
        }

        public StatePredicate build() {
            AlcoholicFluidState.Builder stateBuilder = AlcoholicFluidState.builder();
            fermentationRange.min().ifPresent(stateBuilder::fermentation);
            maturationRange.min().ifPresent(stateBuilder::maturation);
            distillationRange.min().ifPresent(stateBuilder::distillation);
            if (vinegar == TriState.TRUE) {
                stateBuilder.vinegar();
            }
            return new StatePredicate(fermentationRange, maturationRange, distillationRange, vinegar, stateBuilder.build());
        }
    }
}