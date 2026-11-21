/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BurdenedLatticeBlock extends LatticeBlock implements BonemealableBlock {
    public static final MapCodec<BurdenedLatticeBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("spreads").forGetter(block -> block.spreads),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("stem").forGetter(block -> block.stem),
            Codec.INT.fieldOf("shearedAge").forGetter(block -> block.shearedAge),
            BlockBehaviour.propertiesCodec()
    ).apply(instance, BurdenedLatticeBlock::new));

    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;
    public static final int MAX_AGE = 3;

    private boolean spreads;

    @Nullable
    private final Block stem;

    private final int shearedAge;

    @Nullable
    private ResourceKey<LootTable> farmingLootTableKey;

    public BurdenedLatticeBlock(boolean spreads, @Nullable Block stem, int shearedAge, Properties settings) {
        super(settings);
        this.spreads = spreads;
        this.stem = stem;

        this.shearedAge = shearedAge;
        registerDefaultState(defaultBlockState().setValue(AGE, 0).setValue(PERSISTENT, false));
    }

    @Override
    protected MapCodec<? extends BurdenedLatticeBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AGE, PERSISTENT);
    }

    @Override
    protected SoundType getSoundType(BlockState state) {
        if (state.getValue(AGE) > 0) {
            return SoundType.GRASS;
        }
        return super.getSoundType(state);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        ItemStack tool = builder.getOptionalParameter(LootContextParams.TOOL);
        if (tool != null && !tool.isEmpty() && EnchantmentHelper.getEnchantmentsForCrafting(tool).getLevel(builder.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.SILK_TOUCH)) > 0) {
            ItemStack drop = asItem().getDefaultInstance();
            drop.setDamageValue(state.getValue(AGE));
            return List.of(drop);
        }
        return super.getDrops(state, builder);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(Items.SHEARS)) {

            if (state.getValue(AGE) < MAX_AGE || state.getValue(PERSISTENT)) {
                return ItemInteractionResult.FAIL;
            }

            world.playSound(null, player, SoundEvents.SHEEP_SHEAR, player.getSoundSource(), 1, 1);

            if (!world.isClientSide) {
                world.getServer().reloadableRegistries().getLootTable(getFarmingLootTableKey())
                    .getRandomItems(new LootParams.Builder((ServerLevel)world)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                        .withParameter(LootContextParams.TOOL, player.getItemInHand(hand))
                        .withParameter(LootContextParams.BLOCK_STATE, state)
                        .withOptionalParameter(LootContextParams.BLOCK_ENTITY, world.getBlockEntity(pos))
                        .create(LootContextParamSets.BLOCK)).forEach(s -> Block.popResource(world, pos, s));
                world.setBlockAndUpdate(pos, state.setValue(AGE, shearedAge));
            }
            if (!player.isCreative()) {
                player.getItemInHand(hand).hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            }
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public final ResourceKey<LootTable> getFarmingLootTableKey() {
        if (farmingLootTableKey == null) {
            farmingLootTableKey = ResourceKey.create(Registries.LOOT_TABLE, getLootTable().location().withSuffix("_farming"));
        }

        return farmingLootTableKey;
    }


    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (state.getValue(PERSISTENT)) {
            return;
        }

        if (checkConnectivity(world, state, pos)) {
            if (world.getMaxLocalRawBrightness(pos) >= 9 && random.nextInt(35) == 0) {
                if (state.getValue(AGE) < MAX_AGE) {
                    world.setBlock(pos, state.cycle(AGE), Block.UPDATE_ALL);
                    world.updateNeighborsAt(pos, this);
                }

                if (state.getValue(AGE) > 0) {
                    trySpread(state, world, pos);
                }
            }
        } else if (state.getValue(AGE) > 0) {
            world.setBlock(pos, state.setValue(AGE, state.getValue(AGE) - 1), Block.UPDATE_ALL);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
        return (state.getValue(AGE) < MAX_AGE || canSpread(state, world, pos)) && !state.getValue(PERSISTENT);
    }

    @Override
    public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
        if (state.getValue(AGE) < MAX_AGE) {
            world.setBlock(pos, state.setValue(AGE, MAX_AGE), Block.UPDATE_ALL);
        } else {
            trySpread(state, world, pos);
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(world, pos, state);
        stack.setDamageValue(state.getValue(AGE));
        return stack;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return super.getStateForPlacement(ctx)
                .setValue(AGE, ctx.getItemInHand().getDamageValue() % (MAX_AGE + 1))
                .setValue(PERSISTENT, true);
    }

    private void trySpread(BlockState state, Level world, BlockPos pos) {
        if (!spreads) {
            return;
        }
        visitNeighbours(state, pos)
            .filter(mPos -> world.getBlockState(mPos).is(PSBlocks.LATTICE))
            .findFirst()
            .ifPresent(mPos -> world.setBlockAndUpdate(mPos, copyStateProperties(defaultBlockState(), world.getBlockState(mPos))));
    }

    private boolean canSpread(BlockState state, LevelReader world, BlockPos pos) {
        return spreads && visitNeighbours(state, pos).anyMatch(mPos -> world.getBlockState(mPos).is(PSBlocks.LATTICE));
    }

    public boolean checkConnectivity(LevelReader world, BlockState state, BlockPos pos) {
        if (stem == null) {
            return true;
        }

        Set<BlockPos> visitedPositions = new HashSet<>();
        visitedPositions.add(pos.immutable());
        return checkConnectivity(world, state, pos, visitedPositions, 20);
    }

    private boolean checkConnectivity(LevelReader world, BlockState state, BlockPos pos, Set<BlockPos> visitedPositions, int maxDepth) {

        if (state.is(stem)) {
            return true;
        }

        if (!state.is(this)) {
            return false;
        }

        if (getFreeConnections(state).anyMatch(facing -> world.getBlockState(pos.relative(facing)).is(stem))) {
            return true;
        }

        if (maxDepth > 1) {
            return visitNeighbours(state, pos)
                    .filter(visitedPositions::add)
                    .anyMatch(neighbourPos -> checkConnectivity(world, world.getBlockState(neighbourPos), neighbourPos, visitedPositions, maxDepth - 1));
        }

        return false;
    }

    private static Stream<BlockPos> visitNeighbours(BlockState state, BlockPos pos) {
        return getConnections(state).flatMap(d -> {
            BlockPos onLevel = pos.relative(d);
            return Stream.of(onLevel.above(), onLevel, onLevel.below());
        });
    }
}
