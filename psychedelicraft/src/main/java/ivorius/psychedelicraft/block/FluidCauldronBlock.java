package ivorius.psychedelicraft.block;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.block.entity.SyncedBlockEntity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidCauldronBlock extends LayeredCauldronBlock implements EntityBlock {
    public static final MapCodec<FluidCauldronBlock> CODEC = Block.simpleCodec(FluidCauldronBlock::new);

    public static final CauldronInteraction.InteractionMap BEHAVIOUR = CauldronInteraction.newInteractionMap("psychedelicraft:cauldron");

    public FluidCauldronBlock(Properties settings) {
        super(Precipitation.NONE, BEHAVIOUR, settings);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public MapCodec<LayeredCauldronBlock> codec() {
        return (MapCodec)CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected boolean canReceiveStalactiteDrip(Fluid fluid) {
        return fluid != Fluids.EMPTY;
    }

    @Override
    protected void receiveStalactiteDrip(BlockState state, Level world, BlockPos pos, Fluid fluid) {
        if (world.getBlockEntity(pos, PSBlockEntities.CAULDRON).filter(data -> data.fluid.isOf(fluid)).isPresent()) {
            super.receiveStalactiteDrip(state, world, pos, fluid);
        }
    }

    @Override
    public void handlePrecipitation(BlockState state, Level world, BlockPos pos, Biome.Precipitation precipitation) {
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state) {
        return Items.CAULDRON.getDefaultInstance();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new Data(pos, state);
    }

    public static class Data extends SyncedBlockEntity {
        public ItemFluids fluid = ItemFluids.EMPTY;

        public Data(BlockPos pos, BlockState state) {
            super(PSBlockEntities.CAULDRON, pos, state);
        }

        public void setFluid(ItemFluids fluid) {
            this.fluid = fluid;
            setChanged();
        }

        public ItemFluids getFluid() {
            return fluid;
        }

        @Override
        protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider lookup) {
            fluid = ItemFluids.decode(nbt.get("fluid"));
        }

        @Override
        protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider lookup) {
            nbt.put("fluid", fluid.encode());
        }
    }
}
