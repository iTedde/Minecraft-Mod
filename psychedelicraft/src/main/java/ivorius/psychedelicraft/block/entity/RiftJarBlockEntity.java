/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import java.util.*;

import ivorius.psychedelicraft.client.render.bezier.Bezier;
import ivorius.psychedelicraft.entity.RealityRiftEntity;
import ivorius.psychedelicraft.entity.PSEntities;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RiftJarBlockEntity extends SyncedBlockEntity {
    public float currentRiftFraction;
    public int ticksAliveVisual;

    public boolean isOpening;
    public float fractionOpen;

    public boolean jarBroken = false;
    public boolean suckingRifts = true;
    public float fractionHandleUp;

    private final Map<UUID, JarRiftConnection> riftConnections = new HashMap<>();

    public RiftJarBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.RIFT_JAR, pos, state);
    }

    public Collection<JarRiftConnection> getConnections() {
        return riftConnections.values();
    }

    public void tickAnimation() {
        fractionOpen = MathUtils.nearValue(fractionOpen, isOpening ? 1 : 0, 0, 0.02F);
        fractionHandleUp = MathUtils.nearValue(fractionHandleUp, isSuckingRifts() ? 0 : 1, 0, 0.04F);
        ticksAliveVisual++;
    }

    public void tick(ServerLevel world) {
        tickAnimation();

//        if (!world.isClient)
//        {
//            boolean before = suckingRifts;
//            suckingRifts = !world.isDaytime() && world.canBlockSeeTheSky(xCoord, yCoord, zCoord);
//
//            if (before != suckingRifts)
//            {
//                markDirty();
//                world.markBlockForUpdate(xCoord, yCoord, zCoord);
//            }
//        }

        if (isSuckingRifts()) {
            if (fractionOpen > 0) {
                List<RealityRiftEntity> rifts = getAffectedRifts();

                if (rifts.size() > 0) {
                    float minus = (1F / rifts.size()) * 0.001f * fractionOpen;
                    rifts.forEach(rift -> {
                        currentRiftFraction += rift.takeFromRift(minus);

                        JarRiftConnection connection = createAndGetRiftConnection(rift);
                        connection.fractionUp = Math.min(1, connection.fractionUp + 0.02f * fractionOpen);
                    });
                }
            }
        } else {
            if (fractionOpen > 0) {
                float minus = Math.min(0.0004f * fractionOpen * currentRiftFraction + 0.0004f, currentRiftFraction);

                BlockPos pos = getBlockPos();
                Vec3 center = pos.getCenter();
                world.getEntitiesOfClass(LivingEntity.class, new AABB(
                        pos.getX() - 5, pos.getY() - 5, pos.getZ() - 2,
                        pos.getX() + 6, pos.getY() + 6, pos.getZ() + 6
                    ), EntitySelector.NO_CREATIVE_OR_SPECTATOR
                ).stream().flatMap(DrugProperties::stream).forEach(drugProperties -> {
                    double effect = (5 - drugProperties.asEntity().position().distanceTo(center)) * 0.2F * minus;
                    drugProperties.addToDrug(DrugType.ZERO, effect * 5);
                    drugProperties.addToDrug(DrugType.POWER, effect * 35);
                });

                currentRiftFraction -= minus;
            }
        }

        riftConnections.values().removeIf(connection -> (connection.fractionUp -= 0.01F) <= 0);

        if (currentRiftFraction > 1) {
            jarBroken = true;

            releaseRift();
            world.destroyBlock(worldPosition, false);
            Vec3 explosionPosition = getBlockPos().getCenter();
            world.explode(null, explosionPosition.x, explosionPosition.y, explosionPosition.z, 1, false, ExplosionInteraction.BLOCK);
        }
    }

    public JarRiftConnection createAndGetRiftConnection(RealityRiftEntity rift) {
        return riftConnections.computeIfAbsent(rift.getUUID(), id -> new JarRiftConnection(rift));
    }

    public boolean toggleRiftJarOpen() {
        if (level instanceof ServerLevel) {
            isOpening = !isOpening;

            setChanged();
        }
        return isOpening;
    }

    public void toggleSuckingRifts() {
        if (level instanceof ServerLevel) {
            suckingRifts = !suckingRifts;

            setChanged();
        }
    }

    public boolean isSuckingRifts() {
        return suckingRifts;
    }

    public void releaseRift() {
        if (currentRiftFraction > 0) {
            List<RealityRiftEntity> rifts = getAffectedRifts();

            if (rifts.size() > 0) {
                rifts.get(0).addToRift(currentRiftFraction);
            } else if (!level.isClientSide) {
                RealityRiftEntity rift = PSEntities.REALITY_RIFT.create(level);
                rift.setPos(getBlockPos().getCenter().add(5, 3, 0.5));
                rift.setRiftSize(currentRiftFraction);
                level.addFreshEntity(rift);
            }

            currentRiftFraction = 0.0f;
        }
    }

    public List<RealityRiftEntity> getAffectedRifts() {
        BlockPos pos = getBlockPos();
        return level.getEntitiesOfClass(RealityRiftEntity.class, new AABB(
                pos.getX() - 2.0f, pos.getY() + 0.0f, pos.getZ() - 2.0f,
                pos.getX() + 3.0f, pos.getY() + 10, pos.getZ() + 3
            ), EntitySelector.ENTITY_STILL_ALIVE
        );
    }

    @Override
    public void saveAdditional(CompoundTag compound, Provider lookup) {
        compound.putFloat("currentRiftFraction", currentRiftFraction);
        compound.putBoolean("isOpening", isOpening);
        compound.putFloat("fractionOpen", fractionOpen);
        compound.putBoolean("jarBroken", jarBroken);
        compound.putBoolean("suckingRifts", suckingRifts);
        compound.putFloat("fractionHandleUp", fractionHandleUp);
    }

    @Override
    public void loadAdditional(CompoundTag compound, Provider lookup) {
        currentRiftFraction = compound.getFloat("currentRiftFraction");
        isOpening = compound.getBoolean("isOpening");
        fractionOpen = compound.getFloat("fractionOpen");
        jarBroken = compound.getBoolean("jarBroken");
        suckingRifts = compound.getBoolean("suckingRifts");
        fractionHandleUp = compound.getFloat("fractionHandleUp");
    }

    public static class JarRiftConnection {
        public final UUID riftID;
        public final Vec3 position;

        public Bezier bezier;
        public float fractionUp;

        public JarRiftConnection(RealityRiftEntity rift) {
            riftID = rift.getUUID();
            position = rift.getEyePosition();
        }
    }
}
