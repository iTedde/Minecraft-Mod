/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.entity.drug.influence.DelayType;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.fluid.physical.FluidStateManager;
import ivorius.psychedelicraft.item.component.ItemFluids;

/**
 * Created by lukas on 22.10.14.
 */
public class CoffeeFluid extends DrugFluid implements Processable {
    public static final Attribute<Integer> WARMTH = Attribute.ofInt("warmth", 0, 2);
    private static final FluidStateManager.FluidProperty<Integer> TEMPERATURE = new FluidStateManager.FluidProperty<>(IntegerProperty.create("temperature", 0, 2), WARMTH::set, WARMTH::get);

    public CoffeeFluid(ResourceLocation id, ivorius.psychedelicraft.fluid.DrugFluid.Settings settings) {
        super(id, settings.with(TEMPERATURE));
    }

    @Override
    protected void getDrugInfluencesPerLiter(ItemFluids stack, Consumer<DrugInfluence> consumer) {
        super.getDrugInfluencesPerLiter(stack, consumer);
        float warmth = (float)WARMTH.get(stack) / 2F;
        consumer.accept(new DrugInfluence(DrugType.CAFFEINE, DelayType.INHALED, 0.002, 0.001, 0.25F + warmth * 0.05F));
        consumer.accept(new DrugInfluence(DrugType.WARMTH, DelayType.IMMEDIATE, 0.1, 0, 0.8F * warmth));
    }

    @Override
    public void onRandomTick(Level world, BlockPos pos, FluidState state, RandomSource random) {
        int temperature = state.getValue(TEMPERATURE.property());
        if (temperature > 0 && world.getBlockState(pos).getBlock() instanceof LiquidBlock) {
            world.setBlockAndUpdate(pos, state.setValue(TEMPERATURE.property(), temperature - 1).createLegacyBlock());
        }
    }

    @Override
    public Component getName(ItemFluids stack) {
        return Component.translatable(getTranslationKey() + ".temperature." + WARMTH.get(stack));
    }


    @Override
    public Stream<ItemFluids> getDefaultStacks(int capacity) {
        return Stream.concat(
                super.getDefaultStacks(capacity),
                WARMTH.steps().map(Tuple::getB).map(warmth -> WARMTH.set(getDefaultStack(capacity), warmth))
        );
    }

    @Override
    public String getUniqueKey(ItemFluids fluids) {
        return switch (WARMTH.get(fluids)) {
            case 1 -> "_warm";
            case 2 -> "_hot";
            default -> "_cold";
        };
    }

    @Override
    public boolean isSuitableContainer(ItemStack container) {
        return container.is(getPreferredContainerTag());
    }

    @Override
    public TagKey<Item> getPreferredContainerTag() {
        return PSTags.Items.SUITABLE_HOT_DRINK_RECEPTICALS;
    }

    @Override
    public ProcessType modifyProcess(Resovoir tank, ProcessType type) {
        return WARMTH.get(tank.getContents()) > 0 ? ProcessType.COOL : type;
    }

    @Override
    public int getProcessingTime(Resovoir tank, ProcessType type) {
        return switch (type) {
            case COOL -> WARMTH.get(tank.getContents()) > 0 ? 300 : UNCONVERTABLE;
            case PURIFY -> 1;
            default -> UNCONVERTABLE;
        };
    }

    @Override
    public void process(Context context, ProcessType type, ByProductConsumer output) {
        Resovoir tank = context.getPrimaryTank();
        if (type == ProcessType.COOL) {
            tank.setContents(WARMTH.set(tank.getContents(), Math.max(0, WARMTH.get(tank.getContents()) - 1)));
        }
        if (type == ProcessType.PURIFY) {
            int amount = Math.max(1, context.getPrimaryTank().getContents().amount() / 10);
            context.getPrimaryTank().drain(amount * 2);
            output.accept(PSFluids.CAFFEINE.getDefaultStack(amount));
        }
    }

    @Override
    public Stream<Process> getProcesses() {
        return Stream.concat(WARMTH.steps()
            .flatMapToInt(step -> IntStream.of(step.getA(), step.getB()))
            .distinct()
            .mapToObj(warmth -> {
                return new Process(this, getId().withSuffix("_purifying_" + warmth), List.of(
                        new Transition(ProcessType.PURIFY, 0, 1, from -> WARMTH.set(from, warmth).ofAmount(2), to -> PSFluids.CAFFEINE.getDefaultStack(1))
                ));
            }), Stream.of(
                    new Process(this, getId().withSuffix("_cooling"), WARMTH.steps().map(step -> new Transition(ProcessType.FERMENT, 300, -1,
                            stack -> WARMTH.set(stack, step.getB()),
                            stack -> WARMTH.set(stack, step.getA())
                    )).toList())
                ));
    }

    @Override
    public int getHash(ItemFluids stack) {
        return Objects.hash(this, WARMTH.get(stack));
    }
}
