package dev.craft.powerplants;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.powerplants.block.PlantPartBlockEntity;
import dev.craft.powerplants.cokeoven.CokeOvenBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PpBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Craft.MODID);

    /** All casing parts of all power plants share one type. */
    public static final Supplier<BlockEntityType<PlantPartBlockEntity>> PLANT_PART = BLOCK_ENTITY_TYPES.register(
            "plant_part", () -> BlockEntityType.Builder.of(PlantPartBlockEntity::new,
                    PpBlocks.COKE_OVEN_BRICKS.get()).build(null));
    public static final Supplier<BlockEntityType<CokeOvenBlockEntity>> COKE_OVEN = BLOCK_ENTITY_TYPES.register(
            "coke_oven", () -> BlockEntityType.Builder.of(CokeOvenBlockEntity::new, PpBlocks.COKE_OVEN.get())
                    .build(null));

    private PpBlockEntityTypes() {
    }
}
