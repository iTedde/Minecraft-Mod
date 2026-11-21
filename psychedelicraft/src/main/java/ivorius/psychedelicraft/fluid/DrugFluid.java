/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.fluid.alcohol.FluidAppearance;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Created by lukas on 22.10.14.
 */
public class DrugFluid extends SimpleFluid implements ConsumableFluid {

    protected final Map<String, ResourceLocation> flowTextures = new HashMap<>();
    protected final Map<String, ResourceLocation> stillTextures = new HashMap<>();

    public DrugFluid(ResourceLocation id, ivorius.psychedelicraft.fluid.DrugFluid.Settings settings) {
        super(id, settings);
    }

    @Nullable
    public FoodProperties getFoodLevel(ItemStack fluidStack) {
        return ((ivorius.psychedelicraft.fluid.DrugFluid.Settings)getSettings()).foodLevel;
    }

    public void getDrugInfluences(ItemFluids fluidStack, List<DrugInfluence> list) {
        getDrugInfluencesPerLiter(fluidStack, influence -> {
            list.add(influence.copyWithTarget((influence.target() / FluidVolumes.BUCKET) * fluidStack.amount()));
        });
    }

    protected void getDrugInfluencesPerLiter(ItemFluids stack, Consumer<DrugInfluence> consumer) {
        ((ivorius.psychedelicraft.fluid.DrugFluid.Settings)getSettings()).drugInfluences.forEach(consumer);
    }

    @Override
    public boolean canConsume(ItemStack fluidStack, LivingEntity entity, ConsumptionType type) {
        if (type == ConsumptionType.DRINK) {
            return ((ivorius.psychedelicraft.fluid.DrugFluid.Settings)getSettings()).drinkable && (
                    !(entity instanceof Player)
                    || getFoodLevel(fluidStack) == null
                    || ((Player) entity).getFoodData().needsFood()
                );
        }

        return ((ivorius.psychedelicraft.fluid.DrugFluid.Settings)getSettings()).injectable;
    }

    @Override
    public void consume(ItemFluids stack, LivingEntity entity, ConsumptionType type) {
        DrugProperties.of(entity).ifPresent(drugProperties -> {
            List<DrugInfluence> drugInfluences = new ArrayList<>();
            getDrugInfluences(stack, drugInfluences);
            drugProperties.addAll(drugInfluences);
        });

        if (type == ConsumptionType.DRINK) {
            if (((ivorius.psychedelicraft.fluid.DrugFluid.Settings)getSettings()).foodLevel != null && entity instanceof Player player) {
                player.getFoodData().eat(((ivorius.psychedelicraft.fluid.DrugFluid.Settings)getSettings()).foodLevel.nutrition(), ((ivorius.psychedelicraft.fluid.DrugFluid.Settings)getSettings()).foodLevel.saturation());
            }
        }
    }

    @Override
    public float getFireStrength(ItemFluids fluidStack) {
        return getAlcohol(fluidStack) * fluidStack.amount() / FluidVolumes.BUCKET * 2.0f;
    }

    @Override
    public float getExplosionStrength(ItemFluids fluidStack) {
        return getAlcohol(fluidStack) * fluidStack.amount() / FluidVolumes.BUCKET * 0.6f;
    }

    @Override
    public Optional<ResourceLocation> getFlowTexture(ItemFluids stack) {
        return Optional.ofNullable(((ivorius.psychedelicraft.fluid.DrugFluid.Settings)getSettings()).appearance.apply(stack))
                .map(FluidAppearance::flowing)
                .map(name -> flowTextures.computeIfAbsent(name, this::getFlowTexture));
    }

    @Override
    public Optional<ResourceLocation> getStandingTexture(ItemFluids stack) {
        return Optional.ofNullable(((ivorius.psychedelicraft.fluid.DrugFluid.Settings)getSettings()).appearance.apply(stack))
                .map(FluidAppearance::still)
                .map(name -> stillTextures.computeIfAbsent(name, this::getFlowTexture));
    }

    protected ResourceLocation getFlowTexture(String name) {
        return getId().withPath(p -> "block/fluid/" + name);
    }

    private float getAlcohol(ItemFluids fluidStack) {
        float alcohol = 0.0f;

        List<DrugInfluence> drugInfluences = new ArrayList<>();
        getDrugInfluences(fluidStack, drugInfluences);

        for (DrugInfluence drugInfluence : drugInfluences) {
            if (drugInfluence.isOf(DrugType.ALCOHOL)) {
                alcohol += drugInfluence.target();
            }
        }
        return Mth.clamp(alcohol, 0, 1);
    }

    @Override
    public boolean isSuitableContainer(ItemStack container) {
        return container.is(PSItems.SYRINGE) || container.is(Items.GLASS_BOTTLE) || container.is(PSItems.FILLED_GLASS_BOTTLE);
    }

    @Override
    public TagKey<Item> getPreferredContainerTag() {
        return PSTags.Items.DRUG_RECEPTICALS;
    }

    public static class Settings extends SimpleFluid.Settings {
        private boolean drinkable;
        private boolean injectable;

        private List<DrugInfluence> drugInfluences = new ArrayList<>();
        private FoodProperties foodLevel;

        protected Function<ItemFluids, FluidAppearance> appearance = stack -> null;

        public ivorius.psychedelicraft.fluid.DrugFluid.Settings drinkable() {
            drinkable = true;
            return this;
        }

        public ivorius.psychedelicraft.fluid.DrugFluid.Settings injectable() {
            injectable = true;
            return this;
        }

        public ivorius.psychedelicraft.fluid.DrugFluid.Settings influence(DrugInfluence... influences) {
            drugInfluences.addAll(List.of(influences));
            return this;
        }

        public ivorius.psychedelicraft.fluid.DrugFluid.Settings food(FoodProperties food) {
            this.foodLevel = food;
            return this;
        }

        public ivorius.psychedelicraft.fluid.DrugFluid.Settings appearance(FluidAppearance appearance) {
            this.appearance = stack -> appearance;
            return this;
        }
    }
}
