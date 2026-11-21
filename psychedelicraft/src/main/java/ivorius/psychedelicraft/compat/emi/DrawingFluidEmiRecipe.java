package ivorius.psychedelicraft.compat.emi;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import net.minecraft.resources.ResourceLocation;

/**
 * Drawing a fluid out of a barrel, distillery or flask into its preferred containers
 * (upstream: {@code compat/tia/DrawingFluidEmiRecipe}).
 */
final class DrawingFluidEmiRecipe extends PSEmiRecipe {
    private final PSEmiCategory category;
    private final EmiUtil.Contents contents;
    private final ResourceLocation background;
    private final ResourceLocation id;
    private final int capacity;

    static BiConsumer<PSEmiCategory, EmiRegistry> generate(int capacity) {
        return (category, registry) -> {
            Set<ResourceLocation> ids = new HashSet<>();
            SimpleFluid.REGISTRY.stream()
                    .filter(f -> !f.isEmpty())
                    .forEach(fluid -> fluid.getDefaultStacks(capacity).forEach(stack -> {
                        var contents = EmiUtil.Contents.of(EmiIngredient.of(fluid.getPreferredContainerTag()), stack);
                        String key = fluid.getId().toDebugFileName() + fluid.getUniqueKey(stack);
                        ResourceLocation id = Psychedelicraft.id("/" + category.getId().getPath() + "/" + sanitize(key));
                        for (int i = 2; !ids.add(id); i++) {
                            id = Psychedelicraft.id("/" + category.getId().getPath() + "/" + sanitize(key) + "_" + i);
                        }
                        registry.addRecipe(new DrawingFluidEmiRecipe(category, contents, id, capacity));
                    }));
        };
    }

    private static String sanitize(String key) {
        return key.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9/._-]", "_");
    }

    private DrawingFluidEmiRecipe(PSEmiCategory category, EmiUtil.Contents contents, ResourceLocation id, int capacity) {
        this.category = category;
        this.background = category.getId().withPath(p -> "textures/gui/" + p + ".png");
        this.contents = contents;
        this.id = id;
        this.capacity = capacity;
    }

    @Override
    public PSEmiCategory getCategory() {
        return category;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(contents.empty());
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.copyOf(contents.filled().getEmiStacks());
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return List.of(contents.contents());
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        FluidBoxWidget.create(contents.type(), capacity, 50, 2, 40, 50, widgets);
        widgets.addTexture(new EmiTexture(background, 15, 15, 120, 64), 0, 0);
        widgets.addAnimatedTexture(new EmiTexture(background, 191, 20, 39, 64), 68, 47, 2000, true, true, true);
        widgets.addSlot(contents.empty(), 2, getDisplayHeight() - 16 - 4);
        widgets.addSlot(contents.contents(), 2 + 16 + 4, getDisplayHeight() - 16 - 4).catalyst(true);
        widgets.addSlot(contents.filled(), 107, 45).recipeContext(this);
    }
}
