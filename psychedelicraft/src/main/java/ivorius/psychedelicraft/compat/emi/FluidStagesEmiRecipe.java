package ivorius.psychedelicraft.compat.emi;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.Processable;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.PSItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Processing stages of a fluid (fermenting, distilling, maturing …), similar processes combined
 * (upstream: {@code compat/tia/FluidStagesEmiRecipe}).
 */
final class FluidStagesEmiRecipe extends PSEmiRecipe {
    static BiConsumer<PSEmiCategory, EmiRegistry> generate() {
        return (category, registry) -> {
            Set<ResourceLocation> ids = new HashSet<>();
            FluidStageUtil.combineSimilarProcesses(SimpleFluid.REGISTRY.stream()
                            .filter(fluid -> fluid instanceof Processable)
                            .flatMap(fluid -> ((Processable) fluid).getProcesses().filter(process -> !process.transitions().isEmpty())),
                    List::of,
                    List::copyOf
            ).forEach(processes -> {
                ResourceLocation base = processes.get(0).id();
                ResourceLocation id = Psychedelicraft.id("/" + category.getId().getPath() + "/" + base.getNamespace() + "/" + base.getPath());
                for (int i = 2; !ids.add(id); i++) {
                    id = Psychedelicraft.id("/" + category.getId().getPath() + "/" + base.getNamespace() + "/" + base.getPath() + "_" + i);
                }
                registry.addRecipe(new FluidStagesEmiRecipe(category, id, processes));
            });
        };
    }

    private final ResourceLocation id;
    private final PSEmiCategory category;
    private final List<EmiIngredient> catalysts;
    private final List<Stage> stages;

    private FluidStagesEmiRecipe(PSEmiCategory category, ResourceLocation id, List<Processable.Process> processes) {
        this.category = category;
        this.id = id;
        Processable.Process process = processes.get(0);
        stages = IntStream.range(0, process.transitions().size()).mapToObj(i -> {
            var transition = process.transitions().get(i);
            return new Stage(transition,
                    EmiIngredient.of(processes.stream().map(p -> EmiUtil.toEmiStack(p.transitions().get(i).input().apply(p.fluid().getDefaultStack()))).distinct().toList()),
                    EmiIngredient.of(processes.stream().map(p -> EmiUtil.toEmiStack(p.transitions().get(i).output().apply(p.fluid().getDefaultStack()))).distinct().toList()),
                    Stage.getStackForProcess(transition.type())
            );
        }).toList();
        catalysts = processes.stream().map(Processable.Process::fluid).distinct()
                .map(SimpleFluid::getPreferredContainerTag).distinct()
                .map(tag -> (EmiIngredient) EmiIngredient.of(tag)).toList();
    }

    record Stage(Processable.Transition transition, EmiIngredient input, EmiIngredient output, EmiIngredient container) {
        void addWidgets(int x, int y, WidgetHolder widgets, boolean last) {
            widgets.addSlot(input, x, y);
            widgets.addSlot(container, x + 19, y).catalyst(true);

            int ticks = Math.max(1, transition.time() * transition.multiplier());

            widgets.addFillingArrow(x + 40, y, arrowMillis(ticks)).tooltipText(List.of(
                    Component.translatable("gui.psychedelicraft.recipe.fluid_process",
                            Component.translatable("fluid.status." + transition.type().getSerializedName()),
                            EmiUtil.formatTicks(ticks)
                    )
            ));
            widgets.addText(Component.literal(transition.multiplier() + "x"), x + 45, y + 9, -1, true);

            if (last) {
                widgets.addSlot(output, x + 65, y);
            }
        }

        private static EmiIngredient getStackForProcess(Processable.ProcessType type) {
            return switch (type) {
                case SEPARATE, FERMENT, ACETIFY, COOL -> EmiStack.of(PSItems.MASH_TUB);
                case PURIFY -> EmiStack.of(PSItems.BUNSEN_BURNER);
                case MATURE -> EmiIngredient.of(PSTags.Items.BARRELS);
                case DISTILL -> EmiStack.of(PSItems.DISTILLERY);
                case IDLE -> EmiStack.of(PSItems.BOTTLE);
            };
        }
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
        return List.of(stages.get(0).input());
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.copyOf(stages.get(stages.size() - 1).output().getEmiStacks());
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return catalysts;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int totalWidth = stages.size() * 65 + 8;
        int left = (category.width - totalWidth) / 2;

        for (int i = 0; i < stages.size(); i++) {
            stages.get(i).addWidgets(left + i * 65, i, widgets, i == stages.size() - 1);
        }
    }
}
