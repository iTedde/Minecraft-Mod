package dev.craft.forestry.machine;

import com.mojang.serialization.MapCodec;
import dev.craft.forestry.FrBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Carpenter ({@code BlockMachine} meta 5). */
public class CarpenterBlock extends ForestryMachineBlock<CarpenterBlockEntity> {
    public CarpenterBlock(Properties properties) {
        super(properties, FrBlockEntityTypes.CARPENTER);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(CarpenterBlock::new);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CarpenterBlockEntity(pos, state);
    }
}
