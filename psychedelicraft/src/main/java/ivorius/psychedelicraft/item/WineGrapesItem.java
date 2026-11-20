/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.block.LatticeBlock;
import ivorius.psychedelicraft.block.PSBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

public class WineGrapesItem extends SpecialFoodItem {
    public WineGrapesItem(Properties settings, int eatSpeed) {
        super(settings, eatSpeed);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.isSecondaryUseActive() || !context.getPlayer().mayInteract(context.getLevel(), context.getClickedPos())) {
            return InteractionResult.PASS;
        }

        BlockPos pos = context.getClickedPos();
        BlockState state = context.getLevel().getBlockState(pos);

        if (state.is(PSBlocks.LATTICE)) {
            context.getItemInHand().consume(1, context.getPlayer());
            context.getLevel().playSound(null, context.getPlayer(), SoundEvents.AZALEA_LEAVES_HIT, context.getPlayer().getSoundSource(), 1, 1);
            context.getLevel().setBlockAndUpdate(pos, LatticeBlock.copyStateProperties(PSBlocks.WINE_GRAPE_LATTICE.defaultBlockState(), state));
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
