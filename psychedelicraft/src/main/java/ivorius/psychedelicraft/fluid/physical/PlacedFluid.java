package ivorius.psychedelicraft.fluid.physical;

import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.WaterFluid;
import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.particle.FluidParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;

public abstract class PlacedFluid extends WaterFluid {
    private static final Direction[] ALL_DIRECTIONS = Direction.values();

    protected abstract PhysicalFluid getPysicalFluid();

    @Override
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
        super.createFluidStateDefinition(builder);
        getType().getStateManager().appendProperties(builder);
    }

    @Override
    public net.neoforged.neoforge.fluids.FluidType getFluidType() {
        var fluidType = getPysicalFluid().getFluidType();
        return fluidType == null ? super.getFluidType() : fluidType;
    }

    public SimpleFluid getType() {
        return getPysicalFluid().getType();
    }

    @Override
    public Fluid getFlowing() {
        return getPysicalFluid().getFlowingFluid();
    }

    @Override
    public Fluid getSource() {
        return getPysicalFluid().getStandingFluid();
    }

    @Override
    public Item getBucket() {
        return getType().isEmpty() ? Items.BUCKET : PSItems.FILLED_BUCKET;
    }

    @Override
    public BlockState createLegacyBlock(FluidState state) {
        return getType().getStateManager().copyStateValues(state, getPysicalFluid().getBlock().defaultBlockState()
                .trySetValue(LiquidBlock.LEVEL, getLegacyLevel(state))
        );
    }

    @Override
    protected boolean isRandomlyTicking() {
        return getType().hasRandomTicks();
    }

    @Override
    public int getDropOff(LevelReader world) {
        return getType().getViscocity();
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == getSource() || fluid == getFlowing();
    }

    @Override
    public void animateTick(Level world, BlockPos pos, FluidState state, RandomSource random) {
        super.animateTick(world, pos, state, random);
        getType().randomDisplayTick(world, pos, state, random);
    }

    @Override
    protected void randomTick(Level world, BlockPos pos, FluidState state, RandomSource random) {
        super.randomTick(world, pos, state, random);
        getType().onRandomTick(world, pos, state, random);
    }

    @Override
    protected FluidState getNewLiquid(Level world, BlockPos pos, BlockState state) {
        return getType().getStateManager().computeAverage(Stream.of(ALL_DIRECTIONS)
                .map(direction -> world.getBlockState(pos.relative(direction)).getFluidState())
                .filter(neighbourState -> neighbourState.getType().isSame(this)),
                super.getNewLiquid(world, pos, state)
        );
    }

    @Override
    protected boolean canConvertToSource(Level world) {
        return false;
    }

    @Nullable
    public ParticleOptions getParticle(FluidState state) {
        return new FluidParticleEffect(PSParticles.DRIPPING_FLUID, getType().getStack(state, 1));
    }

    @Nullable
    @Override
    public ParticleOptions getDripParticle() {
        return new FluidParticleEffect(PSParticles.DRIPPING_FLUID, getType().getDefaultStack());
    }

    static PlacedFluid still(PhysicalFluid physical) {
        return new PlacedFluid() {
            @Override
            public int getAmount(FluidState state) {
                return 8;
            }

            @Override
            public boolean isSource(FluidState state) {
                return true;
            }

            @Override
            protected PhysicalFluid getPysicalFluid() {
                return physical;
            }
        };
    }

    static PlacedFluid flowing(PhysicalFluid physical) {
        return new PlacedFluid() {
            @Override
            protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
                super.createFluidStateDefinition(builder);
                builder.add(LEVEL);
            }

            @Override
            public int getAmount(FluidState state) {
                return state.getValue(LEVEL);
            }

            @Override
            public boolean isSource(FluidState state) {
                return false;
            }

            @Override
            protected PhysicalFluid getPysicalFluid() {
                return physical;
            }
        };
    }
}