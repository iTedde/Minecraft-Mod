package ivorius.psychedelicraft.fluid.physical;

import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;

public abstract class PlacedFluidBlock extends LiquidBlock {
    protected abstract PhysicalFluid getPysicalFluid();

    static PlacedFluidBlock create(PhysicalFluid physical) {
        return new PlacedFluidBlock((FlowingFluid)physical.getFlowingFluid()) {
            @Override
            protected PhysicalFluid getPysicalFluid() {
                return physical;
            }
        };
    }

    PlacedFluidBlock(FlowingFluid fluid) {
        super(fluid, Properties.ofFullCopy(Blocks.WATER).randomTicks());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        getPysicalFluid().getType().getStateManager().appendProperties(builder);
    }

    @Override
    public ItemStack pickupBlock(Player player, LevelAccessor world, BlockPos pos, BlockState state) {
        if (state.getValue(LEVEL) == 0) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_IMMEDIATE);
            ItemStack stack = PSItems.FILLED_BUCKET.getDefaultInstance();
            return ItemFluids.set(stack, getPysicalFluid().getType().getStack(state.getFluidState(), FluidCapacity.get(stack)));
        }
        return ItemStack.EMPTY;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return getPysicalFluid().getType().getStateManager().copyStateValues(state, super.getFluidState(state));
    }
}