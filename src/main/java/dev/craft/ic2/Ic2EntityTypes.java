package dev.craft.ic2;

import dev.craft.Craft;
import dev.craft.ic2.item.tool.MiningLaserEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Ic2EntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Craft.MODID);

    /** Tracking range 160 blocks, position updates every 5 ticks as in IC2. */
    public static final DeferredHolder<EntityType<?>, EntityType<MiningLaserEntity>> MINING_LASER =
            ENTITY_TYPES.register("mining_laser", () -> EntityType.Builder
                    .<MiningLaserEntity>of(MiningLaserEntity::new, MobCategory.MISC)
                    .sized(0.8F, 0.8F)
                    .noSave()
                    .clientTrackingRange(10)
                    .updateInterval(5)
                    .build("mining_laser"));

    private Ic2EntityTypes() {
    }
}
