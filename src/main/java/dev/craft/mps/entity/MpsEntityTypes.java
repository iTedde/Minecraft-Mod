package dev.craft.mps.entity;

import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Entities of Modular Powersuits. */
public final class MpsEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Craft.MODID);

    /** {@code registerModEntity(…, 64, 20, true)}: tracked 64 blocks, every 20 ticks, with velocity. */
    public static final DeferredHolder<EntityType<?>, EntityType<PlasmaBoltEntity>> PLASMA_BOLT =
            ENTITY_TYPES.register("plasma_bolt", () -> EntityType.Builder
                    .<PlasmaBoltEntity>of(PlasmaBoltEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .noSave()
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("plasma_bolt"));

    private MpsEntityTypes() {
    }
}
