/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity.contents;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.BurnerBlock;
import ivorius.psychedelicraft.block.entity.BurnerBlockEntity;
import ivorius.psychedelicraft.block.entity.BurnerBlockEntity.Contents;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EmptyContents implements BurnerBlockEntity.Contents {
    public static final ResourceLocation ID = Psychedelicraft.id("empty");

    private final BurnerBlockEntity entity;

    public EmptyContents(BurnerBlockEntity entity) {
        this.entity = entity;
    }

    @Override
    public VoxelShape getOutlineShape() {
        return BurnerBlock.SHAPE;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public InteractionResultHolder<Contents> interact(ItemStack stack, Player player, InteractionHand hand, Direction side) {
        if (!isValidContainer(stack)) {
            return InteractionResultHolder.fail(this);
        }

        ItemStack container = stack.copyWithCount(1);
        int capacity = FluidCapacity.get(stack);
        stack.consume(1, player);

        entity.setContainer(container);
        entity.playSound(player, SoundType.GLASS.getPlaceSound());
        return InteractionResultHolder.success(container.is(PSItems.BOTTLE) ? new LargeContents(entity, capacity, container) : new SmallContents(entity, capacity, container));
    }

    private boolean isValidContainer(ItemStack stack) {
        return stack.is(PSTags.Items.BUNSEN_BURNER_INSERTABLE);
    }

    public Contents getForStack(Level world, BlockPos pos, BlockState state, ItemStack stack) {
        if (!isValidContainer(stack)) {
            return this;
        }
        world.playLocalSound(pos, SoundType.GLASS.getPlaceSound(), SoundSource.BLOCKS, 1, 1, true);
        int capacity = FluidCapacity.get(stack);
        return stack.is(PSItems.BOTTLE) ? new LargeContents(entity, capacity, stack) : new SmallContents(entity, capacity, stack);
    }

    @Override
    public void tick(ServerLevel world) {
    }

    @Override
    public void toNbt(CompoundTag compound, Provider lookup) {
    }

    @Override
    public void fromNbt(CompoundTag compound, Provider lookup) {
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public ItemStack getFilled(ItemStack container, boolean dryRun, float drainPercentage) {
        return container;
    }
}
