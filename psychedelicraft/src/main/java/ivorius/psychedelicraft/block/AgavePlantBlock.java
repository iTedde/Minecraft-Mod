/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.item.PSItems;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AgavePlantBlock extends SucculentPlantBlock {
    public static final MapCodec<AgavePlantBlock> CODEC = simpleCodec(AgavePlantBlock::new);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_5;
    public static final int MAX_AGE = BlockStateProperties.MAX_AGE_5;

    public static final VoxelShape[] SHAPES = {
            Block.box(6, 0, 6, 10,  4, 10),
            Block.box(6, 0, 6, 10,  8, 10),
            Block.box(5, 0, 5, 11, 10, 11),
            Block.box(5, 0, 5, 11, 10, 11),
            Block.box(4, 0, 4, 12, 12, 12),
            Block.box(2, 0, 2, 14, 14, 14)
    };

    public AgavePlantBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends AgavePlantBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape[] getShapes() {
        return SHAPES;
    }

    @Override
    protected IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    protected int getMaxAge() {
        return MAX_AGE;
    }

    @Override
    protected int getGrowthRate(BlockState state) {
        return 320;
    }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity) || entity.getType() == EntityType.FOX || entity.getType() == EntityType.BEE) {
            return;
        }
        entity.makeStuckInBlock(state, new Vec3(0.8F, 0.75F, 0.8F));
        if (!(world.isClientSide
                || state.getValue(getAgeProperty()) <= 0
                || entity.xOld == entity.getX() && entity.zOld == entity.getZ()
                || entity.isShiftKeyDown())) {
            if (Math.max(
                    Math.abs(entity.getX() - entity.xOld),
                    Math.abs(entity.getZ() - entity.zOld)
                ) >= 0.003F) {
                entity.hurt(entity.damageSources().cactus(), 1);
            }
        }
    }

    @Deprecated
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        int age = state.getValue(getAgeProperty());

        if ((stack.is(Tags.Items.TOOLS_SHEAR) && age >= 1)) {
            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            popResource(world, pos, new ItemStack(PSItems.AGAVE_LEAF, 1 + world.random.nextInt(2)));

            state = state.setValue(getAgeProperty(), age - 1);
            world.setBlock(pos, state, Block.UPDATE_CLIENTS);
            world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state));

            if (stack.is(Items.BONE_MEAL)) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            world.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS,
                    1,
                    0.8F + world.random.nextFloat() * 0.4F
            );
            return ItemInteractionResult.sidedSuccess(world.isClientSide);
        }
        if (stack.isEmpty()) {
            player.hurt(player.damageSources().cactus(), 1);
            return ItemInteractionResult.SUCCESS;
        }

        return super.useItemOn(stack, state, world, pos, player, hand, hit);
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (!isAppropriateTool(player.getItemInHand(InteractionHand.MAIN_HAND))) {
            player.hurt(player.damageSources().cactus(), 1);
        }
        return super.playerWillDestroy(world, pos, state, player);
    }

    private boolean isAppropriateTool(ItemStack stack) {
        return stack.is(ItemTags.PICKAXES)
                || stack.is(ItemTags.SHOVELS)
                || stack.is(ItemTags.SWORDS)
                || stack.is(ItemTags.AXES)
                || stack.is(ItemTags.HOES)
                || stack.is(Tags.Items.TOOLS_SHEAR);
    }
}
