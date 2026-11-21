package ivorius.psychedelicraft.entity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetLookAndInteract;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromBlockMemory;
import net.minecraft.world.entity.ai.behavior.ShowTradesToPlayer;
import net.minecraft.world.entity.ai.behavior.StrollAroundPoi;
import net.minecraft.world.entity.ai.behavior.StrollToPoi;
import net.minecraft.world.entity.ai.behavior.StrollToPoiList;
import net.minecraft.world.entity.ai.behavior.UpdateActivityFromSchedule;
import net.minecraft.world.entity.ai.behavior.UseBonemeal;
import net.minecraft.world.entity.ai.behavior.WorkAtPoi;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.block.entity.DryingTableBlockEntity;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.recipe.DryingRecipe;
import ivorius.psychedelicraft.recipe.PSRecipes;
import ivorius.psychedelicraft.recipe.RecipeUtils;

public class DealerTaskListProvider {

    public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> createWorkTasks(Pair<Integer, BehaviorControl<LivingEntity>> busyFollowTasks, float speed) {
        return ImmutableList.of(
                busyFollowTasks,
                Pair.of(5, new RunOne<>(ImmutableList.of(
                        Pair.of(new DealerWorkTask(), 7),
                        Pair.of(StrollAroundPoi.create(MemoryModuleType.JOB_SITE, 0.4f, 4), 2),
                        Pair.of(StrollToPoi.create(MemoryModuleType.JOB_SITE, 0.4f, 1, 10), 5),
                        Pair.of(StrollToPoiList.create(MemoryModuleType.SECONDARY_JOB_SITE, speed, 1, 6, MemoryModuleType.JOB_SITE), 5),
                        Pair.of(new DealerVillagerTask(), 2),
                        Pair.of(new UseBonemeal(), 4))
                )),
                Pair.of(10, new ShowTradesToPlayer(400, 1600)),
                Pair.of(10, SetLookAndInteract.create(EntityType.PLAYER, 4)),
                Pair.of(2, SetWalkTargetFromBlockMemory.create(MemoryModuleType.JOB_SITE, speed, 9, 100, 1200)),
            //  Pair.of(3, new GiveGiftsToHeroTask(100)),
                Pair.of(99, UpdateActivityFromSchedule.create()
        ));
    }

    static class DealerWorkTask extends WorkAtPoi {
        @Override
        protected void useWorkstation(ServerLevel world, Villager entity) {
            entity.getBrain().getMemory(MemoryModuleType.JOB_SITE).ifPresent(globalPos -> {
                if (world.getBlockState(globalPos.pos()).is(PSTags.Blocks.DRYING_TABLES)) {
                    world.getBlockEntity(globalPos.pos(), PSBlockEntities.DRYING_TABLE).ifPresent(blockEntity -> {
                        ItemStack output = blockEntity.getItem(DryingTableBlockEntity.OUTPUT_SLOT_INDEX);
                        DryingRecipe.Input input = new DryingRecipe.Input(output, entity.getInventory().getItems());

                        world.getRecipeManager().getRecipeFor(PSRecipes.DRYING_TYPE, input, world)
                                .map(RecipeHolder::value)
                                .ifPresent(recipe -> {
                                    ItemStack result = recipe.assemble(input, world.registryAccess());
                                    NonNullList<ItemStack> remainder = recipe.getRemainingItems(input);
                                    entity.getInventory().clearContent();
                                    for (int i = 0; i < remainder.size(); i++) {
                                        entity.getInventory().setItem(i, remainder.get(i));
                                    }

                                    if (output.isEmpty()) {
                                        result.grow(output.getCount());
                                    }
                                    blockEntity.setItem(DryingTableBlockEntity.OUTPUT_SLOT_INDEX, result);
                        });
                    });
                }
            });
        }
    }

    static class DealerVillagerTask extends Behavior<Villager> { // version of FarmerVillagerTask changed to work with drug crops
        private static final int MAX_RUN_TIME = 200;
        public static final float WALK_SPEED = 0.5f;

        @Nullable
        private BlockPos currentTarget;
        private long nextResponseTime;
        private int ticksRan;
        private final List<BlockPos> targetPositions = Lists.newArrayList();

        public DealerVillagerTask() {
            super(ImmutableMap.of(
                    MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT,
                    MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                    MemoryModuleType.SECONDARY_JOB_SITE, MemoryStatus.VALUE_PRESENT
            ));
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel world, Villager entity) {
            if (!world.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                return false;
            }

            if (entity.getVillagerData().getProfession() != PSTradeOffers.DRUG_DEALER_PROFESSION) {
                return false;
            }

            BlockPos.MutableBlockPos mutable = entity.blockPosition().mutable();
            targetPositions.clear();

            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    for (int k = -1; k <= 1; ++k) {
                        mutable.set(
                                entity.getX() + i,
                                entity.getY() + j,
                                entity.getZ() + k
                        );
                        if (!isSuitableTarget(mutable, world)) {
                            continue;
                        }
                        targetPositions.add(mutable.immutable());
                    }
                }
            }

            return (currentTarget = chooseRandomTarget(world)) != null;
        }

        @Nullable
        private BlockPos chooseRandomTarget(ServerLevel world) {
            return targetPositions.isEmpty() ? null : targetPositions.get(world.getRandom().nextInt(targetPositions.size()));
        }

        private boolean isSuitableTarget(BlockPos pos, ServerLevel world) {
            BlockState blockState = world.getBlockState(pos);
            return  blockState.getBlock() instanceof CropBlock crop
                    && crop.isMaxAge(blockState)
                    || blockState.isAir() && world.getBlockState(pos.below()).getBlock() instanceof FarmBlock;
        }

        @Override
        protected void start(ServerLevel world, Villager entity, long l) {
            if (l > nextResponseTime && currentTarget != null) {
                entity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(currentTarget));
                entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(currentTarget), 0.5f, 1));
            }
        }

        @Override
        protected void stop(ServerLevel world, Villager entity, long l) {
            entity.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
            entity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            ticksRan = 0;
            nextResponseTime = l + 40L;
        }

        @Override
        protected void tick(ServerLevel world, Villager entity, long l) {
            if (currentTarget != null && !currentTarget.closerToCenterThan(entity.position(), 1)) {
                return;
            }

            if (currentTarget != null && l > nextResponseTime) {
                BlockState state = world.getBlockState(currentTarget);

                if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
                    world.destroyBlock(currentTarget, true, entity);
                }

                if (state.isAir() && world.getBlockState(currentTarget.below()).getBlock() instanceof FarmBlock) {
                    RecipeUtils.consume(entity.getInventory(), i -> i.getItem() instanceof BlockItem && i.is(PSTags.Items.DRUG_CROP_SEEDS)).ifPresent(stack -> {
                        BlockState plantedState = ((BlockItem)stack.getItem()).getBlock().defaultBlockState();
                        world.setBlockAndUpdate(currentTarget, plantedState);
                        world.gameEvent(GameEvent.BLOCK_PLACE, currentTarget, GameEvent.Context.of(entity, plantedState));
                        world.playSound(null,
                                currentTarget.getX(),
                                currentTarget.getY(),
                                currentTarget.getZ(),
                                SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1, 1);
                    });
                }

                if (state.getBlock() instanceof CropBlock crop && !crop.isMaxAge(state)) {
                    targetPositions.remove(currentTarget);
                    currentTarget = chooseRandomTarget(world);
                    if (currentTarget != null) {
                        nextResponseTime = l + 20L;
                        entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(currentTarget), 0.5f, 1));
                        entity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(currentTarget));
                    }
                }
            }

            ++ticksRan;
        }

        @Override
        protected boolean canStillUse(ServerLevel world, Villager entity, long l) {
            return ticksRan < MAX_RUN_TIME;
        }
    }
}
