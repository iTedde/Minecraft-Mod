package ivorius.psychedelicraft.entity;

import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.SetLookAndInteract;
import net.minecraft.world.entity.ai.behavior.ShowTradesToPlayer;
import net.minecraft.world.entity.ai.behavior.UpdateActivityFromSchedule;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableLong;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;

import ivorius.psychedelicraft.PSDamageTypes;

public class AddictTaskListProvider {
    public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> createWorkTasks(Pair<Integer, BehaviorControl<LivingEntity>> busyFollowTasks, float speed) {
        return ImmutableList.of(
                busyFollowTasks,
                Pair.of(7, new WorkTask()),
                Pair.of(10, new ShowTradesToPlayer(400, 1600)),
                Pair.of(10, SetLookAndInteract.create(EntityType.PLAYER, 8)),
                Pair.of(11, goToPlayer(MemoryModuleType.NEAREST_PLAYERS, speed, 9)),
                Pair.of(99, UpdateActivityFromSchedule.create()
        ));
    }

    public static OneShot<PathfinderMob> goToPlayer(MemoryModuleType<List<Player>> posModule, float walkSpeed, int maxDistance) {
        MutableLong mutableLong = new MutableLong(0L);
        return BehaviorBuilder.create(context -> context.group(context.registered(MemoryModuleType.WALK_TARGET), context.present(posModule)).apply(context, (walkTarget, pos) -> (world, entity, time) -> {
            Player target = context.get(pos).get(0);
            if (target == null || !target.position().closerThan(entity.position(), maxDistance)) {
                return false;
            }
            if (time <= mutableLong.getValue()) {
                return true;
            }
            Optional<Vec3> optional = Optional.ofNullable(LandRandomPos.getPos(entity, 8, 6));
            walkTarget.setOrErase(optional.map(targetPos -> new WalkTarget(targetPos, walkSpeed, 1)));
            mutableLong.setValue(time + 180L);
            return true;
        }));
    }

    public static float getShakeAmount(LivingEntity entity) {
        float healthScale = 1 - (entity.getHealth() / entity.getMaxHealth());
        float shakeAmount = (float)(Math.cos(entity.tickCount * 3.25) * Math.PI * 0.4F * (1 + healthScale * 8));
        entity.hurtTime = Math.abs(shakeAmount) > 5F ? 1 : 0;
        return shakeAmount;
    }

    static class WorkTask extends Behavior<Villager> {
        private static final long RUN_TIME = 300;
        private long lastCheckedTime;

        public WorkTask() {
            super(ImmutableMap.of());
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel world, Villager entity) {
            if (world.getGameTime() - lastCheckedTime < RUN_TIME || world.random.nextInt(2) != 0) {
                return false;
            }
            this.lastCheckedTime = world.getGameTime();
            return true;
        }

        @Override
        protected void start(ServerLevel world, Villager entity, long l) {
            if (entity.shouldRestock()) {
                entity.playWorkSound();
                entity.restock();
                entity.hurt(PSDamageTypes.create(world, PSDamageTypes.OVERDOSE), 1 + (world.getRandom().nextFloat() * 5));
            }
        }
    }
}
