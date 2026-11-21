package ivorius.psychedelicraft.compat.emi;

import java.util.Set;
import java.util.stream.Collectors;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiWorldInteractionRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.PSItems;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

/**
 * EMI integration (upstream used the TLA API: {@code compat/tia/Main}): drying table, mash tub, barrels, distillery,
 * flask, tray, fluid preparation stages and world interactions (shears, buckets).
 */
@EmiEntrypoint
public final class PSEmiPlugin implements EmiPlugin {
    static final ResourceLocation WIDGETS = Psychedelicraft.id("textures/gui/widgets.png");

    @Override
    public void register(EmiRegistry registry) {
        PSEmiCategory.bootstrap(registry);
        registerWorldInteractions(registry);

        // Containers differ only by their fluid component
        for (Item item : new Item[] {PSItems.WOODEN_MUG, PSItems.STONE_CUP, PSItems.GLASS_CHALICE,
                PSItems.SHOT_GLASS, PSItems.BOTTLE, PSItems.MOLOTOV_COCKTAIL, PSItems.SYRINGE,
                PSItems.FILLED_GLASS_BOTTLE, PSItems.FILLED_BUCKET, PSItems.FILLED_BOWL}) {
            registry.setDefaultComparison(item, Comparison.compareComponents());
        }

        Comparison fluidComparison = Comparison.of(
                (left, right) -> EmiUtil.toItemFluids(left).canCombine(EmiUtil.toItemFluids(right)),
                stack -> EmiUtil.toItemFluids(stack).getHash());
        SimpleFluid.REGISTRY.stream()
                .flatMap(f -> java.util.stream.Stream.of(f.getPhysical().getStandingFluid(), f.getPhysical().getFlowingFluid()))
                .distinct()
                .forEach(fluid -> registry.setDefaultComparison(fluid, fluidComparison));
    }

    private static void registerWorldInteractions(EmiRegistry registry) {
        var customFluids = SimpleFluid.REGISTRY.holders()
                .filter(entry -> entry.value().getPhysical().getStandingFluid().getBucket() == PSItems.FILLED_BUCKET)
                .toList();
        Set<Fluid> bucketFluids = customFluids.stream()
                .map(entry -> entry.value().getPhysical().getStandingFluid())
                .collect(Collectors.toSet());

        // EMI's generic bucket filling shows the filled bucket without its fluid component – replace it
        registry.removeRecipes(recipe -> recipe.getCategory() == VanillaEmiRecipeCategories.WORLD_INTERACTION
                && isGenericBucketFilling(recipe, bucketFluids));

        for (Holder.Reference<SimpleFluid> fluid : customFluids) {
            ResourceLocation key = fluid.key().location();
            registry.addRecipe(EmiWorldInteractionRecipe.builder()
                    .id(Psychedelicraft.id("/world/bucket_filling/" + key.getNamespace() + "/" + key.getPath()))
                    .leftInput(EmiStack.of(Items.BUCKET))
                    .rightInput(EmiStack.of(fluid.value().getPhysical().getStandingFluid(), 1000), false)
                    .output(EmiUtil.toEmiStack(Items.BUCKET.getDefaultInstance(), fluid.value().getDefaultStack()))
                    .build());
        }

        EmiIngredient shears = EmiIngredient.of(Tags.Items.TOOLS_SHEAR);
        shearing(registry, "morning_glory_flowers", PSItems.MORNING_GLORY_LATTICE, shears, PSItems.MORNING_GLORY);
        shearing(registry, "juniper_berries", PSItems.FRUITING_JUNIPER_LEAVES, shears, PSItems.JUNIPER_BERRIES);
        shearing(registry, "wine_grapes", PSItems.WINE_GRAPE_LATTICE, shears, PSItems.WINE_GRAPES);
    }

    private static boolean isGenericBucketFilling(EmiRecipe recipe, Set<Fluid> bucketFluids) {
        ResourceLocation id = recipe.getId();
        if (id == null || !id.getNamespace().equals("emi") || !id.getPath().contains("bucket_filling")) {
            return false;
        }
        return recipe.getInputs().stream()
                .flatMap(input -> input.getEmiStacks().stream())
                .anyMatch(stack -> stack.getKey() instanceof Fluid fluid && bucketFluids.contains(fluid));
    }

    private static void shearing(EmiRegistry registry, String name, Item block, EmiIngredient tool, Item result) {
        registry.addRecipe(EmiWorldInteractionRecipe.builder()
                .id(Psychedelicraft.id("/world/shearing/" + name))
                .leftInput(EmiStack.of(block))
                .rightInput(tool, true)
                .output(EmiStack.of(result))
                .build());
    }
}
