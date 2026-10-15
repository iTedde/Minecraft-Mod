package dev.craft.forestry.machine;

import com.mojang.serialization.MapCodec;
import dev.craft.forestry.FrBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Thermionic Fabricator ({@code BlockMachine} meta 11). */
public class FabricatorBlock extends ForestryMachineBlock<FabricatorBlockEntity> {
    public FabricatorBlock(Properties properties) {
        super(properties, FrBlockEntityTypes.FABRICATOR);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(FabricatorBlock::new);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FabricatorBlockEntity(pos, state);
    }
}
