/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PeyoteBlockEntity extends BlockEntity {
    public PeyoteBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.PEYOTE, pos, state);
    }
}
