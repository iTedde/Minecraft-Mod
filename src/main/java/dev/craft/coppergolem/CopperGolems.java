package dev.craft.coppergolem;

import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Entry point of the copper golem (spec 35 §2). */
public final class CopperGolems {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Craft.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    /** 0.49 × 0.98 as in 1.21.9; tracked like the iron golem. */
    public static final DeferredHolder<EntityType<?>, EntityType<CopperGolem>> COPPER_GOLEM =
            ENTITY_TYPES.register("copper_golem", () -> EntityType.Builder
                    .<CopperGolem>of(CopperGolem::new, MobCategory.MISC)
                    .sized(0.49F, 0.98F)
                    .eyeHeight(0.8F)
                    .clientTrackingRange(10)
                    .build("copper_golem"));

    /** Golem only, no chest, unaffected and unwaxed (spec 35 §2.1). */
    public static final DeferredItem<DeferredSpawnEggItem> SPAWN_EGG = ITEMS.registerItem("copper_golem_spawn_egg",
            properties -> new DeferredSpawnEggItem(COPPER_GOLEM, 0xC15A36, 0x5C9E8C, properties));

    private CopperGolems() {
    }

    public static void init(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(CopperGolems::createAttributes);
        modBus.addListener(CopperGolems::commonSetup);
        NeoForge.EVENT_BUS.addListener(CopperGolemBuilder::onEntityPlace);
    }

    private static void createAttributes(EntityAttributeCreationEvent event) {
        event.put(COPPER_GOLEM.get(), CopperGolem.createAttributes().build());
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(CopperGolemBuilder::registerDispenserBehaviors);
    }
}
