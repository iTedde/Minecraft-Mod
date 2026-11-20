package ivorius.psychedelicraft.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.CommonColors;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FlammableGasBlock extends AirBlock {
    public static final MapCodec<FlammableGasBlock> CODEC = simpleCodec(FlammableGasBlock::new);

    public FlammableGasBlock(Properties settings) {
        super(settings.randomTicks());
        ivorius.psychedelicraft.util.PSHooks.flammable(this, 150, 2000);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public MapCodec<AirBlock> codec() {
        return (MapCodec)CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        Vec3 center = pos.getCenter();
        var effect = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, CommonColors.GREEN);
        for (int i = 0; i < 10; i++) {
            world.addParticle(effect,
                    random.triangle(center.x, 0.5F),
                    random.triangle(center.y, 0.5F),
                    random.triangle(center.z, 0.5F),
                    0, 0, 0
            );
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        for (Direction direction : Direction.values()) {
            BlockState neighbor = world.getBlockState(pos.relative(direction));
            if (neighbor.getBlock() instanceof TorchBlock || neighbor.getFluidState().is(FluidTags.LAVA)) {
                world.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
                return;
            }
        }
        BlockState below = world.getBlockState(pos.below());
        if (below.isAir() && !below.is(this)) {
            world.setBlockAndUpdate(pos.below(), state);
        }
        super.randomTick(state, world, pos, random);
    }
}
