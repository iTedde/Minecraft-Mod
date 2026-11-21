/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.FluidProcessingBlockEntity;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.particle.DrugDustParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import ivorius.psychedelicraft.util.MathUtils;

/**
 * Created by lukas on 27.10.14.
 */
public class SlurryFluid extends SimpleFluid implements Processable {
    public static final int FLUID_PER_DIRT = FluidVolumes.BUCKET * 4;

    public SlurryFluid(ResourceLocation id, Settings settings) {
        super(id, settings.sprites(
                id.withPath(p -> "block/fluid/" + p + "_flow"),
                id.withPath(p -> "block/fluid/" + p + "_still")
        ));
    }

    @Override
    public void randomDisplayTick(Level world, BlockPos pos, FluidState state, RandomSource random) {
        ParticleUtils.spawnParticlesOnBlockFaces(world, pos, new DrugDustParticleEffect(PSParticles.BUBBLE, MathUtils.unpackRgb(getColor(getDefaultStack())), 1), ConstantInt.of(5));

        world.playLocalSound(pos, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.BLOCKS,
                0.5F + world.getRandom().nextFloat(),
                0.3F + world.getRandom().nextFloat(), true);
    }

    @Override
    public ProcessType modifyProcess(Resovoir tank, ProcessType type) {
        return type == ProcessType.FERMENT || type == ProcessType.MATURE ? ProcessType.SEPARATE : type;
    }

    @Override
    public int getProcessingTime(Resovoir tank, ProcessType type) {
        if (type == ProcessType.SEPARATE) {
            return tank.getContents().amount() >= FLUID_PER_DIRT ? Psychedelicraft.getConfig().slurryHardeningTime.get() : UNCONVERTABLE;
        }
        return UNCONVERTABLE;
    }

    @Override
    public void process(Context context, ProcessType type, ByProductConsumer output) {
        if (type == ProcessType.SEPARATE) {
            Resovoir tank = context.getPrimaryTank();
            if (tank.getContents().amount() >= FLUID_PER_DIRT) {
                tank.drain(FLUID_PER_DIRT);
                output.accept(Items.MUD.getDefaultInstance());
                output.accept(SimpleFluid.of(Fluids.WATER).getDefaultStack(FluidVolumes.BUCKET * 3));
            }
        }
    }

    @Override
    public Stream<Process> getProcesses() {
        return Stream.empty();
    }

    @Override
    public void appendTankTooltip(ItemFluids stack, @Nullable Level world, List<Component> tooltip, FluidProcessingBlockEntity tank) {
        super.appendTankTooltip(stack, world, tooltip, tank);

        if (tank.getProcessType() == ProcessType.DISTILL || tank.getProcessType() == ProcessType.FERMENT) {
            tooltip.add(Component.literal("Requirements:"));
            if (tank.getPrimaryTank().getAmount() < FLUID_PER_DIRT) {
                tooltip.add(Component.translatable("* Requires 4 buckets of slurry to process").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
            } else {
                tooltip.add(Component.translatable("* Has 4 buckets of slurry").withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
