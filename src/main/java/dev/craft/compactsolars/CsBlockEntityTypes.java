package dev.craft.compactsolars;

import java.util.Arrays;
import java.util.function.Supplier;

import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CsBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Craft.MODID);

    public static final Supplier<BlockEntityType<SolarArrayBlockEntity>> SOLAR_ARRAY = BLOCK_ENTITY_TYPES.register(
            "solar_array", () -> BlockEntityType.Builder.of(SolarArrayBlockEntity::new,
                    Arrays.stream(SolarArrayType.values()).map(type -> CsBlocks.array(type).get())
                            .toArray(Block[]::new)).build(null));

    private CsBlockEntityTypes() {
    }
}
