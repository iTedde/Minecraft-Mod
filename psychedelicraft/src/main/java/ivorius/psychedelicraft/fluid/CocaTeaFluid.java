/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.stream.Stream;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.PSItems;

/**
 * Created by lukas on 22.10.14.
 */
public class CocaTeaFluid extends DrugFluid implements Processable {
    public CocaTeaFluid(ResourceLocation id, ivorius.psychedelicraft.fluid.DrugFluid.Settings settings) {
        super(id, settings);
    }

    @Override
    public int getProcessingTime(Resovoir tank, ProcessType type) {
        return type != ProcessType.PURIFY ? Processable.UNCONVERTABLE : 1;
    }

    @Override
    public void process(Context context, ProcessType type, ByProductConsumer output) {
        if (type == ProcessType.PURIFY) {
            int amount = Math.max(1, context.getPrimaryTank().getContents().amount() / 10);
            context.getPrimaryTank().drain(amount * 2);
            output.accept(PSFluids.COCAINE.getDefaultStack(amount));
        }
    }

    @Override
    public Stream<Process> getProcesses() {
        return Stream.of(new Process(this, getId().withSuffix("_purified"), List.of(
            new Transition(ProcessType.PURIFY, 0, 1, from -> from.ofAmount(2), to -> PSFluids.COCAINE.getDefaultStack(1))
        )));
    }

    @Override
    public boolean isSuitableContainer(ItemStack container) {
        return container.is(PSItems.STONE_CUP) || container.is(PSTags.Items.BOTTLE_RACK_INSERTABLE);
    }

    @Override
    public TagKey<Item> getPreferredContainerTag() {
        return PSTags.Items.SUITABLE_HOT_DRINK_RECEPTICALS;
    }
}
