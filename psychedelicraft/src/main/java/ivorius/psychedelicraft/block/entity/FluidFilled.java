package ivorius.psychedelicraft.block.entity;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;

public interface FluidFilled extends LiquidBlockContainer {
    double getFluidHeight(Level world, BlockState state, BlockPos pos);

    Optional<FluidState> getContainedFluid(Level world, BlockState state, BlockPos pos);

    default AABB getFluidCollisionBox(Level world, BlockState state, BlockPos pos) {
        double height = getFluidHeight(world, state, pos);
        AABB box = state.getShape(world, pos).bounds();

        return box.move(pos).setMaxY(pos.getY() + height)
                .inflate(-0.01, 0, -0.01);
    }
}
