/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * Created by lukas on 25.10.14.
 * Updated by Sollace on 1 Jan 2023
 */
public class FlaskItem extends BlockItem {
    public FlaskItem(Block block, Properties settings) {
        super(block, settings);
    }

    @Override
    public Component getName(ItemStack stack) {
        ItemFluids fluids = ItemFluids.of(stack);

        if (!fluids.isEmpty()) {
            return Component.translatable(getDescriptionId() + ".filled", fluids.fluid().getName(fluids));
        }

        return super.getName(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !ItemFluids.of(stack).isEmpty();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int)(MAX_BAR_WIDTH * FluidCapacity.getPercentage(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xAAAAFF;
    }
}
