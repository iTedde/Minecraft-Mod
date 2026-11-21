package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.advancements.critereon.MinMaxBounds.Doubles;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.Pool;

public record EntityHallucinationType (ResourceLocation id, Function<Player, Hallucination> factory, float chance, Doubles strengthRange, @Nullable Predicate<EntityHallucinationList> condition) {
    public static final Map<ResourceLocation, EntityHallucinationType> REGISTRY = new HashMap<>();

    static {
        register(HallucinationTypeKeys.RASTA_HEAD, RastaHeadHallucination::new, 0.1F, Doubles.ANY, list -> {
            return list.getNumberOfHallucinations(a -> a instanceof RastaHeadHallucination) == 0 && list.getProperties().getDrugValue(DrugType.CANNABIS) > 0.4F;
        });
        register(HallucinationTypeKeys.MULTIPLE_ENTITY, MultipleEntityHallucination::new, 0.5F, Doubles.ANY);
        register(HallucinationTypeKeys.SINGLE_ENTITY, EntityHallucination::new, 1, Doubles.ANY);
        register(HallucinationTypeKeys.HOSTILE_VILLAGERS, (player -> new EntityIdentitySwapHallucination(player, EntityType.VILLAGER, Pool.create(EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER))), 1, Doubles.atLeast(0.8F));
        register(HallucinationTypeKeys.FRIENDLY_ZOMBIES, (player -> new EntityIdentitySwapHallucination(player, EntityType.ZOMBIE, Pool.create(EntityType.VILLAGER))), 1, Doubles.atLeast(0.8F));
    }

    public static Stream<EntityHallucinationType> getCandidates(EntityHallucinationList list) {
        RandomSource weight = list.getProperties().asEntity().getRandom();
        return REGISTRY.values().stream()
                .filter(i -> i.strengthRange().matches(list.getManager().getEntityHallucinationStrength())
                            && weight.nextFloat() <= i.chance()
                            && (i.condition() == null || i.condition().test(list)
                ));
    }

    static void register(ResourceLocation id, Function<Player, Hallucination> factory, float chance, Doubles strengthRange) {
        register(id, factory, chance, Doubles.ANY, null);
    }

    static void register(ResourceLocation id, Function<Player, Hallucination> factory, float chance, Doubles strengthRange, @Nullable Predicate<EntityHallucinationList> condition) {
        REGISTRY.put(id, new EntityHallucinationType(id, factory, chance, strengthRange, condition));
    }
}
